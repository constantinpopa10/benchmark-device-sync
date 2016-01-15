package org.alfresco.bm.devicesync.dao;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.mongodb.DBObject;

/**
 * 
 * @author sglover
 *
 */
public interface ExtendedResultsService
{
    Set<String> distinctSitesForEvent(String eventName, String key);

    Stream<List<DBObject>> syncs(int skip, int limit);

    double avgSyncTime(int skip, int limit);
}
