/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * https://github.com/CILEA/dspace-cris/wiki/License
 */
package org.dspace.app.cris.statistics.plugin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.dspace.app.cris.discovery.CrisSearchService;
import org.dspace.app.cris.metrics.common.model.ConstantMetrics;
import org.dspace.app.cris.metrics.common.model.CrisMetrics;
import org.dspace.app.cris.metrics.common.services.MetricsPersistenceService;
import org.dspace.app.cris.model.ACrisObject;
import org.dspace.app.cris.model.CrisConstants;
import org.dspace.app.cris.model.ResearchObject;
import org.dspace.app.cris.service.ApplicationService;
import org.dspace.app.cris.statistics.CrisSolrLogger;
import org.dspace.core.Context;
import org.dspace.discovery.SearchServiceException;
import org.dspace.kernel.ServiceManager;
import org.dspace.utils.DSpace;

public class StatsAggregateIndicatorsPlugin<ACO extends ACrisObject>
        extends AStatsIndicatorsPlugin
{

    private static Logger log = Logger
            .getLogger(StatsAggregateIndicatorsPlugin.class);

    private String field = "author_authority";

    private String type;

    private String queryDefault = "*:*";

    private Class<ACO> crisEntityClazz;

    private Integer crisEntityTypeId;

    @Override
    public void buildIndicator(Context context,
            ApplicationService applicationService, CrisSolrLogger statsService,
            CrisSearchService searchService, String level)
                    throws SearchServiceException
    {
        ServiceManager serviceManager = new DSpace().getServiceManager();
        MetricsPersistenceService pService = serviceManager.getServiceByName(
                MetricsPersistenceService.class.getName(),
                MetricsPersistenceService.class);

        List<ACO> rs = new ArrayList<ACO>();

        if (crisEntityTypeId > 1000)
        {
            rs = (List<ACO>)applicationService.getResearchObjectByShortNameType(CrisConstants.getEntityTypeText(crisEntityTypeId));
        }
        else
        {
            rs = applicationService.getList(crisEntityClazz);
        }

        for (ACO rp : rs)
        {
            int itemsCited = 0;
            int citations = 0;
            SolrQuery query = new SolrQuery();
            query.setQuery(queryDefault);
            query.addFilterQuery("{!field f=" + field + "}" + rp.getCrisID(),
                    "NOT(withdrawn:true)");
            query.setFields("search.resourceid", "search.resourcetype");

            query.setRows(Integer.MAX_VALUE);

            QueryResponse response = searchService.search(query);
            SolrDocumentList results = response.getResults();
            for (SolrDocument doc : results)
            {
                Integer resourceType = (Integer) doc
                        .getFirstValue("search.resourcetype");
                Integer resourceId = (Integer) doc
                        .getFirstValue("search.resourceid");

                if (resourceId != null)
                {
                    CrisMetrics citation = pService
                            .getLastMetricByResourceIDAndResourceTypeAndMetricsType(
                                    resourceId, resourceType, type);
                    if (citation != null && citation.getMetricCount() > 0)
                    {
                        itemsCited++;
                        citations += citation.getMetricCount();
                    }
                }
            }

            Date timestamp = new Date();
            buildIndicator(pService, applicationService, rp.getUuid(), rp.getType(),
                    rp.getId(), citations,
                    type + ConstantMetrics.SUFFIX_STATS_INDICATOR_TYPE_AGGREGATE,
                    null, timestamp, null);
            buildIndicator(pService, applicationService, rp.getUuid(), rp.getType(),
                    rp.getId(), itemsCited,
                    type + ConstantMetrics.SUFFIX_STATS_INDICATOR_TYPE_COUNT,
                    null, timestamp, null);
        }
    }

    public void setQueryDefault(String queryDefault)
    {
        this.queryDefault = queryDefault;
    }

    public String getField()
    {
        return field;
    }

    public void setField(String field)
    {
        this.field = field;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getQueryDefault()
    {
        return queryDefault;
    }

    public Class<ACO> getCrisEntityClazz()
    {
        return crisEntityClazz;
    }

    public void setCrisEntityClazz(Class<ACO> crisEntityClazz)
    {
        this.crisEntityClazz = crisEntityClazz;
    }

    public Integer getCrisEntityTypeId()
    {
        return crisEntityTypeId;
    }

    public void setCrisEntityTypeId(Integer crisEntityTypeId)
    {
        this.crisEntityTypeId = crisEntityTypeId;
    }

}
