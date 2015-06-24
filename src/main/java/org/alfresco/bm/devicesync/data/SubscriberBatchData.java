package org.alfresco.bm.devicesync.data;

import java.io.Serializable;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * 
 * @author sglover
 *
 */
public class SubscriberBatchData implements Serializable
{
	private static final long serialVersionUID = 946578159221599841L;

	public static final String FIELD_COUNT = "count";
	public static final String FIELD_NEXT_EVENT_NAME = "nextEventName";
	public static final String FIELD_BATCH_SIZE = "batchSize";
	public static final String FIELD_NUM_BATCHES = "numBatches";
	public static final String FIELD_WAIT_TIME_BETWEEN_BATCHES = "waitTimeBetweenBatches";

	private Integer batchSize;
	private Integer numBatches;
	private Integer waitTimeBetweenBatches; 
	private int count;
	private String nextEventName;

	public SubscriberBatchData(int count, Integer batchSize, Integer numBatches, Integer waitTimeBetweenBatches,
			String nextEventName)
	{
		this.count = count;
		this.batchSize = batchSize;
		this.numBatches = numBatches;
		this.waitTimeBetweenBatches = waitTimeBetweenBatches;
		this.nextEventName = nextEventName;
	}

	public SubscriberBatchData(int count)
    {
	    super();
	    this.count = count;
    }

    public String getNextEventName()
	{
		return nextEventName;
	}

	public Integer getBatchSize()
	{
		return batchSize;
	}

	public Integer getNumBatches()
	{
		return numBatches;
	}

	public Integer getWaitTimeBetweenBatches()
	{
		return waitTimeBetweenBatches;
	}

	public int getCount()
	{
		return count;
	}

	public DBObject toDBObject()
    {
    	BasicDBObjectBuilder builder = BasicDBObjectBuilder
        		.start(FIELD_COUNT, getCount())
        		.add(FIELD_BATCH_SIZE, batchSize)
        		.add(FIELD_NUM_BATCHES, numBatches)
        		.add(FIELD_WAIT_TIME_BETWEEN_BATCHES, waitTimeBetweenBatches)
        		.add(FIELD_NEXT_EVENT_NAME, nextEventName);
    	DBObject dbObject = builder.get();
    	return dbObject;
    }

    public static SubscriberBatchData fromDBObject(DBObject dbObject)
    {
    	Integer count = (Integer)dbObject.get(FIELD_COUNT);
    	Integer batchSize = (Integer)dbObject.get(FIELD_BATCH_SIZE);
    	Integer numBatches = (Integer)dbObject.get(FIELD_NUM_BATCHES);
    	Integer waitTimeBetweenBatches = (Integer)dbObject.get(FIELD_WAIT_TIME_BETWEEN_BATCHES);
    	String nextEventName = (String)dbObject.get(FIELD_NEXT_EVENT_NAME);
    	SubscriberBatchData syncData = new SubscriberBatchData(count, batchSize, numBatches, waitTimeBetweenBatches,
    			nextEventName);
    	return syncData;
    }
}
