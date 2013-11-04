
package org.xbib.fst.operations;

import org.xbib.fst.Arc;
import org.xbib.fst.Fst;
import org.xbib.fst.ImmutableFst;
import org.xbib.fst.Pair;
import org.xbib.fst.State;
import org.xbib.fst.semiring.Semiring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Compose operation
 * <p/>
 * See: M. Mohri, "Weighted automata algorithms", Handbook of Weighted Automata.
 * Springer, pp. 213-250, 2009.
 */
public class Compose {

    private Compose() {
    }

    /**
     * Computes the composition of two Fsts. Assuming no epsilon transitions.
     * <p/>
     * Input Fsts are not modified.
     *
     * @param fst1     the first Fst
     * @param fst2     the second Fst
     * @param semiring the semiring to use in the operation
     * @param sorted
     * @return the composed Fst
     */
    public static Fst compose(Fst fst1, Fst fst2, Semiring semiring,
                              boolean sorted) {
        if (!Arrays.equals(fst1.getOutputSymbols(), fst2.getInputSymbols())) {
            // symboltables do not match
            return null;
        }

        Fst res = new Fst(semiring);

        HashMap<Pair<State, State>, State> stateMap = new HashMap<Pair<State, State>, State>();
        ArrayList<Pair<State, State>> queue = new ArrayList<Pair<State, State>>();

        State s1 = fst1.getStart();
        State s2 = fst2.getStart();

        if ((s1 == null) || (s2 == null)) {
            return null;
        }

        Pair<State, State> p = new Pair<State, State>(s1, s2);
        State s = new State(semiring.times(s1.getFinalWeight(),
                s2.getFinalWeight()));

        res.addState(s);
        res.setStart(s);
        stateMap.put(p, s);
        queue.add(p);

        while (queue.size() > 0) {
            p = queue.remove(0);
            s1 = p.getLeft();
            s2 = p.getRight();
            s = stateMap.get(p);
            int numArcs1 = s1.getNumArcs();
            int numArcs2 = s2.getNumArcs();
            for (int i = 0; i < numArcs1; i++) {
                Arc a1 = s1.getArc(i);
                for (int j = 0; j < numArcs2; j++) {
                    Arc a2 = s2.getArc(j);
                    if (sorted && a1.getOutputLabel() < a2.getInputLabel()) {
                        break;
                    }
                    if (a1.getOutputLabel() == a2.getInputLabel()) {
                        State nextState1 = a1.getNextState();
                        State nextState2 = a2.getNextState();
                        Pair<State, State> nextPair = new Pair<State, State>(
                                nextState1, nextState2);
                        State nextState = stateMap.get(nextPair);
                        if (nextState == null) {
                            nextState = new State(semiring.times(
                                    nextState1.getFinalWeight(),
                                    nextState2.getFinalWeight()));
                            res.addState(nextState);
                            stateMap.put(nextPair, nextState);
                            queue.add(nextPair);
                        }
                        Arc a = new Arc(a1.getInputLabel(), a2.getOutputLabel(),
                                semiring.times(a1.getWeight(), a2.getWeight()),
                                nextState);
                        s.addArc(a);
                    }
                }
            }
        }

        res.setInputSymbols(fst1.getInputSymbols());
        res.setOutputSymbols(fst2.getOutputSymbols());

        return res;
    }

    /**
     * Computes the composition of two Fsts. The two Fsts are augmented in order
     * to avoid multiple epsilon paths in the resulting Fst
     *
     * @param fst1     the first Fst
     * @param fst2     the second Fst
     * @param semiring the semiring to use in the operation
     * @return the composed Fst
     */
    public static Fst get(Fst fst1, Fst fst2, Semiring semiring) {
        if ((fst1 == null) || (fst2 == null)) {
            return null;
        }

        if (!Arrays.equals(fst1.getOutputSymbols(), fst2.getInputSymbols())) {
            // symboltables do not match
            return null;
        }

        Fst filter = getFilter(fst1.getOutputSymbols(), semiring);
        augment(1, fst1, semiring);
        augment(0, fst2, semiring);

        Fst tmp = Compose.compose(fst1, filter, semiring, false);

        Fst res = Compose.compose(tmp, fst2, semiring, false);

        // Connect.apply(res);

        return res;
    }

    /**
     * Get a filter to use for avoiding multiple epsilon paths in the resulting
     * Fst
     * <p/>
     * See: M. Mohri, "Weighted automata algorithms", Handbook of Weighted
     * Automata. Springer, pp. 213-250, 2009.
     *
     * @param syms     the gilter's input/output symbols
     * @param semiring the semiring to use in the operation
     * @return the filter
     */
    public static Fst getFilter(String[] syms, Semiring semiring) {
        Fst filter = new Fst(semiring);

        int e1index = syms.length;
        int e2index = syms.length + 1;

        filter.setInputSymbols(syms);
        filter.setOutputSymbols(syms);

        // State 0
        State s0 = new State(syms.length + 3);
        s0.setFinalWeight(semiring.one());
        State s1 = new State(syms.length);
        s1.setFinalWeight(semiring.one());
        State s2 = new State(syms.length);
        s2.setFinalWeight(semiring.one());
        filter.addState(s0);
        s0.addArc(new Arc(e2index, e1index, semiring.one(), s0));
        s0.addArc(new Arc(e1index, e1index, semiring.one(), s1));
        s0.addArc(new Arc(e2index, e2index, semiring.one(), s2));
        for (int i = 1; i < syms.length; i++) {
            s0.addArc(new Arc(i, i, semiring.one(), s0));
        }
        filter.setStart(s0);

        // State 1
        filter.addState(s1);
        s1.addArc(new Arc(e1index, e1index, semiring.one(), s1));
        for (int i = 1; i < syms.length; i++) {
            s1.addArc(new Arc(i, i, semiring.one(), s0));
        }

        // State 2
        filter.addState(s2);
        s2.addArc(new Arc(e2index, e2index, semiring.one(), s2));
        for (int i = 1; i < syms.length; i++) {
            s2.addArc(new Arc(i, i, semiring.one(), s0));
        }

        return filter;
    }

    /**
     * Augments the labels of an Fst in order to use it for composition avoiding
     * multiple epsilon paths in the resulting Fst
     *
     * @param label    constant denoting if the augment should take place on input
     *                 or output labels For value equal to 0 augment will take place
     *                 for input labels For value equal to 1 augment will take place
     *                 for output labels
     * @param fst      the fst to augment
     * @param semiring the semiring to use in the operation
     */
    public static void augment(int label, Fst fst, Semiring semiring) {
        // label: 0->augment on ilabel
        // 1->augment on olabel

        String[] isyms = fst.getInputSymbols();
        String[] osyms = fst.getOutputSymbols();

        int e1inputIndex = isyms.length;
        int e2inputIndex = isyms.length + 1;

        int e1outputIndex = osyms.length;
        int e2outputIndex = osyms.length + 1;

        int numStates = fst.getNumStates();
        for (int i = 0; i < numStates; i++) {
            State s = fst.getState(i);
            // Immutable fsts hold an additional (null) arc for augmention
            int numArcs = (fst instanceof ImmutableFst) ? s.getNumArcs() - 1
                    : s.getNumArcs();
            for (int j = 0; j < numArcs; j++) {
                Arc a = s.getArc(j);
                if ((label == 1) && (a.getOutputLabel() == 0)) {
                    a.setOutputLabel(e2outputIndex);
                } else if ((label == 0) && (a.getInputLabel() == 0)) {
                    a.setInputLabel(e1inputIndex);
                }
            }
            if (label == 0) {
                if (fst instanceof ImmutableFst) {
                    s.setArc(numArcs, new Arc(e2inputIndex, 0, semiring.one(),
                            s));
                } else {
                    s.addArc(new Arc(e2inputIndex, 0, semiring.one(), s));
                }
            } else if (label == 1) {
                if (fst instanceof ImmutableFst) {
                    s.setArc(numArcs, new Arc(0, e1outputIndex, semiring.one(),
                            s));
                } else {
                    s.addArc(new Arc(0, e1outputIndex, semiring.one(), s));
                }
            }
        }
    }
}
