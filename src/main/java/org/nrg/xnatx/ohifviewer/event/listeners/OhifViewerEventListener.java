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
package org.nrg.xnatx.ohifviewer.event.listeners;

import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.WrkWorkflowdata;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xft.event.entities.WorkflowStatusEvent;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xnatx.ohifviewer.inputcreator.CreateExperimentMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;

import javax.inject.Inject;

import static reactor.bus.selector.Selectors.R;

/**
 *
 * @author jpetts
 */
@Service
public class OhifViewerEventListener implements Consumer<Event<WorkflowStatusEvent>> {
  private static final Logger logger = LoggerFactory.getLogger(OhifViewerEventListener.class);
  
  @Inject
  public OhifViewerEventListener(EventBus eventBus) {    
    eventBus.on(R(WorkflowStatusEvent.class.getName() + "[.]?(" + PersistentWorkflowUtils.COMPLETE + ")"), this);
  }
  
  @Override
  public void accept(Event<WorkflowStatusEvent> event) {
    final WorkflowStatusEvent wfsEvent = event.getData();
    if (wfsEvent.getWorkflow() instanceof WrkWorkflowdata) {
        handleEvent(wfsEvent);
    }
  }
  
  private void handleEvent (WorkflowStatusEvent wfsEvent) {
    final WrkWorkflowdata workflow = (WrkWorkflowdata)wfsEvent.getWorkflow();
    String pipelineName = workflow.getPipelineName();
    String experimentId = workflow.getId();
    
    logger.debug("handling event in OhifViewerEventListener, pipelineName: " + pipelineName);
    logger.debug(workflow.getDataType());
    logger.debug(experimentId);
    
    // TODO If event is Transferred, Update and Folder Deleted, rebuild json.
    if (pipelineName.equals("Transferred")
        || pipelineName.equals("Update")
        || pipelineName.equals("Folder Deleted")
        || pipelineName.equals("Folder Created")
        || pipelineName.equals("Removed scan")
        || pipelineName.equals("Modified Subject")
        || pipelineName.equals("Created resource")
        || pipelineName.matches("Modified .* Session")
        || pipelineName.equals("Modified project")
        || pipelineName.equals("Configured project sharing")) {
      checkIfImageSessiondata(experimentId);
    }
  }
  
  private void checkIfImageSessiondata(String experimentId) {
    XnatExperimentdata experimentData = XnatExperimentdata.getXnatExperimentdatasById(experimentId, null, false);
    if (experimentData instanceof XnatImagesessiondata) {
      generateJsonForExperiment(experimentId);
    } else {
      logger.debug("Workflow event not referencing image session data, no JSON to be created.");
    }
  }
  
  private void generateJsonForExperiment(String experimentId) {
    try {
      String xnatRootURL = XDAT.getSiteConfigPreferences().getSiteUrl();
      String xnatArchivePath = XDAT.getSiteConfigPreferences().getArchivePath();
      logger.debug("Rebuilding viewer JSON metadata for experiment " + experimentId);
      CreateExperimentMetadata.createMetadata(experimentId);
    } catch (Exception e) {
     logger.error(e.getMessage());
    }
  }  
  
}
