package org.alfresco.bm.devicesync.data;

import com.mongodb.DBObject;

/**
 * 
 * @author sglover
 *
 */
public class PrepareSubscriptionsData
{
    public static final String FIELD_MAX_SUBSCRIPTIONS = "max_subscriptions";
    public static final String FIELD_NUM_NEW_SUBSCRIPTIONS = "num_new_subscriptions";

    private Integer maxSubscriptions;
    private Integer numNewSubscriptions;

    public PrepareSubscriptionsData(Integer maxSubscriptions,
            Integer numNewSubscriptions)
    {
        super();
        this.maxSubscriptions = maxSubscriptions;
        this.numNewSubscriptions = numNewSubscriptions;
    }

    public Integer getMaxSubscriptions()
    {
        return maxSubscriptions;
    }

    public Integer getNumNewSubscriptions()
    {
        return numNewSubscriptions;
    }

    public static PrepareSubscriptionsData fromDBObject(DBObject dbObject)
    {
        Integer maxSubscriptions = (Integer) dbObject
                .get(FIELD_MAX_SUBSCRIPTIONS);
        Integer numNewSubscriptions = (Integer) dbObject
                .get(FIELD_NUM_NEW_SUBSCRIPTIONS);
        PrepareSubscriptionsData prepareSubscriptionsData = new PrepareSubscriptionsData(
                maxSubscriptions, numNewSubscriptions);
        return prepareSubscriptionsData;
    }
}
