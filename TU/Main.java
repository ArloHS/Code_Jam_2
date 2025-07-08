import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;


public class Main {
    private static final boolean DEBUG = false;

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
        String inpCase = in.nextLine().trim();

        int posToSplit = -1;
        int depth = 0;

        // Split input into two terms s1 and s2
        for (int i = 0; i < inpCase.length(); i++) {
            char c = inpCase.charAt(i);
            if (c == '(') {
                depth++;

            } else if (c == ')') {
                depth--;

            } else if (c == ' ' && depth == 0) {
                posToSplit = i;
                break;
            }
        }

        if (posToSplit < 0) {
            posToSplit = inpCase.indexOf(' ');
        }

        String s1 = inpCase.substring(0, posToSplit).trim();
        String s2 = inpCase.substring(posToSplit + 1).trim();
        debugln("Term1: " + s1 + ", Term2: " + s2);

        // Parse terms next
        Parser p1 = new Parser(s1);
        Parser p2 = new Parser(s2);
        Term t1 = p1.parseTerm();
        Term t2 = p2.parseTerm();
        debugln("Parsed t1: " + t1);
        debugln("Parsed t2: " + t2);

        // Unify substitutions mapping
        Map<String, Term> substitute = new HashMap<>();
        boolean isUnify = unification(apply(t1, substitute), apply(t2, substitute), substitute);
    
        if (!isUnify) {
            debugln("Unification failed");
            out.println("none");
            return;
        }

        debugln("Unification succeeded: subs=" + substitute);

        // get all vars
        Set<String> setOfVars = new HashSet<>();
        collectVars(t1, setOfVars);
        collectVars(t2, setOfVars);
        List<String> vars = new ArrayList<>(setOfVars);
        Collections.sort(vars);

        // make sure all vars hae a binding
        for (String v : vars) {
            if (!substitute.containsKey(v)) {
                substitute.put(v, new Term());
                debugln("Default bind " + v + "=()");
            }
        }
        // Prit assignments
        for (String v : vars) {
            Term rhs = apply(substitute.get(v), substitute);
            out.println(v + "=" + rhs);
        }

        out.println(".");
    }

    public static void process(Scanner in, PrintStream out) {
        int N = Integer.parseInt(in.nextLine().trim());
        debugln("Number of cases ===> " + N);

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

    private static boolean unification(Term a, Term b, Map<String, Term> subs) {
        a = apply(a, subs);
        b = apply(b, subs);
        debugln("Unifying " + a + " and " + b);

        // check same var
        if (a.isVar && b.isVar && a.name.equals(b.name)) {
            return true;
        }

        // check var on th left
        if (a.isVar) {
            if (occurs(a.name, b, subs)) {
                return false;
            }

            // Else
            subs.put(a.name, b);
            debugln("Binding " + a.name + "= " + b);
            return true;
        }

        // Check var on the right
        if (b.isVar) {
            if (occurs(b.name, a, subs)) {
                return false;
            }

            // Else
            subs.put(b.name, a);
            debugln("Binding " + b.name + "= " + a);
            return true;
        }

        // check both structs
        if (a.isStruct && b.isStruct && a.name.equals(b.name)
                && a.children.size() == b.children.size()) {
            for (int i = 0; i < a.children.size(); i++) {
                if (!unification(a.children.get(i), b.children.get(i), subs)) {
                    return false;
                }
            }

            return true;
        }

        // Check if both constants
        if (!a.isStruct && !b.isStruct && a.name.equals(b.name)) {
            return true;
        }

        // Default case: unification fails
        return false;
    }

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

    private static boolean occurs(String var, Term t, Map<String, Term> subs) {
        t = apply(t, subs);
        if (t.isVar) {
            return t.name.equals(var);
        }

        for (Term c : t.children) {
            if (occurs(var, c, subs)) {
                return true;
            }
        }
        return false;
    }

    private static void collectVars(Term t, Set<String> acc) {
        if (t.isVar) {
            acc.add(t.name);
        }
        for (Term c : t.children) {
            collectVars(c, acc);
        }
    }

    // Parser class
    static class Parser {
        // term to parse
        String s;
        // index
        int i;

        Parser(String s) {
            this.s = s;
            this.i = 0;
        }

        // Parses a term and return its input
        Term parseTerm() {
            skipSpaces();

            if (i < s.length() && s.charAt(i) == '(') {
                i++;
                skipSpaces();
                if (i < s.length() && s.charAt(i) == ')') {
                    i++;
                    return new Term();
                }
                String name = "";
                while (i < s.length() && s.charAt(i) != ' ' && s.charAt(i) != ')') {
                    name += s.charAt(i++);
                }
                Term node = new Term(name, true);
                skipSpaces();
                while (i < s.length() && s.charAt(i) != ')') {
                    node.children.add(parseTerm());
                    skipSpaces();
                }
                if (i < s.length() && s.charAt(i) == ')') {
                    i++;
                }
                return node;
            }

            String name = "";
            while (i < s.length() && s.charAt(i) != ' ' && s.charAt(i) != ')') {
                name += s.charAt(i++);
            }
            boolean isVar = name.length() == 1 && Character.isUpperCase(name.charAt(0));
            return new Term(name, false, isVar);
        }

        // skips whitespace
        void skipSpaces() {
            while (i < s.length() && s.charAt(i) == ' ') {
                i++;
            }
        }
    }

    // Term class
    static class Term {
        // Name of term
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
            this.isVar = (!isStruct && name.length() == 1 && Character.isUpperCase(name.charAt(0)));
        }

        Term(String name, boolean isStruct, boolean isVar) {
            this.name = name;
            this.isStruct = isStruct;
            this.isVar = isVar;
        }

        @Override
        public String toString() {
            if (!isStruct) {
                return name;
            }
            if (name.equals("()")) {
                return "()";
            }
            StringBuilder sb = new StringBuilder();
            sb.append("(").append(name);
            for (Term c : children) {
                sb.append(" ").append(c);
            }
            sb.append(")");
            return sb.toString();
        }
    }
}
