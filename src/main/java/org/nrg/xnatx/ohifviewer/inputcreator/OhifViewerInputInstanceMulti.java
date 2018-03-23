/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nrg.xnatx.ohifviewer.inputcreator;

import etherj.dicom.Series;
import etherj.dicom.SopInstance;
import java.io.File;
import static org.nrg.xnatx.ohifviewer.inputcreator.OhifViewerInputInstance.RESOURCES;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jpetts
 */
public class OhifViewerInputInstanceMulti extends OhifViewerInputInstance {
  private String wadouri;
  
  // TEMP
  private static final Logger logger = LoggerFactory.getLogger(CreateOhifViewerMetadata.class);
  // TEMP
  
  public OhifViewerInputInstanceMulti(SopInstance sop, Series ser, String xnatScanUrl, String scanId)
  {
    super(sop, ser, xnatScanUrl, scanId);
    String file = new File(sop.getPath()).getName();
    String sopClassUid = sop.getSopClassUid();    
    String resource = getResourceType(sopClassUid);
    
    String urlString = xnatScanUrl + scanId + RESOURCES + resource + FILES + file;
    
    logger.error("seriesId: " + scanId);
    logger.error("resource: " + resource);
    logger.error("urlString: " + urlString);
    
    setWadouri(urlString);
  }

  
  public String getWadouri()
	{
		return wadouri;
	}

	private void setWadouri(String wadouri)
	{
		this.wadouri = wadouri;
	}
}
