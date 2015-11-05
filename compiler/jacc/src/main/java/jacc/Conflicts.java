package jacc;

import jacc.grammar.Machine;

public class Conflicts {

    private static final int SR = 0;
    private static final int RR = 1;
    private int type;
    private int arg1;
    private int arg2;
    private jacc.grammar.Grammar.Symbol sym;
    private Conflicts next;

    private Conflicts(int i, int j, int k, jacc.grammar.Grammar.Symbol symbol, Conflicts conflicts) {
        type = i;
        arg1 = j;
        arg2 = k;
        sym = symbol;
        next = conflicts;
    }

    public static Conflicts sr(int i, int j, jacc.grammar.Grammar.Symbol symbol, Conflicts conflicts) {
        return append(conflicts, new Conflicts(0, i, j, symbol, null));
    }

    public static Conflicts rr(int i, int j, jacc.grammar.Grammar.Symbol symbol, Conflicts conflicts) {
        return append(conflicts, new Conflicts(1, i, j, symbol, null));
    }

    private static Conflicts append(Conflicts conflicts, Conflicts conflicts1) {
        if (conflicts == null) {
            return conflicts1;
        }
        Conflicts conflicts2;
        for (conflicts2 = conflicts; conflicts2.next != null; conflicts2 = conflicts2.next) {
            ;
        }
        conflicts2.next = conflicts1;
        return conflicts;
    }

    public static String describe(Machine machine, int i, Conflicts conflicts) {
        if (conflicts == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        String s = System.getProperty("line.separator", "\n");
        for (; conflicts != null; conflicts = conflicts.next) {
            sb.append(i);
            sb.append(": ");
            if (conflicts.type == 0) {
                sb.append("shift/reduce conflict (");
                if (conflicts.arg1 < 0) {
                    sb.append("$end");
                } else {
                    sb.append("shift ");
                    sb.append(conflicts.arg1);
                }
                sb.append(" and red'n ");
                sb.append(machine.reduceItem(i, conflicts.arg2).getSeqNo());
            } else {
                sb.append("reduce/reduce conflict (red'ns ");
                sb.append(machine.reduceItem(i, conflicts.arg1).getSeqNo());
                sb.append(" and ");
                sb.append(machine.reduceItem(i, conflicts.arg2).getSeqNo());
            }
            sb.append(") on ");
            sb.append(conflicts.sym.getName());
            sb.append(s);
        }

        return sb.toString();
    }
}
