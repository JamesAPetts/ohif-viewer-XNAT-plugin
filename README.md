# XNAT-ROI OHIF Viewer Plugin 1.10.0 Beta #

This beta plugin integrates the OHIF viewer into XNAT. It differs from the publically released OHIf Viewer plugin in that it has functionality to create ROIs and save/load these to/from XNAT.
This plugin depends on the xnat-roi plugin for 1.7.4.1 (current beta release included in /dist folder).

**PLEASE DO NOT ATTEMPT TO USE IN PRODUCTION AT THIS STAGE.**

# Public beta: #

Up to date viewer jars are available in the dist directory.

**1.10.0**
UX/UI:
- Sessions shared into projects can now be viewed in the viewer.
- Fullscreen viewer. After some architectural changes to the viewer's router, the viewer no longer has to sit in an XNAT window to opperate. The viewer now opens fullscreen in a seperate tab instead of appearing in an iframe.

**1.9.3**
UX:
- Updated to latest version of cornerstoneTools.
  - Freehand spacing is based on image resolution, not canvas size/zoom.
  - Freehand sculpter is more effecient and clever. The max size is adapted and it only adds new points where they are needed to alter the geometry.
- When clicking on 'draw' or 'paint' for the first time on a new scan, the dialog for metadata input appears immediately, as opposed to requiring a click first.

**1.9.2**
- Improved select -> drag UX of the freehand sculpter.
- Fix edge case of erroneous "export failed" message when the export actually succeeded.
- Fixed ctrl + click which broken in cornerstoneTools V3 (also pushed back to the cTools repo).

**1.9.1**
Temporary Feature:
- If you edit a NIFTI mask you can save back as SEG. Eventually it will just save back to native NIFTI.

**1.9.0**
Features:
- NIFTI mask import. You can now import NIFTI-based RoiCollections. An uploader capable of uploading NIFTI will come in the future.
  - NOTE: There is no NIFTI export yet, and you will be warned of this when you try to export.
  - NOTE: You can currently only view NIFTI masks that map onto DICOM images. A full NIFTI workflow is in the works.
- If you are drawing on an imported NIFTI mask, the brush mode is set to non-overlapping, i.e. drawing with one color overwrites another, and the mask data more closely resembles a NIFTI formatted file.
- You can now rotate and flip the image and the brush layer will react accordingly. You can paint the mask from any orientation.
- The client side backup feature has been disabled for now, as it needs a few optimisations to be useful for large NIFTI masks without significantly inhibiting UX. I will come back to this feature in the future and multithread it with webworkers.

**1.8.0**

Features:
- DICOM-SEG import and export. Saves as RoiCollections of type SEG. There is currently a hard cap of 20 segmentations per series, but this limit will be removed in future developments.
- Client side edge server. A big new feature is that Masks and contour based ROIs are backed up every minute in your browser. Should your browser crash, or your internet drop, etc. You will be given the option to recover this data when you return to view the session. The local backups are deleted if you choose not to restore the data, or when data is successfully pushed back to XNAT. No sensitive information is held on the client side database. Series instance UIDs are hashed using a one way hashing algorithm, and this hash is used to relate the data to a particular series.
- Reference lines back in! Feature parity with the old cornerstoneTools v2 version of the view.

Bug-Fixes:
- Fixed passive cornerstoneTools functionality being broken in 1.7.2.
- Fixed memory leak issue in OHIF viewer.

UI:
- New UI to go along with DICOM-SEG support.
- Cleaned up and prettified some existing UI dialogs.


**1.7.2**

Bug-Fixes:
- Bumped cornerstoneTools dev version to fix issues with broken ROI tools. This broke reference lines, which will be fixed soon.

**1.7.1**

Bug-Fixes:
- Fixed stack scroll with keyboard and bar.
- Fixed reference lines.

**1.7.0**

CornerstoneTools v3:
- Upgraded to a pre-release of cornerstoneTools v3! On the surface not a lot will have appeared to change yet, appart from tool UX bellow. But the new major version of library opens the doors to lots of possibilities going forward.
- Improved the speed of all ROI UI/caching. This will likely only be noticable in larger studies.
- The image screenshot functionality has been removed whilst the OHIF foundation updates it to v3. When it returns we plan to be able to store the generated images to XNAT.

Features:
- Brush Tool: New segmentation mask brush tool from cornerstoneTools v3. One may multiple 3D segmentations, and they may overlap. There are instructions available in the help menu. Export to DICOM-SEG and NIFTI masks to XNAT will come in the future.

UX:
- Freehand Draw: No more shift clicking is required for 'pencil mode'. To draw using the pencil simply click and drag , release the mouse to close the ROI. Click-move-click to use polygon mode. You can now freely switch between the two modes during use.
- Freehand Sculpter: No more ctrl-click needed. Double click near an ROI to select it. A live preview of the toolsize can be seen during use, making it a lot easier to do precision sculpting. Just try it!

Bug-Fixes:
- Fixes erroneous mapping of RTSTRUCT contours onto images for some edge case orientations.

**1.6.2**
UI:
- Moved the smooth toggle to the viewport overlay, under sync. Smoothing is off by default.


**1.6.1**
Bug-Fixes:
- Fixes cases where the URLs of the package libraries would sometimes be prepended by multiple slashes, causing no problems to the user, but causing Tomcat to throw exceptions to the log on the backend.


**1.6.0**
Features:
- Added a toggle for image smoothing, as it is sometimes necessary to see each pixel clearly when segmenting small objects.
- Added serverside creation of RTSTRUCT when roiCollection is exported to AIM. Both representations will appear under the roiCollection in XNAT.


**1.5.4**
Bug-Fixes:
- Changing tool whilst midway through drawing an ROI will finish that roi instead of doing crazy things.


**1.5.3**

Bug-Fixes:
- Fix broken help menu button.


**1.5.2**
Bug-Fixes:

- Updated EtherJ-Core and EtherJ-XNAT Versions. This fixes issues that occured whilst converting to and from AIM and RTSTRUCT using the ROI api.


**1.5.1**
Bug-Fixes:

- Fixed Issue #6.

**1.5.0**

Features:

- Clicking import ROIs now brings up a menu which allows importing of specific ROI collections, instead of importing all that the AsyncRoiFetcher can find. You can still import all if you wish.

UI:

- Thanks to updates in the ROI plugin's schema. Only collections that reference the active series are shown in the import menu (Previously we had to grab each file and parse it to see if it references the current series, so even importing all eligile collections can be significantly faster if ROI Collections exist in the XNAT Session that reference other scans.

- If an ROI Collection has already been imported, it won't appear on the import list again.

- A message is displayed to tell the user there are no ROI Collections to import if either there are none in the Session that reference the series, or they have all already been imported.

UX:

- The import menu now pops up instantly and fills asynchronusly as it recieves data from the backend.

**1.4.0**

Features:

- The volume management dialog now gives the option to disable the visbility of imported roiCollections, such that only a selection of the collections are rendered and interactable.

**1.3.0**

Features:

Synchronization:

- Using the tick boxes at the top right of each viewport, one can synchronise scans to scroll together.
- The 'Sync Settings' menu has the option to set all viewports to be synced by default.
- The 'Sync Settings' menu allows you to configure the synchronization to opperate via image position, or by frame index. Image position is the default.

Export:
- The user can now choose which ROIs to export as a collection, instead of exporting all ROI's by default.
- The exported ROIs will become locked as before, and will be listed as an ROI collection in the ROI management interface.

UI:
- Consolidated Help to a single interface.
- The ROI management UI now displays ROIs in organised ROI collections when imported.
- New export UI.

** 1.2.1 - Please use BOTH new plugins jars in the dist directory**

UI:

- Cleaned up UI of subject and project views, so that they don't list roiCollections. roiCollections are still listed in the Session they correspond to (Thanks James D'Arcy!).

**1.2.0:**

Features:

Delete tools. Under the delete menu there is:

- The Eraser tool, which can delete any annotation by clicking on its handle. Note there is no highlighting of the tools being deleted currently. This will come later once we have a more developed cornerstoneTools API.

- Clear, which deletes all annotations on the slice.

- Locked ROIs cannot be deleted by the Eraser tool, or by Clear.

UI:

- New Icons for import/export

- XNAT logo overlaying OHIF logo on the top left.

- ROI plugin version number top left.

- Side bar open by default.


**1.1.0:**

Features:

- Full multiframe support. I've tested on both primary and secondary multi-frame DICOMs and can sucuessfully save/load AIMs to these. If anyone has an RTSTRUCT that references a multiframe DICOM I would appreciate the data!

Bug fixes:

- I painfully went through the list of SOP classes and selected what the viewer should and should not attempted to load. If any non-imaging resources sit as "scans", the viewer will no longer try to load these.

- If image assessors other than the roiCollections are present in the assessor list, the viewer will now count the correct number of roiCollections and won't hang on import.



Please check the issues page. Label new issues with the "XNAT-ROI Beta" tag as well as relevent tags (e.g. enhancement, bug, etc).
Please check that the issue does not already have an existing thread.

Thank you!

**This plugin is in the testing phase and not yet meant to be used in production**

# A) Deploying the Pre-built plugin #

**New since 1.0.0: single plugin deployment.**

1. Stop your tomcat with "sudo service tomcat7 stop"

2. Copy the dist/ohif-viewer-x.y.z-SNAPSHOT.jar plugin to the **plugins** folder for your XNAT installation. The location of the
**plugins** folder varies based on how and where you have installed your XNAT. If you are running
a virtual machine created through the [XNAT Vagrant project](https://bitbucket/xnatdev/xnat-vagrant.git),
you can copy the plugin to the appropriate configuration folder and then copy it within the VM from
**/vagrant** to **/data/xnat/home/plugins**.

5. `sudo service tomcat7 start`

# B) (Optional) Initialising the viewer in a populated database #

In the likely event you are installing this plugin on an XNAT with an already populated database, an admin may call the REST command **POST XNAT_ROOT_URL/xapi/viewer/generate-all-metadata** in order to initiate a process that will scour the databas$

# Upgrading from 0.X.X #

As of 1.0.0, the automatic JSON generation on session creation/modification is now handled by server side event handlers, and hosting the old VIEWER.war as a seperate application is no longer required.

If you are upgrading the plugin from 0.X.X:
1. Deactivate any related automation scripts in the Administration/Automation interface.
2. Remove the old VIEWER.war, the VIEWER is now included within the plugin jar.
3. replace the old plugin jar with the new one.


# Building (Note this is for developers and is not required to use the plugin) #

To build the XNAT OHIF viewer plugin

1. If you haven't already, clone [this repository](https://bitbucket.org/xnatx/ohif-viewer-plugin.git) and cd to the newly cloned folder.

2. Build the plugin:

    `./gradlew clean fatjar`

    On Windows, you can use the batch file:

    `gradlew.bat clean fatjar`

    This should build the plugin in the file **build/libs/ohif-viewer-plugin-X.X.X-SNAPSHOT.jar**
    Note: the fatjar command is currently required as EtherJ.jar is not currently hosted in a place gradle can find it, this will change in the future.
    The viewer application itself (src/main/resources/META-INF/resources/VIEWER) is a custom build of a modified OHIF viewer (https://github.com/JamesAPetts/OHIF-Viewer-XNAT/tree/xnat-prod).


