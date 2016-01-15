package org.alfresco.bm.devicesync.data;

import com.mongodb.DBObject;

/**
 * 
 * @author sglover
 *
 */
public class CreatePeriodicSubscribersData
{
    public static final String FIELD_TIME_BETWEEN_BATCHES = "time_between_batches";
    public static final String FIELD_NUM_SUBSCRIBERS_PER_BATCH = "num_subscribers_per_batch";
    public static final String FIELD_COUNT = "count";
    public static final String FIELD_MAX = "max";

    private final int timeBetweenBatches;
    private final int numSubscribersPerBatch;
    private final int max;
    private final int count;

    public CreatePeriodicSubscribersData(int numSubscribersPerBatch,
            int timeBetweenBatches, int max, int count)
    {
        super();
        this.numSubscribersPerBatch = numSubscribersPerBatch;
        this.timeBetweenBatches = timeBetweenBatches;
        this.max = max;
        this.count = count;
    }

    public int getTimeBetweenBatches()
    {
        return timeBetweenBatches;
    }

    public int getNumSubscribersPerBatch()
    {
        return numSubscribersPerBatch;
    }

    public int getMax()
    {
        return max;
    }

    public int getCount()
    {
        return count;
    }

    public static CreatePeriodicSubscribersData fromDBObject(DBObject dbObject)
    {
        int timeBetweenBatches = (Integer) dbObject
                .get(FIELD_TIME_BETWEEN_BATCHES);
        int numSubscribersPerBatch = (Integer) dbObject
                .get(FIELD_NUM_SUBSCRIBERS_PER_BATCH);
        int max = (Integer) dbObject.get(FIELD_MAX);
        int count = (Integer) dbObject.get(FIELD_COUNT);
        CreatePeriodicSubscribersData prepareSubscribersData = new CreatePeriodicSubscribersData(
                numSubscribersPerBatch, timeBetweenBatches, max, count);
        return prepareSubscribersData;
    }
}
