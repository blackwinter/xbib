package compiler;

public class CacheSource extends Source
{

    private Source source;
    private String cache[];
    private int used;

    public CacheSource(Handler handler, Source source1)
    {
        super(handler);
        source = source1;
        cache = null;
        used = 0;
    }

    public String describe()
    {
        return source.describe();
    }

    public String readLine()
    {
        String s = source.readLine();
        if (s != null)
        {
            if (cache == null)
                cache = new String[10];
            else
            if (used >= cache.length)
            {
                String as[] = new String[2 * cache.length];
                for (int i = 0; i < used; i++)
                    as[i] = cache[i];

                cache = as;
            }
            cache[used++] = s;
        }
        return s;
    }

    public int getLineNo()
    {
        return source.getLineNo();
    }

    public String getLine(int i)
    {
        return cache != null && i > 0 && i <= used ? cache[i - 1] : null;
    }

    public void close()
    {
        source.close();
        cache = null;
    }
}
