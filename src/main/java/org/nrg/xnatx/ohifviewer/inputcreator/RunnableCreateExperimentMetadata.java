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
package org.nrg.xnatx.ohifviewer.inputcreator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.apache.commons.io.IOUtils;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jpetts
 */
public class RunnableCreateExperimentMetadata implements Runnable {
  private static final Logger logger = LoggerFactory.getLogger(RunnableCreateExperimentMetadata.class);
  private static final String SEP = File.separator;
  private final CountDownLatch doneSignal;
  private final String threadName = "ohifviewer.RunnableCreateExperimentMetadata";
  private final String xnatRootURL;
  private final String xnatArchivePath;
  private final String experimentId;
  private Thread thread;
  
   
  public RunnableCreateExperimentMetadata(CountDownLatch doneSignal, String _xnatRootURL, String _xnatArchivePath, String _experimentId) {
    this.doneSignal = doneSignal;
    this.xnatRootURL = _xnatRootURL;
    this.xnatArchivePath = _xnatArchivePath;
    this.experimentId = _experimentId;
  }
  
  public void start()
  {
    if (thread == null) {
      thread = new Thread (this, threadName);
      thread.start ();
    }
  }
  
  public void run()
  {
    try
    {
      doWork();
      doneSignal.countDown();
    }
    catch (Exception ex)
    {
      thread.interrupt();
      logger.error(ex.getMessage());
    }
  }
  
  
  private void doWork()
  {
    HashMap<String,String> experimentData = getDirectoryInfo(experimentId);
    String proj     = experimentData.get("proj");
    String expLabel = experimentData.get("expLabel");
    String subj     = experimentData.get("subj");

    HashMap<String,String> seriesUidToScanIdMap = getSeriesUidToScanIdMap(experimentId);

    String xnatScanPath = xnatArchivePath + SEP + proj
      + SEP + "arc001" + SEP + expLabel + SEP + "SCANS";

    String xnatScanUrl  = xnatRootURL
      + "/data/archive/projects/" + proj
      + "/subjects/" + subj
      + "/experiments/" + experimentId
      + "/scans/";

    String jsonString = "";
    try
    {
      CreateOhifViewerMetadata jsonCreator = new CreateOhifViewerMetadata(xnatScanPath, xnatScanUrl, seriesUidToScanIdMap);
      jsonString = jsonCreator.jsonifyStudy(experimentId);
    }
    catch (Exception ex)
    {
      logger.error("Jsonifier exception:\n" + ex.getMessage());
    }

    String writeFilePath = getStudyPath(xnatArchivePath, proj, expLabel, experimentId);

    // Create RESOURCES/metadata if it doesn't exist
    createFilePath(writeFilePath);

    // Write to file and send back response code
    writeJSON(jsonString, writeFilePath);
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
    
  private void writeJSON(String jsonString, String writeFilePath)
  {
    try
    {
      // Write to file
      final Writer writer = new FileWriter(writeFilePath);
      IOUtils.write(jsonString, writer);
      writer.close();
      logger.debug("Wrote to: " + writeFilePath);;
    }
    catch (IOException ioEx)
    {
      logger.error(ioEx.getMessage());
    }
    
    return;
  }
}
