package jacc.util;

public class BitSet
{
    private static class BitSetInterator extends Interator
    {

        int set[];
        int pos;
        int mask;
        int num;
        int bitCount;

        private void advance()
        {
            num++;
            if (++bitCount == 32)
            {
                pos++;
                bitCount = 0;
                mask = 1;
            } else
            {
                mask <<= 1;
            }
        }

        public int next()
        {
            int i = num;
            advance();
            return i;
        }

        public boolean hasNext()
        {
            while (pos < set.length && (set[pos] & mask) == 0) 
                advance();
            return pos < set.length;
        }

        BitSetInterator(int ai[], int i)
        {
            set = ai;
            num = i;
            pos = 0;
            mask = 1;
            bitCount = 0;
        }
    }


    private static final int LOG_BITS_PER_WORD = 5;
    private static final int BITS_PER_WORD = 32;
    private static final int BIT_MASK = 31;

    private BitSet()
    {
    }

    public static int[] make(int i)
    {
        return new int[(i + 32) - 1 >> 5];
    }

    public static int[] copy(int ai[])
    {
        int ai1[] = new int[ai.length];
        for (int i = 0; i < ai.length; i++)
            ai1[i] = ai[i];

        return ai1;
    }

    public static void clear(int ai[])
    {
        for (int i = 0; i < ai.length; i++)
            ai[i] = 0;

    }

    public static boolean isEmpty(int ai[])
    {
        for (int i = 0; i < ai.length; i++)
            if (ai[i] != 0)
                return false;

        return true;
    }

    public static boolean equal(int ai[], int ai1[])
    {
        int i;
        for (i = 0; i < ai.length && i < ai1.length; i++)
            if (ai[i] != ai1[i])
                return false;

        return i >= ai.length && i >= ai1.length;
    }

    public static boolean disjoint(int ai[], int ai1[])
    {
        int i;
        for (i = 0; i < ai.length && i < ai1.length; i++)
            if ((ai[i] & ai1[i]) != 0)
                return false;

        return i >= ai.length && i >= ai1.length;
    }

    public static void union(int ai[], int ai1[])
    {
        for (int i = 0; i < ai.length; i++)
            ai[i] |= ai1[i];

    }

    public static void intersect(int ai[], int ai1[])
    {
        for (int i = 0; i < ai.length; i++)
            ai[i] &= ai1[i];

    }

    public static boolean addTo(int ai[], int ai1[])
    {
        if (ai.length < ai1.length)
            throw new Error("bitset arguments do not match");
        int i = 0;
        boolean flag = false;
        for (; i < ai1.length; i++)
        {
            if (ai1[i] == 0)
                continue;
            int j = ai[i] | ai1[i];
            if (j != ai[i])
                flag = true;
            ai[i] = j;
        }

        return flag;
    }

    public static boolean addTo(int ai[], int i)
    {
        int j = 1 << (i & 0x1f);
        int k = i >> 5;
        int l = ai[k] | j;
        if (l != ai[k])
        {
            ai[k] = l;
            return true;
        } else
        {
            return false;
        }
    }

    public static void set(int ai[], int i)
    {
        int j = 1 << (i & 0x1f);
        int k = i >> 5;
        ai[k] |= j;
    }

    public static boolean get(int ai[], int i)
    {
        int j = 1 << (i & 0x1f);
        int k = i >> 5;
        return (ai[k] & j) != 0;
    }

    public static int[] members(int ai[])
    {
        int i = 0;
label0:
        for (int j = 0; j < ai.length; j++)
        {
            if (ai[j] == 0)
                continue;
            int k = ai[j];
            int i1 = 0;
            do
            {
                if (i1 >= 32 || k == 0)
                    continue label0;
                if ((k & 1) != 0)
                    i++;
                k >>= 1;
                i1++;
            } while (true);
        }

        int ai1[] = new int[i];
        int l = 0;
label1:
        for (int j1 = 0; j1 < ai.length && l < i; j1++)
        {
            if (ai[j1] == 0)
                continue;
            int k1 = j1 << 5;
            int l1 = ai[j1];
            int i2 = 0;
            do
            {
                if (i2 >= 32 || l1 == 0)
                    continue label1;
                if ((l1 & 1) != 0)
                    ai1[l++] = k1 + i2;
                l1 >>= 1;
                i2++;
            } while (true);
        }

        return ai1;
    }

    public static Interator interator(int ai[], int i)
    {
        return new BitSetInterator(ai, i);
    }
}
