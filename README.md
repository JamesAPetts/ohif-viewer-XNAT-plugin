# XNAT-ROI OHIF Viewer Plugin 1.0.0 Beta #

This beta plugin integrates the OHIF viewer into XNAT. It differs from the publically released OHIf Viewer plugin in that it has functionality to create ROIs and save/load these to/from XNAT.
This plugin depends on the xnat-roi plugin for 1.7 (current beta release included in /dist folder).

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


