import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.io.PrintStream;

public class Main {
    private static final boolean DEBUG = false;

    // Num words and list of words
    private static int K;
    private static String WORDS[];

    // Best curr chain strength
    private static long maxCurrStrength;

    private static void debug(String msg) {
        if (DEBUG) {
            System.out.print(msg);
        }
    }

    private static void debugln(String msg) {
        if (DEBUG) {
            System.out.println(msg);
        }
    }

    public static void processCase(Scanner in, PrintStream out) {
        // Read in curr num words and word list
        K = in.nextInt();
        WORDS = new String[K];

        for (int i = 0; i < K; i++) {
            WORDS[i] = in.next();
        }

        // Init and reset global best curr strength
        maxCurrStrength = 0;
        debugln("Start Case: K = " + K);

        // Try every one of words as starting
        for (int i = 0; i < K; i++) {
            List<Integer> usedWordsList = new ArrayList<>();
            usedWordsList.add(i);
            backPropogation(usedWordsList, i, 0, 0);
        }

        // Otp current cases result
        debugln("End Case: K = " + K);
        debugln("Max Current Strength: " + maxCurrStrength);
        out.println(maxCurrStrength);

    }

    public static void process(Scanner in, PrintStream out) {
        // Read num test cases
        int N = in.nextInt();
        debugln("Num Cases ===> " + N);

        for (int i = 1; i <= N; i++) {
            out.print(i + ": ");
            processCase(in, out);
        }
    }

    public static void main(String[] args) {
        process(new Scanner(System.in), System.out);
    }

    private static void backPropogation(List<Integer> usedWordsList, int last, int overlapLen, long strength) {
        // Init counter for words being used
        int countUsedWords = usedWordsList.size();

        // End condition reached??
        if (countUsedWords >= 2) {
            int lastLen = WORDS[last].length();
            if ((lastLen - overlapLen) >= 2 && (strength > maxCurrStrength)) {
                maxCurrStrength = strength;
                debugln("New Max Strength: " + maxCurrStrength + " with overlapLen: " + overlapLen);
            }
        }

        // Check all possible words
        for (int i = 0; i < K; i++) {
            // Check used
            if (usedWordsList.contains(i)) {
                continue;
            }

            int lastLen = WORDS[last].length();
            int nextLen = WORDS[i].length();
            int maxCurrOverlap = Math.min(lastLen, nextLen);

            for (int j = 2; j <= maxCurrOverlap; j++) {
                // cmp suffix and prefix
                boolean match = true;
                for (int k = 0; k < j; k++) {
                    if (WORDS[last].charAt(lastLen - j + k) != WORDS[i].charAt(k)) {
                        match = false;
                        break;
                    }
                }

                if (!match) {
                    continue;
                }

                if (countUsedWords == 1) {
                    // 1st 2 letters of word are correct
                    if (j <= (lastLen - 2)) {
                        debugln("First overlap: " + j);
                        usedWordsList.add(i);
                        backPropogation(usedWordsList, i, j, strength + j);
                        usedWordsList.remove(Integer.valueOf(i));
                    }
                } else {
                    if ((overlapLen + j) <= lastLen) {
                        // disjointed overlap
                        debugln("Overlap: " + j);
                        usedWordsList.add(i);
                        backPropogation(usedWordsList, i, j, strength + j);
                        usedWordsList.remove(Integer.valueOf(i));
                    }
                }
            }
        }

    }

}
