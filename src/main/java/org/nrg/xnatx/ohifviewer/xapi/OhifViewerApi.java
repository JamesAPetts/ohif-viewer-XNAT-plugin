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
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.HashMap;
import org.nrg.xft.security.UserI;
import java.util.ArrayList;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xdat.security.helpers.AccessLevel;

/**
 * 
 * @author jpetts
 * @author RickHerrick
 */

@Api("Get and set viewer metadata.")
@XapiRestController
@RequestMapping(value = "/viewer")
public class OhifViewerApi extends AbstractXapiRestController {
    private static final Logger logger = LoggerFactory.getLogger(OhifViewerApi.class);
    private static final String SEP = File.separator;
    private static Boolean isLocked = false;
    
    @Autowired
    public OhifViewerApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder) {
   		super(userManagementService, roleHolder);
    }
    
    
    
    
    
    /*=================================
    // Study level GET/POST
    =================================*/
    
    @ApiOperation(value = "Returns 200 if Study level JSON exists")
    @ApiResponses({
      @ApiResponse(code = 302, message = "The session JSON exists."),
      @ApiResponse(code = 403, message = "The user does not have permission to view the indicated experiment."),
      @ApiResponse(code = 404, message = "The specified JSON does not exist."),
      @ApiResponse(code = 500, message = "An unexpected error occurred.")
    })    
    @XapiRequestMapping(value = "exists/{_experimentId}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)    
    public ResponseEntity<String> doesStudyJsonExist(final @PathVariable String _experimentId) throws IOException {
      // Grab the data archive path
      String xnatArchivePath = XDAT.getSiteConfigPreferences().getArchivePath();
      
      // Get directory info from _experimentId
      HashMap<String,String> experimentData = getDirectoryInfo(_experimentId);
      String proj     = experimentData.get("proj");
      String expLabel = experimentData.get("expLabel");
      
      String readFilePath = getStudyPath(xnatArchivePath, proj, expLabel, _experimentId);
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
    public StreamingResponseBody getExperimentJson(final @PathVariable String _experimentId) throws FileNotFoundException {
      
      // Grab the data archive path
      String xnatArchivePath = XDAT.getSiteConfigPreferences().getArchivePath();
      
      // Get directory info from _experimentId
      HashMap<String,String> experimentData = getDirectoryInfo(_experimentId);
      String proj     = experimentData.get("proj");
      String expLabel = experimentData.get("expLabel");
      
      String readFilePath = getStudyPath(xnatArchivePath, proj, expLabel, _experimentId);
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
      @ApiResponse(code = 403, message = "The user does not have permission to post to the indicated experient."),
      @ApiResponse(code = 500, message = "An unexpected error occurred.")
    })
    @XapiRequestMapping(value = "{_experimentId}", method = RequestMethod.POST)
    public ResponseEntity<String> postExperimentJson(final @PathVariable String _experimentId) throws IOException {
      // Grab the data archive path
      String xnatRootURL      = XDAT.getSiteConfigPreferences().getSiteUrl();
      String xnatArchivePath  = XDAT.getSiteConfigPreferences().getArchivePath();
      
      ResponseEntity<String> postResult = generateExperimentMetadata(xnatRootURL, xnatArchivePath, _experimentId);
      return postResult;
    }
    
    
    
    
    // JamesAPetts -- WIP -- Generate all JSON for the database
    @ApiOperation(value = "Generates the session JSON for every experiment in the database.")
    @ApiResponses({
      @ApiResponse(code = 201, message = "The JSON metadata has been created for every experiment in the database."),
      @ApiResponse(code = 403, message = "The user does not have permission to view the indicated experient."),
      @ApiResponse(code = 500, message = "An unexpected error occurred.")
    })
    @XapiRequestMapping(value = "generate-all-metadata", method = RequestMethod.POST, restrictTo = AccessLevel.Admin)
    public ResponseEntity<String> setAllJson() throws IOException {
      // Grab the data archive path
      String xnatRootURL      = XDAT.getSiteConfigPreferences().getSiteUrl();
      String xnatArchivePath  = XDAT.getSiteConfigPreferences().getArchivePath();
      
      ArrayList<String> experimentIds = getAllExperimentIds(xnatRootURL, xnatArchivePath);
      
      for (int i = 0; i< experimentIds.size(); i++)
      {
        final String experimentId = experimentIds.get(i);
        logger.error("experimentId " + experimentId);
        ResponseEntity<String> postResult = generateExperimentMetadata(xnatRootURL, xnatArchivePath, experimentId);
        if (postResult.getStatusCodeValue() == 500)
        {
          return postResult;
        }
      }
      
      return new ResponseEntity<String>(HttpStatus.CREATED);
      
      
    }
    
    
    
    /*=================================    
    // Series level GET/POST- WIP
    =================================*/
    /*
    
    @ApiOperation(value = "Returns 200 if series level JSON exists")
    @ApiResponses({
      @ApiResponse(code = 302, message = "The session JSON exists."),
      @ApiResponse(code = 403, message = "The user does not have permission to view the indicated experiment."),
      @ApiResponse(code = 404, message = "The specified JSON does not exist."),
      @ApiResponse(code = 500, message = "An unexpected error occurred.")
    })    
    @XapiRequestMapping(value = "exists/{_experimentId}/{_seriesId}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)    
    public ResponseEntity<String> doesSeriesJsonExist(final @PathVariable String _experimentId, @PathVariable String _seriesId) throws IOException {
      // Grab the data archive path
      String xnatArchivePath = XDAT.getSiteConfigPreferences().getArchivePath();
      
      // Get directory info from _experimentId
      HashMap<String,String> experimentData = getDirectoryInfo(_experimentId);
      String proj     = experimentData.get("proj");
      String expLabel = experimentData.get("expLabel");
      
      String readFilePath = getSeriesPath(xnatArchivePath, proj, expLabel, _seriesId);
      File file = new File(readFilePath);
      if (file.exists())
      {
        return new ResponseEntity<>(HttpStatus.FOUND);
      }
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    
    @ApiOperation(value = "Returns the session JSON for the specified series.")
    @ApiResponses({
      @ApiResponse(code = 200, message = "The session was located and properly rendered to JSON."),
      @ApiResponse(code = 403, message = "The user does not have permission to view the indicated experiment."),
      @ApiResponse(code = 500, message = "An unexpected error occurred.")
    })
    @XapiRequestMapping(value = "{_experimentId}/{_seriesId}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public StreamingResponseBody getSeriesJson(final @PathVariable String _experimentId, @PathVariable String _seriesId) throws FileNotFoundException {
    // Grab the data archive path
      String xnatArchivePath = XDAT.getSiteConfigPreferences().getArchivePath();
      
      // Get directory info from _experimentId
      HashMap<String,String> experimentData = getDirectoryInfo(_experimentId);
      String proj     = experimentData.get("proj");
      String expLabel = experimentData.get("expLabel");
      
      logger.debug("proj, expLabel, _seriesId: " + proj + " " + expLabel + " " + _seriesId);
      
      String readFilePath = getSeriesPath(xnatArchivePath, proj, expLabel, _seriesId);
      
      logger.debug("Getting series Path: " + readFilePath);
      
      final Reader reader = new FileReader(readFilePath);
      
      return new StreamingResponseBody() {
          @Override
          public void writeTo(final OutputStream output) throws IOException {
              IOUtils.copy(reader, output);
          }
      };
    }
    
    
    @ApiOperation(value = "Generates the session JSON for the specified series.")
    @ApiResponses({
      @ApiResponse(code = 201, message = "The session JSON has been created."),
      @ApiResponse(code = 403, message = "The user does not have permission to view the indicated experient."),
      @ApiResponse(code = 500, message = "An unexpected error occurred.")
    })
    @XapiRequestMapping(value = "{_experimentId}/{_seriesId}", method = RequestMethod.POST)
    public ResponseEntity<String> setSeriesJson(final @PathVariable String _experimentId, @PathVariable String _seriesId) throws IOException {
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
      
            // Generate JSON string -- // TODO Change to exp only in JSONifier!
      //
      //
      //
      //
      //
      //
      
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
        jsonString = jsonCreator.jsonifySeries(xnatScanPath,xnatScanUrl,_experimentId, _seriesId);
      }
      catch (Exception ex)
      {
        isLocked = false;
        logger.error("Jsonifier exception:\n" + ex.getMessage());
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
      }
      
      //
      //
      //
      //
      //
      //
      
      String writeFilePath = getSeriesPath(xnatArchivePath, proj, expLabel, _seriesId);

      // Create RESOURCES/metadata if it doesn't exist
      createFilePath(writeFilePath);
      
      // Write to file and send back response code
      ResponseEntity<String> POSTStatus = writeJSON(jsonString, writeFilePath);
      return POSTStatus;
    }
    
    
    */
    
    
    private ResponseEntity<String> generateExperimentMetadata(String xnatRootURL, String xnatArchivePath, String _experimentId)
    {
            //Only allow one process to write
      if (isLocked) return new ResponseEntity<>(HttpStatus.LOCKED);
      isLocked = true;

      HashMap<String,String> experimentData = getDirectoryInfo(_experimentId);
      String proj     = experimentData.get("proj");
      String expLabel = experimentData.get("expLabel");
      String subj     = experimentData.get("subj");
      
      HashMap<String,String> seriesUidToScanIdMap = getSeriesUidToScanIdMap(_experimentId);
      
      String xnatScanPath = xnatArchivePath + SEP + proj
        + SEP + "arc001" + SEP + expLabel + SEP + "SCANS";
      


      //String xnatScanUrl  = rootURL.replace("http", "dicomweb")
      String xnatScanUrl  = xnatRootURL
        + "/data/archive/projects/" + proj
        + "/subjects/" + subj
        + "/experiments/" + _experimentId
        + "/scans/";
      
      // Generate JSON string
      String jsonString = "";
      try
      {
        CreateOhifViewerMetadata jsonCreator = new CreateOhifViewerMetadata(xnatScanPath, xnatScanUrl, seriesUidToScanIdMap);
        jsonString = jsonCreator.jsonifyStudy(_experimentId);
      }
      catch (Exception ex)
      {
        isLocked = false;
        logger.error("Jsonifier exception:\n" + ex.getMessage());
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
      }
      
      String writeFilePath = getStudyPath(xnatArchivePath, proj, expLabel, _experimentId);

      // Create RESOURCES/metadata if it doesn't exist
      createFilePath(writeFilePath);
      
      // Write to file and send back response code
      ResponseEntity<String> POSTStatus = writeJSON(jsonString, writeFilePath);
      return POSTStatus;
    }
    
    
    
    private ArrayList<String> getAllExperimentIds(String xnatRootURL, String xnatArchivePath)
    {
      ArrayList<String> experimentIds = new ArrayList<>();
      
      UserI user = getSessionUser();
      ArrayList<XnatExperimentdata> experiments = XnatExperimentdata.getAllXnatExperimentdatas(user, true);
      
      for (int i = 0; i< experiments.size(); i++)
      {
        final XnatExperimentdata experimentI = experiments.get(i);
        if ( experimentI instanceof XnatImagesessiondata )
        {
          experimentIds.add(experimentI.getId());
        }
      }
      
      return experimentIds;
    }
    
    
    
    
    private HashMap<String, String> getDirectoryInfo(String _experimentId)
    {
      // Get Experiment data and Project data from the experimentId
      XnatExperimentdata expData = XnatExperimentdata.getXnatExperimentdatasById(_experimentId, null, false);
      XnatProjectdata projData = expData.getProjectData();
      
      XnatImagesessiondata session=(XnatImagesessiondata)expData;      

      // Get the subject data
      XnatSubjectdata subjData = XnatSubjectdata.getXnatSubjectdatasById(session.getSubjectId(), null, false);
      
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
    
    
    private HashMap<String, String> getSeriesUidToScanIdMap(String _experimentId)
    {
      // WIP
      HashMap<String, String> seriesUidToScanIdMap = new HashMap<String, String>();
      XnatExperimentdata expData = XnatExperimentdata.getXnatExperimentdatasById(_experimentId, null, false);
      
      XnatImagesessiondata session = null;
      try
      {
        session=(XnatImagesessiondata)expData;
      }
      catch (Exception ex)
      {
        logger.error(ex.getMessage());
      }

      List<XnatImagescandataI> scans = session.getScans_scan();
      
      for (final XnatImagescandataI scan:scans)
      {
        
        logger.error(scan.getUid() + " " + scan.getId());
        String seriesInstanceUid = scan.getUid();
        String scanId = scan.getId();
        seriesUidToScanIdMap.put(seriesInstanceUid, scanId);
        
      }
      
      return seriesUidToScanIdMap;
    }
    
    private String getStudyPath(String xnatArchivePath, String proj, String expLabel, String _experimentId)
    {
      String filePath = xnatArchivePath + SEP + proj + SEP + "arc001"
      + SEP + expLabel + SEP + "RESOURCES/metadata/" + _experimentId +".json";
      return filePath;
    }
    
    // TODO: Refactor, too many arguments
    private String getSeriesPath(String xnatArchivePath, String proj, String expLabel, String _seriesId)
    {
      String filePath = xnatArchivePath + SEP + proj + SEP + "arc001"
      + SEP + expLabel + SEP + "SCANS" + SEP + _seriesId + SEP + "RESOURCES/metadata/" + _seriesId +".json";
      return filePath;
    }
    
    private void createFilePath(String filePath)
    { // Create RESOURCES/metadata if it doesn't exist
      try
      {
        File file = new File(filePath);
        if (!file.exists())
        {
          Files.createDirectories(Paths.get(file.getParent().toString()));
        }
      }
      catch (Exception ex)
      {
        logger.error("Error creating directories: " + ex.getMessage());
      }
    }
    
    private ResponseEntity<String> writeJSON(String jsonString, String writeFilePath)
    {
      try
      {
        // Write to file
        final Writer writer = new FileWriter(writeFilePath);
        IOUtils.write(jsonString, writer);
        writer.close();
        logger.debug("Wrote to: " + writeFilePath);
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
}
