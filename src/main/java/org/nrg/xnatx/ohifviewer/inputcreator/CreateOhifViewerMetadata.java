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
  
  public String jsonifyStudy(final String xnatScanPath, final String xnatScanUrl, final String transactionId)
  {
    
    logger.error("Its Study JSONifying time!");
    
    String serialisedOvi = "";
    try
    {
      // Use Etherj to do the heavy lifting of sifting through all the scan data.
      PatientRoot root = scanPath(xnatScanPath);
      // Transform the Etherj output into a java object with the structure needed
      // by the OHIF viewer.
      OhifViewerInput ovi = createStudyInput(transactionId, xnatScanUrl, root);
    
      // Convert the Java object to a JSON string
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      serialisedOvi = gson.toJson(ovi);
    }
    catch (Exception ex)
    {
      logger.error(ex.getMessage());
    }

    logger.error("Study JSONification complete, radical!");
    
    return serialisedOvi;
  }
  
  
  public String jsonifySeries(final String xnatScanPath, final String xnatScanUrl, final String experimentId, final String seriesNo)
  {
    final String transactionId = experimentId + "_" + seriesNo;
    
    logger.error("Its Series JSONifying time!");
    
    String serialisedOvi = "";
    try
    {
      // Use Etherj to do the heavy lifting of sifting through all the scan data.
      PatientRoot root = scanPath(xnatScanPath);
      // Transform the Etherj output into a java object with the structure needed
      // by the OHIF viewer.
      OhifViewerInput ovi = createSeriesInput(seriesNo, transactionId, xnatScanUrl, root);
    
      // Convert the Java object to a JSON string
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      serialisedOvi = gson.toJson(ovi);
    }
    catch (Exception ex)
    {
      logger.error(ex.getMessage());
    }
    
    logger.error("Series JSONification complete, radical!");
    
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

  
  private OhifViewerInput createStudyInput(String transactionId, String xnatScanUrl, PatientRoot root)
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
        OhifViewerInputStudy oviStd = new OhifViewerInputStudy(std, pat);
        oviStudyList.add(oviStd);

        List<Series> seriesList = std.getSeriesList();
        for (Series ser : seriesList)
        {
          OhifViewerInputSeries oviSer = new OhifViewerInputSeries(ser);
          oviStd.addSeries(oviSer);

          List<SopInstance> sopList = ser.getSopInstanceList();
          for (SopInstance sop : sopList)
          {
            OhifViewerInputInstance oviInst = new OhifViewerInputInstance(sop, xnatScanUrl, ser);
            oviSer.addInstances(oviInst);			
          }
        }
      }
    }

    return ovi;
  }
  
  
  // REFACTOR ONCE WORKING, un-needed duplication
  // TODO - Using seriesNo rather than SeriesId... see if the right thing to do
  private OhifViewerInput createSeriesInput(String seriesNo, String transactionId, String xnatScanUrl, PatientRoot root)
  {
    OhifViewerInput ovi = new OhifViewerInput();
    List<OhifViewerInputStudy> oviStudyList = new ArrayList<>();

    ovi.setTransactionId(transactionId);
    ovi.setStudies(oviStudyList);
    
    logger.error("Making json for seriesNo:" + seriesNo);
    
    //TODO fix this function, Current prints out:
    /*
    {
     "transactionId": "XNAT_JPETTS_E00007_1007",
      "studies": [
        {
          "studyInstanceUid": "2.25.216540488264669880754620301267990043721",
          "patientName": "CASE_4_LIVER",
          "seriesList": []
        }
      ]
    }
    */
    

    List<Patient> patList = root.getPatientList();
    for (Patient pat : patList)
    {
      List<Study> studyList = pat.getStudyList();			
      for (Study std : studyList)
      {
        OhifViewerInputStudy oviStd = new OhifViewerInputStudy(std, pat);
        oviStudyList.add(oviStd);

        List<Series> seriesList = std.getSeriesList();
        for (Series ser : seriesList)
        {
          Integer serNo = ser.getNumber();
          if (serNo.toString() == seriesNo)
          {
            OhifViewerInputSeries oviSer = new OhifViewerInputSeries(ser);
            oviStd.addSeries(oviSer);

            List<SopInstance> sopList = ser.getSopInstanceList();
            for (SopInstance sop : sopList)
            {
              OhifViewerInputInstance oviInst = new OhifViewerInputInstance(sop, xnatScanUrl, ser);
              oviSer.addInstances(oviInst);			
            }
            break;
          }
          
        }
      }
    }
    
    
    return ovi;
  }
  
}
