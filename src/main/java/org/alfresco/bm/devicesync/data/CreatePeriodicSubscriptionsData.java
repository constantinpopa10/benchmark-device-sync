package org.alfresco.bm.devicesync.data;

import com.mongodb.DBObject;

/**
 * 
 * @author sglover
 *
 */
public class CreatePeriodicSubscriptionsData
{
	public static final String FIELD_TIME_BETWEEN_BATCHES  = "time_between_batches";
	public static final String FIELD_NUM_SUBSCRIPTIONS_PER_BATCH  = "num_subscriptions_per_batch";
	public static final String FIELD_COUNT = "count";
	public static final String FIELD_MAX = "max";

	private final int timeBetweenBatches;
	private final int numSubscriptionsPerBatch;
	private final int max;
	private final int count;

	public CreatePeriodicSubscriptionsData(int numSubscriptionsPerBatch, int timeBetweenBatches, int max,
			int count)
    {
	    super();
	    this.numSubscriptionsPerBatch = numSubscriptionsPerBatch;
	    this.timeBetweenBatches = timeBetweenBatches;
	    this.max = max;
	    this.count = count;
    }

    public int getTimeBetweenBatches()
	{
		return timeBetweenBatches;
	}

	public int getNumSubscriptionsPerBatch()
	{
		return numSubscriptionsPerBatch;
	}

	public int getMax()
	{
		return max;
	}

	public int getCount()
	{
		return count;
	}

	public static CreatePeriodicSubscriptionsData fromDBObject(DBObject dbObject)
    {
    	int timeBetweenBatches = (Integer)dbObject.get(FIELD_TIME_BETWEEN_BATCHES);
    	int numSubscriptionsPerBatch = (Integer)dbObject.get(FIELD_NUM_SUBSCRIPTIONS_PER_BATCH);
    	int max = (Integer)dbObject.get(FIELD_MAX);
    	int count = (Integer)dbObject.get(FIELD_COUNT);
    	CreatePeriodicSubscriptionsData prepareSubscriptionsData = new CreatePeriodicSubscriptionsData(numSubscriptionsPerBatch,
    			timeBetweenBatches, max, count);
    	return prepareSubscriptionsData;
    }
}
