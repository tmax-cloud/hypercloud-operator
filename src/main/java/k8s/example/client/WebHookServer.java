package k8s.example.client;

import java.io.IOException;

import org.slf4j.Logger;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;
import k8s.example.client.handler.CatalogHandler;
import k8s.example.client.handler.EmailHandler;
import k8s.example.client.handler.NameSpaceClaimHandler;
import k8s.example.client.handler.NameSpaceHandler;
import k8s.example.client.handler.RegistryEventHandler;
import k8s.example.client.handler.ServiceBindingHandler;
import k8s.example.client.handler.ServiceInstanceHandler;
import k8s.example.client.handler.UserHandler;
import k8s.example.client.metering.handler.MeteringHandler;

public class WebHookServer extends RouterNanoHTTPD {
    private Logger logger = Main.logger;
    
    public WebHookServer() throws IOException {
        super(28677);
        addMappings();
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        logger.info("Nano HTTPD is running!!");
    }
  
    @Override
    public void addMappings() {
    	addRoute("/user", UserHandler.class);
    	addRoute("/email", EmailHandler.class);
        addRoute("/v2/catalog", CatalogHandler.class);
        addRoute("/v2/service_instances/:instance_id/service_bindings/:binding_id", ServiceBindingHandler.class);
    	addRoute("/v2/service_instances/:instance_id", ServiceInstanceHandler.class);
    	addRoute("/metering", MeteringHandler.class);
    	addRoute("/nameSpace", NameSpaceHandler.class);
    	addRoute("/nameSpaceClaim", NameSpaceClaimHandler.class);
    	addRoute("/registry/event", RegistryEventHandler.class);
    }
}