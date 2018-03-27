/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nrg.xnatx.ohifviewer.inputcreator;

import etherj.dicom.Series;
import etherj.dicom.SopInstance;
import org.nrg.dcm.SOPModel;

/**
 *
 * @author jpetts
 */
public abstract class OhifViewerInputInstance extends OhifViewerInputItem {
  private String  sopInstanceUid;
	private Integer instanceNumber;
	private Integer columns;
	private Integer rows;
	private String  frameOfReferenceUID;
	private String  imagePositionPatient;
	private String  imageOrientationPatient;
	private String  pixelSpacing;
  // @simond: Here's the bit that needs changing when we decide exactly how we want to store the files.
  //private static final String SUBDIRECTORY = "/resources/DICOM/files/";
  protected static final String RESOURCES = "/resources/";
  protected static final String FILES = "/files/";
  
  public OhifViewerInputInstance(SopInstance sop, Series ser, String xnatScanUrl, String scanId)
  {
    setSopInstanceUid(sop.getUid());
    setInstanceNumber(sop.getInstanceNumber());
    setColumns(sop.getColumnCount());
    setRows(sop.getRowCount());
    setFrameOfReferenceUID(sop.getFrameOfReferenceUid());
    setImagePositionPatient(dbl2DcmString(sop.getImagePositionPatient()));
    setImageOrientationPatient(dbl2DcmString(sop.getImageOrientationPatient()));
    setPixelSpacing(dbl2DcmString(sop.getPixelSpacing()));
  }
  
  protected String getResourceType(String sopClassUid)
  {
    
    String resourceType;
    if (SOPModel.isPrimaryImagingSOP(sopClassUid))
    {
      resourceType = "DICOM";
    }
    else
    {
      resourceType = "secondary";
    }
    
    return resourceType;
  }

	public String getPixelSpacing()
	{
		return pixelSpacing;
	}

	private void setPixelSpacing(String pixelSpacing)
	{
		this.pixelSpacing = pixelSpacing;
	}

	public String getSopInstanceUid()
	{
		return sopInstanceUid;
	}

	private void setSopInstanceUid(String sopInstanceUid)
	{
		this.sopInstanceUid = sopInstanceUid;
	}

	public Integer getInstanceNumber()
	{
		return instanceNumber;
	}

	private void setInstanceNumber(Integer instanceNumber)
	{
		this.instanceNumber = instanceNumber;
	}

	public Integer getColumns()
	{
		return columns;
	}

	private void setColumns(Integer columns)
	{
		this.columns = columns;
	}

	public Integer getRows()
	{
		return rows;
	}

	private void setRows(Integer rows)
	{
		this.rows = rows;
	}

	public String getFrameOfReferenceUID()
	{
		return frameOfReferenceUID;
	}

	private void setFrameOfReferenceUID(String frameOfReferenceUID)
	{
		this.frameOfReferenceUID = frameOfReferenceUID;
	}

	public String getImagePositionPatient()
	{
		return imagePositionPatient;
	}

	private void setImagePositionPatient(String imagePositionPatient)
	{
		this.imagePositionPatient = imagePositionPatient;
	}

	public String getImageOrientationPatient()
	{
		return imageOrientationPatient;
	}

	private void setImageOrientationPatient(String imageOrientationPatient)
	{
		this.imageOrientationPatient = imageOrientationPatient;
	}
}
