package org.nrg.xnatx.ohifviewer.xapi;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.io.IOUtils;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;

@XapiRestController
@RequestMapping(value = "/viewer")
public class OhifViewerApi {
    @ApiOperation(value = "Returns the session JSON for the specified experiment ID.")
    @ApiResponses({@ApiResponse(code = 200, message = "The session was located and properly rendered to JSON."),
                   @ApiResponse(code = 403, message = "The user does not have permission to view the indicated experiment."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{sessionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public StreamingResponseBody getSessionJson(final @PathVariable String sessionId) throws FileNotFoundException {
        final Reader reader = new FileReader("/tmp/XNAT_E00002.json");
        return new StreamingResponseBody() {
            @Override
            public void writeTo(final OutputStream output) throws IOException {
                IOUtils.copy(reader, output);
            }
        };
    }
}
