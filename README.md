# XNAT OHIF Viewer Plugin #

This is the XNAT 1.7 OHIF Viewer plugin. This plugin integrates the OHIF Cornerstone-based stand-alone viwer into
XNAT. It replaces previous support for the XimgViewer plugin.

# Building # (Note an up to date distribution is available in the repo under /dist)

To build the XNAT OHIF viewer plugin

1. If you haven't already, clone [this repository](https://bitbucket.org/xnatx/ohif-viewer-plugin.git) and cd to the newly cloned folder.

2. Build the plugin:

    `./gradlew clean fatjar` 
    
    On Windows, you can use the batch file:
    
    `gradlew.bat clean fatjar`
    
    This should build the plugin in the file **build/libs/ohif-viewer-plugin-1.0.0-SNAPSHOT.jar** 
    (the version may differ based on updates to the code).
    
3. Copy the plugin jar to your plugins folder: 

    `cp build/libs/ohif-viewer-plugin-1.0.0-SNAPSHOT.jar /data/xnat/home/plugins`

# Deploying the Plugin #

Deploying your XNAT plugin requires the following steps:

1. Stop your tomcat with "sudo service tomcat7 stop"

2. Copy the dist/ohif-viewer-X.X-SNAPSHOT.jar plugin to the **plugins** folder for your XNAT installation. The location of the 
**plugins** folder varies based on how and where you have installed your XNAT. If you are running 
a virtual machine created through the [XNAT Vagrant project](https://bitbucket/xnatdev/xnat-vagrant.git),
you can copy the plugin to the appropriate configuration folder and then copy it within the VM from 
**/vagrant** to **/data/xnat/home/plugins**.

3. Copy dist/VIEWER.war to the webapps folder of your Tomcat server ( /var/lib/tomcat7/webapps/ by default if using xnat-vagrant).

    (VIEWER.war is the latest production build of https://github.com/JamesAPetts/OHIF-Viewer-XNAT/).

If you are serving your XNAT on your Tomcat's root, e.g. "www.domain.com/":

4. `sudo service tomcat7 start`

5. Enjoy!

If you are serving your XNAT on a subdirectory, e.g. "www.domain.com/XNAT_SERVER/":

4. rename "VIEWER.war" to "XNAT_SERVER#VIEWER.war", where "XNAT_SERVER" is the directory you are serving XNAT under.

5. `sudo service tomcat7 start`

6. in the newly created XNAT_SERVER#VIEWER/index.html: replace "ROOT_URL":"VIEWER" with "ROOT_URL":"XNAT_SERVER/VIEWER", where XNAT_SERVER is the directory you are serving XNAT under (Note this last step is a hotfix and does not require restarting Tomcat again).

7. Enjoy!  

