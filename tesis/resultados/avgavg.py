import sys

import csv

filename = sys.argv[1]

with open(filename, 'r') as f:
	reader = csv.reader(f)
	avgs = [float(r[-1]) for r in reader]

suma = sum(avgs[1:10]) + avgs[11] + sum(avgs[-10:-1])
avg = suma / 21
print avg
