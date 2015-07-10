package org.alfresco.bm.devicesync.dao;

import java.util.Set;

public interface ExtendedResultsService
{
	Set<String> distinctSitesForEvent(String eventName, String key);
}
