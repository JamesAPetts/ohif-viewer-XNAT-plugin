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
import org.nrg.xnatx.ohifviewer.inputcreator.RunnableCreateMetadata;

/**
 *
 * @author jpetts
 */
public class RunnableCreateSeriesMetadata extends RunnableCreateMetadata {
  private final String experimentId;
  private final String seriesId;
  
   
  public RunnableCreateSeriesMetadata(CountDownLatch doneSignal, String _xnatRootURL, String _xnatArchivePath, String _experimentId, String _seriesId) {
    super(doneSignal, _xnatRootURL, _xnatArchivePath);
    this.experimentId = _experimentId;
    this.seriesId = _seriesId;
    this.threadName = "ohifviewer.RunnableCreateExperimentMetadata";
  }
  
  
  protected void doWork()
  {
    HashMap<String,String> experimentData = getDirectoryInfo(this.experimentId);
    String proj     = experimentData.get("proj");
    String expLabel = experimentData.get("expLabel");
    String subj     = experimentData.get("subj");

    HashMap<String,String> seriesUidToScanIdMap = getSeriesUidToScanIdMap(this.experimentId);

    String xnatScanPath = xnatArchivePath + SEP + proj
      + SEP + "arc001" + SEP + expLabel + SEP + "SCANS" + SEP + this.seriesId;

    String xnatExperimentScanUrl = getXnatScanUrl(proj, subj, expLabel);

    String jsonString = "";
    try
    {
      CreateOhifViewerMetadata jsonCreator = new CreateOhifViewerMetadata(xnatScanPath, xnatExperimentScanUrl, seriesUidToScanIdMap);
      jsonString = jsonCreator.jsonify(this.experimentId);
    }
    catch (Exception ex)
    {
      logger.error("Jsonifier exception:\n" + ex.getMessage());
    }

    String writeFilePath = getSeriesPath(xnatArchivePath, proj, expLabel, this.seriesId);

    // Create RESOURCES/metadata if it doesn't exist
    createFilePath(writeFilePath);

    // Write to file and send back response code
    writeJSON(jsonString, writeFilePath);
  }
 
  
  private String getSeriesPath(String xnatArchivePath, String proj, String expLabel, String _seriesId)
  {
    String filePath = xnatArchivePath + SEP + proj + SEP + "arc001"
    + SEP + expLabel + SEP + "RESOURCES/metadata/" + _seriesId +".json";
    return filePath;
  }


}
