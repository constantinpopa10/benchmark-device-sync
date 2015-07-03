package org.alfresco.bm.devicesync.dao;

import java.util.stream.Stream;

import org.alfresco.bm.data.DataCreationState;
import org.alfresco.bm.site.SiteDataService;
import org.alfresco.bm.site.SiteMemberData;

/**
 * 
 * @author sglover
 *
 */
public interface ExtendedSiteDataService extends SiteDataService
{
	Stream<SiteMemberData> randomSiteMembers(DataCreationState state, String[] roles,
			int max);
}
