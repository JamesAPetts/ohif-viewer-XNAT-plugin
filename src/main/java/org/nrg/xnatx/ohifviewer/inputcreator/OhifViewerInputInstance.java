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
* Java class: OhifViewerInputInstance.java
* First created on Sep 12, 2017 at 12:34:36 PM
* 
* Component of OhifViewerInput, which is serialised to JSON by
* CreateOhifViewerInputJson.java
* 
*********************************************************************/

package org.nrg.xnatx.ohifviewer.inputcreator;

public class OhifViewerInputInstance
{
	private String  sopInstanceUid;
	private Integer instanceNumber;
	private Integer columns;
	private Integer rows;
	private String  frameOfReferenceUID;
	private String  imagePositionPatient;
	private String  imageOrientationPatient;
	private String  pixelSpacing;
	private String  url;		
	

	public String getPixelSpacing()
	{
		return pixelSpacing;
	}

	public void setPixelSpacing(String pixelSpacing)
	{
		this.pixelSpacing = pixelSpacing;
	}

	public String getSopInstanceUid()
	{
		return sopInstanceUid;
	}

	public void setSopInstanceUid(String sopInstanceUid)
	{
		this.sopInstanceUid = sopInstanceUid;
	}

	public Integer getInstanceNumber()
	{
		return instanceNumber;
	}

	public void setInstanceNumber(Integer instanceNumber)
	{
		this.instanceNumber = instanceNumber;
	}

	public Integer getColumns()
	{
		return columns;
	}

	public void setColumns(Integer columns)
	{
		this.columns = columns;
	}

	public Integer getRows()
	{
		return rows;
	}

	public void setRows(Integer rows)
	{
		this.rows = rows;
	}

	public String getFrameOfReferenceUID()
	{
		return frameOfReferenceUID;
	}

	public void setFrameOfReferenceUID(String frameOfReferenceUID)
	{
		this.frameOfReferenceUID = frameOfReferenceUID;
	}

	public String getImagePositionPatient()
	{
		return imagePositionPatient;
	}

	public void setImagePositionPatient(String imagePositionPatient)
	{
		this.imagePositionPatient = imagePositionPatient;
	}

	public String getImageOrientationPatient()
	{
		return imageOrientationPatient;
	}

	public void setImageOrientationPatient(String imageOrientationPatient)
	{
		this.imageOrientationPatient = imageOrientationPatient;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}
	
}
