package org.alfresco.bm.devicesync.data;

/**
 * 
 * @author sglover
 *
 */
public class SyncResults
{
    private int count = 0;

    public SyncResults(int count)
    {
        super();
        this.count = count;
    }

    public SyncResults combine(SyncResults a)
    {
        return new SyncResults(count + a.count);
    }

    public void apply(SyncStateData u)
    {
        SyncState state = u.getSyncState();
        switch (state)
        {
        case Ready:
            count++;
            break;
        default:
        }
    }

    public int getCount()
    {
        return count;
    }
}
