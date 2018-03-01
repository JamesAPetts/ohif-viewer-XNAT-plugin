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


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import etherj.PathScan;
import etherj.dicom.DicomReceiver;
import etherj.dicom.DicomToolkit;
import etherj.dicom.Patient;
import etherj.dicom.PatientRoot;
import etherj.dicom.Series;
import etherj.dicom.SopInstance;
import etherj.dicom.Study;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.dcm4che2.data.DicomObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author simond
 * @author jpetts
 */
public class CreateOhifViewerMetadata {
  private static final Logger logger = LoggerFactory.getLogger(CreateOhifViewerMetadata.class);
  private static final DicomToolkit dcmTk = DicomToolkit.getDefaultToolkit();
  
  public String jsonify(String xnatScanPath, String xnatScanUrl, String transactionId)
  {
    
    logger.info("Its JSONifying time!");

    // Use Etherj to do the heavy lifting of sifting through all the scan data.
    PatientRoot root = new DicomReceiver().getPatientRoot();
    try
    {
      root = scanPath(xnatScanPath);
      logger.error("root: " + root.toString());
    }
    catch (Exception ex)
    {
      logger.error(ex.getMessage());
    }

    // Transform the Etherj output into a java object with th structure needed
    // by the OHIF viewer.
    
    OhifViewerInput ovi = new OhifViewerInput();
    try
    {
      ovi = createOhifViewerInput(transactionId, xnatScanUrl, root);
      logger.error("ovi generated!");
    }
    catch (Exception ex)
    {
      logger.error(ex.getMessage());
    }
    
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    String serialisedOvi = "";

    // Serialise the viewer input object to a JSON string.
    try
    {
      serialisedOvi = gson.toJson(ovi);
    }
    catch (Exception ex)
    {
      logger.error(ex.getMessage());
    }
    
    logger.info("JSONification complete, radical!");
    
    return serialisedOvi;
  }
  

  private PatientRoot scanPath(String path)
  {
    logger.error("DICOM search: " + path);

    DicomReceiver dcmRec  = new DicomReceiver();
    PathScan<DicomObject> pathScan = dcmTk.createPathScan();

    pathScan.addContext(dcmRec);
    PatientRoot root = null;
    try
    {
      pathScan.scan(path, true);
      root = dcmRec.getPatientRoot();
    }
    catch (IOException ex)
    {
      logger.warn(ex.getMessage(), ex);
    }
    return root;
  }

  
  private OhifViewerInput createOhifViewerInput(String transactionId, String xnatScanUrl, PatientRoot root)
  {
    OhifViewerInput ovi = new OhifViewerInput();
    List<OhifViewerInputStudy> oviStudyList = new ArrayList<>();

    ovi.setTransactionId(transactionId);
    ovi.setStudies(oviStudyList);

    List<Patient> patList = root.getPatientList();
    for (Patient pat : patList)
    {
      List<Study> studyList = pat.getStudyList();			
      for (Study std : studyList)
      {
        OhifViewerInputStudy oviStd = new OhifViewerInputStudy();
        oviStudyList.add(oviStd);

        oviStd.setStudyInstanceUid(std.getUid());
        oviStd.setPatientName(pat.getName());
        List<OhifViewerInputSeries> oviSeriesList = new ArrayList<>();
        oviStd.setSeriesList(oviSeriesList);

        List<Series> seriesList = std.getSeriesList();
        for (Series ser : seriesList)
        {
          OhifViewerInputSeries oviSer = new OhifViewerInputSeries();
          oviSeriesList.add(oviSer);

          oviSer.setSeriesInstanceUid(ser.getUid());
          oviSer.setSeriesDescription(ser.getDescription());
          oviSer.setSeriesNumber(ser.getNumber());
          List<OhifViewerInputInstance> oviInstanceList = new ArrayList<>();
          oviSer.setInstances(oviInstanceList);

          List<SopInstance> sopList = ser.getSopInstanceList();
          for (SopInstance sop : sopList)
          {
            OhifViewerInputInstance oviInst = new OhifViewerInputInstance();
            oviInstanceList.add(oviInst);

            oviInst.setSopInstanceUid(sop.getUid());
            oviInst.setInstanceNumber(sop.getInstanceNumber());
            oviInst.setColumns(sop.getColumnCount());
            oviInst.setRows(sop.getRowCount());
            oviInst.setFrameOfReferenceUID(sop.getFrameOfReferenceUid());
            oviInst.setImagePositionPatient(dbl2DcmString(sop.getImagePositionPatient()));
            oviInst.setImageOrientationPatient(dbl2DcmString(sop.getImageOrientationPatient()));
            oviInst.setPixelSpacing(dbl2DcmString(sop.getPixelSpacing()));

            // Here's the bit that needs changing when we decide exactly how we want to store the files.
            String file = new File(sop.getPath()).getName();
            oviInst.setUrl(xnatScanUrl + ser.getNumber() + "/resources/DICOM/files/" + file);						
          }
        }
      }
    }

    return ovi;
  }

  private String dbl2DcmString(double[] d)
  {
    StringBuilder sb = new StringBuilder();
    for (int i=0; i<d.length; i++)
    {
      sb.append(d[i]);
      if (i != (d.length-1)) sb.append("\\");
    }
    return sb.toString();
  }
  
}
