// package SF;

import java.io.PrintStream;
import java.util.*;

public class WORKS2 {
    // Toggle debug output
    private static final boolean DEBUG = false;

    // Print debug message without newline
    private static void debug(String msg) {
        if (DEBUG) {
            System.out.print(msg);
        }
    }

    // Print debug message with newline
    private static void debugln(String msg) {
        if (DEBUG) {
            System.out.println(msg);
        }
    }

    /**
     * Process one test case: parse two terms, unify, and print result
     */
    public static void processCase(Scanner in, PrintStream out) {
        // Read input line containing two terms
        String line = in.nextLine().trim();
        debugln("Raw input: " + line);

        // Split the line into s1, s2 at top-level space
        int splitPos = -1, depth = 0;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '(') depth++;
            else if (c == ')') depth--;
            else if (c == ' ' && depth == 0) {
                splitPos = i;
                break;
            }
        }
        if (splitPos < 0) splitPos = line.indexOf(' ');
        String s1 = line.substring(0, splitPos).trim();
        String s2 = line.substring(splitPos + 1).trim();
        debugln("Term1: " + s1 + ", Term2: " + s2);

        // Parse terms
        Parser p1 = new Parser(s1);
        Parser p2 = new Parser(s2);
        Term t1 = p1.parseTerm();
        Term t2 = p2.parseTerm();
        debugln("Parsed t1: " + t1);
        debugln("Parsed t2: " + t2);

        // Unification substitutions map
        Map<String, Term> subs = new HashMap<>();
        boolean unified = unify(apply(t1, subs), apply(t2, subs), subs);
        if (!unified) {
            debugln("Unification failed");
            out.println("none");
            return;
        }
        debugln("Unification succeeded: subs=" + subs);

        // Collect all variables that appeared
        Set<String> varSet = new HashSet<>();
        collectVars(t1, varSet);
        collectVars(t2, varSet);
        List<String> vars = new ArrayList<>(varSet);
        Collections.sort(vars);

        // Ensure each var has a binding (default to empty struct)
        for (String v : vars) {
            if (!subs.containsKey(v)) {
                subs.put(v, new Term());
                debugln("Default bind " + v + "=()");
            }
        }

        // Print assignments in order
        for (String v : vars) {
            Term rhs = apply(subs.get(v), subs);
            out.println(v + "=" + rhs);
        }
        out.println(".");
    }

    /**
     * Process all test cases
     */
    public static void process(Scanner in, PrintStream out) {
        int N = Integer.parseInt(in.nextLine().trim());
        debugln("Number of cases: " + N);
        for (int i = 1; i <= N; i++) {
            out.print(i + ":");
            out.println();
            processCase(in, out);
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        process(in, System.out);
        in.close();
    }

    // ----- Unification logic -----
    private static boolean unify(Term a, Term b, Map<String,Term> subs) {
        a = apply(a, subs);
        b = apply(b, subs);
        debugln("Unify call: " + a + " vs " + b);
        // same variable
        if (a.isVar && b.isVar && a.name.equals(b.name)) return true;
        // var on left
        if (a.isVar) {
            if (occurs(a.name, b, subs)) return false;
            subs.put(a.name, b);
            debugln("Bind " + a.name + "=" + b);
            return true;
        }
        // var on right
        if (b.isVar) {
            if (occurs(b.name, a, subs)) return false;
            subs.put(b.name, a);
            debugln("Bind " + b.name + "=" + a);
            return true;
        }
        // both structs
        if (a.isStruct && b.isStruct && a.name.equals(b.name)
                && a.children.size() == b.children.size()) {
            for (int i = 0; i < a.children.size(); i++) {
                if (!unify(a.children.get(i), b.children.get(i), subs)) return false;
            }
            return true;
        }
        // both constants
        if (!a.isStruct && !b.isStruct && a.name.equals(b.name)) return true;
        return false;
    }

    private static Term apply(Term t, Map<String,Term> subs) {
        if (t.isVar && subs.containsKey(t.name)) {
            return apply(subs.get(t.name), subs);
        }
        Term out = new Term(t.name, t.isStruct);
        for (Term c : t.children) {
            out.children.add(apply(c, subs));
        }
        return out;
    }

    private static boolean occurs(String var, Term t, Map<String,Term> subs) {
        t = apply(t, subs);
        if (t.isVar) return t.name.equals(var);
        for (Term c : t.children) {
            if (occurs(var, c, subs)) return true;
        }
        return false;
    }

    private static void collectVars(Term t, Set<String> acc) {
        if (t.isVar) acc.add(t.name);
        for (Term c : t.children) collectVars(c, acc);
    }

    // ----- Parser & Term classes -----
    static class Parser {
        String s; int i;
        Parser(String s) { this.s = s; this.i = 0; }
        
        Term parseTerm() {
            skipSpaces();
            if (i < s.length() && s.charAt(i)=='(') {
                i++; skipSpaces();
                if (i<s.length() && s.charAt(i)==')') { i++; return new Term(); }
                String name="";
                while (i<s.length() && s.charAt(i)!=' ' && s.charAt(i)!=')') name+=s.charAt(i++);
                Term node = new Term(name, true);
                skipSpaces();
                while (i<s.length() && s.charAt(i)!=')') {
                    node.children.add(parseTerm());
                    skipSpaces();
                }
                if (i<s.length() && s.charAt(i)==')') i++;
                return node;
            }
            String name="";
            while (i<s.length() && s.charAt(i)!=' ' && s.charAt(i)!=')') name+=s.charAt(i++);
            boolean isVar = name.length()==1 && Character.isUpperCase(name.charAt(0));
            return new Term(name, false, isVar);
        }
        void skipSpaces() { while (i<s.length() && s.charAt(i)==' ') i++; }
    }

    static class Term {
        String name;
        boolean isVar;
        boolean isStruct;
        List<Term> children = new ArrayList<>();

        // empty struct
        Term() {
            this.name = "()";
            this.isVar = false;
            this.isStruct = true;
        }
        // struct or leaf
        Term(String name, boolean isStruct) {
            this.name = name;
            this.isStruct = isStruct;
            this.isVar = (!isStruct && name.length()==1 && Character.isUpperCase(name.charAt(0)));
        }
        Term(String name, boolean isStruct, boolean isVar) {
            this.name = name;
            this.isStruct = isStruct;
            this.isVar = isVar;
        }

        @Override
        public String toString() {
            if (!isStruct) return name;
            if (name.equals("()")) return "()";
            StringBuilder sb = new StringBuilder();
            sb.append("(").append(name);
            for (Term c: children) sb.append(" ").append(c);
            sb.append(")");
            return sb.toString();
        }
    }
}
