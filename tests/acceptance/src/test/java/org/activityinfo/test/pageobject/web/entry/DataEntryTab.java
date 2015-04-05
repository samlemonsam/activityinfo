package org.activityinfo.test.pageobject.web.entry;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.activityinfo.test.driver.DataEntryDriver;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.gxt.GxtModal;
import org.activityinfo.test.pageobject.gxt.GxtPanel;
import org.activityinfo.test.pageobject.gxt.GxtTree;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import javax.annotation.Nullable;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.activityinfo.test.pageobject.api.XPathBuilder.withClass;
import static org.activityinfo.test.pageobject.api.XPathBuilder.withText;


public class DataEntryTab {
    private final GxtTree formTree;
    private FluentElement container;

    public DataEntryTab(FluentElement container) {
        this.container = container;
        this.formTree = GxtPanel.find(container, "Forms").tree();
    }
    
    public void navigateToForm(String formName) {
        formTree.waitUntilLoaded();
        Optional<GxtTree.GxtNode> formNode = formTree.search(formName);
        if(!formNode.isPresent()) {
            throw new AssertionError(String.format("Form '%s' is not present in data entry tree", formName));
        }
        formNode.get().select();
    }
    
    public DataEntryDriver newSubmission() {
        container.find().button(withText("New Submission")).clickWhenReady();
        return new GxtDataEntryDriver(new GxtModal(container));
    }
    
    public int getCurrentSiteCount() {
        return container.waitFor(new Function<WebDriver, Integer>() {
            @Override
            public Integer apply(WebDriver input) {
                Optional<FluentElement> countLabel = container.find()
                        .div(withClass("my-paging-display"))
                        .firstIfPresent();
                
                if(countLabel.isPresent()) {
                    String text = countLabel.get().text();
                    if (text.equals("No data to display")) {
                        return 0;
                    }

                    Matcher matcher = Pattern.compile("Displaying ([\\d,]+) - ([\\d+,]+) of ([\\d,]+)").matcher(text);
                    if (matcher.matches()) {
                        return Integer.parseInt(matcher.group(3).replace(",", ""));
                    }
                }
                return null;
            }
        });
    }

    public File export() {
        container.find().button(withText("Export")).clickWhenReady();
    
        String link = container.waitFor(new Function<WebDriver, String>() {
            @Nullable
            @Override
            public String apply(WebDriver input) {
                WebElement link = input.findElement(By.partialLinkText("Click here if your download does not start"));
                return link.getAttribute("href");
            }
        });

        URL url;
        try {
            url = new URL(link);
        } catch (MalformedURLException e) {
            throw new AssertionError("Bad link: " + link);
        }

        try {
            File file = File.createTempFile("export", ".xls");
            ByteStreams.copy(Resources.asByteSource(url), Files.asByteSink(file));
            return file;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
    
}
