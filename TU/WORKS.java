// package SF;

import java.io.PrintStream;
import java.util.*;

public class WORKS {
    // Toggle debugging output
    private static final boolean DEBUG = false;

    private static void debugln(String msg) {
        if (DEBUG)
            System.out.println(msg);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int N = Integer.parseInt(in.nextLine().trim());
        for (int tc = 1; tc <= N; tc++) {
            String line = in.nextLine().trim();
            // find split between the two top‐level terms:
            int splitPos = -1, depth = 0;
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (c == '(')
                    depth++;
                else if (c == ')')
                    depth--;
                else if (c == ' ' && depth == 0) {
                    splitPos = i;
                    break;
                }
            }
            if (splitPos < 0) {
                // fallback: first space
                splitPos = line.indexOf(' ');
            }
            String s1 = line.substring(0, splitPos).trim();
            String s2 = line.substring(splitPos + 1).trim();

            System.out.println(tc + ":");
            processCase(s1, s2, System.out);
        }
        in.close();
    }

    private static void processCase(String s1, String s2, PrintStream out) {
        Term t1 = new Parser(s1).parseTerm();
        Term t2 = new Parser(s2).parseTerm();

        Map<String, Term> subs = new HashMap<>();
        boolean ok = unify(apply(t1, subs), apply(t2, subs), subs);
        if (!ok) {
            out.println("none");
            return;
        }

        // collect all pattern variables
        Set<String> varSet = new HashSet<>();
        collectVars(t1, varSet);
        collectVars(t2, varSet);
        List<String> vars = new ArrayList<>(varSet);
        Collections.sort(vars);

        // ensure every var has at least an empty‐struct binding
        for (String v : vars) {
            if (!subs.containsKey(v)) {
                subs.put(v, new Term()); // ()
            }
        }

        // print each in order
        for (String v : vars) {
            Term rhs = apply(subs.get(v), subs);
            out.println(v + "=" + rhs);
        }
        out.println(".");
    }

    // --- 70% unification ---
    private static boolean unify(Term a, Term b, Map<String, Term> subs) {
        a = apply(a, subs);
        b = apply(b, subs);
        debugln("Unify: " + a + " vs " + b);

        // same variable
        if (a.isVar && b.isVar && a.name.equals(b.name)) {
            return true;
        }
        // var on left
        if (a.isVar) {
            if (occurs(a.name, b, subs))
                return false;
            subs.put(a.name, b);
            return true;
        }
        // var on right
        if (b.isVar) {
            if (occurs(b.name, a, subs))
                return false;
            subs.put(b.name, a);
            return true;
        }
        // both structures
        if (a.isStruct && b.isStruct && a.name.equals(b.name)
                && a.children.size() == b.children.size()) {
            for (int i = 0; i < a.children.size(); i++) {
                if (!unify(a.children.get(i), b.children.get(i), subs)) {
                    return false;
                }
            }
            return true;
        }
        // both constants (non-vars, non-structs)
        if (!a.isStruct && !b.isStruct && a.name.equals(b.name)) {
            return true;
        }
        return false;
    }

    // apply current substitutions fully
    private static Term apply(Term t, Map<String, Term> subs) {
        if (t.isVar && subs.containsKey(t.name)) {
            return apply(subs.get(t.name), subs);
        }
        Term out = new Term(t.name, t.isStruct);
        for (Term c : t.children) {
            out.children.add(apply(c, subs));
        }
        return out;
    }

    // occurs‐check: var must not appear inside t
    private static boolean occurs(String var, Term t, Map<String, Term> subs) {
        t = apply(t, subs);
        if (t.isVar) {
            return t.name.equals(var);
        }
        for (Term c : t.children) {
            if (occurs(var, c, subs))
                return true;
        }
        return false;
    }

    // collect all pattern‐variables
    private static void collectVars(Term t, Set<String> acc) {
        if (t.isVar)
            acc.add(t.name);
        for (Term c : t.children) {
            collectVars(c, acc);
        }
    }

    // --- Parser & Term classes ---
    static class Parser {
        String s;
        int i;

        Parser(String s) {
            this.s = s;
            this.i = 0;
        }

        Term parseTerm() {
            skipSpaces();
            // structure
            if (i < s.length() && s.charAt(i) == '(') {
                i++;
                skipSpaces();
                // empty
                if (i < s.length() && s.charAt(i) == ')') {
                    i++;
                    return new Term(); // empty struct
                }
                // functor
                String name = "";
                while (i < s.length() && s.charAt(i) != ' ' && s.charAt(i) != ')') {
                    name += s.charAt(i++);
                }
                Term node = new Term(name, true);
                skipSpaces();
                // children
                while (i < s.length() && s.charAt(i) != ')') {
                    node.children.add(parseTerm());
                    skipSpaces();
                }
                if (i < s.length() && s.charAt(i) == ')')
                    i++;
                return node;
            }
            // variable or constant
            String name = "";
            while (i < s.length() && s.charAt(i) != ' ' && s.charAt(i) != ')') {
                name += s.charAt(i++);
            }
            boolean isVar = (name.length() == 1 && Character.isUpperCase(name.charAt(0)));
            return new Term(name, false, isVar);
        }

        void skipSpaces() {
            while (i < s.length() && s.charAt(i) == ' ')
                i++;
        }
    }

    static class Term {
        String name;
        boolean isVar;
        boolean isStruct;
        List<Term> children = new ArrayList<>();

        // empty structure ()
        Term() {
            this.name = "()";
            this.isVar = false;
            this.isStruct = true;
        }

        // named struct or leaf
        Term(String name, boolean isStruct) {
            this.name = name;
            this.isStruct = isStruct;
            this.isVar = (!isStruct && name.length() == 1 && Character.isUpperCase(name.charAt(0)));
        }

        // leaf with explicit var flag
        Term(String name, boolean isStruct, boolean isVar) {
            this.name = name;
            this.isStruct = isStruct;
            this.isVar = isVar;
        }

        @Override
        public String toString() {
            if (!isStruct) {
                // constant or variable
                return name;
            }
            // structure
            if (name.equals("()")) {
                return "()";
            }
            StringBuilder sb = new StringBuilder();
            sb.append("(").append(name);
            for (Term c : children) {
                sb.append(" ").append(c.toString());
            }
            sb.append(")");
            return sb.toString();
        }
    }
}
