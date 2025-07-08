import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    private static final boolean DEBUG = false;

    // Global Minimum of minimum perimeter sum
    private static long minSumPerimeter;

    // Global memoized cache for rectangle perims
    private static long[][] minVerticalPerimeter;
    private static long[][] minHorizontalPerimeter;

    private static void debug(String message) {
        if (DEBUG) {
            System.out.print(message);
        }
    }

    private static void debugln(String message) {
        if (DEBUG) {
            System.out.println(message);
        }
    }

    public static void main(String[] argv) {
        debugln("Starting NW Run");
        process(new Scanner(System.in), System.out);
        debugln("Ending NW Run");
    }

    public static void process(Scanner in, PrintStream out) {
        int N = in.nextInt();
        debugln("Num Test Cases --> " + N);

        for (int i = 1; i <= N; i++) {
            out.print("" + i + ": ");
            debugln("Processing Test Case ==> " + i);
            processCase(in, out);
        }
    }

    public static void processCase(Scanner in, PrintStream out) {
        // Getting grid dims
        // Rows
        int X = in.nextInt();
        // Columns
        int Y = in.nextInt();
        // Num houses
        int H = in.nextInt();
        // Targets
        int K = in.nextInt();
        debugln("Grid Dimensions: X = " + X + ", Y = " + Y + ", H = " + H + ", K = " + K);

        // Init grid, mark house locs in 2D array
        int hGrid[][] = new int[X][Y];
        // Populate
        for (int i = 0; i < H; i++) {
            int x = in.nextInt();
            int y = in.nextInt();
            hGrid[x][y]++;
        }

        if (DEBUG) {
            displayTest(hGrid);
        }

        // Run test iteration loop
        long res = runTesti(X, Y, K, hGrid);

        if (res == Long.MAX_VALUE) {
            out.println("none");
            debugln("Result: none");
        } else {
            out.println(res);
            debugln("Result: " + res);
        }
    }

    public static long runTesti(int X, int Y, int K, int hGrid[][]) {
        minSumPerimeter = Long.MAX_VALUE;
        // init Arrays
        minVerticalPerimeter = new long[X][X];
        minHorizontalPerimeter = new long[Y][Y];

        // Init sum array of prefixes with max vals
        for (int i = 0; i < X; i++) {
            Arrays.fill(minVerticalPerimeter[i], Long.MAX_VALUE);
        }

        for (int i = 0; i < Y; i++) {
            Arrays.fill(minHorizontalPerimeter[i], Long.MAX_VALUE);
        }

        // Gets the prefix sum array --> much more efficent as it does queroes up front
        long prefixes[][] = compSumPrefixes(X, Y, hGrid);
        debugln("Prefix Sum Array Computed Successfully");

        // Iterate through all recs with K houses exactly
        iterateThroughRecs(X, Y, K, prefixes);
        debugln("Iterated through all rectangles with K houses");

        // Broadcast min perimeters
        broadcastMinimumPerimeters(X, Y);
        debugln("Broadcasted minimum perimeters");

        // Get min perimeter sum for horizontals and verticals
        computeMinimumSum(X, Y);
        debugln("Final min perim sum: " + (minSumPerimeter == Long.MAX_VALUE ? "none" : minSumPerimeter));

        return minSumPerimeter;
    }

    private static long[][] compSumPrefixes(int X, int Y, int hGrid[][]) {
        // Init arrays with +1's to account for edge cases!
        long prefixes[][] = new long[X + 1][Y + 1];

        // Compoute sum of houses in subgrid
        for (int i = 0; i < X; i++) {
            for (int j = 0; j < Y; j++) {
                // long gridPos = hGrid[i][j];
                // long prefixPos = prefixes[i + 1][j] + prefixes[i][j + 1] - prefixes[i][j];
                // prefixes[i + 1][j + 1] = gridPos + prefixPos;
                prefixes[i + 1][j + 1] = hGrid[i][j] + prefixes[i + 1][j] + prefixes[i][j + 1] - prefixes[i][j];
            }
        }

        return prefixes;
    }

    private static void iterateThroughRecs(int X, int Y, int K, long prefixes[][]) {
        // For all possible boundaries...
        for (int x1 = 0; x1 < X; x1++) {
            for (int x2 = x1; x2 < X; x2++) {
                for (int y1 = 0; y1 < Y; y1++) {
                    for (int y2 = y1; y2 < Y; y2++) {
                        // Get the sum of houses in the curr rectangle
                        long sum = getSum(prefixes, x1, y1, x2, y2);
                        if (sum == K) {
                            // dim of curr rectangle
                            int w = x2 - x1 + 1;
                            int h = y2 - y1 + 1;
                            long perim = 2L * (w + h);

                            // Update global min perimeter for columns and rows
                            minVerticalPerimeter[x1][x2] = Math.min(minVerticalPerimeter[x1][x2], perim);
                            minHorizontalPerimeter[y1][y2] = Math.min(minHorizontalPerimeter[y1][y2], perim);
                            debugln("Found rectangle with K houses: " + x1 + ", " + y1 + ", " + x2 + ", " + y2);
                            debugln("Perimeter: " + perim);
                        }
                    }
                }
            }
        }
    }

    private static long getSum(long prefixes[][], int x1, int y1, int x2, int y2) {
        // Num houses in rectangle
        // long total = prefixes[x2 + 1][y2 + 1];
        // long top = prefixes[x1][y2 + 1];
        // long left = prefixes[x2 + 1][y1];
        // long overlap = prefixes[x1][y1];
        //
        // return total - top - left + overlap;

        return prefixes[x2 + 1][y2 + 1] - prefixes[x1][y2 + 1] - prefixes[x2 + 1][y1] + prefixes[x1][y1];
    }

    private static void broadcastMinimumPerimeters(int X, int Y) {
        // For each column [x1, x2], get min perimeter for cols x2>=x1
        for (int i = 0; i < X; i++) {
            long min = Long.MAX_VALUE;
            for (int j = i; j < X; j++) {
                min = Math.min(min, minVerticalPerimeter[i][j]);
                minVerticalPerimeter[i][j] = min;
            }
        }

        // For each row [y1, y2], get min perimeter for rows y2>=y1
        for (int i = 0; i < Y; i++) {
            long min = Long.MAX_VALUE;
            for (int j = i; j < Y; j++) {
                min = Math.min(min, minHorizontalPerimeter[i][j]);
                minHorizontalPerimeter[i][j] = min;
            }
        }
    }

    private static void computeMinimumSum(int X, int Y) {
        // Vertical splits
        for (int i = 1; i < X; i++) {
            // Left--> min perimeter for any rectangle with x2 < i
            long leftMin = Long.MAX_VALUE;
            for (int x1 = 0; x1 < i; x1++) {
                if (minVerticalPerimeter[x1][i - 1] != Long.MAX_VALUE) {
                    leftMin = Math.min(leftMin, minVerticalPerimeter[x1][i - 1]);
                }
            }

            // Right--> min perimeter for any rectangle with x1 >= i
            long rightMin = Long.MAX_VALUE;
            for (int x2 = i; x2 < X; x2++) {
                if (minVerticalPerimeter[i][x2] != Long.MAX_VALUE) {
                    rightMin = Math.min(rightMin, minVerticalPerimeter[i][x2]);
                }
            }
            // If both left and right min are not max, update minSumPerimeter
            if (leftMin != Long.MAX_VALUE && rightMin != Long.MAX_VALUE) {
                minSumPerimeter = Math.min(minSumPerimeter, leftMin + rightMin);
            }
        }

        // Horizontal splits
        for (int i = 1; i < Y; i++) {
            // Top--> min perimeter for any rectangle with y2 < i
            long topMin = Long.MAX_VALUE;
            for (int y1 = 0; y1 < i; y1++) {
                if (minHorizontalPerimeter[y1][i - 1] != Long.MAX_VALUE) {
                    topMin = Math.min(topMin, minHorizontalPerimeter[y1][i - 1]);
                }
            }

            // Bottom--> min perimeter for any rectangle with y1 >= i
            long bottomMin = Long.MAX_VALUE;
            for (int y2 = i; y2 < Y; y2++) {
                if (minHorizontalPerimeter[i][y2] != Long.MAX_VALUE) {
                    bottomMin = Math.min(bottomMin, minHorizontalPerimeter[i][y2]);
                }
            }
            // If both top and bottom min are not max, update minSumPerimeter
            if (topMin != Long.MAX_VALUE && bottomMin != Long.MAX_VALUE) {
                minSumPerimeter = Math.min(minSumPerimeter, topMin + bottomMin);
            }
        }
    }

    public static void displayTest(int hGrid[][]) {
        debugln("Current Case: ");
        debugln(viewArray(hGrid));
    }

    public static String viewArray(int hGrid[][]) {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < hGrid.length; i++) {
            for (int j = 0; j < hGrid[0].length; j++) {
                otp.append(hGrid[i][j]);
                if (j < hGrid[0].length - 1) {
                    otp.append(" ");
                }
            }
            if (i < hGrid.length - 1) {
                otp.append("\n");
            }
        }

        return otp.toString();
    }

}
