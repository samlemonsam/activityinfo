package org.activityinfo.test.pageobject.web.design.designer;

import com.google.common.collect.Lists;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.api.FluentElements;

import java.util.List;

import static org.activityinfo.test.pageobject.api.XPathBuilder.withClass;
import static org.activityinfo.test.pageobject.api.XPathBuilder.withText;


public class FieldPalette {
    private FluentElement panel;

    public FieldPalette(FluentElement panel) {
        this.panel = panel;
    }

    public void dropNewField(String name) {
        dropLabel(name).clickWhenReady();
    }

    public DropLabel dropLabel(String name) {
        return new DropLabel(panel.find().div(withText(name)).waitForFirst(), name);
    }
    
    public List<String> getFieldTypes() {
        FluentElements elements = panel.find().div(withClass("btn")).asList();
        List<String> types = Lists.newArrayList();
        for (FluentElement element : elements) {
            types.add(element.text());
        }
        return types;
    }
}
