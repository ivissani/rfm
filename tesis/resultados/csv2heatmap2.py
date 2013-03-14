import sys

import csv
import math
import numpy as np
import matplotlib.pyplot as plt
import matplotlib.cm as cm


def readstats(filename):
	stats = []
	with open(filename, 'r') as f:
			reader = csv.reader(f)
			longestrowlen = 0
			reader.next()
			for row in reader:
				if len(row) > longestrowlen:
					longestrowlen = len(row)
				stats.append([row[0]] + map(float, row[1:]))
	
	# Complete shorter rows with negative values (i.e. N/A values)
	retstats = []
	for row in stats:
		thisrowlen = len(row)
		stretchedrow = row
		if thisrowlen < longestrowlen:
			stretchedrow = row[:-1] + [-10.0 for i in range(longestrowlen - thisrowlen)] + row[-1:]
		retstats.append(stretchedrow)

	return retstats

def normalizedstats(stats):
	nullvalues = [v for v in stats if v[0] == 'nulo'][0]
	def normalizedrow(row):
		norm = [row[0]]
		for i in range(1, len(row)):
			norm.append(row[i] / nullvalues[i])
		return norm
	return map(normalizedrow, stats)

def drange(start, stop, step):
	r = start
	while r < stop:
		yield r
		r += step

def val2rgb(val, levels=16.0, maxspeedup=2.0, minspeedup=0.5):
	assert levels < 765.0 # cannot handle more than 765 levels of color cause we're using RGB

	nacolor = (0.5, 0.5, 0.5, 1) # grey color for N/A values

	bluelevel = float(0)/255
	maxcolor = 255
	red = maxcolor
	green = maxcolor
	if val < 0.0:
		return (val, nacolor)
	else:
		redrange = range(127, 255) + [255 for i in range(0, 255)] + range(254, -1, -1) + [0 for i in range(0, 127)]
		greenrange = [0 for i in range(0, 127)] + range(0, 255) + [255 for i in range(0, 255)] + range(254, 126, -1)
		together = zip(redrange, greenrange)
		center = 382 # i.e. len(together)/2 == 765/2 == 382

		stepweight = int(382/(levels/2))
		#print stepweight
		stepweightval = (stepweight/382.0)
		#print stepweightval
		
		speedup = 1.0/val
		#print "speedup {}".format(speedup)
		if speedup >= maxspeedup:
			red, green = together[-1]
		elif speedup <= minspeedup:
			red, green = together[0]
		else:
			steps = 0
			upperstep = ((maxspeedup-1.0)/(levels/2))
			while speedup > (1.0 + upperstep):
				#print "speedup {}".format(speedup)
				steps = steps + 1
				speedup = speedup - upperstep
			lowerstep = ((1.0-minspeedup)/(levels/2))
			while speedup < (1.0 - lowerstep):
				steps = steps - 1
				speedup = speedup + lowerstep
			# print "val {}".format(val)
			# print "steps {}".format(steps)
			red, green = together[center + (steps*stepweight)]
	return (val, (float(red)/255, float(green)/255, bluelevel, 1))

try:
	title = sys.argv[2]
	filename = sys.argv[1]
	outputfilename = sys.argv[3]
except:
	print "Usage: python csv2heatmap.py <filename.csv> <heatmaptitle> <imagefilename.fmt> [levels]"
	print "	fmt may be one of the matplotlib supported formats. Typically one of [png, jpg, svg, pdf, eps, ps]"
	print "	levels is optional and it should be an int. Indicates the amount of levels in which to divide the heat scale. Default is 16"
	sys.exit(1)

try:
	levels = float(int(sys.argv[4]))
except:
	levels = 32.0
maxspeedup = 5.0
minspeedup = 0.5

labels = []
with open(filename, 'r') as f:
	reader = csv.reader(f)
	labels = reader.next()[1:]

normstats = normalizedstats(readstats(filename))
colorfunc = lambda val: val2rgb(val, levels, maxspeedup, minspeedup)

statscolored = [map(colorfunc, v[1:]) for v in normstats]
statscolored = [[u[1] for u in v] for v in statscolored]

fig = plt.figure(dpi=120)
ax = fig.add_subplot(121)
cax=ax.imshow(statscolored, interpolation='none', aspect='auto')
ax.set_title(title)

# Decorate graphic
ax.xaxis.set_ticks(range(len(normstats[0])-1))
ax.xaxis.set_ticklabels(labels)#(map(str, range(1, len(normstats[0])-1)) + ['avg'])
#ax.xaxis.grid(True)

ax.yaxis.set_ticks([i for i in range(len(normstats))])
ax.yaxis.set_ticklabels([v[0] for v in normstats])
for label in ax.yaxis.get_ticklabels():
	label.set_visible(True)
	label.set_size(6)
#ax.yaxis.grid(True)

ax2 = fig.add_subplot(122)

# prepare color scale for legend purpose
upperincrement = (maxspeedup-1.0)/(levels/2)
upperscale = list(drange(1.0, maxspeedup+upperincrement, upperincrement))
lowerincrement = ((1/minspeedup)-1.0)/(levels/2)
lowerscale = list(reversed(map(lambda x: 1/x, list(drange(1.0, 1/minspeedup + lowerincrement, lowerincrement))[1:])))
scale = list(reversed(map(lambda x: 1/x, lowerscale + upperscale)))

ax2.imshow([[v[1]] for v in map(colorfunc, scale)], interpolation='none', aspect='equal')

for label in ax2.xaxis.get_ticklabels():
	label.set_visible(False)

ax2.set_title('speed up scale')

ax2.yaxis.set_ticks([i for i in range(len(scale))])
ax2_yaxis = map(lambda f: str(round(1/f, 2)) + 'x', scale)
ax2_yaxis[0] = '> ' + ax2_yaxis[0]
ax2_yaxis[-1] = '< ' + ax2_yaxis[-1]
ax2.yaxis.set_ticklabels(ax2_yaxis)
for label in ax2.yaxis.get_ticklabels():
	label.set_visible(True)
	label.set_size(6)

plt.show()
fig.savefig(outputfilename, dpi=480, bbox_inches='tight')
