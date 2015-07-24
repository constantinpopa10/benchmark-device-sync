package org.alfresco.bm.devicesync.util;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

import org.alfresco.bm.data.DataCreationState;
import org.alfresco.bm.devicesync.dao.SubscriptionsService;
import org.alfresco.bm.devicesync.data.SubscriptionData;
import org.alfresco.bm.devicesync.data.UploadFileData;
import org.alfresco.bm.site.SiteData;
import org.alfresco.bm.site.SiteDataService;
import org.alfresco.bm.site.SiteMemberData;
import org.alfresco.repomirror.dao.NodesDataService;
import org.alfresco.repomirror.data.PathInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author sglover
 *
 */
public class SiteSampleSelector
{
    private static Log logger = LogFactory.getLog(SiteSampleSelector.class);

	private final SiteDataService siteDataService;
	private final SubscriptionsService subscriptionsService;
	private final NodesDataService nodesDataService;
    private final int sitesLimit;
    private List<String> sites = new LinkedList<>();

    private Random random = new Random(System.currentTimeMillis());

    public SiteSampleSelector(SiteDataService siteDataService, SubscriptionsService subscriptionsService,
    		NodesDataService nodesDataService, int sitesLimit)
    {
    	this.siteDataService = siteDataService;
    	this.subscriptionsService = subscriptionsService;
    	this.nodesDataService = nodesDataService;
        this.sitesLimit = sitesLimit;

    	if(sitesLimit > -1)
    	{
    		for(int i = 0; i < sitesLimit; i++)
    		{
    			SiteData siteData = siteDataService.randomSite("default", DataCreationState.Created);
    			String siteId = siteData.getSiteId();
    			sites.add(siteId);
    		}
    	}
    }

    public String getSite()
    {
    	String siteId = null;

		if(sitesLimit > -1)
		{
			int idx = random.nextInt(sites.size());
			siteId = sites.get(idx);
		}
		else
		{
			SiteData siteData = siteDataService.randomSite("default", DataCreationState.Created);
			siteId = siteData.getSiteId();
		}

		return siteId;
    }

    public Stream<UploadFileData> getSubscriptions(int max)
    {
//		Stream<PathInfo> sites = nodesDataService.randomPaths(max);
//		Stream<UploadFileData> subscriptions = sites.map(pathInfo -> {
//			String siteId = pathInfo.getSiteId();
//			String path = pathInfo.getPath();
//			Integer numChildren = pathInfo.getNumChildren();
//			Integer numChildFolders = pathInfo.getNumChildFolders();

    	Stream<SubscriptionData> subscriptions = subscriptionsService.getRandomSubscriptions(null, max);
    	Stream<UploadFileData> ret = subscriptions.map(subscriptionData ->
    	{
    		String siteId = subscriptionData.getSiteId();
    		String username = subscriptionData.getUsername();
    		String subscriberId = subscriptionData.getSubscriberId();
    		String subscriptionId = subscriptionData.getSubscriptionId();

    		logger.debug("Random site " + siteId + ", subscription " + subscriptionData);

    		SiteMemberData siteMemberData = siteDataService.getSiteMember(siteId, username);
    		String siteRole = siteMemberData.getRole();

    		PathInfo pathInfo = nodesDataService.randomFolderInSite(siteId);
    		UploadFileData uploadFileData = null;
    		if(pathInfo != null)
    		{
	    		String path = pathInfo.getPath();
	    		Integer numChildren = pathInfo.getNumChildren();
	    		Integer numChildFolders = pathInfo.getNumChildFolders();
	
	    		uploadFileData = new UploadFileData(username, subscriberId, subscriptionId,
	    				siteId, siteRole, path, numChildren, numChildFolders);
    		}

    		return uploadFileData;
    	})
    	.filter(ufd -> ufd != null);

    	return ret;
//
//			SubscriptionData subscriptionData = subscriptionsService.getRandomSubscriptionInSite(siteId);
//			UploadFileData uploadFileData = null;
//			if(subscriptionData != null)
//			{
//				String subscriberId = subscriptionData.getSubscriberId();
//				String subscriptionId = subscriptionData.getSubscriptionId();
//				String username = subscriptionData.getUsername();
//
//				logger.debug("Random site " + siteId + ", subscription " + subscriptionData);
//	
//	        	Set<String> roles = new HashSet<>();
//	        	roles.add("SiteManager");
//	        	roles.add("SiteCollaborator");
//	        	SiteMemberData siteMemberData = siteDataService.getSiteMember(siteId, username);
//	        	String siteRole = siteMemberData.getRole();
//	
//				uploadFileData = new UploadFileData(username, subscriberId, subscriptionId,
//						siteId, siteRole, path, numChildren, numChildFolders);
//			}
//
//			return uploadFileData;
//		})
//		.filter(ufd -> ufd != null);

//		return subscriptions;
    }
}
