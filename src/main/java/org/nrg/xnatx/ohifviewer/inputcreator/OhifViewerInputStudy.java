/********************************************************************
* Copyright (c) 2017, Institute of Cancer Research
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

/********************************************************************
* @author Simon J Doran
* Java class: OhifViewerInputStudy.java
* First created on Sep 12, 2017 at 11:10:00 AM
* 
* Component of OhifViewerInput, which is serialised to JSON by
* CreateOhifViewerInputJson.java
*********************************************************************/

package org.nrg.xnatx.ohifviewer.inputcreator;

import etherj.dicom.Patient;
import etherj.dicom.Study;
import java.util.ArrayList;
import java.util.List;

public class OhifViewerInputStudy extends OhifViewerInputItem
{
	private String  studyInstanceUid;
	private String  patientName;
	private List<OhifViewerInputSeries> seriesList = new ArrayList<>();
  
  public OhifViewerInputStudy(Study std, Patient pat)
  {
    setStudyInstanceUid(std.getUid());
    setPatientName(pat.getName());
  }

	public String getStudyInstanceUid()
	{
		return studyInstanceUid;
	}

	private void setStudyInstanceUid(String studyInstanceUid)
	{
		this.studyInstanceUid = studyInstanceUid;
	}

	public String getPatientName()
	{
		return patientName;
	}

	private void setPatientName(String patientName)
	{
		this.patientName = patientName;
	}

	public List<OhifViewerInputSeries> getSeriesList()
	{
		return seriesList;
	}

	private void setSeriesList(List<OhifViewerInputSeries> seriesList)
	{
		this.seriesList = seriesList;
	}
  
  public void addSeries(OhifViewerInputSeries series)
  {
    this.seriesList.add(series);
  }
		
}
