package org.activityinfo.test.pageobject.gxt;


import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.api.XPath;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import static org.activityinfo.test.pageobject.api.XPathBuilder.withText;

public class GalleryView {

    private final FluentElement gallery;

    public GalleryView(FluentElement gallery) {
        this.gallery = gallery;
    }

    public static GalleryView find(FluentElement parent) {
        return new GalleryView(parent.findElement(By.className("gallery")));
    }
    
    public void select(String heading) {
        gallery.find().h4(withText(heading)).clickWhenReady();
    }
    
}
