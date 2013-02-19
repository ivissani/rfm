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

def val2rgb(val, levels=16.0):
	nacolor = (0.5, 0.5, 0.5, 1) # grey color for N/A values

	bluelevel = float(20)/255
	maxcolor = 255
	red = maxcolor
	green = maxcolor
	if val < 0.0:
		return (val, nacolor)
	elif val != 1:
		colorstep = float(maxcolor)/levels
		valuestep = 1.0/levels
		redchannel = reversed(zip(drange(0.0, 1, valuestep), map(lambda v: int(math.ceil(v)), drange(0, maxcolor+1, colorstep))))
		grechannel = zip(list(drange(1, 2, valuestep)), reversed(map(lambda v: int(math.ceil(v)), drange(0, maxcolor+1, colorstep))))
		red = 0
		for redmapval, redmapcolor in redchannel:
			red = redmapcolor
			if val >= redmapval:
				break
		green = 255
		for gremapval, gremapcolor in grechannel:
			green = gremapcolor
			if val <= gremapval:
				break
	return (val, (float(red)/255, bluelevel, float(green)/255, 1))


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
	levels = 16.0

normstats = normalizedstats(readstats(filename))
colorfunc = lambda val: val2rgb(val, levels)

statscolored = [map(colorfunc, v[1:]) for v in normstats]
statscolored = [[u[1] for u in v] for v in statscolored]

fig = plt.figure(dpi=120)
ax = fig.add_subplot(121)
cax=ax.imshow(statscolored, interpolation='none', aspect='auto')
ax.set_title(title)

# Decorate graphic
ax.xaxis.set_ticks(range(len(normstats[0])-1))
ax.xaxis.set_ticklabels(map(str, range(1, len(normstats[0])-1)) + ['avg'])
#ax.xaxis.grid(True)

ax.yaxis.set_ticks([i for i in range(len(normstats))])
ax.yaxis.set_ticklabels([v[0] for v in normstats])
for label in ax.yaxis.get_ticklabels():
	label.set_visible(True)
	label.set_size(6)
#ax.yaxis.grid(True)

ax2 = fig.add_subplot(122)
scale = list(drange(0.125, 2, float(2)/levels))
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

plt.show()
fig.savefig(outputfilename, dpi=480, bbox_inches='tight')
