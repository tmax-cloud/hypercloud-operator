package k8s.example.client;

import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;
import k8s.example.client.handler.AuthClientHandler;
import k8s.example.client.handler.AuthHandler;
import k8s.example.client.handler.CatalogHandler;
import k8s.example.client.handler.LoginHandler;
import k8s.example.client.handler.LoginPageHandler;
import k8s.example.client.handler.LogoutHandler;
import k8s.example.client.handler.RefreshHandler;
import k8s.example.client.handler.ServiceBindingHandler;
import k8s.example.client.handler.ServiceInstanceHandler;

public class WebHookServer extends RouterNanoHTTPD {
    public WebHookServer() throws IOException {
        super(28677);
        addMappings();
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("Nano HTTPD is running!!");
    }
  
    @Override
    public void addMappings() {
    	addRoute("/login", LoginHandler.class);
    	addRoute("/logout", LogoutHandler.class);
    	addRoute("/authenticate", AuthHandler.class);
    	addRoute("/refresh", RefreshHandler.class);
    	addRoute("/v2/catalog", CatalogHandler.class);
    	addRoute("/v2/service_instances/:instance_id/service_bindings/:binding_id", ServiceBindingHandler.class);
    	addRoute("/v2/service_instances/:instance_id", ServiceInstanceHandler.class);
    	addRoute("/authClient", AuthClientHandler.class);
    	addRoute("/loginPage", LoginPageHandler.class);
    }
}