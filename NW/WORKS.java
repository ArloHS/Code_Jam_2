import java.util.*;

public class WORKS {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int N = sc.nextInt();
        for (int test = 1; test <= N; test++) {
            int X = sc.nextInt();
            int Y = sc.nextInt();
            int H = sc.nextInt();
            int K = sc.nextInt();
            int[][] houseGrid = new int[X][Y];
            for (int i = 0; i < H; i++) {
                int x = sc.nextInt();
                int y = sc.nextInt();
                houseGrid[x][y]++;
            }
            long result = solve(X, Y, K, houseGrid);
            System.out.printf("%d: %s%n", test, result == Long.MAX_VALUE ? "none" : result);
        }
        sc.close();
    }

    static long solve(int X, int Y, int K, int[][] houseGrid) {
        // Compute prefix sum array
        long[][] prefix = new long[X + 1][Y + 1];
        for (int x = 0; x < X; x++) {
            for (int y = 0; y < Y; y++) {
                prefix[x + 1][y + 1] = houseGrid[x][y] + prefix[x + 1][y] + prefix[x][y + 1] - prefix[x][y];
            }
        }

        // Store minimal perimeter for each column range [x1, x2] and row range [y1, y2]
        long[][] minPerimVert = new long[X][X]; // [x1][x2]
        long[][] minPerimHoriz = new long[Y][Y]; // [y1][y2]
        for (int i = 0; i < X; i++)
            Arrays.fill(minPerimVert[i], Long.MAX_VALUE);
        for (int i = 0; i < Y; i++)
            Arrays.fill(minPerimHoriz[i], Long.MAX_VALUE);

        // Enumerate all rectangles with K houses
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
                        }
                    }
                }
            }
        }

        // For each column range [x1, x2], propagate minimal perimeter
        for (int x1 = 0; x1 < X; x1++) {
            long min = Long.MAX_VALUE;
            for (int x2 = x1; x2 < X; x2++) {
                min = Math.min(min, minPerimVert[x1][x2]);
                minPerimVert[x1][x2] = min;
            }
        }
        // For each row range [y1, y2]
        for (int y1 = 0; y1 < Y; y1++) {
            long min = Long.MAX_VALUE;
            for (int y2 = y1; y2 < Y; y2++) {
                min = Math.min(min, minPerimHoriz[y1][y2]);
                minPerimHoriz[y1][y2] = min;
            }
        }

        long minSum = Long.MAX_VALUE;

        // Vertical splits
        for (int c = 1; c < X; c++) {
            // Left: min perimeter for any rectangle with x2 < c
            long leftMin = Long.MAX_VALUE;
            for (int x1 = 0; x1 < c; x1++) {
                if (minPerimVert[x1][c - 1] != Long.MAX_VALUE) {
                    leftMin = Math.min(leftMin, minPerimVert[x1][c - 1]);
                }
            }
            // Right: min perimeter for any rectangle with x1 >= c
            long rightMin = Long.MAX_VALUE;
            for (int x2 = c; x2 < X; x2++) {
                if (minPerimVert[c][x2] != Long.MAX_VALUE) {
                    rightMin = Math.min(rightMin, minPerimVert[c][x2]);
                }
            }
            if (leftMin != Long.MAX_VALUE && rightMin != Long.MAX_VALUE) {
                minSum = Math.min(minSum, leftMin + rightMin);
            }
        }

        // Horizontal splits
        for (int r = 1; r < Y; r++) {
            // Top: min perimeter for any rectangle with y2 < r
            long topMin = Long.MAX_VALUE;
            for (int y1 = 0; y1 < r; y1++) {
                if (minPerimHoriz[y1][r - 1] != Long.MAX_VALUE) {
                    topMin = Math.min(topMin, minPerimHoriz[y1][r - 1]);
                }
            }
            // Bottom: min perimeter for any rectangle with y1 >= r
            long bottomMin = Long.MAX_VALUE;
            for (int y2 = r; y2 < Y; y2++) {
                if (minPerimHoriz[r][y2] != Long.MAX_VALUE) {
                    bottomMin = Math.min(bottomMin, minPerimHoriz[r][y2]);
                }
            }
            if (topMin != Long.MAX_VALUE && bottomMin != Long.MAX_VALUE) {
                minSum = Math.min(minSum, topMin + bottomMin);
            }
        }

        return minSum == Long.MAX_VALUE ? Long.MAX_VALUE : minSum;
    }

    static long getSum(long[][] prefix, int x1, int y1, int x2, int y2) {
        return prefix[x2 + 1][y2 + 1] - prefix[x1][y2 + 1] - prefix[x2 + 1][y1] + prefix[x1][y1];
    }
}