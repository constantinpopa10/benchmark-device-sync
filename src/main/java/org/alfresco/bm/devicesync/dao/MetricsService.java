package org.alfresco.bm.devicesync.dao;

import com.mongodb.DBObject;

/**
 * 
 * @author sglover
 *
 */
public interface MetricsService
{
	void addMetrics(DBObject syncMetrics, DBObject subsMetrics);
}
