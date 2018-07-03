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
import icr.etherj.PathScan;
import icr.etherj.dicom.DicomReceiver;
import icr.etherj.dicom.DicomToolkit;
import icr.etherj.dicom.Patient;
import icr.etherj.dicom.PatientRoot;
import icr.etherj.dicom.Series;
import icr.etherj.dicom.SopInstance;
import icr.etherj.dicom.Study;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import org.dcm4che2.data.DicomObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author simond
 * @author jpetts
 */
public class CreateOhifViewerMetadata {
  private static final Logger logger = LoggerFactory.getLogger(CreateOhifViewerMetadata.class);
  private static final DicomToolkit dcmTk = DicomToolkit.getToolkit();
  protected static final ArrayList<String> MULTI_FRAME_SOP_CLASS_UIDS = createMultiFrameSopClassUidList();
  protected static final ArrayList<String> SINGLE_FRAME_SOP_CLASS_UIDS = createSingleFrameImageSopClassUidList();
  
  // TODO move these to a seperate class
  private static ArrayList<String> createMultiFrameSopClassUidList()
  {
        ArrayList<String> result = new ArrayList<>();
        result.add("1.2.840.10008.5.1.4.1.1.3"); // Ultrasound Multiframe Image Storage
        result.add("1.2.840.10008.5.1.4.1.1.3.1"); // Ultrasound Multiframe Image Storage
        result.add("1.2.840.10008.5.1.4.1.1.7.1"); // Multiframe Single Bit Secondary Capture Image Storage
        result.add("1.2.840.10008.5.1.4.1.1.7.2"); // Multiframe Grayscale Byte Secondary Capture Image Storage
        result.add("1.2.840.10008.5.1.4.1.1.7.3"); // Multiframe Grayscale Word Secondary Capture Image Storage
        result.add("1.2.840.10008.5.1.4.1.1.7.4"); // Multiframe True Color Secondary Capture Image Storage
        result.add("1.2.840.10008.5.1.4.1.1.77.2"); // VL Multiframe Image Storage 
        return result;
  }
  
  private static ArrayList<String> createSingleFrameImageSopClassUidList()
  {
        ArrayList<String> result = new ArrayList<>();
        result.add("1.2.840.10008.5.1.4.1.1.1"); // CR Image Storage
        result.add("1.2.840.10008.5.1.4.1.1.1.1"); // Digital X-Ray Image Storage – for Presentation
        result.add("1.2.840.10008.5.1.4.1.1.1.1.1"); //	Digital X-Ray Image Storage – for Processing
        result.add("1.2.840.10008.5.1.4.1.1.1.2"); // Digital Mammography X-Ray Image Storage – for Presentation
        result.add("1.2.840.10008.5.1.4.1.1.1.3"); //	Digital Intra – oral X-Ray Image Storage – for Presentation	 
        result.add("1.2.840.10008.5.1.4.1.1.1.3.1"); //	Digital Intra – oral X-Ray Image Storage – for Processing	 
        result.add("1.2.840.10008.5.1.4.1.1.10"); //	Standalone Modality LUT Storage 	Retired
        result.add("1.2.840.10008.5.1.4.1.1.104.1"); //	Encapsulated PDF Storage	 
        result.add("1.2.840.10008.5.1.4.1.1.11"); //	Standalone VOI LUT Storage 	Retired
        result.add("1.2.840.10008.5.1.4.1.1.11.1"); //	Grayscale Softcopy Presentation State Storage SOP Class	 
        result.add("1.2.840.10008.5.1.4.1.1.11.2"); //	Color Softcopy Presentation State Storage SOP Class	 
        result.add("1.2.840.10008.5.1.4.1.1.11.3"); //	Pseudocolor Softcopy Presentation Stage Storage SOP Class 	 
        result.add("1.2.840.10008.5.1.4.1.1.11.4"); //	Blending Softcopy Presentation State Storage SOP Class	 
        result.add("1.2.840.10008.5.1.4.1.1.12.1"); //	X-Ray Angiographic Image Storage	 
        result.add("1.2.840.10008.5.1.4.1.1.12.1.1"); //	Enhanced XA Image Storage	 
        result.add("1.2.840.10008.5.1.4.1.1.12.2"); //	X-Ray Radiofluoroscopic Image Storage	 
        result.add("1.2.840.10008.5.1.4.1.1.12.2.1"); //	Enhanced XRF Image Storage	 
        result.add("1.2.840.10008.5.1.4.1.1.12.3"); //	X-Ray Angiographic Bi-plane Image Storage 	Retired
        result.add("1.2.840.10008.5.1.4.1.1.128"); //	Positron Emission Tomography Curve Storage 	Retired
        result.add("1.2.840.10008.5.1.4.1.1.129"); //	Standalone Positron Emission Tomography Curve Storage 	Retired
        result.add("1.2.840.10008.5.1.4.1.1.2"); //	CT Image Storage	 
        result.add("1.2.840.10008.5.1.4.1.1.2.1"); //	Enhanced CT Image Storage	 
        result.add("1.2.840.10008.5.1.4.1.1.20"); //	NM Image Storage	 
        result.add("1.2.840.10008.5.1.4.1.1.3"); //	Ultrasound Multiframe Image Storage 	Retired
        result.add("1.2.840.10008.5.1.4.1.1.3.1"); //	Ultrasound Multiframe Image Storage	 
        result.add("1.2.840.10008.5.1.4.1.1.4"); //	MR Image Storage	 
        result.add("1.2.840.10008.5.1.4.1.1.4.1"); //	Enhanced MR Image Storage	 
        result.add("1.2.840.10008.5.1.4.1.1.4.2"); //	MR Spectroscopy Storage
        result.add("1.2.840.10008.5.1.4.1.1.6"); //	Ultrasound Image Storage
        result.add("1.2.840.10008.5.1.4.1.1.6.1"); //	Ultrasound Image Storage
        result.add("1.2.840.10008.5.1.4.1.1.7"); // Secondary Capture Image Storage
        result.add("1.2.840.10008.5.1.4.1.1.77.1"); //	VL (Visible Light) Image Storage 	Retired
        result.add("1.2.840.10008.5.1.4.1.1.77.1.1"); //	VL endoscopic Image Storage	 
        result.add("1.2.840.10008.5.1.4.1.1.77.1.1.1"); //	Video Endoscopic Image Storage	 
        result.add("1.2.840.10008.5.1.4.1.1.77.1.2"); //	VL Microscopic Image Storage	 
        result.add("1.2.840.10008.5.1.4.1.1.77.1.2.1"); //	Video Microscopic Image Storage	 
        result.add("1.2.840.10008.5.1.4.1.1.77.1.3"); //	VL Slide-Coordinates Microscopic Image Storage	 
        result.add("1.2.840.10008.5.1.4.1.1.77.1.4"); //	VL Photographic Image Storage	 
        result.add("1.2.840.10008.5.1.4.1.1.77.1.4.1"); //	Video Photographic Image Storage	 
        result.add("1.2.840.10008.5.1.4.1.1.77.1.5.1"); //	Ophthalmic Photography 8-Bit Image Storage	 
        result.add("1.2.840.10008.5.1.4.1.1.77.1.5.2"); //	Ophthalmic Photography 16-Bit Image Storage
        return result;
  }
  
  private final String xnatScanPath;
  private final String xnatExperimentScanUrl;
  private final HashMap<String,String> seriesUidToScanIdMap;
  
  public CreateOhifViewerMetadata(final String xnatScanPath, final String xnatExperimentScanUrl, final HashMap<String,String> seriesUidToScanIdMap)
  {
    this.xnatScanPath = xnatScanPath;
    this.xnatExperimentScanUrl = xnatExperimentScanUrl;
    this.seriesUidToScanIdMap = seriesUidToScanIdMap;
  }    
    
  public String jsonify(final String transactionId)
  {
    String serialisedOvi = "";

    // Use Etherj to do the heavy lifting of sifting through all the scan data.
    PatientRoot root = scanPath(xnatScanPath);
    // Transform the Etherj output into a java object with the structure needed
    // by the OHIF viewer.
    OhifViewerInput ovi = createInput(transactionId, root);

    // Convert the Java object to a JSON string
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    serialisedOvi = gson.toJson(ovi);

    return serialisedOvi;
  }
  

  private PatientRoot scanPath(String path)
  {
    logger.debug("DICOM search: " + path);

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
      logger.error(ex.getMessage(), ex);
    }
    return root;
  }

  
  private OhifViewerInput createInput(String transactionId, PatientRoot root)
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
          
          String scanId = seriesUidToScanIdMap.get(ser.getUid());

          List<SopInstance> sopList = ser.getSopInstanceList();
          for (SopInstance sop : sopList)
          {
            if (SINGLE_FRAME_SOP_CLASS_UIDS.contains(sop.getSopClassUid()))
            {
              OhifViewerInputInstanceSingle oviInst = new OhifViewerInputInstanceSingle(sop, ser, this.xnatExperimentScanUrl, scanId);
              oviSer.addInstances(oviInst);
            }
            else if (MULTI_FRAME_SOP_CLASS_UIDS.contains(sop.getSopClassUid()))
            {
              OhifViewerInputInstanceMulti oviInst = new OhifViewerInputInstanceMulti(sop, ser, this.xnatExperimentScanUrl, scanId);
              oviSer.addInstances(oviInst);
            }
            
          }
        }
      }
    }

    return ovi;
  }
  
}
