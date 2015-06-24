package org.alfresco.bm.devicesync.data;

import static org.alfresco.bm.devicesync.data.SyncData.FIELD_COUNT;

import java.io.Serializable;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

public class CollectStatsBatchData implements Serializable
{
	private static final long serialVersionUID = 946578159221599841L;

	private int count;

	public CollectStatsBatchData(int count)
    {
	    super();
	    this.count = count;
    }

    public int getCount()
	{
		return count;
	}

	public DBObject toDBObject()
    {
    	BasicDBObjectBuilder builder = BasicDBObjectBuilder
        		.start(FIELD_COUNT, getCount());
    	DBObject dbObject = builder.get();
    	return dbObject;
    }

    public static CollectStatsBatchData fromDBObject(DBObject dbObject)
    {
    	int count = (Integer)dbObject.get(FIELD_COUNT);
    	CollectStatsBatchData syncData = new CollectStatsBatchData(count);
    	return syncData;
    }
}
