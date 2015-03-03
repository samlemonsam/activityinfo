package org.activityinfo.test.pageobject.api;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

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
        this(webDriver, webDriver.findElement(By.tagName("body")));
    }
    
    public void clickWhenReady() {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                try {
                    element.click();
                    return true;
                    
                } catch (WebDriverException e) {
                    return false;
                }
            }
        });
    }


    public void click() {
        Actions actions = new Actions(webDriver);
        actions.click(element).perform();
    }
    
    public void waitUntil(Predicate<WebDriver> predicate) {
        WebDriverWait wait = new WebDriverWait(webDriver, 30);
        wait.until(predicate);
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
        return new FluentElement(webDriver, element.findElement(by));
    }

    public FluentElements findElements(By by) {
        List<FluentElement> elements = Lists.newArrayList();
        for (WebElement webElement : element.findElements(by)) {
            elements.add(new FluentElement(webDriver, webElement));
        }
        return new FluentElements(elements);
    }
    
    public XPathBuilder find() {
        return new XPathBuilder(this, XPathBuilder.Axis.DESCENDANT);
    }
    
    public String text() {
        return element.getText();
    }
    
    public FluentElement root() {
        return new FluentElement(webDriver);
    }

    public void sendKeys(CharSequence... keys) {
        element.sendKeys(keys);
    }

    public boolean exists(By by) {
        return !findElements(by).isEmpty();
    }

    public boolean isDisplayed() {
        try {
            return element.isDisplayed();
        } catch(StaleElementReferenceException ignored) {
            return false;
        }
    }
    
    public Point location() {
        return element.getLocation();
    }


    public Style style() {
        return new Style(element.getAttribute("style"));
    }
    
    public String attribute(String name) {
        return element.getAttribute(name);
    }
}
