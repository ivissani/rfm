import sys

import csv

filename = sys.argv[1]

try:
	limit = int(sys.argv[2])
except:
	limit = 1

values = []
with open(filename, 'r') as f:
	reader = csv.reader(f)
	#for row in reader:
	#	values.append(float(row[-1]))
	#
	sortedresults = sorted(reader, key=lambda r: float(r[-1]))

results = map(lambda r: [r[0], r[-1]], sortedresults)

print '\n'.join(map(','.join, results[0:limit]))
