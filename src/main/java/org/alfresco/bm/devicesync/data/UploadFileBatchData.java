package org.alfresco.bm.devicesync.data;

import java.io.Serializable;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * 
 * @author sglover
 *
 */
public class UploadFileBatchData implements Serializable
{
	private static final long serialVersionUID = 946578159221599841L;

	public static final String FIELD_COUNT = "count";

	private int count;

	public UploadFileBatchData(int count)
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

	@SuppressWarnings("unchecked")
    public static UploadFileBatchData fromDBObject(DBObject dbObject)
    {
    	int count = (Integer)dbObject.get(FIELD_COUNT);
    	UploadFileBatchData syncData = new UploadFileBatchData(count);
    	return syncData;
    }
}
