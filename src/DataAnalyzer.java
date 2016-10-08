import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by a.lemmon777 on 10/8/2016.
 */
public class DataAnalyzer {
    private static int x1;
    private static int y1;
    private static int x2;
    private static int y2;

    public static void main(String[] args) throws InterruptedException {
        if (args.length < 4) {
            System.out
                    .println("Please run with two command line arguments: input and output file names");
            System.exit(0);
        }

        String inputFileName = args[0];
        String outFileName = args[1];
        int gridsize = Integer.parseInt(args[2]);
        int sectors = Integer.parseInt(args[3]);

        int[][] points = readInData(inputFileName);
        int[][] grid = new int[sectors][sectors];
        int sectorWidth = gridsize / sectors;

        for (int i = 0; i < points.length; ++i) {
            int x = points[i][0];
            int y = points[i][1];
            int xSector = Math.min(sectors - 1, x/sectorWidth);
            int ySector = Math.min(sectors - 1, y/sectorWidth);
            grid[xSector][ySector]++;
        }


        writeOutResult(grid, outFileName);

        /**
         * Testing (assuming data2.txt points)
         int[] test1 = {23790, 94342, 8922566};
         int[] test2 = {77579, 45587, 8546858};
         System.out.println(distance(test1));
         System.out.println(distance(test2));
         **/

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

    private static void writeOutResult(int[][] sorted, String outputFilename) {
        try {
            PrintWriter out = new PrintWriter(outputFilename);
            for (int[] point : sorted) {
//                out.println(point[0] + " " + point[1] + " " + point[2]);
                for (int measurement : point) {
                    out.print(measurement + "\t");
                }
                out.println();
            }
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
