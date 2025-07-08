import java.util.Collections;
import java.util.Scanner;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class Main {
    private static final boolean DEBUG = false;
    private static long bestCost = Long.MAX_VALUE; // Track min aqueduct length
    private static Map<String, Long> memo; // Memo for recursion

    // My debug thingy
    private static void debug(String message) {
        if (DEBUG) {
            System.out.println(message);
        }
    }

    // Debug with newline, super useful
    private static void debugln(String message) {
        if (DEBUG) {
            System.out.println(message);
        }
    }

    // Handle one test case
    public static void processCase(Scanner in, PrintStream out) {
        int K = in.nextInt(); // Num towns
        int D = in.nextInt(); // Num dams
        debugln("Test case with K=" + K + ", D=" + D);
        int[] positions = new int[K];
        for (int i = 0; i < K; i++) {
            positions[i] = in.nextInt(); // Town positions
        }
        debugln("Town positions: " + arrayToString(positions));

        long result = runTesti(positions, K, D);
        out.println(result);
    }

    // Main loop for all test cases
    public static void process(Scanner in, PrintStream out) {
        int N = in.nextInt(); // Num test cases
        debugln("Total test cases: " + N);
        for (int i = 1; i <= N; i++) {
            out.print("" + i + ": ");
            processCase(in, out);
        }
    }

    // Kick it off
    public static void main(String[] argv) {
        process(new Scanner(System.in), System.out);
    }

    // Main logic to find min aqueduct length
    public static long runTesti(int[] positions, int K, int D) {
        bestCost = Long.MAX_VALUE;
        memo = new HashMap<>();
        List<Integer> dams = new ArrayList<>();
        debugln("Starting test for K=" + K + ", D=" + D);
        simulate(positions, K, D, 0, dams); // Try all dam combos
        return bestCost;
    }

    // Recursively try all dam placement combos
    private static void simulate(int[] positions, int K, int D, int index, List<Integer> dams) {
        // Got enough dams, calc cost
        if (dams.size() == D) {
            long cost = calcAqueductLength(positions, K, dams);
            if (cost < bestCost) {
                bestCost = cost;
                debugln("New best cost: " + bestCost + " with dams at " + dams);
            }
            return;
        }

        // No more towns to try
        if (index >= K) {
            return;
        }

        // Memo key: sorted dams + index
        String key = damsToString(dams) + "_" + index;
        if (memo.containsKey(key) && memo.get(key) <= bestCost) {
            debugln("Skipping memoized state: " + key);
            return;
        }

        // Try placing dam at current town
        dams.add(index);
        debugln("Trying dam at town " + index);
        simulate(positions, K, D, index + 1, dams);
        dams.remove(dams.size() - 1); // Backtrack

        // Try skipping this town
        debugln("Skipping town " + index);
        simulate(positions, K, D, index + 1, dams);

        memo.put(key, bestCost);
        debugln("Memoized key=" + key + ", cost=" + bestCost);
    }

    // Calc total aqueduct length for a dam setup
    private static long calcAqueductLength(int[] positions, int K, List<Integer> dams) {
        long total = 0;
        // For each town, find closest dam
        for (int i = 0; i < K; i++) {
            // Skip if town has a dam
            if (dams.contains(i)) {
                continue;
            }
            long minDist = Long.MAX_VALUE;
            // Check distance to each dam
            for (int damIndex : dams) {
                long dist = Math.abs(positions[i] - positions[damIndex]);
                minDist = Math.min(minDist, dist);
            }
            total += minDist;
            debugln("Town " + i + " connects to closest dam with dist=" + minDist);
        }
        debugln("Total aqueduct length for dams " + dams + ": " + total);
        return total;
    }

    // Turn dams list into string for memo key
    private static String damsToString(List<Integer> dams) {
        List<Integer> sorted = new ArrayList<>(dams);
        Collections.sort(sorted);
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < sorted.size(); i++) {
            key.append(sorted.get(i));
            if (i < sorted.size() - 1) {
                key.append(",");
            }
        }
        return key.toString();
    }

    // Helper to print array for debug
    private static String arrayToString(int[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            sb.append(arr[i]);
            if (i < arr.length - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}