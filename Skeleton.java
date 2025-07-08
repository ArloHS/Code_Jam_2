import java.io.PrintStream;
import java.util.Scanner;
/*
* Skeleton . java
*
* This code outline is NOT COMPULSORY , but it gives you
* a starting point .
*
*/
public class Skeleton {

    private static final boolean DEBUG = false ;
    private static void debug ( String message ) {

    if (DEBUG) { 
        System.out.print(message); }
    }

    private static void debugln(String message) {
        if (DEBUG) { 
            System.out.println(message);
        }
    }

    public static void processCase (Scanner in , PrintStream out) {
        int param = in.nextInt ();
        /* here the miracle happens */
        out.println (/* answer */);
    }

    public static void process (Scanner in , PrintStream out) {
        int N = in.nextInt ();
        for (int i = 1; i <= N ; i ++) {
            out . print (" " + i + " : ␣ ");
            processCase (in , out);
        }
    }
    public static void main (String [] argv) {
        process(new Scanner (System.in), System.out);
    }
}