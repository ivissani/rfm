import sys

import csv

filename = sys.argv[1]
with open(filename, 'r') as f:
	reader = csv.reader(f)
	writer = csv.writer(sys.stdout)

	first = True
	for row in reader:
		if first:
			row[1] = 'nulo'
			first = False
		writer.writerow(row[1:])

