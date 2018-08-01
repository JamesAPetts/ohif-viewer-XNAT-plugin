# XNAT-ROI OHIF Viewer Plugin 1.0.0 Beta #

This beta plugin integrates the OHIF viewer into XNAT. It differs from the publically released OHIf Viewer plugin in that it has functionality to create ROIs and save/load these to/from XNAT.
This plugin depends on the xnat-roi plugin for 1.7 (current beta release included in /dist folder).


# Public beta: #

Up to date viewer jars are available in the dist directory.

**Hotfix 1.0.2: Please use BOTH new plugins jars in the dist directory**

New:

Delete tools. Under the delete menu there is:

-- The Eraser tool, which can delete any annotation by clicking on its handle. Note there is no highlighting of the tools being deleted currently. This will come later once we have a more developed cornerstoneTools API.

-- Clear, which deletes all annotations on the slice.

Locked ROIs cannot be deleted by the Eraser tool, or by Clear.

Minor changes:

-- New Icons for import/export

-- XNAT logo overlaying OHIF logo on the top left.

-- ROI plugin version number top left.

-- Side bar open by default.


**Hotfix 1.0.1:**

Fixes:

-- Issue #4 -- If image assessors other than the roiCollections are present in the assessor list, the viewer will now count the correct number of roiCollections and won't hang on import.

New:

-- Issue #2 -- Full multiframe support. I've tested on both primary and secondary multi-frame DICOMs and can sucuessfully save/load AIMs to these. If anyone has an RTSTRUCT that references a multiframe DICOM I would appreciate the data!

-- Issue #5 -- I painfully went through the list of SOP classes and selected what the viewer should and should not attempted to load. If any non-imaging resources sit as "scans", the viewer will no longer try to load these.


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


