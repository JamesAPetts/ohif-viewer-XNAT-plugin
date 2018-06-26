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
import java.util.HashMap;
import org.nrg.xft.security.UserI;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.ProjectId;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xnatx.ohifviewer.inputcreator.CreateExperimentMetadata;
import org.nrg.xnatx.ohifviewer.inputcreator.RunnableCreateExperimentMetadata;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 
 * @author jpetts
 */

@Api("Get and set viewer metadata.")
@XapiRestController
@RequestMapping(value = "/viewer")
public class OhifViewerApi extends AbstractXapiRestController {
    private static final Logger logger = LoggerFactory.getLogger(OhifViewerApi.class);
    private static final String SEP = File.separator;
    private static Boolean generateAllJsonLocked = false;
    
    @Autowired
    public OhifViewerApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder) {
   		super(userManagementService, roleHolder);
    }
    
    /*=================================
    // Study level GET/POST
    =================================*/
    
    @ApiOperation(value = "Checks whether user has permission to write to project (intended for private use by the viewer)")
    @ApiResponses({
      @ApiResponse(code = 200, message = "Permission granted."),
      @ApiResponse(code = 403, message = "The user does not have permission to view the indicated project"),
    })
    @XapiRequestMapping(
            value = "projects/{_projectId}/writepermissions",
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.GET,
            restrictTo = AccessLevel.Edit)
    public ResponseEntity<String> canUserWrite(
      @PathVariable("_projectId") @ProjectId final String _projectId)
    {
      return new ResponseEntity<>(HttpStatus.OK);
    }
    
    @ApiOperation(value = "Checks if Study level JSON exists")
    @ApiResponses({
      @ApiResponse(code = 302, message = "The session JSON exists."),
      @ApiResponse(code = 403, message = "The user does not have permission to view the indicated experiment."),
      @ApiResponse(code = 404, message = "The specified JSON does not exist."),
      @ApiResponse(code = 500, message = "An unexpected error occurred."),
    })
    @XapiRequestMapping(
            value = "projects/{_projectId}/experiments/{_experimentId}/exists",
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.GET,
            restrictTo = AccessLevel.Read)
    public ResponseEntity<String> doesStudyJsonExist(
      @PathVariable("_projectId") @ProjectId final String _projectId,
      final @PathVariable String _experimentId)
      throws IOException, FileNotFoundException
    {
      String xnatArchivePath = XDAT.getSiteConfigPreferences().getArchivePath();
      
      // Get directory info from _experimentId
      HashMap<String,String> experimentData;
      String proj = null;
      String expLabel = null;
      try {
        experimentData = getDirectoryInfo(_experimentId);
        proj     = experimentData.get("proj");
        expLabel = experimentData.get("expLabel");
      } catch (Exception ex) {
        logger.error(ex.getMessage());
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
      
      // Check if the experiment corresponds to the project specified in Url
      if (!proj.equals(_projectId)) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
      
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
    @XapiRequestMapping(
      value = "projects/{_projectId}/experiments/{_experimentId}",
      produces = MediaType.APPLICATION_JSON_VALUE,
      method = RequestMethod.GET,
      restrictTo = AccessLevel.Read
    )
    @ResponseBody
    public ResponseEntity<StreamingResponseBody> getExperimentJson(
      @PathVariable("_projectId") @ProjectId final String _projectId,
      final @PathVariable String _experimentId)
      throws FileNotFoundException
    { 
      String xnatArchivePath = XDAT.getSiteConfigPreferences().getArchivePath();
      
      // Get directory info from _experimentId
      HashMap<String,String> experimentData;
      String proj;
      String expLabel;
      try {
        experimentData = getDirectoryInfo(_experimentId);
        expLabel = experimentData.get("expLabel");
        proj = experimentData.get("proj");
      } catch (FileNotFoundException ex) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
      
      if (!proj.equals(_projectId)) {
        logger.error("project ids not equal");
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
      
      String readFilePath = getStudyPath(xnatArchivePath, proj, expLabel, _experimentId);
      final Reader reader = new FileReader(readFilePath);
      
      StreamingResponseBody srb = new StreamingResponseBody() {
        @Override
        public void writeTo(final OutputStream output) throws IOException {
          IOUtils.copy(reader, output);
        }
      };
      
      return new ResponseEntity<>(srb, HttpStatus.OK);
    }
    
    @ApiOperation(value = "Generates the session JSON for the specified experiment ID.")
    @ApiResponses({
      @ApiResponse(code = 201, message = "The session JSON has been created."),
      @ApiResponse(code = 403, message = "The user does not have permission to post to the indicated experient."),
      @ApiResponse(code = 500, message = "An unexpected error occurred.")
    })
    @XapiRequestMapping(
      value = "projects/{_projectId}/experiments/{_experimentId}",
      method = RequestMethod.POST,
      restrictTo = AccessLevel.Edit
    )
    public ResponseEntity<String> postExperimentJson(
      @PathVariable("_projectId") @ProjectId final String _projectId,
      final @PathVariable String _experimentId)
      throws IOException
    {      
      // Get directory info from _experimentId
      HashMap<String,String> experimentData;
      String proj = null;
      try {
        experimentData = getDirectoryInfo(_experimentId);
        proj     = experimentData.get("proj");
      } catch (Exception ex) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
      
      if (!proj.equals(_projectId)) {
        return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
      }
      
      HttpStatus returnHttpStatus = CreateExperimentMetadata.createMetadata(_experimentId);
      
      return new ResponseEntity<String>(returnHttpStatus);
    }
    
    @ApiOperation(value = "Generates the session JSON for every experiment in the database.")
    @ApiResponses({
      @ApiResponse(code = 201, message = "The JSON metadata has been created for every experiment in the database."),
      @ApiResponse(code = 403, message = "The user does not have permission to view the indicated experient."),
      @ApiResponse(code = 500, message = "An unexpected error occurred.")
    })
    @XapiRequestMapping(
      value = "generate-all-metadata",
      method = RequestMethod.POST,
      restrictTo = AccessLevel.Admin
    )
    public ResponseEntity<String> setAllJson()
      throws IOException
    {  
      // Don't allow more generate all processes to be started if one is already running
      if (generateAllJsonLocked == true)
      {
        return new ResponseEntity<String>(HttpStatus.LOCKED);
      }
      else
      {
        generateAllJsonLocked = true;
      }
      
      ArrayList<String> experimentIds = getAllExperimentIds();
      
      // Executes experiment JSON creation in a multithreaded fashion if avialable
      Integer numThreads = Runtime.getRuntime().availableProcessors();
      logger.info("numThreads for parallel JSON creation: " + numThreads);
      ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
      // Create a CountDownLatch in order to check when all processes are finished
      CountDownLatch doneSignal =  new CountDownLatch(experimentIds.size());
      
      for (int i = 0; i< experimentIds.size(); i++)
      {
        final String experimentId = experimentIds.get(i);
      
        logger.error("experimentId " + experimentId);
        RunnableCreateExperimentMetadata createExperimentMetadata =
                new RunnableCreateExperimentMetadata(doneSignal, experimentId);
        executorService.submit(createExperimentMetadata);
      }
      
      HttpStatus returnHttpStatus;
      try
      {
        doneSignal.await();
        returnHttpStatus = HttpStatus.CREATED;
      }
      catch (InterruptedException ex)
      {
        logger.error(ex.getMessage());
        returnHttpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
      }

      generateAllJsonLocked = false;
      return new ResponseEntity<String>(returnHttpStatus);
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
    @XapiRequestMapping(
      value = "projects/{_projectId}/experiments/{_experimentId}/scans/{_scanId}/exists",
      produces = MediaType.APPLICATION_JSON_VALUE,
      method = RequestMethod.GET,
      restrictTo = AccessLevel.Read
    )    
    public ResponseEntity<String> doesSeriesJsonExist(
      @PathVariable("_projectId") @ProjectId final String _projectId,
      final @PathVariable String _experimentId, @PathVariable String _scanId)
      throws IOException
    {
      // Grab the data archive path
      String xnatArchivePath = XDAT.getSiteConfigPreferences().getArchivePath();
      
      // Get directory info from _experimentId
      HashMap<String,String> experimentData = getDirectoryInfo(_experimentId);
      String proj     = experimentData.get("proj");
      String expLabel = experimentData.get("expLabel");
      
      if (!proj.equals(_projectId)) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
      
      String readFilePath = getSeriesPath(xnatArchivePath, proj, expLabel, _scanId);
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
    @XapiRequestMapping(
      value = "projects/{_projectId}/experiments/{_experimentId}/scans/{_scanId}",
      produces = MediaType.APPLICATION_JSON_VALUE,
      method = RequestMethod.GET,
      restrictTo = AccessLevel.Read
    )
    public StreamingResponseBody getSeriesJson(
      @PathVariable("_projectId") @ProjectId final String _projectId,
      final @PathVariable String _experimentId, @PathVariable String _scanId)
      throws FileNotFoundException
    {
    // Grab the data archive path
      String xnatArchivePath = XDAT.getSiteConfigPreferences().getArchivePath();
      
      // Get directory info from _experimentId
      HashMap<String,String> experimentData = getDirectoryInfo(_experimentId);
      String proj     = experimentData.get("proj");
      String expLabel = experimentData.get("expLabel");
      
      String readFilePath = getSeriesPath(xnatArchivePath, proj, expLabel, _scanId);
      
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
    @XapiRequestMapping(
      value = "projects/{_projectId}/experiments/{_experimentId}/scans/{_scanId}",
      method = RequestMethod.POST,
      restrictTo = AccessLevel.Edit
    )
    public ResponseEntity<String> postSeriesJson(
      @PathVariable("_projectId") @ProjectId final String _projectId,
      final @PathVariable String _experimentId, @PathVariable String _scanId)
      throws IOException
    {
      // Grab the data archive path
      String xnatRootURL      = XDAT.getSiteConfigPreferences().getSiteUrl();
      String xnatArchivePath  = XDAT.getSiteConfigPreferences().getArchivePath();
      
      // Get directory info from _experimentId
      HashMap<String,String> experimentData = getDirectoryInfo(_experimentId);
      String proj     = experimentData.get("proj");
      
      if (!proj.equals(_projectId)) {
        return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
      }
      
      // Runs creation process within the active thread.
      RunnableCreateSeriesMetadata createSeriesMetadata =
                new RunnableCreateSeriesMetadata(xnatRootURL, xnatArchivePath, _experimentId, _scanId, null);
      HttpStatus returnHttpStatus = createSeriesMetadata.runOnCurrentThread();
      
      return new ResponseEntity<String>(returnHttpStatus);
    }

    */
    
    private ArrayList<String> getAllExperimentIds()
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
      throws FileNotFoundException
    {
      // Get Experiment data and Project data from the experimentId
      
      XnatExperimentdata expData = null;
      XnatProjectdata projData = null;
      XnatImagesessiondata session = null;
      
      try {
        expData = XnatExperimentdata.getXnatExperimentdatasById(_experimentId, null, false);
        projData = expData.getProjectData();
        session=(XnatImagesessiondata)expData;
      } catch (Exception ex) {
        throw new FileNotFoundException("Experiment not found in project");
      }            

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
   
    
    private String getStudyPath(String xnatArchivePath, String proj, String expLabel, String _experimentId)
    {
      String filePath = xnatArchivePath + SEP + proj + SEP + "arc001"
      + SEP + expLabel + SEP + "RESOURCES/metadata/" + _experimentId +".json";
      return filePath;
    }
    
    /*
    private String getSeriesPath(String xnatArchivePath, String proj, String expLabel, String _scanId)
    {
      String filePath = xnatArchivePath + SEP + proj + SEP + "arc001"
      + SEP + expLabel + SEP + "RESOURCES/metadata/" + _scanId +".json";
      return filePath;
    }
    */
    
}
    

