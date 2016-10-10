#!/bin/bash

javaMain=$1 # The path of the main class, including .java extension
mainFileName=$(basename $javaMain .java) # The name of the main class, without .java extension
project=/mnt/c/Users/a.lemmon777/IdeaProjects/SortingCompetitionMaterials2016/
destination=$project/bin
source=src
outputFile=$project/output.txt
dataFilePath=$project/DataFiles

javac -cp $project/$source $javaMain -d $destination
cd $destination
UNSORTED=(Unsorted_0.txt Unsorted_1.txt Unsorted_2.txt Unsorted_3.txt Unsorted_4.txt Unsorted_5.txt Unsorted_6.txt Unsorted_7.txt)
SORTED=(Sorted_0.txt Sorted_1.txt Sorted_2.txt Sorted_3.txt Sorted_4.txt Sorted_5.txt Sorted_6.txt Sorted_7.txt)
for j in "${!UNSORTED[@]}"; do
	echo "starting ${UNSORTED[$j]}"
	cmd="taskset -c 0 java $mainFileName $dataFilePath/${UNSORTED[$j]} $outputFile"
	$cmd
	diff -q -s ../output.txt ../DataFiles/${SORTED[$j]}
done