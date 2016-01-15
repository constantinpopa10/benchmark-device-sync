package org.alfresco.bm.devicesync.data;

import java.io.Serializable;
import java.util.List;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * 
 * @author sglover
 *
 */
public class SyncBatchData implements Serializable
{
    private static final long serialVersionUID = 946578159221599841L;

    public static final String FIELD_COUNT = "count";

    private int count;
    private List<String> sites;

    public SyncBatchData(int count)
    {
        super();
        this.count = count;
    }

    public SyncBatchData(int count, List<String> sites)
    {
        super();
        this.count = count;
        this.sites = sites;
    }

    public int getCount()
    {
        return count;
    }

    public List<String> getSites()
    {
        return sites;
    }

    public DBObject toDBObject()
    {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start(FIELD_COUNT,
                getCount());
        if (sites != null)
        {
            builder.add("sites", sites);
        }
        DBObject dbObject = builder.get();
        return dbObject;
    }

    @SuppressWarnings("unchecked")
    public static SyncBatchData fromDBObject(DBObject dbObject)
    {
        int count = (Integer) dbObject.get(FIELD_COUNT);
        List<String> sites = (List<String>) dbObject.get("sites");
        SyncBatchData syncData = new SyncBatchData(count, sites);
        return syncData;
    }
}
