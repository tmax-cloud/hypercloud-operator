package k8s.example.client.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD.GeneralHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;
import k8s.example.client.DataObject.RegistryEventDO;
import k8s.example.client.Main;
import k8s.example.client.Util;

public class RegistryEventHandler extends GeneralHandler {
    private Logger logger = Main.logger;
	@Override
    public Response post(
      UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** POST /registry/event");
		
		Map<String, String> body = new HashMap<String, String>();
        try {
			session.parseBody( body );
		} catch (Exception e) {
			e.printStackTrace();
		}
   
        List<RegistryEventDO> events = null;
		String outDO = null;
		IStatus status = null;
		
		try {
			// Read inDO
			events = new ObjectMapper().readValue(body.get( "events" ), new TypeReference<List<RegistryEventDO>>(){});

			logger.info("  Registry Event Count: " + events.size());
			for( RegistryEventDO event : events) {
				logger.info("    Registry Action: " + event.getAction());
				if ( event.getRequest() != null ) 
					logger.info("    Registry Request Host: " + event.getRequest().getHost());
			}

		}catch (Exception e) {
			logger.info( "Exception message: " + e.getMessage() );
		}
		status = Status.OK;
		outDO = "event_get_success";
			
 		return Util.setCors(NanoHTTPD.newFixedLengthResponse(status, NanoHTTPD.MIME_HTML, outDO));
    }
	
	@Override
    public Response other(
      String method, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** OPTIONS /login");
		
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(""));
    }
}