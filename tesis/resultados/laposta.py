import sys
import csv

filenames = sys.argv[1:]


avgs = {}
bestspeedup = {}
worstspeedup = {}

for filename in filenames:
	with open(filename, 'r') as f:
		reader = csv.reader(f)
		thisfileavgs = {r[0]: r[-1] for r in reader}
	for k,v in thisfileavgs.iteritems():
		if k not in avgs:
			avgs[k] = []
		avgs[k].append(float(v))

		thisspeedup = float(thisfileavgs['nulo'])/float(v)
		if k in bestspeedup:
			if bestspeedup[k] < thisspeedup:
				bestspeedup[k] = thisspeedup
		else:
			bestspeedup[k] = thisspeedup
		if k in worstspeedup:
			if worstspeedup[k] > thisspeedup:
				worstspeedup[k] = thisspeedup
		else:
			worstspeedup[k] = thisspeedup

totalavgs = {k: sum(vs)/len(vs) for k,vs in avgs.iteritems()}
writer = csv.writer(sys.stdout)
#for k,v in sorted(totalavgs.iteritems()):
#	writer.writerow([k, v])

avgspeedup = {k: totalavgs['nulo']/v for k, v in totalavgs.iteritems()}
#print avgspeedup
#print bestspeedup
#print worstspeedup

for k,v in sorted(avgspeedup.iteritems()):
	writer.writerow([k, worstspeedup[k], bestspeedup[k], v])
