package jacc.grammar;

public abstract class LookaheadMachine extends Machine
{

    public LookaheadMachine(Grammar grammar)
    {
        super(grammar);
    }

    public abstract int[] getLookaheadAt(int i, int j);
}
