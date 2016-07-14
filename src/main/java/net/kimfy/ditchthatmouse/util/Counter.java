package net.kimfy.ditchthatmouse.util;

public class Counter
{
    private int min;
    private int max;
    private int index = 0;

    public Counter(int min, int max)
    {
        this.min = min;
        this.max = max;
    }

    public void setMin(int min)
    {
        this.min = min;
    }

    public void setMax(int max)
    {
        this.max = max;
    }

    public int getIndex()
    {
        return this.index;
    }
    
    public int increment(int n)
    {
        this.index = this.isMax() ? min : this.index + n;
        return this.getIndex();
    }
    
    public int decrement(int n)
    {
        this.index = this.isMin() ? max : this.index - n;
        return this.getIndex();
    }
    
    public boolean isMin()
    {
        return getIndex() == min;
    }
    
    public boolean isMax()
    {
        return getIndex() == max;
    }

    public void reset()
    {
        this.index = 0;
        this.setMin(0);
        this.setMax(0);
    }
}