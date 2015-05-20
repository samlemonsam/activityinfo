package org.activityinfo.test.ui;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import org.activityinfo.test.pageobject.web.ApplicationPage;
import org.activityinfo.test.pageobject.web.LoginPage;
import org.activityinfo.test.sut.DevServerAccounts;
import org.activityinfo.test.sut.Server;
import org.activityinfo.test.sut.SystemUnderTest;
import org.activityinfo.test.sut.UserAccount;
import org.activityinfo.test.webdriver.PhantomJsInstance;
import org.activityinfo.test.webdriver.ProxiedWebDriver;
import org.activityinfo.test.webdriver.ProxyController;
import org.junit.Test;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.concurrent.TimeUnit;

/**
 * Test suite to verify that the application performs correctly even under very
 * very bad network connections.
 */
public class NetworkUiTest {
    
    
    @Test
    public void navigationHandler() {

        Server server = new Server();
        
        ProxyController proxyController = new ProxyController(new DeferredJsFilterSource());
        proxyController.start();
        
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(CapabilityType.PROXY, proxyController.getWebDriverProxy());

        ChromeDriver webDriver = new ChromeDriver(capabilities);

        DevServerAccounts accounts = new DevServerAccounts();
        UserAccount account = accounts.any();

        LoginPage loginPage = new LoginPage(webDriver, server);
        ApplicationPage applicationPage = loginPage. navigateTo().loginAs(account).andExpectSuccess();
        applicationPage.waitUntilLoaded();
        
        applicationPage.navigateToDataEntryTab();
        
    }
    
    private static class DeferredJsFilterSource extends HttpFiltersSourceAdapter {
        @Override
        public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
            if(originalRequest.getUri().contains("deferredjs")) {
                return new DeferredJsFilter(originalRequest, ctx);    
            }
            return new HttpFiltersAdapter(originalRequest, ctx);
        }
    }
 
    private static class DeferredJsFilter extends HttpFiltersAdapter {

        public DeferredJsFilter(HttpRequest originalRequest, ChannelHandlerContext ctx) {
            super(originalRequest, ctx);
        }

        @Override
        public HttpResponse requestPost(HttpObject httpObject) {
            try {
                Thread.sleep(TimeUnit.MINUTES.toMillis(5));
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted");
            }
            return super.requestPost(httpObject);
        }
    }
    
}
