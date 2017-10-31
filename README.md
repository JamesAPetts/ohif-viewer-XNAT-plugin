# XNAT OHIF Viewer Plugin #

This is the XNAT 1.7 OHIF Viewer plugin. This plugin integrates the OHIF Cornerstone-based stand-alone viwer into
XNAT. It replaces previous support for the XimgViewer plugin.

# Building #

To build the XNAT OHIF viewer plugin

1. If you haven't already, clone [this repository](https://bitbucket.org/xnatx/ohif-viewer-plugin.git) and cd to the newly cloned folder.

1. Build the plugin:

    `./gradlew clean jar distZip` 
    
    On Windows, you can use the batch file:
    
    `gradlew.bat clean jar distZip`
    
    This should build the plugin in the file **build/libs/ohif-viewer-plugin-1.0.0-SNAPSHOT.jar** 
    (the version may differ based on updates to the code).
    
1. Copy the plugin jar to your plugins folder: 

    `cp build/libs/ohif-viewer-plugin-1.0.0-SNAPSHOT.jar /data/xnat/home/plugins`

# Deploying the Plugin #

Deploying your XNAT plugin requires the following steps:

1. Copy the plugin jar to the **plugins** folder for your XNAT installation. The location of the 
**plugins** folder varies based on how and where you have installed your XNAT. If you are running 
a virtual machine created through the [XNAT Vagrant project](https://bitbucket/xnatdev/xnat-vagrant.git),
you can copy the plugin to the appropriate configuration folder and then copy it within the VM from 
**/vagrant** to **/data/xnat/home/plugins**.

Once you've completed these steps, restart the Tomcat server. Your new plugin will be available as soon 
as the restart and initialization process is completed.

