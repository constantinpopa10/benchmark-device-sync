package org.alfresco.bm.devicesync.dao.mongo;

import org.alfresco.bm.devicesync.dao.MetricsService;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.InitializingBean;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;

/**
 * 
 * @author sglover
 *
 */
public class MongoMetricsService implements MetricsService, InitializingBean
{
    /** The collection of users, which can be reused by derived extensions. */
    protected final DBCollection collection;

    public MongoMetricsService(DB db, String collection)
    {
        this.collection = db.getCollection(collection);
    }
    
    @Override
    public void afterPropertiesSet() throws Exception
    {
        checkIndexes();
    }

    /**
     * Ensure that the MongoDB collection has the required indexes associated with
     * this user bean.
     */
    private void checkIndexes()
    {
        collection.setWriteConcern(WriteConcern.SAFE);
    }

    @Override
    public void addMetrics(DBObject syncMetrics, DBObject subsMetrics)
    {
    	long timestamp = System.currentTimeMillis();
    	LocalDateTime time = new LocalDateTime(timestamp, DateTimeZone.UTC);
    	String formattedTime = time.toString();

    	DBObject insert = BasicDBObjectBuilder
    			.start("timestamp", timestamp)
    			.add("time", formattedTime)
    			.add("sync", syncMetrics)
    			.add("subs", subsMetrics)
    			.get();
    	collection.insert(insert);
    }
}
