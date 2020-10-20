#!/bin/bash
# $1 = raw latencies input file
# $2 = output file for latencies of every second change
set -o errexit -o nounset

export INFILE=$1
export OUTFILE=$2

seconds=-1
while IFS=, read -r nanos latency  
do 
  newSeconds=0${nanos%?????????}
  
  if [ $newSeconds -gt $seconds ]; 
    then echo $newSeconds,$latency; 
  fi
  seconds=$newSeconds
done <$INFILE >$OUTFILE
echo ENDE-----------  

