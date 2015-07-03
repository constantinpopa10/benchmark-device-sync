package org.alfresco.bm.devicesync.dao.mongo;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.alfresco.bm.data.DataCreationState;
import org.alfresco.bm.devicesync.dao.ExtendedSiteDataService;
import org.alfresco.bm.site.SiteData;
import org.alfresco.bm.site.SiteDataServiceImpl;
import org.alfresco.bm.site.SiteMemberData;
import org.alfresco.bm.user.UserDataServiceImpl.Range;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;

/**
 * 
 * @author sglover
 *
 */
public class ExtendedSiteDataServiceImpl extends SiteDataServiceImpl implements ExtendedSiteDataService
{
    @SuppressWarnings("unused")
    private DBCollection sitesCollection;
    private DBCollection siteMembersCollection;

    public ExtendedSiteDataServiceImpl(DB db, String sites, String siteMembers)
    {
    	super(db, sites, siteMembers);
        this.sitesCollection = db.getCollection(sites);
        this.siteMembersCollection = db.getCollection(siteMembers);
    }

    @Override
    public void checkIndexes()
    {
    	super.checkIndexes();

        DBObject idxSiteRole = BasicDBObjectBuilder.start()
                .append(SiteData.FIELD_CREATION_STATE, 1)
                .append(SiteMemberData.FIELD_ROLE, 1)
                .append(SiteMemberData.FIELD_RANDOMIZER, 1)
                .get();
        DBObject optIdxSiteRole = BasicDBObjectBuilder.start()
                .append("name", "idx_SiteRole")
                .append("unique", Boolean.FALSE)
                .get();
        siteMembersCollection.createIndex(idxSiteRole, optIdxSiteRole);
    }

    private Range getRandomizerRange(QueryBuilder queryObjBuilder)
    {
        DBObject queryObj = queryObjBuilder.get();

        DBObject fieldsObj = BasicDBObjectBuilder.start()
                .add("randomizer", Boolean.TRUE)
                .get();
        
        DBObject sortObj = BasicDBObjectBuilder.start()
                .add("randomizer", -1)
                .get();
        
        // Find max
        DBObject resultObj = siteMembersCollection.findOne(queryObj, fieldsObj, sortObj);
        int maxRandomizer = resultObj == null ? 0 : (Integer) resultObj.get("randomizer");
        
        // Find min
        sortObj.put("randomizer", +1);
        resultObj = siteMembersCollection.findOne(queryObj, fieldsObj, sortObj);
        int minRandomizer = resultObj == null ? 0 : (Integer) resultObj.get("randomizer");

        return new Range(minRandomizer, maxRandomizer);
    }

	@Override
	public Stream<SiteMemberData> randomSiteMembers(DataCreationState state, String[] roles,
			int max)
	{
        QueryBuilder rangeBuilder = QueryBuilder
        		.start();
        if (state != null)
        {
        	rangeBuilder.and(SiteMemberData.FIELD_CREATION_STATE).is(state.toString());
        }
        if (roles != null && roles.length > 0)
        {
        	rangeBuilder.and(SiteMemberData.FIELD_ROLE).in(roles);
        }
        Range range = getRandomizerRange(rangeBuilder);
        int upper = range.getMax();
        int lower = range.getMin();
        int random = lower + (int) (Math.random() * (double) (upper - lower));

        QueryBuilder builder = QueryBuilder.start();
        if (state != null)
        {
        	builder.and(SiteMemberData.FIELD_CREATION_STATE).is(state.toString());
        }
        if (roles != null && roles.length > 0)
        {
        	builder.and(SiteMemberData.FIELD_ROLE).in(roles);
        }
        builder
        	.and("randomizer").greaterThanEquals(Integer.valueOf(random));
        DBObject queryObj = builder.get();

        boolean ascending = true;

        DBObject dbObject = siteMembersCollection.findOne(queryObj);
        if(dbObject == null)
        {
        	ascending = false;
            queryObj.put("randomizer", new BasicDBObject("$lt", random));
        }

        DBObject orderBy = BasicDBObjectBuilder
        		.start("randomizer", (ascending ? 1 : -1))
        		.get();

    	DBCursor cur = siteMembersCollection.find(queryObj).sort(orderBy).limit(max);

    	Stream<SiteMemberData> stream = StreamSupport.stream(cur.spliterator(), false)
    		.onClose(() -> cur.close())  // need to close cursor;
    		.map(dbo -> SiteDataServiceImpl.convertSiteMemberDBObject(dbo));

        return stream;
	}
}
