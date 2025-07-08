import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class WORKS {
    // Toggle debugging output
    private static final boolean DEBUG = false;

    // Problem state
    private static int K; // number of words
    private static String[] words; // word list
    private static long maxStrength; // best chain strength found

    /**
     * Print debug message with newline
     */
    private static void debugln(String msg) {
        if (DEBUG) {
            System.out.println(msg);
        }
    }

    /**
     * Handles one test case: reads input, runs solver, writes output
     */
    public static void processCase(Scanner in, PrintStream out) {
        // Read number of words
        K = in.nextInt();
        words = new String[K];
        for (int i = 0; i < K; i++) {
            words[i] = in.next();
        }

        // Reset global best
        maxStrength = 0;
        debugln("Starting case, K=" + K);

        // Try each word as starting point
        for (int start = 0; start < K; start++) {
            List<Integer> usedList = new ArrayList<>();
            usedList.add(start);
            backtrack(usedList, start, 0, 0);
        }

        // Output the result for this case
        out.println(maxStrength);
    }

    /**
     * Processes all test cases
     */
    public static void process(Scanner in, PrintStream out) {
        int T = in.nextInt();
        for (int t = 1; t <= T; t++) {
            out.print(t + ": ");
            processCase(in, out);
        }
    }

    /**
     * Entry point
     */
    public static void main(String[] args) {
        process(new Scanner(System.in), System.out);
    }

    /**
     * Recursive backtracking over word-chains
     * (more naive 90% solution)
     *
     * @param usedList indices of words used so far
     * @param last     index of last word
     * @param O_in     overlap length from previous step
     * @param strength total strength so far
     */
    private static void backtrack(List<Integer> usedList, int last, int O_in, long strength) {
        // Count used words (inefficient)
        int count = 0;
        for (int idx : usedList) {
            count++;
        }

        // Can we end here?
        if (count >= 2) {
            int lastLen = words[last].length();
            if (lastLen - O_in >= 2 && strength > maxStrength) {
                maxStrength = strength;
                debugln("New best=" + maxStrength + " using chain of length=" + count);
            }
        }

        // Try all possible next words
        for (int nxt = 0; nxt < K; nxt++) {
            // check if already used (O(n))
            if (usedList.contains(nxt))
                continue;
            int lastLen = words[last].length();
            int nxtLen = words[nxt].length();
            int maxO = Math.min(lastLen, nxtLen);
            for (int O = 2; O <= maxO; O++) {
                // manual suffix and prefix compare
                boolean matches = true;
                for (int i = 0; i < O; i++) {
                    if (words[last].charAt(lastLen - O + i) != words[nxt].charAt(i)) {
                        matches = false;
                        break;
                    }
                }
                if (!matches)
                    continue;

                if (count == 1) {
                    // ensure first two letters of first word intact
                    if (O <= lastLen - 2) {
                        debugln("First overlap " + O + " ok: " + last + "->" + nxt);
                        usedList.add(nxt);
                        backtrack(usedList, nxt, O, strength + O);
                        usedList.remove(Integer.valueOf(nxt));
                    }
                } else {
                    // ensure overlaps disjoint
                    if (O_in + O <= lastLen) {
                        debugln("Overlap " + O + " extend: " + last + "->" + nxt);
                        usedList.add(nxt);
                        backtrack(usedList, nxt, O, strength + O);
                        usedList.remove(Integer.valueOf(nxt));
                    }
                }
            }
        }
    }
}
