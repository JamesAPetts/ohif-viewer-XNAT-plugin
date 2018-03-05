/********************************************************************
* Copyright (c) 2018, Institute of Cancer Research
* All rights reserved.
* 
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
* 
* (1) Redistributions of source code must retain the above copyright
*     notice, this list of conditions and the following disclaimer.
* 
* (2) Redistributions in binary form must reproduce the above
*     copyright notice, this list of conditions and the following
*     disclaimer in the documentation and/or other materials provided
*     with the distribution.
* 
* (3) Neither the name of the Institute of Cancer Research nor the
*     names of its contributors may be used to endorse or promote
*     products derived from this software without specific prior
*     written permission.
* 
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
* "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
* LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
* FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
* COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
* INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
* SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
* HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
* STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
* OF THE POSSIBILITY OF SUCH DAMAGE.
*********************************************************************/

package org.nrg.xnatx.ohifviewer.xapi;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Api;
import org.apache.commons.io.IOUtils;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xnatx.ohifviewer.inputcreator.CreateOhifViewerMetadata;
import org.nrg.xdat.XDAT;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import org.nrg.xft.ItemI;
import java.util.ArrayList;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatSubjectdata;

/**
 * 
 * @author jpetts
 * @author RickHerrick
 */

@Api("Get and set viewer metadata.")
@XapiRestController
@RequestMapping(value = "/viewer")
public class OhifViewerApi {
    private static final Logger logger = LoggerFactory.getLogger(OhifViewerApi.class);
    private static final String SEP = File.separator;
    private static Boolean isLocked = false;
    
    
    @ApiOperation(value = "Returns 200 if JSON exists")
    @ApiResponses({
      @ApiResponse(code = 302, message = "The session JSON exists."),
      @ApiResponse(code = 403, message = "The user does not have permission to view the indicated experiment."),
      @ApiResponse(code = 500, message = "An unexpected error occurred.")
    })
    @XapiRequestMapping(value = "exists/{_experimentId}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public ResponseEntity<String> doesJsonExist(final @PathVariable String _experimentId) throws IOException {
      // Grab the data archive path
      String xnatArchivePath = XDAT.getSiteConfigPreferences().getArchivePath();
      
      // Get directory info from _experimentId
      HashMap<String,String> experimentData = getDirectoryInfo(_experimentId);
      String proj     = experimentData.get("proj");
      String expLabel = experimentData.get("expLabel");
      String subj     = experimentData.get("subj");
      
      String readFilePath = getFilePath(xnatArchivePath, proj, expLabel, _experimentId);
      File file = new File(readFilePath);
      if (file.exists())
      {
        return new ResponseEntity<>(HttpStatus.FOUND);
      }
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    
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
      
      // Get directory info from _experimentId
      HashMap<String,String> experimentData = getDirectoryInfo(_experimentId);
      String proj     = experimentData.get("proj");
      String expLabel = experimentData.get("expLabel");
      String subj     = experimentData.get("subj");
      
      String readFilePath = getFilePath(xnatArchivePath, proj, expLabel, _experimentId);
      final Reader reader = new FileReader(readFilePath);
      
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
    @XapiRequestMapping(value = "{_experimentId}", method = RequestMethod.POST)
    public ResponseEntity<String> setSessionJson(final @PathVariable String _experimentId) throws IOException {
      
      //Only allow one process to write
      if (isLocked) return new ResponseEntity<>(HttpStatus.LOCKED);
      isLocked = true;
      
      // Grab the data archive path
      String rootURL          = XDAT.getSiteConfigPreferences().getSiteUrl();
      String xnatArchivePath  = XDAT.getSiteConfigPreferences().getArchivePath();

      HashMap<String,String> experimentData = getDirectoryInfo(_experimentId);
      String proj     = experimentData.get("proj");
      String expLabel = experimentData.get("expLabel");
      String subj     = experimentData.get("subj");
      
      String xnatScanPath = xnatArchivePath + SEP + proj
        + SEP + "arc001" + SEP + expLabel + SEP + "SCANS";

      String xnatScanUrl  = rootURL.replace("http", "dicomweb")
        + "/data/archive/projects/" + proj
        + "/subjects/" + subj
        + "/experiments/" + _experimentId
        + "/scans/";
      
      String jsonString = "";

      try
      {
        CreateOhifViewerMetadata jsonCreator = new CreateOhifViewerMetadata();
        try
        {
          jsonString = jsonCreator.jsonify(xnatScanPath,xnatScanUrl,_experimentId);
        }
        catch (Exception ex)
        {
          logger.error(ex.getMessage());
        }
      }
      catch (Exception ex)
      {
        isLocked = false;
        logger.error("Jsonifier exception:\n" + ex.getMessage());
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
      }
      
      String writeFilePath = getFilePath(xnatArchivePath, proj, expLabel, _experimentId);
      
      logger.error("Making directories: " + writeFilePath);
      try
      {
        File file = new File(writeFilePath);
        if (!file.exists())
        {
          Files.createDirectories(Paths.get(file.getParent().toString()));
          logger.error("...created directory.");
        }
      }
      catch (Exception ex)
      {
        logger.error("Error creating directories: " + ex.getMessage());
      }
      
      final Writer writer = new FileWriter(writeFilePath);
      
      try
      {
        IOUtils.write(jsonString, writer);
        writer.close();
        logger.info("Wrote to: " + writeFilePath);
        isLocked = false;
        return new ResponseEntity<>(HttpStatus.CREATED);
      }
      catch (IOException ioEx)
      {
        isLocked = false;
        logger.error(ioEx.getMessage());
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
      }
      
    }
    
    private HashMap<String, String> getDirectoryInfo(String _experimentId)
    {
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
      
      // Get the required info
      String expLabel = expData.getArchiveDirectoryName();
      String proj = projData.getId();
      String subj = subjData.getLabel();
      
      // Construct a HashMap to return data
      HashMap<String, String> result = new HashMap<String, String>();
      result.put("expLabel", expLabel);
      result.put("proj", proj);
      result.put("subj", subj);
      
      return result;
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
    
    
    private String getFilePath(String xnatArchivePath, String proj, String expLabel, String _experimentId)
    {
      String filePath = xnatArchivePath + SEP + proj + SEP + "arc001"
      + SEP + expLabel + SEP + "RESOURCES/metadata/" + _experimentId +".json";
      return filePath;
    }
            
}
