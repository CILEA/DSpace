package org.dspace.app.cris.batch.dto;

import java.io.File;

public interface BitstreamInterface
{
    public String getFilepath();
   
    public String getDescription();
 
    public Integer getBitstream_order();
 
    public Integer getPkey(); 

    public Boolean getPrimary_bitstream();
    
    public String getBundle();
    
    public Integer getAssetstore();
    
    public String getName();
    
    public Integer getEmbargoPolicy();
    
    public String getEmbargoStartDate();
    
    public File getBlob();
    
    public String getTypeAttachment();
}
