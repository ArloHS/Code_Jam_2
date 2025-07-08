import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Scanner;

// Main class to solve the problem of finding the minimum sum of perimeters for two rectangles
// containing exactly K houses in a grid, split either vertically or horizontally.
public class WORKS2 {
    // Debug flag to control debug output
    private static final boolean DEBUG = false;
    // Store the minimum perimeter sum found
    private static long minPerimeterSum;
    // Memoization cache for rectangle perimeters
    private static long[][] minPerimVert;
    private static long[][] minPerimHoriz;

    // Utility method to print debug messages without a newline
    private static void debug(String message) {
        if (DEBUG) {
            System.out.print(message);
        }
    }

    // Utility method to print debug messages with a newline
    private static void debugln(String message) {
        if (DEBUG) {
            System.out.println(message);
        }
    }

    // Processes a single test case: reads grid dimensions, number of houses, and
    // target K,
    // then outputs the minimum perimeter sum or "none" if no solution exists.
    public static void processCase(Scanner in, PrintStream out) {
        // Read grid dimensions (X rows, Y columns), number of houses H, and target K
        int X = in.nextInt();
        int Y = in.nextInt();
        int H = in.nextInt();
        int K = in.nextInt();
        debugln("Test case: X=" + X + ", Y=" + Y + ", H=" + H + ", K=" + K);

        // Initialize grid to mark house locations
        int[][] houseGrid = new int[X][Y];
        for (int i = 0; i < H; i++) {
            int x = in.nextInt();
            int y = in.nextInt();
            houseGrid[x][y]++;
            debugln("House at (" + x + ", " + y + ")");
        }

        // Display test case for debugging
        if (DEBUG) {
            displayTest(houseGrid);
        }

        // Run the test and output the result
        long result = runTesti(X, Y, K, houseGrid);
        debugln("Result: " + (result == Long.MAX_VALUE ? "none" : result));
        out.println(result == Long.MAX_VALUE ? "none" : result);
    }

    // Processes multiple test cases: reads the number of cases and delegates to
    // processCase
    public static void process(Scanner in, PrintStream out) {
        int N = in.nextInt();
        debugln("Total test cases: " + N);
        for (int i = 1; i <= N; i++) {
            out.print("" + i + ": ");
            debugln("Processing test case " + i);
            processCase(in, out);
        }
    }

    // Main entry point: initializes scanner and output stream
    public static void main(String[] argv) {
        debugln("Starting program");
        process(new Scanner(System.in), System.out);
        debugln("Program completed");
    }

    // Main computation method: finds the minimum perimeter sum for two rectangles
    // with K houses
    public static long runTesti(int X, int Y, int K, int[][] houseGrid) {
        minPerimeterSum = Long.MAX_VALUE;
        minPerimVert = new long[X][X];
        minPerimHoriz = new long[Y][Y];
        for (int i = 0; i < X; i++)
            Arrays.fill(minPerimVert[i], Long.MAX_VALUE);
        for (int i = 0; i < Y; i++)
            Arrays.fill(minPerimHoriz[i], Long.MAX_VALUE);
        debugln("Initialized memoization arrays");

        // Compute prefix sum array
        long[][] prefix = computePrefixSum(X, Y, houseGrid);
        debugln("Computed prefix sum array");

        // Enumerate all rectangles to find those with exactly K houses
        enumerateRectangles(X, Y, K, prefix);
        debugln("Enumerated all valid rectangles");

        // Propagate minimum perimeters
        propagateMinPerimeters(X, Y);
        debugln("Propagated minimum perimeters");

        // Compute minimum perimeter sum for vertical and horizontal splits
        computeMinSum(X, Y);
        debugln("Final min perimeter sum: " + (minPerimeterSum == Long.MAX_VALUE ? "none" : minPerimeterSum));

        return minPerimeterSum;
    }

    // Computes the 2D prefix sum array for efficient rectangle sum queries
    private static long[][] computePrefixSum(int X, int Y, int[][] houseGrid) {
        long[][] prefix = new long[X + 1][Y + 1];
        for (int x = 0; x < X; x++) {
            for (int y = 0; y < Y; y++) {
                prefix[x + 1][y + 1] = houseGrid[x][y] + prefix[x + 1][y] + prefix[x][y + 1] - prefix[x][y];
            }
        }
        return prefix;
    }

    // Enumerates all possible rectangles and updates minimum perimeters for those
    // with K houses
    private static void enumerateRectangles(int X, int Y, int K, long[][] prefix) {
        for (int x1 = 0; x1 < X; x1++) {
            for (int x2 = x1; x2 < X; x2++) {
                for (int y1 = 0; y1 < Y; y1++) {
                    for (int y2 = y1; y2 < Y; y2++) {
                        long sum = getSum(prefix, x1, y1, x2, y2);
                        if (sum == K) {
                            int w = x2 - x1 + 1;
                            int h = y2 - y1 + 1;
                            long perim = 2L * (w + h);
                            minPerimVert[x1][x2] = Math.min(minPerimVert[x1][x2], perim);
                            minPerimHoriz[y1][y2] = Math.min(minPerimHoriz[y1][y2], perim);
                            debugln("Valid rectangle x1=" + x1 + ", x2=" + x2 + ", y1=" + y1 + ", y2=" + y2
                                    + ", perimeter=" + perim);
                        }
                    }
                }
            }
        }
    }

    // Propagates minimum perimeters across column and row ranges
    private static void propagateMinPerimeters(int X, int Y) {
        // Vertical ranges
        for (int x1 = 0; x1 < X; x1++) {
            long min = Long.MAX_VALUE;
            for (int x2 = x1; x2 < X; x2++) {
                min = Math.min(min, minPerimVert[x1][x2]);
                minPerimVert[x1][x2] = min;
            }
        }
        // Horizontal ranges
        for (int y1 = 0; y1 < Y; y1++) {
            long min = Long.MAX_VALUE;
            for (int y2 = y1; y2 < Y; y2++) {
                min = Math.min(min, minPerimHoriz[y1][y2]);
                minPerimHoriz[y1][y2] = min;
            }
        }
    }

    // Computes the minimum perimeter sum for vertical and horizontal splits
    private static void computeMinSum(int X, int Y) {
        // Vertical splits
        for (int c = 1; c < X; c++) {
            long leftMin = Long.MAX_VALUE;
            for (int x1 = 0; x1 < c; x1++) {
                if (minPerimVert[x1][c - 1] != Long.MAX_VALUE) {
                    leftMin = Math.min(leftMin, minPerimVert[x1][c - 1]);
                }
            }
            long rightMin = Long.MAX_VALUE;
            for (int x2 = c; x2 < X; x2++) {
                if (minPerimVert[c][x2] != Long.MAX_VALUE) {
                    rightMin = Math.min(rightMin, minPerimVert[c][x2]);
                }
            }
            if (leftMin != Long.MAX_VALUE && rightMin != Long.MAX_VALUE) {
                long sum = leftMin + rightMin;
                minPerimeterSum = Math.min(minPerimeterSum, sum);
                debugln("Vertical split at c=" + c + ", leftMin=" + leftMin + ", rightMin=" + rightMin + ", sum="
                        + sum);
            }
        }

        // Horizontal splits
        for (int r = 1; r < Y; r++) {
            long topMin = Long.MAX_VALUE;
            for (int y1 = 0; y1 < r; y1++) {
                if (minPerimHoriz[y1][r - 1] != Long.MAX_VALUE) {
                    topMin = Math.min(topMin, minPerimHoriz[y1][r - 1]);
                }
            }
            long bottomMin = Long.MAX_VALUE;
            for (int y2 = r; y2 < Y; y2++) {
                if (minPerimHoriz[r][y2] != Long.MAX_VALUE) {
                    bottomMin = Math.min(bottomMin, minPerimHoriz[r][y2]);
                }
            }
            if (topMin != Long.MAX_VALUE && bottomMin != Long.MAX_VALUE) {
                long sum = topMin + bottomMin;
                minPerimeterSum = Math.min(minPerimeterSum, sum);
                debugln("Horizontal split at r=" + r + ", topMin=" + topMin + ", bottomMin=" + bottomMin + ", sum="
                        + sum);
            }
        }
    }

    // Computes the sum of houses in a rectangle using prefix sums
    private static long getSum(long[][] prefix, int x1, int y1, int x2, int y2) {
        return prefix[x2 + 1][y2 + 1] - prefix[x1][y2 + 1] - prefix[x2 + 1][y1] + prefix[x1][y1];
    }

    // Displays the house grid for debugging
    public static void displayTest(int[][] grid) {
        debugln("Current Test Case:");
        debugln(viewArray(grid));
    }

    // Converts the grid to a string representation
    public static String viewArray(int[][] grid) {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                otp.append(grid[i][j]);
                if (j < grid[0].length - 1) {
                    otp.append(" ");
                }
            }
            if (i < grid.length - 1) {
                otp.append("\n");
            }
        }
        return otp.toString();
    }

    // Formats the result for output (used for debugging or alternative output)
    public static String returnCost(long result) {
        return "Result: " + (result == Long.MAX_VALUE ? "none" : result);
    }

    // Reads data from a file (unrelated to main logic, kept for completeness)
    public static String[][] readData(String filePath) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(filePath));
        int numIterations = Integer.parseInt(scanner.nextLine());
        debugln("Reading file: " + filePath + ", Number of Tests: " + numIterations);
        String array[][] = new String[numIterations][];

        for (int i = 0; i < numIterations; i++) {
            int numNames = scanner.nextInt();
            String names[] = new String[numNames];
            int counter = 0;
            while (counter < numNames) {
                names[counter] = scanner.next();
                counter++;
            }
            array[i] = names;
            debugln("Read test case " + i + " with " + numNames + " names");
        }
        return array;
    }
}