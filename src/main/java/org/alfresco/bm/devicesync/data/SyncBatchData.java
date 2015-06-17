package org.alfresco.bm.devicesync.data;

import static org.alfresco.bm.devicesync.dao.mongo.MongoSyncsService.FIELD_COUNT;

import java.io.Serializable;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

public class SyncBatchData implements Serializable
{
	private static final long serialVersionUID = 946578159221599841L;

	private int count;

	public SyncBatchData(int count)
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

    public static SyncBatchData fromDBObject(DBObject dbObject)
    {
    	int count = (Integer)dbObject.get(FIELD_COUNT);
    	SyncBatchData syncData = new SyncBatchData(count);
    	return syncData;
    }
}
