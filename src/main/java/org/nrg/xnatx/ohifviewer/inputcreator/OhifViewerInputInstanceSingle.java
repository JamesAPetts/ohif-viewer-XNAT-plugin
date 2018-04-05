/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nrg.xnatx.ohifviewer.inputcreator;

import etherj.dicom.Series;
import etherj.dicom.SopInstance;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jpetts
 */
public class OhifViewerInputInstanceSingle extends OhifViewerInputInstance {
  private String url;
  
  private static final Logger logger = LoggerFactory.getLogger(OhifViewerInputInstanceSingle.class);
  
  public OhifViewerInputInstanceSingle(SopInstance sop, Series ser, String xnatScanUrl, String scanId)
  {
    super(sop, ser, xnatScanUrl, scanId);
    String file = new File(sop.getPath()).getName();
    String sopClassUid = sop.getSopClassUid();    
    String resource = getResourceType(sopClassUid);
    
    xnatScanUrl = selectCorrectProtocol(xnatScanUrl, sopClassUid);
    
    String urlString = xnatScanUrl + scanId + RESOURCES + resource + FILES + file;
    
    setUrl(urlString);
  }
  
  private String selectCorrectProtocol(String xnatScanUrl, String sopClassUid)
  {
    try
    {
      xnatScanUrl = selectProtocol(xnatScanUrl, sopClassUid);
    }
    catch (Exception ex)
    {
      logger.error(ex.getMessage());
    }
    
    return xnatScanUrl;
  }
  
  private String selectProtocol(String xnatScanUrl, String sopClassUid)
  throws Exception
  {
    //Elegance please James...
    //TODO: Use URL type and just replace protocol with dicomweb

    if (xnatScanUrl.contains("https"))
    {
      return xnatScanUrl.replace("https", "dicomweb");
    }
    else if (xnatScanUrl.contains("http"))
    {
      return xnatScanUrl.replace("http", "dicomweb");
    }
    else
    {
      throw new Exception("unrecognised protocol in xnat url");
    }

  }
  
  public String getUrl()
	{
		return url;
	}

	private void setUrl(String url)
	{
		this.url = url;
	}
  
}
