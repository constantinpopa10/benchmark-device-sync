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
public class SubscriptionBatchData implements Serializable
{
	private static final long serialVersionUID = 946578159221599841L;

	public static final String FIELD_COUNT = "count";
	public static final String FIELD_NEXT_EVENT_NAME = "nextEventName";
	public static final String FIELD_BATCH_SIZE = "batchSize";
	public static final String FIELD_NUM_BATCHES = "numBatches";
	public static final String FIELD_WAIT_TIME_BETWEEN_BATCHES = "waitTimeBetweenBatches";

	private int count;
	private Integer numBatches;
	private Integer waitTimeBetweenBatches; 
	private Integer batchSize;
	private String nextEventName;
	private List<String> sites;

	public SubscriptionBatchData(int count, Integer batchSize, Integer numBatches, Integer waitTimeBetweenBatches,
			String nextEventName, List<String> sites)
	{
		this.count = count;
		this.batchSize = batchSize;
		this.numBatches = numBatches;
		this.waitTimeBetweenBatches = waitTimeBetweenBatches;
		this.nextEventName = nextEventName;
		this.sites = sites;
	}

	public SubscriptionBatchData(int count)
    {
	    super();
	    this.count = count;
    }

    public List<String> getSites()
	{
		return sites;
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
        		.add(FIELD_NEXT_EVENT_NAME, nextEventName)
        		.add("sites", sites);
    	DBObject dbObject = builder.get();
    	return dbObject;
    }

    @SuppressWarnings("unchecked")
    public static SubscriptionBatchData fromDBObject(DBObject dbObject)
    {
    	Integer count = (Integer)dbObject.get(FIELD_COUNT);
    	Integer batchSize = (Integer)dbObject.get(FIELD_BATCH_SIZE);
    	Integer numBatches = (Integer)dbObject.get(FIELD_NUM_BATCHES);
    	Integer waitTimeBetweenBatches = (Integer)dbObject.get(FIELD_WAIT_TIME_BETWEEN_BATCHES);
    	String nextEventName = (String)dbObject.get(FIELD_NEXT_EVENT_NAME);
    	List<String> sites = (List<String>)dbObject.get("sites");
    	SubscriptionBatchData subscriptionBatchData = new SubscriptionBatchData(count, batchSize, numBatches,
    			waitTimeBetweenBatches, nextEventName, sites);
    	return subscriptionBatchData;
    }
}
