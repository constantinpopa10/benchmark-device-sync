package org.alfresco.bm.devicesync.dao;

import java.util.List;
import java.util.stream.Stream;

import org.alfresco.bm.data.DataCreationState;
import org.alfresco.bm.devicesync.data.SubscriptionData;
import org.bson.types.ObjectId;

/**
 * 
 * @author sglover
 *
 */
public interface SubscriptionsService
{
    void addSubscription(String siteId, String username, String subscriberId, String subscriptionType, String path, DataCreationState state);
    void addSubscription(String siteId, String username, String subscriberId, String subscriptionId, String subscriptionType,
    		String path, DataCreationState state);
    void removeSubscription(String subscriptionId);
    SubscriptionData getSubscription(String subscriptionId);
    void updateSubscription(ObjectId objecId, String subscriptionId, DataCreationState state);
    void updateSubscription(ObjectId objecId, DataCreationState state);
    long countSubscriptions(DataCreationState state);
    List<SubscriptionData> getSubscriptions(DataCreationState state, int skip, int count);
    SubscriptionData getRandomSubscription(String username);
    SubscriptionData getRandomSubscriptionInSite(String siteId);
    Stream<SubscriptionData> getRandomSubscriptions(String username, int limit);
}
