# XNAT-OHIF Viewer Plugin 1.15.2 Beta

<p align="center">
  <img src="assets/Logo.png" width="256" title="OHIF-XNAT-logo">
</p>

This beta plugin integrates the OHIF viewer into XNAT. It differs from the publicly released OHIf Viewer plugin in that it has functionality to create ROIs and save/load these to/from XNAT.
Up to date viewer jars are available in the dist directory.

**PLEASE DO NOT ATTEMPT TO USE IN PRODUCTION AT THIS STAGE.**

**1.15.2 Beta**
Fixes:
- Updated DICOM SEG version to properly add ReferencedSOPClassUIDs to relevant lists. The individual segmentations now come up in the ROI list in the ROICollection view.

**1.15.1 Beta**
Features:
- Navigation from View Subject/View Session:
 - These buttons now open the viewer directly in the window, as this has much better support for mobile.

**1.15.0 Beta**
Features:
- Navigation Bar:
 - A navigation bar can now be accessed by opening the right-hand tab in the viewer.
 - The navigation bar allows you to switch to a different subject/session, allowing you to segment many scans without leaving the viewer.
 - If a subject/session is shared from another project, the projectId of its parent is also displayed.
 - Under "This Project", the current project is displayed, with the current subject/session in yellow.
 - You may navigate to other projects via the "Other Projects" menu.
 - The navigation bar fetches data on Projects/Subjects/Sessions as requested, providing a quick and responsive UI.

A full list of changes are available in the `CHANGELOG`.

Please check the issues page. Label new issues with the "XNAT-ROI Beta" tag as well as relevant tags (e.g. enhancement, bug, etc).
Please check that the issue does not already have an existing thread.

**This plugin is in the testing phase and not yet meant to be used in production**

# A) Deploying the Pre-built plugin

**New since 1.0.0: single plugin deployment.**

1. Stop your tomcat with "sudo service tomcat7 stop"

2. Copy both plugins in the `dist` directory to the **plugins** directory of your XNAT installation. The location of the
   **plugins** folder varies based on how and where you have installed your XNAT. If you are running
   a virtual machine created through the [XNAT Vagrant project](https://bitbucket/xnatdev/xnat-vagrant.git),
   you can copy the plugin to the appropriate configuration folder and then copy it within the VM from
   **/vagrant** to **/data/xnat/home/plugins**.

3. `sudo service tomcat7 start`

# B) (Optional) Initialising the viewer in a populated database

In the likely event you are installing this plugin on an XNAT with an already populated database, an admin may call the REST command \*\*POST XNAT_ROOT_URL/xapi/viewer/generate-all-metadata\*\* in order to initiate a process that will scour the database.

# Upgrading from 0.X.X

As of 1.0.0, the automatic JSON generation on session creation/modification is now handled by server side event handlers, and hosting the old VIEWER.war as a seperate application is no longer required.

If you are upgrading the plugin from 0.X.X:

1. Deactivate any related automation scripts in the Administration/Automation interface.
2. Remove the old VIEWER.war, the VIEWER is now included within the plugin jar.
3. replace the old plugin jar with the new one.

# Building (Note this is for developers and is not required to use the plugin)

To build the XNAT OHIF viewer plugin

1. If you haven't already, clone [this repository](https://bitbucket.org/xnatx/ohif-viewer-plugin.git) and cd to the newly cloned folder.

2. Build the plugin:

   `./gradlew clean fatjar`

   On Windows, you can use the batch file:

   `gradlew.bat clean fatjar`

   This should build the plugin in the file **build/libs/ohif-viewer-plugin-X.X.X-SNAPSHOT.jar**
   Note: the fatjar command is currently required as EtherJ.jar is not currently hosted in a place gradle can find it, this will change in the future.
   The viewer application itself (src/main/resources/META-INF/resources/VIEWER) is a custom build of a modified OHIF viewer (https://github.com/JamesAPetts/OHIF-Viewer-XNAT/tree/xnat-prod).
