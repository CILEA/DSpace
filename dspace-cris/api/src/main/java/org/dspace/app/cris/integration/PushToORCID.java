/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * https://github.com/CILEA/dspace-cris/wiki/License
 */
package org.dspace.app.cris.integration;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.dspace.app.cris.configuration.RelationPreferenceConfiguration;
import org.dspace.app.cris.model.CrisConstants;
import org.dspace.app.cris.model.Project;
import org.dspace.app.cris.model.RelationPreference;
import org.dspace.app.cris.model.ResearcherPage;
import org.dspace.app.cris.model.jdyna.RPPropertiesDefinition;
import org.dspace.app.cris.model.jdyna.RPProperty;
import org.dspace.app.cris.model.orcid.OrcidHistory;
import org.dspace.app.cris.model.orcid.OrcidPreferencesUtils;
import org.dspace.app.cris.model.orcid.OrcidQueue;
import org.dspace.app.cris.service.ApplicationService;
import org.dspace.app.cris.service.RelationPreferenceService;
import org.dspace.app.cris.util.ResearcherPageUtils;
import org.dspace.authority.orcid.OrcidExternalIdentifierType;
import org.dspace.authority.orcid.OrcidService;
import org.dspace.authority.orcid.jaxb.Address;
import org.dspace.authority.orcid.jaxb.Amount;
import org.dspace.authority.orcid.jaxb.Biography;
import org.dspace.authority.orcid.jaxb.Citation;
import org.dspace.authority.orcid.jaxb.CitationType;
import org.dspace.authority.orcid.jaxb.ContactDetails;
import org.dspace.authority.orcid.jaxb.Contributor;
import org.dspace.authority.orcid.jaxb.ContributorAttributes;
import org.dspace.authority.orcid.jaxb.ContributorEmail;
import org.dspace.authority.orcid.jaxb.Country;
import org.dspace.authority.orcid.jaxb.CreditName;
import org.dspace.authority.orcid.jaxb.CurrencyCode;
import org.dspace.authority.orcid.jaxb.Day;
import org.dspace.authority.orcid.jaxb.Email;
import org.dspace.authority.orcid.jaxb.ExternalIdCommonName;
import org.dspace.authority.orcid.jaxb.ExternalIdReference;
import org.dspace.authority.orcid.jaxb.ExternalIdUrl;
import org.dspace.authority.orcid.jaxb.ExternalIdentifier;
import org.dspace.authority.orcid.jaxb.ExternalIdentifiers;
import org.dspace.authority.orcid.jaxb.Funding;
import org.dspace.authority.orcid.jaxb.FundingContributor;
import org.dspace.authority.orcid.jaxb.FundingContributorAttributes;
import org.dspace.authority.orcid.jaxb.FundingContributors;
import org.dspace.authority.orcid.jaxb.FundingExternalIdentifier;
import org.dspace.authority.orcid.jaxb.FundingExternalIdentifiers;
import org.dspace.authority.orcid.jaxb.FundingList;
import org.dspace.authority.orcid.jaxb.FundingTitle;
import org.dspace.authority.orcid.jaxb.FuzzyDate;
import org.dspace.authority.orcid.jaxb.Iso3166Country;
import org.dspace.authority.orcid.jaxb.JournalTitle;
import org.dspace.authority.orcid.jaxb.Keyword;
import org.dspace.authority.orcid.jaxb.Keywords;
import org.dspace.authority.orcid.jaxb.LanguageCode;
import org.dspace.authority.orcid.jaxb.Month;
import org.dspace.authority.orcid.jaxb.OrcidActivities;
import org.dspace.authority.orcid.jaxb.OrcidBio;
import org.dspace.authority.orcid.jaxb.OrcidId;
import org.dspace.authority.orcid.jaxb.OrcidMessage;
import org.dspace.authority.orcid.jaxb.OrcidProfile;
import org.dspace.authority.orcid.jaxb.OrcidWork;
import org.dspace.authority.orcid.jaxb.OrcidWorks;
import org.dspace.authority.orcid.jaxb.Organization;
import org.dspace.authority.orcid.jaxb.OrganizationAddress;
import org.dspace.authority.orcid.jaxb.OtherNames;
import org.dspace.authority.orcid.jaxb.PersonalDetails;
import org.dspace.authority.orcid.jaxb.PublicationDate;
import org.dspace.authority.orcid.jaxb.ResearcherUrl;
import org.dspace.authority.orcid.jaxb.ResearcherUrls;
import org.dspace.authority.orcid.jaxb.Subtitle;
import org.dspace.authority.orcid.jaxb.TranslatedTitle;
import org.dspace.authority.orcid.jaxb.Url;
import org.dspace.authority.orcid.jaxb.WorkContributors;
import org.dspace.authority.orcid.jaxb.WorkExternalIdentifier;
import org.dspace.authority.orcid.jaxb.WorkExternalIdentifiers;
import org.dspace.authority.orcid.jaxb.WorkTitle;
import org.dspace.authority.orcid.jaxb.Year;
import org.dspace.content.DCPersonName;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.I18nUtil;
import org.dspace.discovery.SearchService;
import org.dspace.discovery.SearchServiceException;
import org.dspace.handle.HandleManager;
import org.dspace.util.SimpleMapConverter;
import org.dspace.utils.DSpace;

import it.cilea.osd.common.core.SingleTimeStampInfo;
import it.cilea.osd.jdyna.value.BooleanValue;
import it.cilea.osd.jdyna.value.TextValue;

public class PushToORCID {

	private static final String ORCID_PUSH_MANUAL = "orcid-push-manual";
	public static final String EMAIL_TEMPLATE_NAME = "orcid-alerts";
	private static final int ORCID_PUBLICATION_PREFS_DISABLED = 0;
	private static final int ORCID_PUBLICATION_PREFS_ALL = 1;
	private static final int ORCID_PUBLICATION_PREFS_SELECTED = 2;

	private static final int ORCID_PROJECTS_PREFS_DISABLED = 0;
	private static final int ORCID_PROJECTS_PREFS_ALL = 1;
	private static final int ORCID_PROJECTS_PREFS_SELECTED = 2;

	/** the logger */
	private static Logger log = Logger.getLogger(PushToORCID.class);

	public static void prepareAndSend(Context context, List<ResearcherPage> rps,
			RelationPreferenceService relationPreferenceService, SearchService searchService,
			ApplicationService applicationService) throws Exception {
		log.debug("Working... push to ORCID");

		Map<String, Map<String, List<String>>> mapResearcherMetadataToSend = new HashMap<String, Map<String, List<String>>>();
		Map<String, List<Integer>> mapPublicationsToSend = new HashMap<String, List<Integer>>();
		Map<String, List<Integer>> mapProjectsToSend = new HashMap<String, List<Integer>>();

		boolean byPassManualMode = ConfigurationManager.getBooleanProperty("cris",
				"system.script.pushtoorcid.force", false);

		boolean buildORCIDProfile = ConfigurationManager.getBooleanProperty("cris",
				"system.script.pushtoorcid.profile.create.new", false);

		// if defined then the script works in this default mode
		String defaultPreference = ConfigurationManager.getProperty("cris",
				"system.script.pushtoorcid.default.preference");

		Map<String, String> mapResearcherOrcid = new HashMap<String, String>();
		Map<String, String> mapResearcherTokenUpdateBio = new HashMap<String, String>();
		Map<String, String> mapResearcherTokenCreateWorks = new HashMap<String, String>();
		Map<String, String> mapResearcherTokenUpdateWorks = new HashMap<String, String>();
		Map<String, String> mapResearcherTokenCreateFundings = new HashMap<String, String>();
		Map<String, String> mapResearcherTokenUpdateFundings = new HashMap<String, String>();
		List<String> listNewResearcherToPushOnOrcid = new ArrayList<String>();

		Map<String, String> orcidConfigurationMapping = prepareConfigurationMappingForProfile(applicationService);

		external: for (ResearcherPage researcher : rps) {

			String crisID = researcher.getCrisID();
			List<Integer> itemIDsToSend = new ArrayList<Integer>();
			List<Integer> projectsIDsToSend = new ArrayList<Integer>();

			boolean isManualMode = false;
			if (!byPassManualMode) {
				isManualMode = isManualModeEnable(researcher);
			}

			if (isManualMode) {
				sendEmail(context, researcher, crisID);
				continue external;
			}
			boolean prepareUpdateProfile = false;
			boolean prepareCreateWork = false;
			boolean prepareUpdateWork = false;
			boolean prepareCreateFunding = false;
			boolean prepareUpdateFunding = false;

			boolean foundORCID = false;

			for (RPProperty orcid : researcher.getAnagrafica4view().get("orcid")) {
				mapResearcherOrcid.put(crisID, orcid.toString());
				foundORCID = true;
				break;
			}

			String publicationsPrefs = ResearcherPageUtils.getStringValue(researcher,
					OrcidPreferencesUtils.ORCID_PUBLICATIONS_PREFS);
			String projectPrefs = ResearcherPageUtils.getStringValue(researcher,
					OrcidPreferencesUtils.ORCID_PROJECTS_PREFS);

			int parseIntPublicationsPrefs = ORCID_PUBLICATION_PREFS_DISABLED;
			if (publicationsPrefs != null) {
				parseIntPublicationsPrefs = Integer.parseInt(publicationsPrefs);
			}
			int parseIntProjectPrefs = ORCID_PROJECTS_PREFS_DISABLED;
			if (projectPrefs != null) {
				parseIntProjectPrefs = Integer.parseInt(projectPrefs);
			}

			if (buildORCIDProfile && !foundORCID) {
				listNewResearcherToPushOnOrcid.add(crisID);
				// default pushing all profile field founded on configuration
				prepareUpdateProfile(applicationService, mapResearcherMetadataToSend, researcher, true);
				// default pushing selected works

				prepareWorks(relationPreferenceService, searchService, mapPublicationsToSend, researcher, crisID,
						itemIDsToSend, defaultPreference == null ? "" + parseIntPublicationsPrefs : defaultPreference);
				// default pushing selected fundings
				prepareFundings(relationPreferenceService, searchService, mapProjectsToSend, researcher, crisID,
						projectsIDsToSend, defaultPreference == null ? "" + parseIntProjectPrefs : defaultPreference);
			} else {
				for (RPProperty tokenRP : researcher.getAnagrafica4view().get("system-orcid-token-orcid-bio-update")) {
					mapResearcherTokenUpdateBio.put(crisID, tokenRP.toString());
					prepareUpdateProfile = true;
					break;
				}
				for (RPProperty tokenRP : researcher.getAnagrafica4view()
						.get("system-orcid-token-orcid-works-create")) {
					mapResearcherTokenCreateWorks.put(crisID, tokenRP.toString());
					prepareCreateWork = true;
					break;
				}
				for (RPProperty tokenRP : researcher.getAnagrafica4view()
						.get("system-orcid-token-orcid-works-update")) {
					mapResearcherTokenUpdateWorks.put(crisID, tokenRP.toString());
					prepareUpdateWork = true;
					break;
				}
				for (RPProperty tokenRP : researcher.getAnagrafica4view().get("system-orcid-token-funding-create")) {
					mapResearcherTokenCreateFundings.put(crisID, tokenRP.toString());
					prepareCreateFunding = true;
					break;
				}
				for (RPProperty tokenRP : researcher.getAnagrafica4view().get("system-orcid-token-funding-update")) {
					mapResearcherTokenUpdateFundings.put(crisID, tokenRP.toString());
					prepareUpdateFunding = true;
					break;
				}

				if (prepareUpdateProfile) {
					prepareUpdateProfile(applicationService, mapResearcherMetadataToSend, researcher, false);
				}

				if (prepareCreateWork && prepareUpdateWork) {
					prepareWorks(relationPreferenceService, searchService, mapPublicationsToSend, researcher, crisID,
							itemIDsToSend, publicationsPrefs);
				}

				if (prepareCreateFunding && prepareUpdateFunding) {
					prepareFundings(relationPreferenceService, searchService, mapProjectsToSend, researcher, crisID,
							projectsIDsToSend, projectPrefs);
				}
			}
		}

		log.debug("Create DSpace context and use browse indexing");

		OrcidService orcidService = OrcidService.getOrcid();

		if (buildORCIDProfile) {
			log.info("Starts push new ORCID Profile");
			for (String crisId : listNewResearcherToPushOnOrcid) {
				log.info("Prepare push for ResearcherPage crisID:" + crisId);
				try {
					OrcidProfile profile = buildOrcidProfile(applicationService, crisId, mapResearcherMetadataToSend,
							orcidConfigurationMapping);
					OrcidWorks works = buildOrcidWorks(context, crisId, mapPublicationsToSend);
					FundingList fundings = buildOrcidFundings(context, applicationService, crisId, mapProjectsToSend);
					OrcidActivities activities = new OrcidActivities();
					activities.setOrcidWorks(works);
					activities.setFundingList(fundings);
					profile.setOrcidActivities(activities);

					String orcid = orcidService.buildProfile(profile);
					ResearcherPage rp = applicationService.getEntityByCrisId(crisId, ResearcherPage.class);
					ResearcherPageUtils.buildTextValue(rp, orcid, "orcid");
					applicationService.saveOrUpdate(ResearcherPage.class, rp);
					log.info("OK!!! pushed ResearcherPage crisID:" + crisId + "; assign orcid iD:" + orcid);
				} catch (Exception e) {
					log.info("ERROR!!! ResearcherPage crisID:" + crisId);
					log.error(e.getMessage());
				}
			}
			log.info("Ends push new ORCID Profile");
		}

		log.info("Starts update ORCID Profile");
		for (String crisId : mapResearcherOrcid.keySet()) {

			String orcid = mapResearcherOrcid.get(crisId);

			if (StringUtils.isNotBlank(orcid)) {
				log.info("Prepare push for ResearcherPage crisID:" + crisId + " AND orcid iD:" + orcid);
				try {
					String tokenUpdateBio = mapResearcherTokenUpdateBio.get(crisId);
					if (StringUtils.isNotBlank(tokenUpdateBio)) {
						log.info("(Q1)Prepare OrcidProfile for ResearcherPage crisID:" + crisId);
						OrcidProfile profile = buildOrcidProfile(applicationService, crisId,
								mapResearcherMetadataToSend, orcidConfigurationMapping);
						if (profile != null) {
							orcidService.updateBio(orcid, tokenUpdateBio, profile);
							log.info("(A1) OrcidProfile for ResearcherPage crisID:" + crisId);
						}
					}

					String tokenCreateWorks = mapResearcherTokenCreateWorks.get(crisId);
					if (StringUtils.isNotBlank(tokenCreateWorks)) {
						log.info("(Q2)Prepare OrcidWorks for ResearcherPage crisID:" + crisId);
						OrcidWorks works = buildOrcidWorks(context, crisId, mapPublicationsToSend);
						if (works != null) {
							orcidService.appendWorks(orcid, tokenCreateWorks, works);
							log.info("(A2) OrcidWorks for ResearcherPage crisID:" + crisId);
						}
					}

					String tokenCreateFundings = mapResearcherTokenCreateFundings.get(crisId);
					if (StringUtils.isNotBlank(tokenCreateFundings)) {
						log.info("(Q3)Prepare FundingList for ResearcherPage crisID:" + crisId);
						FundingList fundings = buildOrcidFundings(context, applicationService, crisId,
								mapProjectsToSend);
						if (fundings != null) {
							orcidService.appendFundings(orcid, tokenCreateWorks, fundings);
							log.info("(A3) FundingList for ResearcherPage crisID:" + crisId);
						}
					}
				} catch (Exception ex) {
					log.info("ERROR!!! ResearcherPage crisID:" + crisId);
					log.error(ex.getMessage());
				}
			}
		}
		log.info("Ends update ORCID Profile");

	}

	public static void sendEmail(Context context, ResearcherPage researcher, String crisID) {
		try {
			org.dspace.core.Email email = org.dspace.core.Email.getEmail(
					I18nUtil.getEmailFilename(context.getCurrentLocale(), PushToORCID.EMAIL_TEMPLATE_NAME));
			email.addRecipient(researcher.getEmail().getValue());
			email.addArgument(ConfigurationManager.getProperty("dspace.url") + "/cris/rp/" + crisID);
			email.send();
		} catch (MessagingException | IOException e) {
			log.error("Email not send for:" + crisID);
		}
	}

	/**
	 * Check if manual mode is enable or we have the PUT signal for works/fundings/profile
	 * 
	 * @param researcher
	 * @param isManualMode
	 * @return
	 */
	public static boolean isManualModeEnable(ResearcherPage researcher) {
		boolean isManualMode = false;
		for (RPProperty pushManual : researcher.getAnagrafica4view().get(ORCID_PUSH_MANUAL)) {
			TextValue value = (TextValue) pushManual.getValue();
			if (value.getObject().equals("1")) {
				isManualMode = true;
			}
			break;
		}
		
		for (RPProperty pushManual : researcher.getAnagrafica4view().get(OrcidPreferencesUtils.ORCID_PUSH_ITEM_ACTIVATE_PUT)) {
			BooleanValue bval = (BooleanValue) (pushManual.getValue());
			if(bval.getObject()) {
				isManualMode = true;
			}
			break;
		}
		
		for (RPProperty pushManual : researcher.getAnagrafica4view().get(OrcidPreferencesUtils.ORCID_PUSH_CRISPJ_ACTIVATE_PUT)) {
			BooleanValue bval = (BooleanValue) (pushManual.getValue());
			if(bval.getObject()) {
				isManualMode = true;
			}
			break;
		}
		
		for (RPProperty pushManual : researcher.getAnagrafica4view().get(OrcidPreferencesUtils.ORCID_PUSH_CRISRP_ACTIVATE_PUT)) {
			BooleanValue bval = (BooleanValue) (pushManual.getValue());
			if(bval.getObject()) {
				isManualMode = true;
			}
			break;
		}
		return isManualMode;
	}

	public static Map<String, String> prepareConfigurationMappingForProfile(ApplicationService applicationService) {
		// prepare configuration mapping ORCID->DSPACE-CRIS
		Map<String, String> orcidConfigurationMapping = new HashMap<String, String>();
		List<RPPropertiesDefinition> metadataDefinitions = applicationService
				.likePropertiesDefinitionsByShortName(RPPropertiesDefinition.class, OrcidPreferencesUtils.PREFIX_ORCID_PROFILE_PREF);
		for (RPPropertiesDefinition rppd : metadataDefinitions) {
			String metadataShortnameINTERNAL = rppd.getShortName().replaceFirst(OrcidPreferencesUtils.PREFIX_ORCID_PROFILE_PREF, "");
			String metadataShortnameORCID = rppd.getLabel();
			orcidConfigurationMapping.put(metadataShortnameORCID, metadataShortnameINTERNAL);
		}
		return orcidConfigurationMapping;
	}

	private static void prepareFundings(RelationPreferenceService relationPreferenceService,
			SearchService searchService, Map<String, List<Integer>> mapProjectsToSend, ResearcherPage researcher,
			String crisID, List<Integer> projectsIDsToSend, String projectsPrefs) {
		if (StringUtils.isNotBlank(projectsPrefs)) {
			if (Integer.parseInt(projectsPrefs) != ORCID_PROJECTS_PREFS_DISABLED) {
				if (Integer.parseInt(projectsPrefs) == ORCID_PROJECTS_PREFS_ALL) {
					log.info("...it will work on all researcher...");
					SolrQuery query = new SolrQuery("*:*");
					query.addFilterQuery("{!field f=search.resourcetype}" + CrisConstants.PROJECT_TYPE_ID);
					query.addFilterQuery("projectinvestigators_authority:" + crisID);
					query.setFields("search.resourceid", "search.resourcetype");
					query.setRows(Integer.MAX_VALUE);
					try {
						QueryResponse response = searchService.search(query);
						SolrDocumentList docList = response.getResults();
						Iterator<SolrDocument> solrDoc = docList.iterator();
						while (solrDoc.hasNext()) {
							SolrDocument doc = solrDoc.next();
							Integer rpId = (Integer) doc.getFirstValue("search.resourceid");
							projectsIDsToSend.add(rpId);
						}
					} catch (SearchServiceException e) {
						log.error("Error retrieving documents", e);
					}
				} else {
					if (Integer.parseInt(projectsPrefs) == ORCID_PROJECTS_PREFS_SELECTED) {
						List<RelationPreference> selected = new ArrayList<RelationPreference>();
						for (RelationPreferenceConfiguration configuration : relationPreferenceService
								.getConfigurationService().getList()) {
							if (configuration.getRelationConfiguration().getRelationClass().equals(Project.class)) {
								selected = relationPreferenceService.findRelationsPreferencesByUUIDByRelTypeAndStatus(
										researcher.getUuid(),
										configuration.getRelationConfiguration().getRelationName(),
										RelationPreference.SELECTED);
							}
							for (RelationPreference sel : selected) {
								projectsIDsToSend.add(sel.getItemID());
							}
						}

					} else {
						log.warn(crisID + " - projects preferences NOT recognized");
					}
				}
			} else {
				log.info(crisID + " - DISABLED projects preferences");
			}
		}

		mapProjectsToSend.put(crisID, projectsIDsToSend);
	}

	private static void prepareWorks(RelationPreferenceService relationPreferenceService, SearchService searchService,
			Map<String, List<Integer>> mapPublicationsToSend, ResearcherPage researcher, String crisID,
			List<Integer> itemIDsToSend, String publicationsPrefs) {
		if (publicationsPrefs != null) {
			if (Integer.parseInt(publicationsPrefs) != ORCID_PUBLICATION_PREFS_DISABLED) {
				if (Integer.parseInt(publicationsPrefs) == ORCID_PUBLICATION_PREFS_ALL) {
					log.info("...it will work on all researcher...");
					SolrQuery query = new SolrQuery("*:*");
					query.addFilterQuery("{!field f=search.resourcetype}" + Constants.ITEM);
					query.addFilterQuery("author_authority:" + crisID);
					query.setFields("search.resourceid", "search.resourcetype");
					query.setRows(Integer.MAX_VALUE);
					try {
						QueryResponse response = searchService.search(query);
						SolrDocumentList docList = response.getResults();
						Iterator<SolrDocument> solrDoc = docList.iterator();
						while (solrDoc.hasNext()) {
							SolrDocument doc = solrDoc.next();
							Integer rpId = (Integer) doc.getFirstValue("search.resourceid");
							itemIDsToSend.add(rpId);
						}
					} catch (SearchServiceException e) {
						log.error("Error retrieving documents", e);
					}
				} else {
					if (Integer.parseInt(publicationsPrefs) == ORCID_PUBLICATION_PREFS_SELECTED) {
						List<RelationPreference> selected = new ArrayList<RelationPreference>();
						for (RelationPreferenceConfiguration configuration : relationPreferenceService
								.getConfigurationService().getList()) {
							if (configuration.getRelationConfiguration().getRelationClass().equals(Item.class)) {
								selected = relationPreferenceService.findRelationsPreferencesByUUIDByRelTypeAndStatus(
										researcher.getUuid(),
										configuration.getRelationConfiguration().getRelationName(),
										RelationPreference.SELECTED);
							}
							for (RelationPreference sel : selected) {
								itemIDsToSend.add(sel.getItemID());
							}
						}

					} else {
						log.warn(crisID + " - publications preferences NOT recognized");
					}
				}
			} else {
				log.info(crisID + " - DISABLED publications preferences");
			}
		}
		mapPublicationsToSend.put(crisID, itemIDsToSend);
	}

	public static void prepareUpdateProfile(ApplicationService applicationService,
			Map<String, Map<String, List<String>>> mapResearcherMetadataToSend, ResearcherPage researcher,
			boolean force) {
		List<RPPropertiesDefinition> metadataDefinitions = applicationService
				.likePropertiesDefinitionsByShortName(RPPropertiesDefinition.class, OrcidPreferencesUtils.PREFIX_ORCID_PROFILE_PREF);
		Map<String, List<String>> mapMetadata = new HashMap<String, List<String>>();
		for (RPPropertiesDefinition rppd : metadataDefinitions) {
			String metadataShortnameINTERNAL = rppd.getShortName().replaceFirst(OrcidPreferencesUtils.PREFIX_ORCID_PROFILE_PREF, "");
			String metadataShortnameORCID = rppd.getLabel();

			List<RPProperty> metadatas = researcher.getAnagrafica4view().get(metadataShortnameINTERNAL);

			List<String> listMetadata = new ArrayList<String>();

			for (RPProperty metadata : metadatas) {
				if (force || (researcher.getProprietaDellaTipologia(rppd) != null
						&& !researcher.getProprietaDellaTipologia(rppd).isEmpty()
						&& (Boolean) researcher.getProprietaDellaTipologia(rppd).get(0).getObject())) {
					listMetadata.add(metadata.toString());
				}
			}

			if (!listMetadata.isEmpty()) {
				mapMetadata.put(metadataShortnameORCID, listMetadata);
			}

		}
		mapResearcherMetadataToSend.put(researcher.getCrisID(), mapMetadata);
	}

	private static FundingList buildOrcidFundings(Context context, ApplicationService applicationService, String crisId,
			Map<String, List<Integer>> mapProjectsToSend) throws SQLException {
		FundingList fundingList = new FundingList();
		List<Integer> listOfItems = mapProjectsToSend.get(crisId);
		for (Integer ii : listOfItems) {
			Project project = applicationService.get(Project.class, ii);
			Funding funding = buildOrcidFunding(context, applicationService, project);
			fundingList.getFunding().add(funding);
		}
		return fundingList;
	}

	private static Funding buildOrcidFunding(Context context, ApplicationService applicationService, Project project)
			throws SQLException {
		OrcidFundingMetadata itemMetadata = new OrcidFundingMetadata(context, project);

		Funding funding = new Funding();
		if (StringUtils.isNotBlank(itemMetadata.getAmount())) {
			Amount amount = new Amount();
			CurrencyCode currencyCode = CurrencyCode.fromValue(itemMetadata.getCurrencyCode());
			amount.setValue(itemMetadata.getAmount());
			amount.setCurrencyCode(currencyCode);
			funding.setAmount(amount);
		}

		if (StringUtils.isNotBlank(itemMetadata.getStartYear()) || StringUtils.isNotBlank(itemMetadata.getStartMonth())
				|| StringUtils.isNotBlank(itemMetadata.getStartDay())) {
			FuzzyDate fuzzyDate = new FuzzyDate();
			if (StringUtils.isNotBlank(itemMetadata.getStartYear())) {
				Year year = new Year();
				year.setValue(itemMetadata.getStartYear());
				fuzzyDate.setYear(year);
			}
			if (StringUtils.isNotBlank(itemMetadata.getStartMonth())) {
				Month month = new Month();
				month.setValue(itemMetadata.getStartMonth());
				fuzzyDate.setMonth(month);
			}
			if (StringUtils.isNotBlank(itemMetadata.getStartDay())) {
				Day day = new Day();
				day.setValue(itemMetadata.getStartDay());
				fuzzyDate.setDay(day);
			}
			funding.setStartDate(fuzzyDate);
		}
		if (StringUtils.isNotBlank(itemMetadata.getEndYear()) || StringUtils.isNotBlank(itemMetadata.getEndMonth())
				|| StringUtils.isNotBlank(itemMetadata.getEndDay())) {
			FuzzyDate fuzzyDate = new FuzzyDate();
			if (StringUtils.isNotBlank(itemMetadata.getEndYear())) {
				Year year = new Year();
				year.setValue(itemMetadata.getEndYear());
				fuzzyDate.setYear(year);
			}
			if (StringUtils.isNotBlank(itemMetadata.getEndMonth())) {
				Month month = new Month();
				month.setValue(itemMetadata.getEndMonth());
				fuzzyDate.setMonth(month);
			}
			if (StringUtils.isNotBlank(itemMetadata.getEndDay())) {
				Day day = new Day();
				day.setValue(itemMetadata.getEndDay());
				fuzzyDate.setDay(day);
			}
			funding.setEndDate(fuzzyDate);
		}

		boolean buildFundingContributors = false;
		FundingContributors fundingContributors = new FundingContributors();
		if (itemMetadata.getContributorsLead() != null) {
			for (String valContributor : itemMetadata.getContributorsLead()) {
				addFundingContributor(fundingContributors, valContributor, "lead");
				buildFundingContributors = true;
			}
		}

		if (itemMetadata.getContributorsCoLead() != null) {
			for (String valContributor : itemMetadata.getContributorsCoLead()) {
				addFundingContributor(fundingContributors, valContributor, "colead");
				buildFundingContributors = true;
			}
		}
		if (buildFundingContributors) {
			funding.setFundingContributors(fundingContributors);
		}

		if (itemMetadata.getExternalIdentifier() != null) {
			FundingExternalIdentifiers fundingExternalIdentifiers = new FundingExternalIdentifiers();
			for (String valIdentifier : itemMetadata.getExternalIdentifier()) {
				FundingExternalIdentifier fundingExternalIdentifier = new FundingExternalIdentifier();
				fundingExternalIdentifier
						.setFundingExternalIdentifierType(itemMetadata.getExternalIdentifierType(valIdentifier));
				fundingExternalIdentifier.setFundingExternalIdentifierValue(valIdentifier);
				fundingExternalIdentifiers.getFundingExternalIdentifier().add(fundingExternalIdentifier);
			}
			funding.setFundingExternalIdentifiers(fundingExternalIdentifiers);
		}

		if (StringUtils.isNotBlank(itemMetadata.getTitle())) {
			FundingTitle fundingTitle = new FundingTitle();
			fundingTitle.setTitle(itemMetadata.getTitle());
			funding.setFundingTitle(fundingTitle);
		}

		if (StringUtils.isNotBlank(itemMetadata.getType())) {
			funding.setFundingType(itemMetadata.getType());
		}

		if (StringUtils.isNotBlank(itemMetadata.getAbstract())) {
			funding.setShortDescription(itemMetadata.getAbstract());
		}
		if (StringUtils.isNotBlank(itemMetadata.getURL())) {
			Url url = new Url();
			url.setValue(itemMetadata.getURL());
			funding.setUrl(url);
		}
		if (StringUtils.isNotBlank(itemMetadata.getOrganization())) {
			Organization organization = new Organization();
			organization.setName(itemMetadata.getOrganization());
			OrganizationAddress organizationAddress = new OrganizationAddress();
			organizationAddress.setCity(itemMetadata.getOrganizationCity());
			organizationAddress.setCountry(Iso3166Country.fromValue(itemMetadata.getOrganizationCountry()));
			organization.setAddress(organizationAddress);
			funding.setOrganization(organization);
		}
		return funding;
	}

	private static void addFundingContributor(FundingContributors fundingContributors, String valContributor,
			String type) {
		FundingContributor contributor = new FundingContributor();

		Integer id = ResearcherPageUtils.getRealPersistentIdentifier(valContributor, ResearcherPage.class);
		String name = valContributor;
		String email = "";
		String orcid = "";
		if (null != id) {
			ResearcherPage ro = ResearcherPageUtils.getCrisObject(id, ResearcherPage.class);
			name = ResearcherPageUtils.getStringValue(ro, "fullName");
			email = ResearcherPageUtils.getStringValue(ro, "email");
			orcid = ResearcherPageUtils.getStringValue(ro, "orcid");

			if (StringUtils.isNotBlank(email)) {
				ContributorEmail contributorEmail = new ContributorEmail();
				contributorEmail.setValue(email);
				contributor.setContributorEmail(contributorEmail);
			}
			if (StringUtils.isNotBlank(orcid)) {
				String domainOrcid = ConfigurationManager.getProperty("cris",
						"external.domainname.authority.service.orcid");
				OrcidId orcidID = new OrcidId();
				JAXBElement<String> jaxBOrcid = new JAXBElement<String>(
						new QName("http://www.orcid.org/ns/orcid", "uri", "ns2"), String.class, domainOrcid + orcid);
				orcidID.getContent().add(jaxBOrcid);
				contributor.setContributorOrcid(orcidID);
			}
		}

		CreditName creditName = new CreditName();
		creditName.setValue(name);
		contributor.setCreditName(creditName);

		FundingContributorAttributes attributes = new FundingContributorAttributes();
		attributes.setFundingContributorRole(type);
		fundingContributors.getFundingContributor().add(contributor);
	}

	private static OrcidWorks buildOrcidWorks(Context context, String crisId,
			Map<String, List<Integer>> mapPublicationsToSend) throws SQLException {
		OrcidWorks orcidWorks = new OrcidWorks();
		List<Integer> listOfItems = mapPublicationsToSend.get(crisId);
		for (Integer ii : listOfItems) {
			orcidWorks.getOrcidWork().add(buildOrcidWork(context, ii));
		}
		return orcidWorks;
	}

	private static OrcidWork buildOrcidWork(Context context, Integer ii) throws SQLException {
		OrcidWork orcidWork = new OrcidWork();

		Item item = Item.find(context, ii);

		OrcidWorkMetadata itemMetadata = new OrcidWorkMetadata(context, item);

		WorkTitle worktitle = new WorkTitle();
		worktitle.setTitle(itemMetadata.getTitle());

		if (StringUtils.isNotBlank(itemMetadata.getSubTitle())) {
			Subtitle subtitle = new Subtitle();
			subtitle.setContent(itemMetadata.getSubTitle());
			worktitle.setSubtitle(subtitle);
		}

		if (StringUtils.isNotBlank(itemMetadata.getTranslatedTitle())) {
			TranslatedTitle translatedTitle = new TranslatedTitle();
			translatedTitle.setValue(itemMetadata.getTranslatedTitle());
			String translatedLanguageCode = itemMetadata.getTranslatedTitleLanguage();

			try {
				LanguageCode langCode = LanguageCode.fromValue(translatedLanguageCode);
				translatedTitle.setLanguageCode(langCode);
			} catch (Exception ex) {
				translatedTitle.setLanguageCode(LanguageCode.EN);
			}
			worktitle.setTranslatedTitle(translatedTitle);
		}

		String citationVal = itemMetadata.getCitation();
		if (StringUtils.isNotBlank(citationVal)) {
			Citation citation = new Citation();
			citation.setWorkCitationType(CitationType.fromValue(itemMetadata.getCitationType()));
			citation.setCitation(citationVal);
			orcidWork.setWorkCitation(citation);
		}

		orcidWork.setWorkTitle(worktitle);

		if (StringUtils.isNotBlank(itemMetadata.getJournalTitle())) {
			JournalTitle journalTitle = new JournalTitle();
			journalTitle.setContent(itemMetadata.getJournalTitle());
			orcidWork.setJournalTitle(journalTitle);
		}

		DecimalFormat dateMonthAndDayFormat = new DecimalFormat("00");

		if (StringUtils.isNotBlank(itemMetadata.getYear()) || StringUtils.isNotBlank(itemMetadata.getMonth())
				|| StringUtils.isNotBlank(itemMetadata.getDay())) {
			PublicationDate publicationDate = new PublicationDate();
			if (StringUtils.isNotBlank(itemMetadata.getYear())) {
				Year year = new Year();
				year.setValue(itemMetadata.getYear());
				publicationDate.setYear(year);
			}
			if (StringUtils.isNotBlank(itemMetadata.getMonth())) {
				Month month = new Month();
				month.setValue(dateMonthAndDayFormat.format(Long.parseLong(itemMetadata.getMonth())));
				publicationDate.setMonth(month);
			}
			if (StringUtils.isNotBlank(itemMetadata.getDay())) {
				Day day = new Day();
				day.setValue(dateMonthAndDayFormat.format(Long.parseLong(itemMetadata.getDay())));
				publicationDate.setDay(day);
			}
			orcidWork.setPublicationDate(publicationDate);
		}

		if (StringUtils.isNotBlank(itemMetadata.getURL())) {
			Url url = new Url();
			url.setValue(itemMetadata.getURL());
			orcidWork.setUrl(url);
		}

		// add source internal id
		WorkExternalIdentifiers workExternalIdentifiers = new WorkExternalIdentifiers();
		WorkExternalIdentifier workExternalIdentifierInternal = new WorkExternalIdentifier();
		workExternalIdentifierInternal.setWorkExternalIdentifierId("" + item.getID());
		workExternalIdentifierInternal.setWorkExternalIdentifierType(OrcidExternalIdentifierType.SOURCE_ID.toString());
		workExternalIdentifiers.getWorkExternalIdentifier().add(workExternalIdentifierInternal);

		// add other external id
		if (itemMetadata.getExternalIdentifier() != null && !itemMetadata.getExternalIdentifier().isEmpty()) {
			for (String valIdentifier : itemMetadata.getExternalIdentifier()) {
				WorkExternalIdentifier workExternalIdentifier = new WorkExternalIdentifier();
				workExternalIdentifier.setWorkExternalIdentifierId(valIdentifier);
				workExternalIdentifier
						.setWorkExternalIdentifierType(itemMetadata.getExternalIdentifierType(valIdentifier));
				workExternalIdentifiers.getWorkExternalIdentifier().add(workExternalIdentifier);
			}
		}
		orcidWork.setWorkExternalIdentifiers(workExternalIdentifiers);

		// export if have an authority value
		WorkContributors workContributors = new WorkContributors();
		boolean haveContributor = false;
		for (String valContributor : itemMetadata.getAuthors()) {
			Contributor contributor = new Contributor();

			Integer id = ResearcherPageUtils.getRealPersistentIdentifier(valContributor, ResearcherPage.class);
			String name = valContributor;
			String email = "";
			String orcid = "";
			if (null != id) {
				ResearcherPage ro = ResearcherPageUtils.getCrisObject(id, ResearcherPage.class);
				name = ResearcherPageUtils.getStringValue(ro, "fullName");
				email = ResearcherPageUtils.getStringValue(ro, "email");
				orcid = ResearcherPageUtils.getStringValue(ro, "orcid");

				if (StringUtils.isNotBlank(email)) {
					ContributorEmail contributorEmail = new ContributorEmail();
					contributorEmail.setValue(email);
					contributor.setContributorEmail(contributorEmail);
				}
				if (StringUtils.isNotBlank(orcid)) {
					String domainOrcid = ConfigurationManager.getProperty("cris",
							"external.domainname.authority.service.orcid");
					OrcidId orcidID = new OrcidId();

					JAXBElement<String> jaxBOrcid = new JAXBElement<String>(
							new QName("http://www.orcid.org/ns/orcid", "uri", "ns2"), String.class,
							domainOrcid + orcid);
					orcidID.getContent().add(jaxBOrcid);
					contributor.setContributorOrcid(orcidID);
				}
			}

			CreditName creditName = new CreditName();
			creditName.setValue(name);
			contributor.setCreditName(creditName);

			ContributorAttributes attributes = new ContributorAttributes();
			// TODO now supported only author/additional
			attributes.setContributorRole("author");
			attributes.setContributorSequence("additional");
			contributor.setContributorAttributes(attributes);
			workContributors.getContributor().add(contributor);
			haveContributor = true;
		}
		if (haveContributor) {
			orcidWork.setWorkContributors(workContributors);
		}

		if (StringUtils.isNotBlank(itemMetadata.getLanguage())) {
			LanguageCode language = LanguageCode.fromValue(itemMetadata.getLanguage());
			orcidWork.setLanguageCode(language);
		}

		SimpleMapConverter mapConverterModifier = new DSpace().getServiceManager()
				.getServiceByName("mapConverterOrcidWorkType", SimpleMapConverter.class);
		if (mapConverterModifier == null) {
			orcidWork.setWorkType(itemMetadata.getWorkType());
		} else {
			orcidWork.setWorkType(mapConverterModifier.getValue(itemMetadata.getWorkType()));
		}

		return orcidWork;
	}

	public static OrcidProfile buildOrcidProfile(ApplicationService applicationService, String crisId,
			Map<String, Map<String, List<String>>> mapResearcherMetadataToSend,
			Map<String, String> orcidMetadataConfiguration) {

		OrcidProfile profile = new OrcidProfile();

		Map<String, List<String>> metadata = mapResearcherMetadataToSend.get(crisId);

		List<String> name = metadata.get("name");

		// retrieve data from map
		String givenNames = null;
		String familyName = null;
		if (name != null && !name.isEmpty()) {
			givenNames = getGivenName(name.get(0));
			familyName = getFamilyName(name.get(0));
		}

		String creditName = null;
		List<String> creditname = metadata.get("credit-name");
		if (creditname != null && !creditname.isEmpty()) {
			creditName = creditname.get(0);
		}

		List<String> otherNames = new ArrayList<String>();
		otherNames.add(name.get(0));
		List<String> othernamesPlatform = metadata.get("other-names");
		if (othernamesPlatform != null && !othernamesPlatform.isEmpty()) {
			otherNames.addAll(othernamesPlatform);
		}

		List<String> biographyString = metadata.get("biography");
		String biography = null;
		if (biographyString != null && !biographyString.isEmpty()) {
			biography = biographyString.get(0);
		}

		String primaryEmail = null;
		List<String> primaryEmailString = metadata.get("primary-email");
		if (primaryEmailString != null && !primaryEmailString.isEmpty()) {
			primaryEmail = primaryEmailString.get(0);
		}

		List<String> emails = metadata.get("other-emails");

		List<String> isoCountryString = metadata.get("iso-3166-country");
		String country = null;
		if (isoCountryString != null && !isoCountryString.isEmpty()) {
			country = isoCountryString.get(0);
		}

		List<String> keywords = metadata.get("keywords");

		OrcidBio bioJAXB = new OrcidBio();

		// write personal details
		PersonalDetails personalDetails = new PersonalDetails();
		personalDetails.setGivenNames(givenNames);
		if (StringUtils.isNotBlank(familyName)) {
			personalDetails.setFamilyName(familyName);
		}

		if (StringUtils.isNotBlank(creditName)) {
			CreditName creditNameJAXB = new CreditName();
			creditNameJAXB.setValue(creditName);
			personalDetails.setCreditName(creditNameJAXB);
		}

		if (otherNames != null && !otherNames.isEmpty()) {
			OtherNames otherNamesJAXB = new OtherNames();
			for (String otherName : otherNames) {
				otherNamesJAXB.getOtherName().add(otherName);
			}
			personalDetails.setOtherNames(otherNamesJAXB);
		}

		bioJAXB.setPersonalDetails(personalDetails);

		// start biography
		if (StringUtils.isNotBlank(biography)) {
			Biography bio = new Biography();
			bio.setValue(biography);
		}

		// start researcher-urls and external-identifiers
		ResearcherUrls researcherUrls = new ResearcherUrls();
		ExternalIdentifiers externalIdentifiers = new ExternalIdentifiers();
		boolean buildResearcherUrls = false;
		boolean buildExternalIdentifier = false;
		for (String key : metadata.keySet()) {
			if (key.startsWith("researcher-url-") || key.startsWith("external-identifier-")) {
				RPPropertiesDefinition rpPD = applicationService.findPropertiesDefinitionByShortName(
						RPPropertiesDefinition.class, orcidMetadataConfiguration.get(key));

				if (key.startsWith("researcher-url-")) {
					for (String value : metadata.get(key)) {
						if (StringUtils.isNotBlank(value)) {
							ResearcherUrl researcherUrl = new ResearcherUrl();
							researcherUrl.setUrlName(rpPD.getLabel());
							Url url = new Url();
							url.setValue(value);
							researcherUrl.getUrl();
							researcherUrls.getResearcherUrl().add(researcherUrl);
							buildResearcherUrls = true;
						}
					}
				}
				if (key.startsWith("external-identifier-")) {
					for (String value : metadata.get(key)) {
						if (StringUtils.isNotBlank(value)) {
							ExternalIdentifier externalIdentifier = new ExternalIdentifier();
							ExternalIdCommonName commonName = new ExternalIdCommonName();
							commonName.setContent(rpPD.getLabel());
							externalIdentifier.setExternalIdCommonName(commonName);
							ExternalIdReference externalIdReference = new ExternalIdReference();
							externalIdReference.setContent(value);
							externalIdentifier.setExternalIdReference(externalIdReference);
							ExternalIdUrl externalIdUrl = new ExternalIdUrl();
							externalIdUrl.setValue(value);
							externalIdentifier.setExternalIdUrl(externalIdUrl);
							externalIdentifiers.getExternalIdentifier().add(externalIdentifier);
							buildExternalIdentifier = true;
						}
					}
				}
			}
		}
		if (buildResearcherUrls) {
			bioJAXB.setResearcherUrls(researcherUrls);
		}
		if (buildExternalIdentifier) {
			bioJAXB.setExternalIdentifiers(externalIdentifiers);
		}

		// start contact details
		ContactDetails contactDetailsJAXB = new ContactDetails();

		List<Email> emailsJAXB = contactDetailsJAXB.getEmail();

		boolean buildContactDetails = false;
		if (StringUtils.isNotBlank(primaryEmail)) {
			Email emailJAXB = new Email();
			emailJAXB.setPrimary(true);
			emailJAXB.setValue(primaryEmail);
			emailsJAXB.add(emailJAXB);
			buildContactDetails = true;
		}

		if (emails != null) {
			for (String e : emails) {
				if (StringUtils.isNotBlank(e)) {
					Email ee = new Email();
					ee.setValue(e);
					emailsJAXB.add(ee);
					buildContactDetails = true;
				}
			}
		}

		if (StringUtils.isNotBlank(country)) {
			Address addressJAXB = new Address();
			Country countryJAXB = new Country();
			countryJAXB.setValue(country);
			addressJAXB.setCountry(countryJAXB);
			contactDetailsJAXB.setAddress(addressJAXB);
			buildContactDetails = true;
		}

		if (buildContactDetails) {
			bioJAXB.setContactDetails(contactDetailsJAXB);
		}

		// start keywords
		boolean buildKeywords = false;
		Keywords keywordsJAXB = new Keywords();
		if (keywords != null) {
			for (String kk : keywords) {
				if (StringUtils.isNotBlank(kk)) {
					Keyword k = new Keyword();
					k.setContent(kk);
					keywordsJAXB.getKeyword().add(k);
					buildKeywords = true;
				}
			}
		}
		if (buildKeywords) {
			bioJAXB.setKeywords(keywordsJAXB);
		}

		profile.setOrcidBio(bioJAXB);
		return profile;
	}

	private static String getFamilyName(String text) {
		DCPersonName tmpPersonName = new DCPersonName(text);

		if (StringUtils.isNotBlank(tmpPersonName.getLastName())) {
			return tmpPersonName.getLastName();
		}
		return "";
	}

	private static String getGivenName(String text) {
		DCPersonName tmpPersonName = new DCPersonName(text);

		if (StringUtils.isNotBlank(tmpPersonName.getFirstNames())) {
			return tmpPersonName.getFirstNames();
		}
		return "";
	}

	public static String getTokenReleasedForSync(ResearcherPage researcher, String tokenName) {
		return ResearcherPageUtils.getStringValue(researcher, tokenName);
	}

	public static boolean sendOrcidQueue(ApplicationService applicationService, OrcidQueue orcidQueue) {
		boolean result = false;
		if (orcidQueue != null) {
			String owner = orcidQueue.getOwner();
			String uuid = orcidQueue.getFastlookupUuid();
			switch (orcidQueue.getTypeId()) {
			case CrisConstants.RP_TYPE_ID:
				result = sendOrcidProfile(applicationService, owner, uuid);
				break;
			case CrisConstants.PROJECT_TYPE_ID:
				result = sendOrcidFunding(applicationService, owner, uuid);
				break;
			default:
				result = sendOrcidWork(applicationService, owner, uuid);
				break;
			}
		}
		return result;
	}

	public static boolean sendOrcidWork(ApplicationService applicationService, String crisId, String uuid) {
		boolean result = false;
		log.debug("Create DSpace context");
		Context context = null;
		try {
			ResearcherPage researcher = (ResearcherPage) applicationService.getEntityByCrisId(crisId,
					ResearcherPage.class);

			context = new Context();
			context.turnOffAuthorisationSystem();

			OrcidService orcidService = OrcidService.getOrcid();

			log.info("Starts update ORCID Profile for:" + crisId);

			String orcid = null;
			for (RPProperty propOrcid : researcher.getAnagrafica4view().get("orcid")) {
				orcid = propOrcid.toString();
				break;
			}

			if (StringUtils.isNotBlank(orcid)) {
				log.info("Prepare push for Work:" + uuid + " for ResearcherPage crisID:" + crisId + " AND orcid iD:"
						+ orcid);
				OrcidHistory orcidHistory = null;
				try {
					String tokenCreateWork = PushToORCID.getTokenReleasedForSync(researcher,
							"system-orcid-token-orcid-works-update");
					DSpaceObject dso = HandleManager.resolveToObject(context, uuid);

					orcidHistory = applicationService.uniqueOrcidHistoryByOwnerAndEntityIdAndTypeId(
							researcher.getCrisID(), dso.getID(), dso.getType());
					if (tokenCreateWork != null) {
						if (orcidHistory == null) {
							orcidHistory = new OrcidHistory();
							orcidHistory.setEntityId(dso.getID());
							orcidHistory.setEntityUuid(dso.getHandle());
							orcidHistory.setTypeId(dso.getType());
							orcidHistory.setOwner(researcher.getCrisID());
						}
						SingleTimeStampInfo timestampAttempt = new SingleTimeStampInfo(new Date());
						orcidHistory.setTimestampLastAttempt(timestampAttempt);

						log.info("(Q1)Prepare for Work:" + uuid + " for ResearcherPage crisID:" + crisId);
						OrcidWork work = PushToORCID.buildOrcidWork(context, dso.getID());
						if (work != null) {
							try {
								orcidService.appendWork(orcid, tokenCreateWork, work);
								result = true;
								orcidHistory.setTimestampSuccessAttempt(timestampAttempt);
								log.info("(A1) OK for Work:" + uuid + " for ResearcherPage crisID:" + crisId);
							} catch (Exception ex) {
								// build message for orcid history
								orcidHistory.setResponseMessage(ex.getMessage());
								log.error("ERROR!!! (E1) ERROR for Work:" + uuid + " for ResearcherPage crisID:"
										+ crisId);
							}
							applicationService.saveOrUpdate(OrcidHistory.class, orcidHistory);
							log.info("(A2) OK OrcidHistory for Work:" + uuid + " for ResearcherPage crisID:" + crisId);
						}
					}

				} catch (Exception ex) {
					log.info("ERROR!!! (E1) ERROR for Work:" + uuid + " for ResearcherPage crisID:" + crisId);
					log.error(ex.getMessage());
					if (orcidHistory != null) {
						orcidHistory.setResponseMessage(ex.getMessage());
						applicationService.saveOrUpdate(OrcidHistory.class, orcidHistory);
					}
				}
			} else {
				log.warn("WARNING!!! (W0) Orcid not found for ResearcherPage:" + crisId);
			}
			log.info("Ends append ORCID  Work:" + uuid + " for ResearcherPage crisID:" + crisId);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (context != null && context.isValid()) {
				context.abort();
			}
		}

		if (result) {
			applicationService.deleteOrcidQueueByOwnerAndUuid(crisId, uuid);
		}
		return result;
	}

	public static boolean sendOrcidProfile(ApplicationService applicationService, String crisId, String uuid) {
		boolean result = false;
		try {
			Map<String, Map<String, List<String>>> mapResearcherMetadataToSend = new HashMap<String, Map<String, List<String>>>();
			Map<String, String> orcidConfigurationMapping = PushToORCID
					.prepareConfigurationMappingForProfile(applicationService);
			ResearcherPage researcher = (ResearcherPage) applicationService.getEntityByCrisId(crisId,
					ResearcherPage.class);
			PushToORCID.prepareUpdateProfile(applicationService, mapResearcherMetadataToSend, researcher, true);

			OrcidService orcidService = OrcidService.getOrcid();

			log.info("Starts update ORCID Profile for:" + crisId);

			String orcid = null;
			for (RPProperty propOrcid : researcher.getAnagrafica4view().get("orcid")) {
				orcid = propOrcid.toString();
				break;
			}

			if (StringUtils.isNotBlank(orcid)) {
				log.info("Prepare push for ResearcherPage crisID:" + crisId + " AND orcid iD:" + orcid);
				OrcidHistory orcidHistory = null;
				try {
					String tokenUpdateBio = getTokenReleasedForSync(researcher, "system-orcid-token-orcid-bio-update");
					orcidHistory = applicationService.uniqueOrcidHistoryByOwnerAndEntityIdAndTypeId(
							researcher.getCrisID(), researcher.getID(), researcher.getType());

					if (tokenUpdateBio != null) {

						if (orcidHistory == null) {
							orcidHistory = new OrcidHistory();
							orcidHistory.setEntityId(researcher.getId());
							orcidHistory.setEntityUuid(researcher.getUuid());
							orcidHistory.setTypeId(researcher.getType());
							orcidHistory.setOwner(researcher.getCrisID());
						}
						SingleTimeStampInfo timestampAttempt = new SingleTimeStampInfo(new Date());
						orcidHistory.setTimestampLastAttempt(timestampAttempt);

						log.info("(Q1)Prepare OrcidProfile for ResearcherPage crisID:" + crisId);
						OrcidProfile profile = PushToORCID.buildOrcidProfile(applicationService, crisId,
								mapResearcherMetadataToSend, orcidConfigurationMapping);
						if (profile != null) {
							try {
								OrcidMessage message = orcidService.updateBio(orcid, tokenUpdateBio, profile);
								if (message.getErrorDesc() != null) {
									throw new RuntimeException(message.getErrorDesc().getContent());
								} else {
									result = true;
									orcidHistory.setTimestampSuccessAttempt(timestampAttempt);
									log.info("(A1) OK OrcidProfile for ResearcherPage crisID:" + crisId);
								}
							} catch (Exception ex) {
								// build message for orcid history
								orcidHistory.setResponseMessage(ex.getMessage());
								log.error("ERROR!!! (E1) ERROR OrcidProfile ResearcherPage crisID:" + crisId);
							}
							applicationService.saveOrUpdate(OrcidHistory.class, orcidHistory);
							log.info("(A2) OK OrcidHistory for ResearcherPage crisID:" + crisId);
						}
					} else {
						log.warn("WARNING!!! (W1) Token not released for:" + crisId);
					}

				} catch (Exception ex) {
					log.error("ERROR!!! (E2) ERROR OrcidProfile ResearcherPage crisID:" + crisId, ex);
					if (orcidHistory != null) {
						orcidHistory.setResponseMessage(ex.getMessage());
						applicationService.saveOrUpdate(OrcidHistory.class, orcidHistory);
					}
				}
			} else {
				log.warn("WARNING!!! (W0) Orcid not found for:" + crisId);
			}
			log.info("Ends update ORCID Profile for:" + crisId);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		if (result && StringUtils.isNotBlank(uuid)) {
			applicationService.deleteOrcidQueueByOwnerAndUuid(crisId, uuid);
		}
		return result;
	}

	public static boolean sendOrcidFunding(ApplicationService applicationService, String crisId, String uuid) {
		boolean result = false;
		Context context = null;
		try {
			ResearcherPage researcher = (ResearcherPage) applicationService.getEntityByCrisId(crisId,
					ResearcherPage.class);

			context = new Context();
			context.turnOffAuthorisationSystem();

			OrcidService orcidService = OrcidService.getOrcid();

			log.info("Starts update ORCID Profile for:" + crisId);

			String orcid = null;
			for (RPProperty propOrcid : researcher.getAnagrafica4view().get("orcid")) {
				orcid = propOrcid.toString();
				break;
			}

			if (StringUtils.isNotBlank(orcid)) {
				log.info("Prepare push for Funding:" + uuid + " for ResearcherPage crisID:" + crisId + " AND orcid iD:"
						+ orcid);
				OrcidHistory orcidHistory = null;
				try {
					String tokenCreateFunding = getTokenReleasedForSync(researcher,
							"system-orcid-token-funding-update");

					Project project = (Project) applicationService.getEntityByUUID(uuid);
					orcidHistory = applicationService.uniqueOrcidHistoryByOwnerAndEntityIdAndTypeId(
							researcher.getCrisID(), project.getID(), project.getType());
					if (tokenCreateFunding != null) {

						if (orcidHistory == null) {
							orcidHistory = new OrcidHistory();
							orcidHistory.setEntityId(project.getId());
							orcidHistory.setEntityUuid(project.getUuid());
							orcidHistory.setTypeId(project.getType());
							orcidHistory.setOwner(researcher.getCrisID());
						}
						SingleTimeStampInfo timestampAttempt = new SingleTimeStampInfo(new Date());
						orcidHistory.setTimestampLastAttempt(timestampAttempt);

						log.info("(Q1)Prepare Funding:" + uuid + " for ResearcherPage crisID:" + crisId);
						Funding funding = PushToORCID.buildOrcidFunding(context, applicationService, project);
						if (funding != null) {
							try {
								orcidService.appendFunding(orcid, tokenCreateFunding, funding);
								result = true;
								orcidHistory.setTimestampSuccessAttempt(timestampAttempt);
								log.info("(A1) OK for Funding:" + uuid + " for ResearcherPage crisID:" + crisId);
							} catch (Exception ex) {
								// build message for orcid history
								orcidHistory.setResponseMessage(ex.getMessage());
								log.error("ERROR!!! (E1) ERROR for Funding:" + uuid + " for ResearcherPage crisID:"
										+ crisId);
							}
							applicationService.saveOrUpdate(OrcidHistory.class, orcidHistory);
							log.info("(A2) OK OrcidHistory for for Funding:" + uuid + " for ResearcherPage crisID:"
									+ crisId);
						}
					} else {
						log.warn("WARNING!!! (W1) Token not released for:" + crisId);
					}

				} catch (Exception ex) {
					log.error("ERROR!!! (E2) ERROR for Funding:" + uuid + " for ResearcherPage crisID:" + crisId, ex);
					if (orcidHistory != null) {
						orcidHistory.setResponseMessage(ex.getMessage());
						applicationService.saveOrUpdate(OrcidHistory.class, orcidHistory);
					}
				}
			} else {
				log.warn("WARNING!!! (W0) Orcid not found for:" + crisId);
			}
			log.info("Ends update ORCID Funding:" + uuid + " for ResearcherPage crisID:" + crisId);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (context != null && context.isValid()) {
				context.abort();
			}
		}

		if (result) {
			applicationService.deleteOrcidQueueByOwnerAndUuid(crisId, uuid);
		}
		return result;
	}

	public static boolean putOrcidProfile(ApplicationService applicationService, String owner) {
		return sendOrcidProfile(applicationService, owner, null);
	}

	public static boolean putOrcidFundings(ApplicationService applicationService, String owner,
			List<Integer> projects) {
		boolean result = false;
		log.debug("Create DSpace context");
		Context context = null;
		try {
			ResearcherPage researcher = (ResearcherPage) applicationService.getEntityByCrisId(owner,
					ResearcherPage.class);

			context = new Context();
			context.turnOffAuthorisationSystem();

			OrcidService orcidService = OrcidService.getOrcid();

			log.info("Starts update ORCID Profile for:" + owner);

			String orcid = null;
			for (RPProperty propOrcid : researcher.getAnagrafica4view().get("orcid")) {
				orcid = propOrcid.toString();
				break;
			}

			if (StringUtils.isNotBlank(orcid)) {
				log.info("Prepare put for Works for ResearcherPage crisID:" + owner + " AND orcid iD:" + orcid);

				try {
					String tokenCreateWork = PushToORCID.getTokenReleasedForSync(researcher,
							"system-orcid-token-funding-create");
					if (tokenCreateWork != null) {

						Map<String, List<Integer>> mapProjectsToSend = new HashMap<String, List<Integer>>();
						mapProjectsToSend.put(owner, projects);
						FundingList works = PushToORCID.buildOrcidFundings(context, applicationService, owner,
								mapProjectsToSend);
						String error = null;

						if (works != null) {
							try {
								orcidService.putFundings(orcid, tokenCreateWork, works);
								result = true;
								log.info("(A1) OK for put Works for ResearcherPage crisID:" + owner);
							} catch (RuntimeException ex) {
								// build message for orcid history
								error = ex.getMessage();
								log.error("ERROR!!! (E1) ERROR for put Works for ResearcherPage crisID:" + owner);
							}

							for (Integer i : projects) {
								Project project = applicationService.get(Project.class, i);
								OrcidHistory orcidHistory = applicationService
										.uniqueOrcidHistoryByOwnerAndEntityIdAndTypeId(owner, i,
												CrisConstants.PROJECT_TYPE_ID);
								if (orcidHistory == null) {
									orcidHistory = new OrcidHistory();
								}
								orcidHistory.setEntityId(i);
								orcidHistory.setEntityUuid(project.getUuid());
								orcidHistory.setTypeId(CrisConstants.PROJECT_TYPE_ID);
								orcidHistory.setOwner(owner);
								SingleTimeStampInfo timestampAttempt = new SingleTimeStampInfo(new Date());
								orcidHistory.setTimestampLastAttempt(timestampAttempt);
								if (StringUtils.isNotBlank(error)) {
									orcidHistory.setResponseMessage(error);
								} else {
									orcidHistory.setTimestampSuccessAttempt(timestampAttempt);
								}
								applicationService.saveOrUpdate(OrcidHistory.class, orcidHistory);
							}

							log.info("(A2) OK OrcidHistory for put Works for ResearcherPage crisID:" + owner);
						}
					}

				} catch (Exception ex) {
					log.info("ERROR!!! (E1) ERROR for put Works for ResearcherPage crisID:" + owner);
					log.error(ex.getMessage());
				}
			} else {
				log.warn("WARNING!!! (W0) Orcid not found for ResearcherPage:" + owner);
			}
			log.info("Ends append ORCID  Works for ResearcherPage crisID:" + owner);
			applicationService.saveOrUpdate(ResearcherPage.class, researcher, false);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (context != null && context.isValid()) {
				context.abort();
			}
		}
		return result;
	}

	public static boolean putOrcidWorks(ApplicationService applicationService, String owner, List<Integer> items) {
		boolean result = false;
		log.debug("Create DSpace context");
		Context context = null;
		try {
			ResearcherPage researcher = (ResearcherPage) applicationService.getEntityByCrisId(owner,
					ResearcherPage.class);

			context = new Context();
			context.turnOffAuthorisationSystem();

			OrcidService orcidService = OrcidService.getOrcid();

			log.info("Starts update ORCID Profile for:" + owner);

			String orcid = null;
			for (RPProperty propOrcid : researcher.getAnagrafica4view().get("orcid")) {
				orcid = propOrcid.toString();
				break;
			}

			if (StringUtils.isNotBlank(orcid)) {
				log.info("Prepare put for Works for ResearcherPage crisID:" + owner + " AND orcid iD:" + orcid);

				try {
					String tokenCreateWork = PushToORCID.getTokenReleasedForSync(researcher,
							"system-orcid-token-orcid-works-create");
					if (tokenCreateWork != null) {

						Map<String, List<Integer>> mapPublicationsToSend = new HashMap<String, List<Integer>>();
						mapPublicationsToSend.put(owner, items);
						OrcidWorks works = PushToORCID.buildOrcidWorks(context, owner, mapPublicationsToSend);
						String error = null;

						if (works != null) {
							try {
								orcidService.putWorks(orcid, tokenCreateWork, works);
								result = true;
								log.info("(A1) OK for put Works for ResearcherPage crisID:" + owner);
							} catch (RuntimeException ex) {
								// build message for orcid history
								error = ex.getMessage();
								log.error("ERROR!!! (E1) ERROR for put Works for ResearcherPage crisID:" + owner);
							}

							for (Integer i : items) {
								Item item = Item.find(context, i);
								OrcidHistory orcidHistory = applicationService
										.uniqueOrcidHistoryByOwnerAndEntityIdAndTypeId(owner, i, Constants.ITEM);
								if (orcidHistory == null) {
									orcidHistory = new OrcidHistory();
								}
								orcidHistory.setEntityId(i);
								orcidHistory.setEntityUuid(item.getHandle());
								orcidHistory.setTypeId(Constants.ITEM);
								orcidHistory.setOwner(owner);
								SingleTimeStampInfo timestampAttempt = new SingleTimeStampInfo(new Date());
								orcidHistory.setTimestampLastAttempt(timestampAttempt);
								if (StringUtils.isNotBlank(error)) {
									orcidHistory.setResponseMessage(error);
								} else {
									orcidHistory.setTimestampSuccessAttempt(timestampAttempt);
								}
								applicationService.saveOrUpdate(OrcidHistory.class, orcidHistory);
							}

							log.info("(A2) OK OrcidHistory for put Works for ResearcherPage crisID:" + owner);
						}
					}

				} catch (Exception ex) {
					log.info("ERROR!!! (E1) ERROR for put Works for ResearcherPage crisID:" + owner);
					log.error(ex.getMessage());
				}
			} else {
				log.warn("WARNING!!! (W0) Orcid not found for ResearcherPage:" + owner);
			}
			log.info("Ends append ORCID  Works for ResearcherPage crisID:" + owner);
			applicationService.saveOrUpdate(ResearcherPage.class, researcher, false);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (context != null && context.isValid()) {
				context.abort();
			}
		}
		return result;
	}

}
