import org.nrg.xdat.XDAT
import org.nrg.xdat.om.XnatExperimentdata
import org.nrg.xnatx.ohifviewer.inputcreator.RunnableCreateExperimentMetadata;

// Grab the data archive path
String xnatRootURL      = XDAT.getSiteConfigPreferences().getSiteUrl()
String xnatArchivePath  = XDAT.getSiteConfigPreferences().getArchivePath()
String _experimentId = dataId.toString()

// Runs JSON creation process within the active thread.

try {
  RunnableCreateExperimentMetadata createExperimentMetadata = new RunnableCreateExperimentMetadata(xnatRootURL, xnatArchivePath, _experimentId, null);
  createExperimentMetadata.runOnCurrentThread();
} catch (Exception e) {
     println e
}
