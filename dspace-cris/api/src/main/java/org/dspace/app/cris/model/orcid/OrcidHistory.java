/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * https://github.com/CILEA/dspace-cris/wiki/License
 */
package org.dspace.app.cris.model.orcid;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import it.cilea.osd.common.core.SingleTimeStampInfo;
import it.cilea.osd.common.model.IdentifiableObject;

@Entity
@Table(name = "cris_orcid_history")
@NamedQueries({
    @NamedQuery(name = "OrcidHistory.findAll", query = "from OrcidHistory order by id"),
    @NamedQuery(name = "OrcidHistory.findOrcidHistoryByResearcherId", query = "from OrcidHistory where entityId = ? and typeId = 9 order by id"),
    @NamedQuery(name = "OrcidHistory.findOrcidHistoryByProjectId", query = "from OrcidHistory where entityId = ? and typeId = 10 order by id"),
    @NamedQuery(name = "OrcidHistory.findOrcidHistoryByPublicationId", query = "from OrcidHistory where entityId = ? and typeId = 2 order by id"),
    @NamedQuery(name = "OrcidHistory.findOrcidHistoryByEntityIdAndTypeId", query = "from OrcidHistory where entityId = ? and typeId = ? order by id"),
    @NamedQuery(name = "OrcidHistory.findOrcidHistoryInSuccess", query = "from OrcidHistory where timestampLastAttempt.timestamp = timestampSuccessAttempt.timestamp order by id"),
    @NamedQuery(name = "OrcidHistory.findOrcidHistoryInError", query = "from OrcidHistory where timestampLastAttempt.timestamp > timestampSuccessAttempt.timestamp order by id"),
    @NamedQuery(name = "OrcidHistory.findOrcidHistoryInSuccessByOwner", query = "from OrcidHistory where owner = ? and timestampLastAttempt.timestamp = timestampSuccessAttempt.timestamp order by id"),
    @NamedQuery(name = "OrcidHistory.findOrcidHistoryInSuccessByOwnerAndTypeId", query = "from OrcidHistory where owner = ? and typeId = ? and timestampLastAttempt.timestamp = timestampSuccessAttempt.timestamp order by id"),
    @NamedQuery(name = "OrcidHistory.uniqueOrcidHistoryInSuccessByOwnerAndEntityIdAndTypeId", query = "from OrcidHistory where owner = ? and entityId = ? and typeId = ? and timestampLastAttempt.timestamp = timestampSuccessAttempt.timestamp"),
    @NamedQuery(name = "OrcidHistory.uniqueOrcidHistoryByOwnerAndEntityIdAndTypeId", query = "from OrcidHistory where owner = ? and entityId = ? and typeId = ?")    
})
public class OrcidHistory extends IdentifiableObject {
    
	/** DB Primary key */
    @Id
    @GeneratedValue(generator = "CRIS_ORCIDHISTORY_SEQ")
    @SequenceGenerator(name = "CRIS_ORCIDHISTORY_SEQ", sequenceName = "CRIS_ORCIDHISTORY_SEQ", allocationSize = 1)
    private Integer id;
    
    private Integer entityId;
    
    private Integer typeId;
    
    private String entityUuid;
    
    private String owner;
    
    @Embedded
    @AttributeOverride(name = "timestamp", column = @Column(name = "lastAttempt"))
    private SingleTimeStampInfo timestampLastAttempt;

    @Embedded
    @AttributeOverride(name = "timestamp", column = @Column(name = "lastSuccess"))
    private SingleTimeStampInfo timestampSuccessAttempt;
        
    @Type(type="org.hibernate.type.StringClobType")
    private String responseMessage;
    
	@Override
	public Integer getId() {
		return id;
	}
	
    public void setId(Integer id)
    {
        this.id = id;
    }

	public SingleTimeStampInfo getTimestampLastAttempt() {
		return timestampLastAttempt;
	}

	public void setTimestampLastAttempt(SingleTimeStampInfo timestampLastAttempt) {
		this.timestampLastAttempt = timestampLastAttempt;
	}

	public SingleTimeStampInfo getTimestampSuccessAttempt() {
		return timestampSuccessAttempt;
	}

	public void setTimestampSuccessAttempt(SingleTimeStampInfo timestampSuccessAttempt) {
		this.timestampSuccessAttempt = timestampSuccessAttempt;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}

	public Integer getEntityId() {
		return entityId;
	}

	public void setEntityId(Integer entityId) {
		this.entityId = entityId;
	}

	public Integer getTypeId() {
		return typeId;
	}

	public void setTypeId(Integer typeId) {
		this.typeId = typeId;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getEntityUuid() {
		return entityUuid;
	}

	public void setEntityUuid(String entityUuid) {
		this.entityUuid = entityUuid;
	}
}
