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

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import org.springframework.http.HttpStatus;

/**
 *
 * @author jpetts
 */
public class RunnableCreateExperimentMetadata extends RunnableCreateMetadata {
  private final String experimentId;
   
  public RunnableCreateExperimentMetadata(String _xnatRootURL, String _xnatArchivePath, String _experimentId, CountDownLatch doneSignal) {
    super(doneSignal, _xnatRootURL, _xnatArchivePath);
    this.experimentId = _experimentId;
  }
  
  public HttpStatus runOnCurrentThread() {
    // Method that allows single threaded execution (for single POST requests).
    return createMetadata();
  }
  
  @Override
  protected HttpStatus createMetadata()
  {
    HashMap<String,String> experimentData = getDirectoryInfo(experimentId);
    String proj     = experimentData.get("proj");
    String expLabel = experimentData.get("expLabel");
    String subj     = experimentData.get("subj");

    HashMap<String,String> seriesUidToScanIdMap = getSeriesUidToScanIdMap(experimentId);

    String xnatScanPath = xnatArchivePath + SEP + proj
      + SEP + "arc001" + SEP + expLabel + SEP + "SCANS";

    String xnatExperimentScanUrl = getXnatScanUrl(proj, subj, expLabel);

    String jsonString = "";
    try
    {
      CreateOhifViewerMetadata jsonCreator = new CreateOhifViewerMetadata(xnatScanPath, xnatExperimentScanUrl, seriesUidToScanIdMap);
      jsonString = jsonCreator.jsonify(experimentId);
    }
    catch (Exception ex)
    {
      logger.error("Jsonifier exception:\n" + ex.getMessage());
    }

    String writeFilePath = getStudyPath(xnatArchivePath, proj, expLabel, experimentId);

    // Create RESOURCES/metadata if it doesn't exist
    createFilePath(writeFilePath);

    // Write to file and send back response code
    return writeJSON(jsonString, writeFilePath);
  }
  

  private String getStudyPath(String xnatArchivePath, String proj, String expLabel, String _experimentId)
  {
    String filePath = xnatArchivePath + SEP + proj + SEP + "arc001"
    + SEP + expLabel + SEP + "RESOURCES/metadata/" + _experimentId +".json";
    return filePath;
  }

}
