/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.06.16 at 06:33:14 PM CEST 
//


package org.dspace.authority.orcid.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.orcid.org/ns/orcid}non-empty-string"/>
 *         &lt;element name="address" type="{http://www.orcid.org/ns/orcid}organization-address"/>
 *         &lt;element name="disambiguated-organization" type="{http://www.orcid.org/ns/orcid}disambiguated-organization" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "name",
    "address",
    "disambiguatedOrganization"
})
@XmlRootElement(name = "organization")
public class Organization {

    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    protected OrganizationAddress address;
    @XmlElement(name = "disambiguated-organization")
    protected DisambiguatedOrganization disambiguatedOrganization;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the address property.
     * 
     * @return
     *     possible object is
     *     {@link OrganizationAddress }
     *     
     */
    public OrganizationAddress getAddress() {
        return address;
    }

    /**
     * Sets the value of the address property.
     * 
     * @param value
     *     allowed object is
     *     {@link OrganizationAddress }
     *     
     */
    public void setAddress(OrganizationAddress value) {
        this.address = value;
    }

    /**
     * Gets the value of the disambiguatedOrganization property.
     * 
     * @return
     *     possible object is
     *     {@link DisambiguatedOrganization }
     *     
     */
    public DisambiguatedOrganization getDisambiguatedOrganization() {
        return disambiguatedOrganization;
    }

    /**
     * Sets the value of the disambiguatedOrganization property.
     * 
     * @param value
     *     allowed object is
     *     {@link DisambiguatedOrganization }
     *     
     */
    public void setDisambiguatedOrganization(DisambiguatedOrganization value) {
        this.disambiguatedOrganization = value;
    }

}
