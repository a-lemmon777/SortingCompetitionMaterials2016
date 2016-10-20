import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MergeInsertion {
	private static int x1;
	private static int y1;
	private static int x2;
	private static int y2;
	private static double threshold = 0.0000000001;

	public static void main(String[] args) throws InterruptedException {
		if (args.length < 2) {
			System.out
					.println("Please run with two command line arguments: input and output file names");
			System.exit(0);
		}

		String inputFileName = args[0];
		String outFileName = args[1];

		int[][] points = readInData(inputFileName);

		//printArray(points, 100);
		
		int[][] toSort = points.clone();
				
		Pair[] sorted = sort(toSort);
		
		//printArray(sorted, 100);
		
		toSort = points.clone();
		
		Thread.sleep(10); //to let other things finish before timing; adds stability of runs

		long start = System.currentTimeMillis();
		
//		sorted = sort(toSort);

		// ToDo REMOVE THIS!!!
        for (int i = 0; i < 10; ++i) {
            sorted = sort(toSort);
        }
        //REMOVE THIS!!!
		
		long end = System.currentTimeMillis();
		
		System.out.println(end - start);

		writeOutResult(sorted, outFileName);
		
		/**
		 * Testing (assuming data2.txt points)
		int[] test1 = {23790, 94342, 8922566};
		int[] test2 = {77579, 45587, 8546858};
		System.out.println(distance(test1));
		System.out.println(distance(test2));
		**/
		
	}
	

	// YOUR SORTING METHOD GOES HERE. 
	// You may call other methods and use other classes. 
	// Note: you may change the return type of the method. 
	// You would need to provide your own function that prints your sorted array to 
	// a while in the exact same format that my program outputs
    /**
     *
     * @author Aaron Lemmon
     */
	private static Pair[] sort(int[][] toSort) {
        int count = toSort.length;
        Pair[] sortMe = new Pair[count];
        for (int i = 0; i < count; ++i) {
            long xDistanceToRefPoint1 = toSort[i][0] - x1;
            long yDistanceToRefPoint1 = toSort[i][1] - y1;
            long xDistanceToRefPoint2 = toSort[i][0] - x2;
            long yDistanceToRefPoint2 = toSort[i][1] - y2;
            long distanceSquaredToRefPoint1 = xDistanceToRefPoint1 * xDistanceToRefPoint1 + yDistanceToRefPoint1 * yDistanceToRefPoint1;
            long distanceSquaredToRefPoint2 = xDistanceToRefPoint2 * xDistanceToRefPoint2 + yDistanceToRefPoint2 * yDistanceToRefPoint2;
            long distanceSquared = (distanceSquaredToRefPoint1 <= distanceSquaredToRefPoint2) ? distanceSquaredToRefPoint1 : distanceSquaredToRefPoint2;
//            long relativeValue = (distanceSquared << 20) + i;
//            sortMe[i] = relativeValue;
			sortMe[i] = new Pair(toSort[i], distanceSquared);
        }
        Pair[] temp = sortMe.clone();
		mergeSort(sortMe, temp, 0, sortMe.length - 1);
        Arrays.sort(sortMe);
//        int[][] toReturn = new int[count][];
//        for (int i = 0; i < count; ++i) {
//            int index = (int) (sortMe[i] & 0xFFFFF);
//            toReturn[i] = toSort[index];
//        }
//		return toReturn;
		return sortMe;
	}

	private static void mergeSort(Pair[] array, Pair[] temp, int lowIndex, int highIndex) {
		if (highIndex <= lowIndex + 11) {
			insertionSort(array, lowIndex, highIndex);
			return;
		}
		int midIndex = (lowIndex + highIndex) / 2;
		mergeSort(temp, array, lowIndex, midIndex);
		mergeSort(temp, array, midIndex + 1, highIndex);
		merge(array, temp, lowIndex, midIndex, highIndex);
	}

	private static void insertionSort(Pair[] array, int lowIndex, int highIndex) {
		for (int i = lowIndex; i <= highIndex; ++i) {
			for (int j = i; j > lowIndex; --j) {
				int leftNeighbor = j - 1;
				if (array[j].compareTo(array[leftNeighbor]) < 0) {
					Pair temp = array[j];
					array[j] = array[leftNeighbor];
					array[leftNeighbor] = temp;
				} else {
					break;
				}
			}
		}
	}

	private static void merge(Pair[] array, Pair[] temp, int lowIndex, int midIndex, int highIndex) {
		int i = lowIndex;
		int j = midIndex + 1;
		for (int k = lowIndex; k <= highIndex; k++) {
			if (i > midIndex) {
				array[k] = temp[j++];
			} else if (j > highIndex) {
				array[k] = temp[i++];
			} else if (temp[j].compareTo(temp[i]) < 0) {
				array[k] = temp[j++];
			} else {
				array[k] = temp[i++];
			}
		}
	}

	private static class Pair implements Comparable<Pair> {
		int[] data;
		long distanceSquared;

		Pair(int[] data, long distanceSquared) {
			this.data = data;
			this.distanceSquared = distanceSquared;
		}

		@Override
		public int compareTo(Pair other) {
			return (this.distanceSquared < other.distanceSquared) ? -1 : ((this.distanceSquared == other.distanceSquared) ? 0 : 1);
		}
	}

	private static int[][] readInData(String inputFileName) {
		ArrayList<int[]> points = new ArrayList<int[]>();
		Scanner in;
		Pattern p = Pattern.compile("\\d+");
		int counter = 0;
		try {
			in = new Scanner(new File(inputFileName));
			// read the two points:
			x1 = Integer.parseInt(in.next());
			y1 = Integer.parseInt(in.next());
			x2 = Integer.parseInt(in.next());
			y2 = Integer.parseInt(in.next());
			in.nextLine(); // skip the rest of the line
			while (in.hasNextLine()) {
				String point = in.nextLine();
				Matcher m = p.matcher(point);
				int[] arrayPoint = new int[3];
				m.find();
				arrayPoint[0] = Integer.parseInt(m.group()); // x coord
				m.find();
				arrayPoint[1] = Integer.parseInt(m.group()); // y coord
				m.find();
				arrayPoint[2] = Integer.parseInt(m.group()); // timestamp
				points.add(arrayPoint);
				counter++;
			}
			in.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return points.toArray(new int[counter][3]); // convert to array of int
													// arrays
	}
	
	private static void writeOutResult(Pair[] sorted, String outputFilename) {
		try {
			PrintWriter out = new PrintWriter(outputFilename);
			for (Pair pair : sorted) {
				out.println(pair.data[0] + " " + pair.data[1] + " " + pair.data[2]);
			}
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
