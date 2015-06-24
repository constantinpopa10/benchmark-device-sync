package org.alfresco.bm.devicesync.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.alfresco.bm.data.DataCreationState;
import org.alfresco.bm.devicesync.dao.SubscriptionsService;
import org.alfresco.bm.devicesync.data.SubscriptionData;
import org.alfresco.bm.site.SiteData;
import org.alfresco.bm.site.SiteDataService;
import org.alfresco.repomirror.dao.NodesDataService;
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

    public SubscriptionData getSubscription()
    {
    	SubscriptionData subscriptionData = null;

		if(sitesLimit > -1)
		{
			// TODO streams
			int idx = random.nextInt(sites.size());
			String siteId = sites.get(idx);
			subscriptionData = subscriptionsService.getRandomSubscriptionInSite(siteId);
		}
		else
		{
			Stream<SubscriptionData> subscriptionDataStream = subscriptionsService.getRandomSubscriptions(null, 10);
			List<SubscriptionData> matchingSubscriptions = subscriptionDataStream.filter(sd -> {
				String siteId = sd.getSiteId();
				String path = "/Company Home/Sites/" + siteId + "/documentLibrary/";
				long numNodes = nodesDataService.countNodesUnderFolder(path);
				logger.debug("Num nodes for subscription " + sd + ", site " + siteId + " is " + numNodes);
				return numNodes > 0;
			}).limit(1).collect(Collectors.toList());
			if(matchingSubscriptions.size() < 1)
			{
				logger.debug("Giving up, no matching subscriptions with more than 1 node");
				throw new RuntimeException();
			}
			subscriptionData = matchingSubscriptions.get(0);
		}

		return subscriptionData;
    }
}
