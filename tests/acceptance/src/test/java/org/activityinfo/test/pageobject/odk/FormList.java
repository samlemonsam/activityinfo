package org.activityinfo.test.pageobject.odk;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.openqa.selenium.By.xpath;

public class FormList {

    private final AppiumDriver driver;
    private List<BlankForm> forms;

    public FormList(AppiumDriver driver) {
        this.driver = driver;
        driver.get("and-activity://org.odk.collect.android.activities.FormDownloadList");
    }

    /**
     * Refreshes the list of Blank Forms and waits until the
     * list is fully loaded
     */
    public FormList load() {

        refreshIfNeeded();

        pause();

        Stopwatch stopwatch = Stopwatch.createStarted();

        while(true) {
            if(stopwatch.elapsed(TimeUnit.SECONDS) > 30) {
                throw new AssertionError("Timed-out waiting for blank forms");
            }
            if(stillLoading()) {
                pause();

            } else if(weArePromptedForAuthentication()) {
                // we've already supplied username/password, just submit
                justClickOK();

            } else {
                // loaded
                break;
            }
        }

        PageSource pageSource = new PageSource(driver.getPageSource());
        this.forms = parseFormList(pageSource);

        return this;
    }

    private void refreshIfNeeded() {
        List<WebElement> refreshButton = driver.findElements(xpath("//Button[@text='Refresh']"));
        if(!refreshButton.isEmpty()) {
            refreshButton.get(0).click();
        }
    }

    private void justClickOK() {
        driver.findElement(By.partialLinkText("OK")).click();
    }

    private void pause() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
        }
    }

    private boolean stillLoading() {
        return driver.findElements(xpath("//TextView[@text='Connecting to Server']")).size() > 0;
    }

    private boolean weArePromptedForAuthentication() {
        List<WebElement> elements = driver.findElements(
                xpath("//DialogTitle[@value='Server Requires Authentication']"));

        return elements.size() > 0;
    }

    @VisibleForTesting
    static List<BlankForm> parseFormList(PageSource page) {

        NodeList nodes = page.query("//*[@id='list']/TwoItemMultipleChoiceView");
        List<BlankForm> forms = Lists.newArrayList();
        for(int i=0;i!=nodes.getLength();++i) {

            Element choiceView = (Element) nodes.item(i);
            Element labelTextView = (Element) choiceView.getElementsByTagName("TextView").item(0);
            Element checkBox = (Element) choiceView.getElementsByTagName("CheckBox").item(0);

            String name = labelTextView.getAttribute("value");
            String checkBoxRef = checkBox.getAttribute("ref");

            forms.add(new BlankForm(name, checkBoxRef));
        }
        return forms;
    }

    public FormList select(String name) {
        BlankForm form = named(name);
        int index = forms.indexOf(form);

        WebElement checkBox = driver.findElementsByTagName("CheckBox").get(index);
        checkBox.click();

        return this;
    }

    private BlankForm named(String name) {
        if(forms == null) {
            load();
        }
        for(BlankForm form : forms) {
            if(form.getName().equals(name)) {
                return form;
            }
        }
        throw new AssertionError("No form named " + name);
    }

    public List<BlankForm> getForms() {
        if(forms == null) {
            load();
        }
        return forms;
    }

    public void getSelected() {
        driver.findElementByPartialLinkText("Get Selected").click();

        WebDriverWait wait = new WebDriverWait(driver, 120);
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//DialogTitle[@value='Download Result']")));

        // make sure we succeeded!
        driver.findElementsByXPath("//TextView[contains(@value, 'Success')]");

        // close
        driver.findElementByPartialLinkText("OK").click();
    }
}
