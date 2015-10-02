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
public class TreeWalkBatchData implements Serializable
{
    private static final long serialVersionUID = 946578159221599841L;

    private int count;
    private List<String> sites;

    public TreeWalkBatchData(int count)
    {
        super();
        this.count = count;
    }

    public TreeWalkBatchData(int count, List<String> sites)
    {
        this(count);
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
        BasicDBObjectBuilder builder = BasicDBObjectBuilder
                .start("count", count);
        if (sites != null)
        {
            builder.add("sites", sites);
        }
        DBObject dbObject = builder.get();
        return dbObject;
    }

    @SuppressWarnings("unchecked")
    public static TreeWalkBatchData fromDBObject(DBObject dbObject)
    {
        int count = (Integer)dbObject.get("count");
        List<String> sites = (List<String>)dbObject.get("sites");
        TreeWalkBatchData treeWalkData = new TreeWalkBatchData(count, sites);
        return treeWalkData;
    }
}
