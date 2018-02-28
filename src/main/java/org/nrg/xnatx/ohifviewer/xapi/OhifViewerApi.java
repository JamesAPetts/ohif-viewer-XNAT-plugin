package org.nrg.xnatx.ohifviewer.xapi;

import exceptions.XMLException;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Api;
import org.apache.commons.io.IOUtils;
import org.apache.commons.codec.binary.Base64;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.nrg.xdat.XDAT;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import xmlUtilities.XMLUtilities;
import org.nrg.xft.security.UserI;
import org.nrg.xdat.entities.UserAuthI;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;

import java.util.ArrayList;


import org.nrg.xdat.om.XnatSubjectassessordata;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;


@Api("Get and set viewer metadata.")
@XapiRestController
@RequestMapping(value = "/viewer")
public class OhifViewerApi {
    private static final Logger logger = LoggerFactory.getLogger(OhifViewerApi.class);
    private static final String SEP = File.separator;

    @ApiOperation(value = "Returns the session JSON for the specified experiment ID.")
    @ApiResponses({
      @ApiResponse(code = 200, message = "The session was located and properly rendered to JSON."),
      @ApiResponse(code = 403, message = "The user does not have permission to view the indicated experiment."),
      @ApiResponse(code = 500, message = "An unexpected error occurred.")
    })
    @XapiRequestMapping(value = "{_experimentId}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public StreamingResponseBody getSessionJson(final @PathVariable String _experimentId) throws FileNotFoundException {
        
      // Grab the data archive path
      String xnatArchivePath = XDAT.getSiteConfigPreferences().getArchivePath();
      
      // Get Experiment data and Project data from the experimentId
      XnatExperimentdata expData = XnatExperimentdata.getXnatExperimentdatasById(_experimentId, null, false);
      XnatProjectdata projData = expData.getProjectData();
      
      // Get the subjectId in order to get the Subject data
      ItemI imageSessionData = getSubItem((ItemI) expData, "xnat:imageSessionData");
      ItemI subjectAssessorData = getSubItem(imageSessionData, "xnat:subjectAssessorData");
      
      String _subjectId = null;
      try
      {
        _subjectId = subjectAssessorData.getProperty("subject_id").toString();
      }
      catch (Exception ex)
      {
        logger.error(ex.getMessage());
      }

      // Get the subject data
      XnatSubjectdata subjData = XnatSubjectdata.getXnatSubjectdatasById(_subjectId, null, false);
      
      
      String expLabel = expData.getArchiveDirectoryName();
      String proj = projData.getId();
      String subjectLabel = subjData.getLabel();
      
      logger.error("expLabel: " + expLabel);
      logger.error("proj: " + proj);
      logger.error("subjectLabel: " + subjectLabel);


      String filePath = xnatArchivePath + SEP + proj + SEP + "arc001"
      + SEP + expLabel + SEP + "RESOURCES/metadata/" + _experimentId +".json";

      logger.error("filePath: " + filePath);
      final Reader reader = new FileReader(filePath);

      return new StreamingResponseBody() {
          @Override
          public void writeTo(final OutputStream output) throws IOException {
              IOUtils.copy(reader, output);
          }
      };
    }
    
    
    @ApiOperation(value = "Generates the session JSON for the specified experiment ID.")
    @ApiResponses({
      @ApiResponse(code = 201, message = "The session JSON has been created."),
      @ApiResponse(code = 403, message = "The user does not have permission to view the indicated experient."),
      @ApiResponse(code = 500, message = "An unexpected error occurred.")
    })
    @XapiRequestMapping(value = "{expId}", method = RequestMethod.POST)
    public ResponseEntity<String> setSessionJson(final @PathVariable String expId) throws IOException {
      
      // Grab the data archive path
      String rootURL          = XDAT.getSiteConfigPreferences().getSiteUrl();
      String xnatArchivePath  = XDAT.getSiteConfigPreferences().getArchivePath();
      // Perform a REST call to grab the proj and expLabel        
      String urlString = rootURL + "/data/archive/experiments/" + expId;
      /*
      logger.info(urlString);

      HashMap<String,String> experimentData = getProjAndExpLabel(urlString);
      String proj     = experimentData.get("proj");
      String expLabel = experimentData.get("expLabel");
      String subj     = experimentData.get("subj");
      
      String xnatScanPath = xnatArchivePath + SEP + proj
        + SEP + "arc001" + SEP + expLabel + SEP + "SCANS";

      String xnatScanUrl  = rootURL.replace("http", "dicomweb")
        + "/data/archive/projects/" + proj
        + "/subjects/" + subj
        + "/experiments/" + expId
        + "/scans/";
      
      /*
      
      // TODO
      // Call Simons code ala:
      // CreateOhifViewerInputJson jsonCreator = new CreateOhifViewerInputJson();
      
      // TODO Maybe make his class smaller? I don't need most of it,
      //      And perfom the bellow methodology in his class
      
      // TODO: change bellow to return string
      
      //String jsonString = jsonCreator.jsonify(xnatScanPath,xnatScanUrl,expId)
      
      // D) Write to file at xnatScanPath!
      
      // E) Have a well deserved beer
      */
      // TEMP TEMP TEMP -- JUST RETURN CREATED FOR NOW!
      return new ResponseEntity<String>(HttpStatus.CREATED);
    }
    
    
    
    
    private ItemI getSubItem(ItemI itemI, String xsiType)
    {
      ArrayList<ItemI> itemIArrayList = null;
      
      try
      {
        itemIArrayList = itemI.getChildItems();
      }
      catch (Exception ex)
      {
        logger.error(ex.getMessage());
      }
      for (int i=0; i<itemIArrayList.size(); i++)
      {        
        if (itemIArrayList.get(i).getXSIType() == xsiType)
        {
          try
          {
            return itemIArrayList.get(i);
          }
          catch (Exception ex)
          {
            logger.error(ex.getMessage());
          }
        }
      }
      
      return null;
    }
            
}
