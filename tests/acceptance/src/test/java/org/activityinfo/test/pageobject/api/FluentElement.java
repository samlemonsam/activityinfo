package org.activityinfo.test.pageobject.api;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Light-weight wrapper around WebDriver
 */
public class FluentElement {
    
    private WebDriver webDriver;
    private WebElement element;
    

    public FluentElement(WebDriver webDriver, WebElement element) {
        this.webDriver = webDriver;
        this.element = element;
    }

    public FluentElement(WebDriver webDriver) {
        this(webDriver, null);
    }
    
    private SearchContext context() {
        if(element == null) {
            return webDriver;
        } else {
            return element;
        }
    }
    
    public void clickWhenReady() {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                try {
                    element().click();
                    return true;
                    
                } catch (WebDriverException e) {
                    return false;
                }
            }
        });
    }
    
    public URI getCurrentUri() {
        try {
            return new URI(webDriver.getCurrentUrl());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


    public void click() {
        Actions actions = new Actions(webDriver);
        actions.click(element()).perform();
    }

    public WebElement element() {
        Preconditions.checkState(element != null, "no element has been selected yet");
        return element;
    }

    public void waitUntil(Predicate<WebDriver> predicate) {
        WebDriverWait wait = new WebDriverWait(webDriver, 30);
        wait.until(predicate);
    }
    
    public <T> void waitUntil(ExpectedCondition<T> condition) {
        WebDriverWait wait = new WebDriverWait(webDriver, 30);
        wait.until(condition);
    }

    public FluentElement waitFor(By by) {
        WebDriverWait wait = new WebDriverWait(webDriver, 30);
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(by));
        return new FluentElement(webDriver, element);
    }
    
    public <T> T waitFor(Function<WebDriver, T> function) {
        WebDriverWait wait = new WebDriverWait(webDriver, 30);
        return wait.until(function);
    }
    
    public FluentElement findElement(By by) {
        return new FluentElement(webDriver, context().findElement(by));
    }

    public FluentElements findElements(By by) {
        List<FluentElement> elements = Lists.newArrayList();
        for (WebElement webElement : context().findElements(by)) {
            elements.add(new FluentElement(webDriver, webElement));
        }
        return new FluentElements(elements);
    }
    
    public XPathBuilder find() {
        return new XPathBuilder(this, XPathBuilder.Axis.DESCENDANT);
    }
    
    public String text() {
        return element().getText();
    }
    
    public FluentElement root() {
        return new FluentElement(webDriver);
    }

    public void sendKeys(CharSequence... keys) {
        element().sendKeys(keys);
    }

    public boolean exists(By by) {
        return !findElements(by).isEmpty();
    }

    public boolean isDisplayed() {
        try {
            return element().isDisplayed();
        } catch(StaleElementReferenceException ignored) {
            return false;
        }
    }
    
    public Point location() {
        return element().getLocation();
    }


    public Style style() {
        return new Style(element().getAttribute("style"));
    }
    
    public String attribute(String name) {
        return element().getAttribute(name);
    }

    public WebDriver.Navigation navigate() {
        return webDriver.navigate();
    }
    
    public WebDriverWait wait(long duration, TimeUnit unit) {
        return new WebDriverWait(webDriver, unit.toSeconds(duration));
    }

    public String getTagName() {
        return element().getTagName();
    }

    public FluentElement clear() {
        element.clear();
        return this;
    }

    public Optional<FluentElement> focusedElement() {
        WebElement focusedElement = webDriver.switchTo().activeElement();
        if(focusedElement.getTagName().equalsIgnoreCase("body")) {
            return Optional.of(new FluentElement(webDriver, focusedElement));
        } else {
            return Optional.absent();
        }
    }

    public void dragAndDropBy(int pixelsToLeft, int pixelsDown) {
        Actions actions = new Actions(webDriver);
        actions.dragAndDropBy(element, pixelsToLeft, pixelsDown);
        actions.perform();
    }
}
