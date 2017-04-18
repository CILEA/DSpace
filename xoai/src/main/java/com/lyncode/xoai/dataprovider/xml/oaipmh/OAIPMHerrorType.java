//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.2-147 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.01.13 at 08:24:23 PM WET 
//

package com.lyncode.xoai.dataprovider.xml.oaipmh;

import com.lyncode.xoai.dataprovider.exceptions.WritingXmlException;
import com.lyncode.xoai.dataprovider.xml.XMLWritable;
import com.lyncode.xoai.dataprovider.xml.XmlOutputContext;

import javax.xml.bind.annotation.*;
import javax.xml.stream.XMLStreamException;

/**
 *
 * Java class for OAI-PMHerrorType complex type.
 *
 *
 * The following schema fragment specifies the expected content contained within
 * this class.
 *









 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OAI-PMHerrorType", propOrder = {"value"})
public class OAIPMHerrorType implements XMLWritable {

    @XmlValue
    protected String value;
    @XmlAttribute(name = "code", required = true)
    protected OAIPMHerrorcodeType code;

    /**
     * Gets the value of the value property.
     *
     * @return possible object is ;
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     *
     * @param value allowed object is ;
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the code property.
     *
     * @return possible object is ;
     */
    public OAIPMHerrorcodeType getCode() {
        return code;
    }

    /**
     * Sets the value of the code property.
     *
     * @param value allowed object is ;
     */
    public void setCode(OAIPMHerrorcodeType value) {
        this.code = value;
    }

    @Override
    public void write(XmlOutputContext context) throws WritingXmlException {
        try {
            if (this.code != null)
                context.getWriter().writeAttribute("code", this.code.value());

            if (this.value != null)
                context.getWriter().writeCharacters(value);
        } catch (XMLStreamException e) {
            throw new WritingXmlException(e);
        }
    }

}