package org.activityinfo.test.pageobject.odk;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.TouchAction;
import org.activityinfo.test.driver.DataEntryDriver;
import org.joda.time.LocalDate;
import org.openqa.selenium.Dimension;

public class Question implements DataEntryDriver {
    private final AppiumDriver driver;

    public Question(AppiumDriver driver) {
        this.driver = driver;
    }

    public Question forward() {
//        JavascriptExecutor js = (JavascriptExecutor) driver;
//        HashMap<String, Double> swipeObject = new HashMap<String, Double>();
//        swipeObject.put("startX", 0.95);
//        swipeObject.put("startY", 0.5);
//        swipeObject.put("endX", 0.05);
//        swipeObject.put("endY", 0.5);
//        swipeObject.put("duration", 1.8);
//        js.executeScript("mobile: swipe", swipeObject);
//        
//        HasTouchScreen touchScreen = (HasTouchScreen) driver;
//        touchScreen.getTouch().flick(-500, 0);
//        
        // swipe across the screen horizontally
//        driver.findElementById("form_forward_button").click();
//        System.out.println(driver.getPageSource());
        Dimension size = driver.manage().window().getSize();

        
        
        int fromX = size.width - 10;
        int toX = 10;

        int y = size.height / 2;

        TouchAction action = new TouchAction(driver);
        action.press(fromX, y);
        action.moveTo(toX, y);
        action.release();
        action.perform();
        return this;
        
    }

    @Override
    public boolean nextField() {
        return false;
    }

    @Override
    public void submit() throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getLabel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fill(String text) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fill(LocalDate date) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void select(String itemLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isValid() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isNextEnabled() {
        return false;
    }

    @Override
    public void sendKeys(CharSequence keys) {
        throw new UnsupportedOperationException();
    }
}
