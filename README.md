# XNAT OHIF Viewer Plugin #

This is the beta version of the XNAT 1.7 OHIF Viewer plugin. This plugin integrates the OHIF Cornerstone-based stand-alone viwer into
XNAT. It replaces previous support for the XimgViewer plugin.

# A) Deploying the Pre-built plugin #

Deploying your XNAT plugin requires the following steps:

1. Stop your tomcat with "sudo service tomcat7 stop"

2. Copy the dist/ohif-viewer-x.y.z-SNAPSHOT.jar plugin to the **plugins** folder for your XNAT installation. The location of the 
**plugins** folder varies based on how and where you have installed your XNAT. If you are running 
a virtual machine created through the [XNAT Vagrant project](https://bitbucket/xnatdev/xnat-vagrant.git),
you can copy the plugin to the appropriate configuration folder and then copy it within the VM from 
**/vagrant** to **/data/xnat/home/plugins**.

3. Copy dist/VIEWER.war to the webapps directory of your Tomcat server ( /var/lib/tomcat7/webapps/ by default if using xnat-vagrant).

    (**NOTE**: when upgrading the plugin you **must** use the latest VIEWER.war. The latest production build is included in the distribution (The latest build of https://github.com/JamesAPetts/OHIF-Viewer-XNAT/).

If you are serving your XNAT on your Tomcat's root, e.g. "www.domain.com/":

4. `sudo service tomcat7 start`

5. Move on to step B

If you are serving your XNAT on a subdirectory, e.g. "www.domain.com/XNAT_SERVER/":

4. rename "VIEWER.war" to "XNAT_SERVER#VIEWER.war", where "XNAT_SERVER" is the directory you are serving XNAT under.

5. `sudo service tomcat7 start`

6. in the newly created XNAT_SERVER#VIEWER/index.html: replace "ROOT_URL":"VIEWER" with "ROOT_URL":"XNAT_SERVER/VIEWER", where XNAT_SERVER is the directory you are serving XNAT under (Note this last step is a hotfix and does not require restarting Tomcat again).

# B) (Optional) Initialising the viewer in a populated database #

In the likely event you are installing this plugin on an XNAT with an already populated database, an admin may call the REST command **POST XNAT_ROOT_URL/xapi/viewer/generate-all-metadata** in order to initiate a process that will scour the database and generate metadata for every session. This process will use as many threads as are available on the server machine, but may take a long time. Note this step is not essential to use the viewer, however if this step is skipped the user will incur a delay when a particular experiment is displayed in the viewer for the first time.

# Upgrading from 0.4.0 or earlier #

As of 0.5.0 the automatic JSON generation on session creation/modification is now handled by server side event handlers, as such manual configuration of automated Groovy scripts is  no longer required.

If you are upgrading the plugin from 0.4.0 or earlier, deactivate the automation scripts in the Administration/Automation interface, as these are no longer necessary.


# Building (Note this is for developers and is not required to use the plugin) #

To build the XNAT OHIF viewer plugin

1. If you haven't already, clone [this repository](https://bitbucket.org/xnatx/ohif-viewer-plugin.git) and cd to the newly cloned folder.

2. Build the plugin:

    `./gradlew clean fatjar`

    On Windows, you can use the batch file:

    `gradlew.bat clean fatjar`

    This should build the plugin in the file **build/libs/ohif-viewer-plugin-X.X.X-SNAPSHOT.jar**
    Note: the fatjar command is currently required as EtherJ.jar is not currently hosted in a place gradle can find it, this will change in the future.
