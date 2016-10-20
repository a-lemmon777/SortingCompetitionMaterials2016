import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrazyQuickSort {
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
				
		long[][] sorted = sort(toSort);
		
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
	private static long[][] sort(int[][] toSort) {
		int count = toSort.length;
		long[][] sortMe = new long[count][4];
		for (int i = 0; i < count; ++i) {
			long xDistanceToRefPoint1 = toSort[i][0] - x1;
			long yDistanceToRefPoint1 = toSort[i][1] - y1;
			long xDistanceToRefPoint2 = toSort[i][0] - x2;
			long yDistanceToRefPoint2 = toSort[i][1] - y2;
			long distanceSquaredToRefPoint1 = xDistanceToRefPoint1 * xDistanceToRefPoint1 + yDistanceToRefPoint1 * yDistanceToRefPoint1;
			long distanceSquaredToRefPoint2 = xDistanceToRefPoint2 * xDistanceToRefPoint2 + yDistanceToRefPoint2 * yDistanceToRefPoint2;
			long distanceSquared = (distanceSquaredToRefPoint1 <= distanceSquaredToRefPoint2) ? distanceSquaredToRefPoint1 : distanceSquaredToRefPoint2;
			long relativeValue = (distanceSquared << 20) + i;
			long[] row = sortMe[i];

			row[0] = relativeValue;
			row[1] = toSort[i][0];
			row[2] = toSort[i][1];
			row[3] = i;
		}
		Comparator<long[]> comparator = new Comparator<long[]>() {
			@Override
			public int compare(long[] first, long[] second) {
				return (first[0] < second[0]) ? -1 : ((first[0] == second[0]) ? 0 : 1);
			}
		};
		sort(sortMe, 0, count - 1, true, comparator);
//        int[][] toReturn = new int[count][];
//        for (int i = 0; i < count; ++i) {
//            int index = (int) (sortMe[i] & 0xFFFFF);
//            toReturn[i] = toSort[index];
//        }
//		return toReturn;
		return sortMe;
	}

	/**
	 * Sorts the specified range of the array by Dual-Pivot Quicksort.
	 *
	 * @param a the array to be sorted
	 * @param left the index of the first element, inclusive, to be sorted
	 * @param right the index of the last element, inclusive, to be sorted
	 * @param leftmost indicates if this part is the leftmost in the range
	 */
	private static void sort(long[][] a, int left, int right, boolean leftmost, Comparator<long[]> comparator) {
		int length = right - left + 1;

		// Use insertion sort on tiny arrays
		if (length < 47) {
			if (leftmost) {
                /*
                 * Traditional (without sentinel) insertion sort,
                 * optimized for server VM, is used in case of
                 * the leftmost part.
                 */
				for (int i = left, j = i; i < right; j = ++i) {
					long[] ai = a[i + 1];
					while (comparator.compare(ai, a[j]) < 0) {
						a[j + 1] = a[j];
						if (j-- == left) {
							break;
						}
					}
					a[j + 1] = ai;
				}
			} else {
                /*
                 * Skip the longest ascending sequence.
                 */
				do {
					if (left >= right) {
						return;
					}
				} while (comparator.compare(a[++left], a[left - 1]) >= 0);

                /*
                 * Every element from adjoining part plays the role
                 * of sentinel, therefore this allows us to avoid the
                 * left range check on each iteration. Moreover, we use
                 * the more optimized algorithm, so called pair insertion
                 * sort, which is faster (in the context of Quicksort)
                 * than traditional implementation of insertion sort.
                 */
				for (int k = left; ++left <= right; k = ++left) {
					long[] a1 = a[k], a2 = a[left];

					if (comparator.compare(a1, a2) < 0) {
						a2 = a1; a1 = a[left];
					}
					while (comparator.compare(a1, a[--k]) < 0) {
						a[k + 2] = a[k];
					}
					a[++k + 1] = a1;

					while (comparator.compare(a2, a[--k]) < 0) {
						a[k + 1] = a[k];
					}
					a[k + 1] = a2;
				}
				long[] last = a[right];

				while (comparator.compare(last, a[--right]) < 0) {
					a[right + 1] = a[right];
				}
				a[right + 1] = last;
			}
			return;
		}

		// Inexpensive approximation of length / 7
		int seventh = (length >> 3) + (length >> 6) + 1;

        /*
         * Sort five evenly spaced elements around (and including) the
         * center element in the range. These elements will be used for
         * pivot selection as described below. The choice for spacing
         * these elements was empirically determined to work well on
         * a wide variety of inputs.
         */
		int e3 = (left + right) >>> 1; // The midpoint
		int e2 = e3 - seventh;
		int e1 = e2 - seventh;
		int e4 = e3 + seventh;
		int e5 = e4 + seventh;

		// Sort these elements using insertion sort
		if (comparator.compare(a[e2], a[e1]) < 0) { long[] t = a[e2]; a[e2] = a[e1]; a[e1] = t; }

		if (comparator.compare(a[e3], a[e2]) < 0) { long[] t = a[e3]; a[e3] = a[e2]; a[e2] = t;
			if (comparator.compare(t, a[e1]) < 0) { a[e2] = a[e1]; a[e1] = t; }
		}
		if (comparator.compare(a[e4], a[e3]) < 0) { long[] t = a[e4]; a[e4] = a[e3]; a[e3] = t;
			if (comparator.compare(t, a[e2]) < 0) { a[e3] = a[e2]; a[e2] = t;
				if (comparator.compare(t, a[e1]) < 0) { a[e2] = a[e1]; a[e1] = t; }
			}
		}
		if (comparator.compare(a[e5], a[e4]) < 0) { long[] t = a[e5]; a[e5] = a[e4]; a[e4] = t;
			if (comparator.compare(t, a[e3]) < 0) { a[e4] = a[e3]; a[e3] = t;
				if (comparator.compare(t, a[e2]) < 0) { a[e3] = a[e2]; a[e2] = t;
					if (comparator.compare(t, a[e1]) < 0) { a[e2] = a[e1]; a[e1] = t; }
				}
			}
		}

		// Pointers
		int less  = left;  // The index of the first element of center part
		int great = right; // The index before the first element of right part

		if (a[e1] != a[e2] && a[e2] != a[e3] && a[e3] != a[e4] && a[e4] != a[e5]) {
            /*
             * Use the second and fourth of the five sorted elements as pivots.
             * These values are inexpensive approximations of the first and
             * second terciles of the array. Note that pivot1 <= pivot2.
             */
			long[] pivot1 = a[e2];
			long[] pivot2 = a[e4];

            /*
             * The first and the last elements to be sorted are moved to the
             * locations formerly occupied by the pivots. When partitioning
             * is complete, the pivots are swapped back into their final
             * positions, and excluded from subsequent sorting.
             */
			a[e2] = a[left];
			a[e4] = a[right];

            /*
             * Skip elements, which are less or greater than pivot values.
             */
			while (comparator.compare(a[++less], pivot1) < 0);
			while (comparator.compare(a[--great], pivot2) > 0);

            /*
             * Partitioning:
             *
             *   left part           center part                   right part
             * +--------------------------------------------------------------+
             * |  < pivot1  |  pivot1 <= && <= pivot2  |    ?    |  > pivot2  |
             * +--------------------------------------------------------------+
             *               ^                          ^       ^
             *               |                          |       |
             *              less                        k     great
             *
             * Invariants:
             *
             *              all in (left, less)   < pivot1
             *    pivot1 <= all in [less, k)     <= pivot2
             *              all in (great, right) > pivot2
             *
             * Pointer k is the first index of ?-part.
             */
			outer:
			for (int k = less - 1; ++k <= great; ) {
				long[] ak = a[k];
				if (comparator.compare(ak, pivot1) < 0) { // Move a[k] to left part
					a[k] = a[less];
                    /*
                     * Here and below we use "a[i] = b; i++;" instead
                     * of "a[i++] = b;" due to performance issue.
                     */
					a[less] = ak;
					++less;
				} else if (comparator.compare(ak, pivot2) > 0) { // Move a[k] to right part
					while (comparator.compare(a[great], pivot2) > 0) {
						if (great-- == k) {
							break outer;
						}
					}
					if (comparator.compare(a[great], pivot1) < 0) { // a[great] <= pivot2
						a[k] = a[less];
						a[less] = a[great];
						++less;
					} else { // pivot1 <= a[great] <= pivot2
						a[k] = a[great];
					}
                    /*
                     * Here and below we use "a[i] = b; i--;" instead
                     * of "a[i--] = b;" due to performance issue.
                     */
					a[great] = ak;
					--great;
				}
			}

			// Swap pivots into their final positions
			a[left]  = a[less  - 1]; a[less  - 1] = pivot1;
			a[right] = a[great + 1]; a[great + 1] = pivot2;

			// Sort left and right parts recursively, excluding known pivots
			sort(a, left, less - 2, leftmost, comparator);
			sort(a, great + 2, right, false, comparator);

            /*
             * If center part is too large (comprises > 4/7 of the array),
             * swap internal pivot values to ends.
             */
			if (less < e1 && e5 < great) {
                /*
                 * Skip elements, which are equal to pivot values.
                 */
				while (a[less] == pivot1) {
					++less;
				}

				while (a[great] == pivot2) {
					--great;
				}

                /*
                 * Partitioning:
                 *
                 *   left part         center part                  right part
                 * +----------------------------------------------------------+
                 * | == pivot1 |  pivot1 < && < pivot2  |    ?    | == pivot2 |
                 * +----------------------------------------------------------+
                 *              ^                        ^       ^
                 *              |                        |       |
                 *             less                      k     great
                 *
                 * Invariants:
                 *
                 *              all in (*,  less) == pivot1
                 *     pivot1 < all in [less,  k)  < pivot2
                 *              all in (great, *) == pivot2
                 *
                 * Pointer k is the first index of ?-part.
                 */
				outer:
				for (int k = less - 1; ++k <= great; ) {
					long[] ak = a[k];
					if (ak == pivot1) { // Move a[k] to left part
						a[k] = a[less];
						a[less] = ak;
						++less;
					} else if (ak == pivot2) { // Move a[k] to right part
						while (a[great] == pivot2) {
							if (great-- == k) {
								break outer;
							}
						}
						if (a[great] == pivot1) { // a[great] < pivot2
							a[k] = a[less];
                            /*
                             * Even though a[great] equals to pivot1, the
                             * assignment a[less] = pivot1 may be incorrect,
                             * if a[great] and pivot1 are floating-point zeros
                             * of different signs. Therefore in float and
                             * double sorting methods we have to use more
                             * accurate assignment a[less] = a[great].
                             */
							a[less] = pivot1;
							++less;
						} else { // pivot1 < a[great] < pivot2
							a[k] = a[great];
						}
						a[great] = ak;
						--great;
					}
				}
			}

			// Sort center part recursively
			sort(a, less, great, false, comparator);

		} else { // Partitioning with one pivot
            /*
             * Use the third of the five sorted elements as pivot.
             * This value is inexpensive approximation of the median.
             */
			long[] pivot = a[e3];

            /*
             * Partitioning degenerates to the traditional 3-way
             * (or "Dutch National Flag") schema:
             *
             *   left part    center part              right part
             * +-------------------------------------------------+
             * |  < pivot  |   == pivot   |     ?    |  > pivot  |
             * +-------------------------------------------------+
             *              ^              ^        ^
             *              |              |        |
             *             less            k      great
             *
             * Invariants:
             *
             *   all in (left, less)   < pivot
             *   all in [less, k)     == pivot
             *   all in (great, right) > pivot
             *
             * Pointer k is the first index of ?-part.
             */
			for (int k = less; k <= great; ++k) {
				if (a[k] == pivot) {
					continue;
				}
				long[] ak = a[k];
				if (comparator.compare(ak, pivot) < 0) { // Move a[k] to left part
					a[k] = a[less];
					a[less] = ak;
					++less;
				} else { // a[k] > pivot - Move a[k] to right part
					while (comparator.compare(a[great], pivot) > 0) {
						--great;
					}
					if (comparator.compare(a[great], pivot) < 0) { // a[great] <= pivot
						a[k] = a[less];
						a[less] = a[great];
						++less;
					} else { // a[great] == pivot
                        /*
                         * Even though a[great] equals to pivot, the
                         * assignment a[k] = pivot may be incorrect,
                         * if a[great] and pivot are floating-point
                         * zeros of different signs. Therefore in float
                         * and double sorting methods we have to use
                         * more accurate assignment a[k] = a[great].
                         */
						a[k] = pivot;
					}
					a[great] = ak;
					--great;
				}
			}

            /*
             * Sort left and right parts recursively.
             * All elements from center part are equal
             * and, therefore, already sorted.
             */
			sort(a, left, less - 1, leftmost, comparator);
			sort(a, great + 1, right, false, comparator);
		}
	}

	/**
	 * Sorts the specified range of the array using the given
	 * workspace array slice if possible for merging
	 *
	 * @param a the array to be sorted
	 * @param left the index of the first element, inclusive, to be sorted
	 * @param right the index of the last element, inclusive, to be sorted
	 * @param work a workspace array (slice)
	 * @param workBase origin of usable space in work array
	 * @param workLen usable size of work array
	 */
	static void sort(long[][] a, int left, int right,
					 long[][] work, int workBase, int workLen, Comparator<long[]> comparator) {
		// Use Quicksort on small arrays
		if (right - left < 286) {
			sort(a, left, right, true, comparator);
			return;
		}

        /*
         * Index run[i] is the start of i-th run
         * (ascending or descending sequence).
         */
		int[] run = new int[67 + 1];
		int count = 0; run[0] = left;

		// Check if the array is nearly sorted
		for (int k = left; k < right; run[count] = k) {
			if (comparator.compare(a[k], a[k+1]) < 0) { // ascending
				while (++k <= right && comparator.compare(a[k-1], a[k]) <= 0);
			} else if (comparator.compare(a[k], a[k+1]) > 0) { // descending
				while (++k <= right && comparator.compare(a[k-1], a[k]) >= 0);
				for (int lo = run[count] - 1, hi = k; ++lo < --hi; ) {
					long[] t = a[lo]; a[lo] = a[hi]; a[hi] = t;
				}
			} else { // equal
				for (int m = 33; ++k <= right && a[k - 1] == a[k]; ) {
					if (--m == 0) {
						sort(a, left, right, true, comparator);
						return;
					}
				}
			}

            /*
             * The array is not highly structured,
             * use Quicksort instead of merge sort.
             */
			if (++count == 67) {
				sort(a, left, right, true, comparator);
				return;
			}
		}

		// Check special cases
		// Implementation note: variable "right" is increased by 1.
		if (run[count] == right++) { // The last run contains one element
			run[++count] = right;
		} else if (count == 1) { // The array is already sorted
			return;
		}

		// Determine alternation base for merge
		byte odd = 0;
		for (int n = 1; (n <<= 1) < count; odd ^= 1);

		// Use or create temporary array b for merging
		long[][] b;                 // temp array; alternates with a
		int ao, bo;              // array offsets from 'left'
		int blen = right - left; // space needed for b
		if (work == null || workLen < blen || workBase + blen > work.length) {
			work = new long[blen][4];
			workBase = 0;
		}
		if (odd == 0) {
			System.arraycopy(a, left, work, workBase, blen);
			b = a;
			bo = 0;
			a = work;
			ao = workBase - left;
		} else {
			b = work;
			ao = 0;
			bo = workBase - left;
		}

		// Merging
		for (int last; count > 1; count = last) {
			for (int k = (last = 0) + 2; k <= count; k += 2) {
				int hi = run[k], mi = run[k - 1];
				for (int i = run[k - 2], p = i, q = mi; i < hi; ++i) {
					if (q >= hi || p < mi && comparator.compare(a[p + ao], a[q + ao]) <= 0) {
						b[i + bo] = a[p++ + ao];
					} else {
						b[i + bo] = a[q++ + ao];
					}
				}
				run[++last] = hi;
			}
			if ((count & 1) != 0) {
				for (int i = right, lo = run[count - 1]; --i >= lo;
					 b[i + bo] = a[i + ao]
						);
				run[++last] = right;
			}
			long[][] t = a; a = b; b = t;
			int o = ao; ao = bo; bo = o;
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

	private static void writeOutResult(long[][] sorted, String outputFilename) {
		try {
			PrintWriter out = new PrintWriter(outputFilename);
			for (long[] point : sorted) {
				out.println(point[1] + " " + point[2] + " " + point[3]);
			}
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
