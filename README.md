# XNAT-ROI OHIF Viewer Plugin 1.5.4 Beta #

This beta plugin integrates the OHIF viewer into XNAT. It differs from the publically released OHIf Viewer plugin in that it has functionality to create ROIs and save/load these to/from XNAT.
This plugin depends on the xnat-roi plugin for 1.7 (current beta release included in /dist folder).

**PLEASE DO NOT ATTEMPT TO USE IN PRODUCTION AT THIS STAGE.**

# Public beta: #

Up to date viewer jars are available in the dist directory.

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


