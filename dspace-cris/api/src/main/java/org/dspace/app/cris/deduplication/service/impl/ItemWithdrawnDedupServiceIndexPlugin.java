package org.dspace.app.cris.deduplication.service.impl;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;
import org.dspace.app.cris.deduplication.service.SolrDedupServiceIndexPlugin;
import org.dspace.content.Item;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.util.ItemUtils;

public class ItemWithdrawnDedupServiceIndexPlugin
        implements SolrDedupServiceIndexPlugin
{

    private static final Logger log = Logger
            .getLogger(ItemWithdrawnDedupServiceIndexPlugin.class);

    @Override
    public void additionalIndex(Context context, Integer firstId,
            Integer secondId, Integer type, SolrInputDocument document)
    {

        if (type == Constants.ITEM)
        {
            internal(context, firstId, document);
            if(firstId!=secondId) {
                internal(context, secondId, document);
            }
        }

    }

    private void internal(Context context, Integer itemId,
            SolrInputDocument document)
    {
        try
        {
            Item item = Item.find(context, itemId);

            Integer status = ItemUtils.getItemStatus(context, item);
            if (status == 3)
            {
                document.addField(SolrDedupServiceImpl.RESOURCE_WITHDRAWN_FIELD, true);
            }
        }
        catch (SQLException e)
        {
            log.error(e.getMessage(), e);
        }
    }

}
