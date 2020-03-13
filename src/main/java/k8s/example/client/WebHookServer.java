package k8s.example.client;

import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;
import k8s.example.client.handler.AuthHandler;
import k8s.example.client.handler.CatalogHandler;
import k8s.example.client.handler.LoginHandler;
import k8s.example.client.handler.LogoutHandler;
import k8s.example.client.handler.RefreshHandler;

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
    }
}