/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * https://github.com/CILEA/dspace-cris/wiki/License
 */
package org.dspace.app.webui.cris.util;

import it.cilea.osd.jdyna.model.Containable;
import it.cilea.osd.jdyna.model.IContainable;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.swing.text.TabExpander;

import org.apache.commons.lang.StringUtils;
import org.dspace.app.cris.dao.RPBoxDao;
import org.dspace.app.cris.model.ACrisObject;
import org.dspace.app.cris.model.Project;
import org.dspace.app.cris.model.jdyna.BoxProject;
import org.dspace.app.cris.model.jdyna.BoxResearcherPage;
import org.dspace.app.cris.model.jdyna.RPNestedObject;
import org.dspace.app.cris.model.jdyna.RPNestedProperty;
import org.dspace.app.cris.model.jdyna.TabProject;
import org.dspace.app.cris.model.jdyna.TabResearcherPage;
import org.dspace.app.cris.service.ApplicationService;
import org.dspace.app.webui.util.ADiscoveryDisplayStrategy;
import org.dspace.app.webui.util.IDisplayMetadataValueStrategy;
import org.dspace.browse.BrowseDSpaceObject;
import org.dspace.browse.BrowseItem;
import org.dspace.content.DCValue;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
import org.dspace.discovery.IGlobalSearchResult;
import org.dspace.utils.DSpace;

public class CrisPJNestedDisplayStrategy extends ADiscoveryDisplayStrategy implements
        IDisplayMetadataValueStrategy
{

    private DSpace dspace = new DSpace();

    public String getMetadataDisplay(HttpServletRequest hrq, int limit,
            boolean viewFull, String browseType, int colIdx, String field,
            DCValue[] metadataArray, BrowseItem item,
            boolean disableCrossLinks, boolean emph, PageContext pageContext)
    {
    	return null;
    }

    public String getMetadataDisplay(HttpServletRequest hrq, int limit,
            boolean viewFull, String browseType, int colIdx, String field,
            DCValue[] metadataArray, Item item, boolean disableCrossLinks,
            boolean emph, PageContext pageContext)
    {
        // not used
        return null;
    }

    public String getExtraCssDisplay(HttpServletRequest hrq, int limit,
            boolean b, String browseType, int colIdx, String field,
            DCValue[] metadataArray, Item item, boolean disableCrossLinks,
            boolean emph, PageContext pageContext) throws JspException
    {
        return null;
    }

    @Override
    public String getExtraCssDisplay(HttpServletRequest hrq, int limit,
            boolean b, String browseType, int colIdx, String field,
            DCValue[] metadataArray, BrowseItem browseItem,
            boolean disableCrossLinks, boolean emph, PageContext pageContext)
            throws JspException
    {
        return null;
    }

	@Override
	public String getMetadataDisplay(HttpServletRequest hrq, int limit, boolean viewFull, String browseType,
			int colIdx, String field, List<String> metadataArray, IGlobalSearchResult item, boolean disableCrossLinks,
			boolean emph, PageContext pageContext) throws JspException {
        Project crisObject = (Project)item;
        String[] splitted = field.split("\\.");
        //FIXME apply aspectjproxy???
        ApplicationService applicationService = dspace.getServiceManager()
                .getServiceByName("applicationService",
                        ApplicationService.class);
        
        List<BoxProject> box = applicationService.findBoxesByTTP(BoxProject.class, crisObject.getClassTypeNested(), splitted[0]);
        
        for (BoxProject ano : box)
        {
        	List<TabProject> tabs = applicationService.getList(TabProject.class);
        	for(TabProject tab : tabs) {
        		List<BoxProject> mask = applicationService.findPropertyHolderInTab(TabProject.class, tab.getId());
        		if(mask.contains(ano)) {
        			String prefix = ConfigurationManager.getProperty("crispjnested.box." + ano.getShortName() +".prefix");
        			if(prefix==null) {
        				prefix = "#";
        			}
        			return "<a href=\"cris/"+ crisObject.getPublicPath() +"/"+ crisObject.getCrisID() +"/"+tab.getShortName()+".html" + prefix + ano.getShortName() + "\">" + metadataArray.get(0) +"</a>";		
        		}
        	}
            
        }
        return metadataArray.get(0);
	}
}
