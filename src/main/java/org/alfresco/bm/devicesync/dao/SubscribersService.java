package org.alfresco.bm.devicesync.dao;

import java.util.List;
import java.util.stream.Stream;

import org.alfresco.bm.data.DataCreationState;
import org.alfresco.bm.devicesync.data.SubscriberData;
import org.bson.types.ObjectId;
import org.springframework.social.alfresco.api.entities.Subscriber;

/**
 * 
 * @author sglover
 *
 */
public interface SubscribersService
{
	void addSubscriber(String username, DataCreationState state);
	void addSubscriber(String username, String subscriberId, String syncServiceURI, DataCreationState state);
	void removeSubscriber(String subscriberId);
	void updateSubscriber(ObjectId objectId, String subscriberId, DataCreationState state);
	void updateSubscriber(ObjectId objectId, DataCreationState state);
	String getSubscriber(String username);
	List<SubscriberData> getSubscribers(DataCreationState creationState, int skip, int count);
	long countSubscribers(DataCreationState state);
	SubscriberData getRandomSubscriber(String username);
	Stream<SubscriberData> randomSubscribers(int batchSize);
}
