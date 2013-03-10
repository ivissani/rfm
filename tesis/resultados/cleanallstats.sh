for F in $(ls *stats*.csv)
do 
	mv $F tmp_stats.csv
	python cleancsv.py tmp_stats.csv > $F
	rm -f tmp_stats.csv
done
