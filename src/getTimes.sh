#!/bin/bash

javaMain=$1 # The path of the main class, including .java extension
resultFileSuffix=$2 # Allows user to have a unique output file for each run
mainFileName=$(basename $javaMain .java) # The name of the main class, without .java extension
resultFile="${mainFileName}_${resultFileSuffix}.txt"
project=/tmp/SortingCompetitionMaterials2016/
destination=$project/bin
dataDestination=DataFiles
dataDirectory=$project/DataFiles # It's important that this has no trailing slash
source=src
timeFile=times.txt
validityFile=validity.txt
outputFile=output.txt

rsync -r -u $dataDirectory $destination # only copies data files if they have been updated
javac -cp $project/$source $javaMain -d $destination
cd $destination
> $timeFile # clear results file
> $validityFile # clear validity file
UNSORTED=(Unsorted_0.txt Unsorted_1.txt Unsorted_2.txt Unsorted_3.txt Unsorted_4.txt Unsorted_5.txt Unsorted_6.txt Unsorted_7.txt)
SORTED=(Sorted_0.txt Sorted_1.txt Sorted_2.txt Sorted_3.txt Sorted_4.txt Sorted_5.txt Sorted_6.txt Sorted_7.txt)
#UNSORTED=(Unsorted_0.txt)
#SORTED=(Sorted_0.txt)

for j in "${!UNSORTED[@]}"; do
	echo "starting ${UNSORTED[$j]}"
	cmd="taskset -c 0 java $mainFileName $dataDestination/${UNSORTED[$j]} $outputFile"
		for i in $(seq 5); do
			echo "on trial $i"
			$cmd | ([ $i -lt 5 ] && tr '\n' ',' || tr '\n' '\n') >> $timeFile
			sleep 1
		done
	diff -q -s $outputFile $dataDestination/${SORTED[$j]} >> $validityFile
	echo "done with ${UNSORTED[$j]}"
done

cat $timeFile $validityFile > $resultFile
cp $resultFile $project/Times
rm $resultFile
