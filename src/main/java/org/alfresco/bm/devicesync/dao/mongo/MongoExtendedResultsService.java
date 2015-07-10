package org.alfresco.bm.devicesync.dao.mongo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.bm.devicesync.dao.ExtendedResultsService;
import org.alfresco.bm.event.mongo.MongoResultService;

import com.mongodb.BasicDBObject;
import com.mongodb.CommandFailureException;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;

public class MongoExtendedResultsService extends MongoResultService implements ExtendedResultsService
{
	private DBCollection collection;

	public MongoExtendedResultsService(DB db, String collection)
    {
	    super(db, collection);
        try
        {
            this.collection = db.createCollection(collection, new BasicDBObject());
        }
        catch (CommandFailureException e)
        {
            if (!db.collectionExists(collection))
            {
                // The collection is really not there
                throw e;
            }
            // Someone else created it
            this.collection = db.getCollection(collection);
        }
    }

	@Override
	@SuppressWarnings({ "rawtypes" })
	public Set<String> distinctSitesForEvent(String eventName, String key)
	{
		Set<String> distinctSites = new HashSet<>();

		QueryBuilder queryBuilder = QueryBuilder
				.start("event.name").is(eventName);
		DBObject query = queryBuilder.get();
		List l = collection.distinct(key, query);
		for(Object o : l)
		{
			distinctSites.add((String)o);
		}

		return distinctSites;
	}
}
