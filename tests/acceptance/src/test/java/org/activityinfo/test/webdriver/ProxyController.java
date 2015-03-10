package org.activityinfo.test.webdriver;

import cucumber.runtime.java.guice.ScenarioScoped;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.extras.SelfSignedMitmManager;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.openqa.selenium.Proxy;

import java.util.Random;

/**
 * Configures a proxy between WebDriver and the 
 * server under test so we can simulate specific connection conditions
 */
@ScenarioScoped
public class ProxyController {
    
    private static final Random RANDOM_PORT = new Random();
    
    private int proxyPort;
    private HttpProxyServer proxyServer;

    public ProxyController() {
        this.proxyPort = 4000 + RANDOM_PORT.nextInt(2000);
    }

    public Proxy webDriverProxy() {
        String proxy = "localhost:" + proxyPort;
        return new Proxy()
                .setHttpProxy(proxy)
                .setSslProxy(proxy);
    }
    
    public void start() {
        this.proxyServer = DefaultHttpProxyServer.bootstrap()
                .withPort(proxyPort)
                .withManInTheMiddle(new SelfSignedMitmManager())
                .start();
    }
    
    public void stop() {
        this.proxyServer.stop();
    }
 
    
}
