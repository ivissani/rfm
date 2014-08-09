import sys
import csv

files = sys.argv[1::2]
labels = sys.argv[2::2]

stats = {}

for filename, label in zip(files, labels):
	with open(filename, 'r') as f:
		reader = csv.reader(f)
		for row in reader:
			if row[0] not in stats:
				stats[row[0]] = {}
			stats[row[0]][label] = float(row[-1])
	for crit in stats:
		if label not in stats[crit]:
			stats[crit][label] = -1.0

writer = csv.writer(sys.stdout)
writer.writerow([''] + sorted(labels))

for k,d in sorted(stats.iteritems()):
	writer.writerow([k] + [v for k,v in sorted(d.iteritems())])
