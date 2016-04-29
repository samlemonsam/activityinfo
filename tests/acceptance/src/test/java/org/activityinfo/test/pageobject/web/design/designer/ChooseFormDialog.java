package org.activityinfo.test.pageobject.web.design.designer;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.test.driver.AliasTable;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.api.XPathBuilder;
import org.activityinfo.test.pageobject.bootstrap.BsModal;

import java.util.List;
import java.util.Set;

/**
 * Created by yuriyz on 4/29/2016.
 */
public class ChooseFormDialog {

    private final BsModal modal;

    public ChooseFormDialog(BsModal modal) {
        this.modal = modal;
    }

    public BsModal getModal() {
        return modal;
    }

    private SetMultimap<String, FluentElement> nodes() { // parsing manually, for some reason /div containsText(nodeName) does not work
        SetMultimap<String, FluentElement> nodes = HashMultimap.create();
        FluentElement formElement = modal.form().getForm();
        for(FluentElement node : formElement.find().span(XPathBuilder.withClass("icon")).ancestor().div().asList().list()) {
            String text = node.text();
            if (!Strings.isNullOrEmpty(text)) {
                nodes.put(text, node);
            }

        }
        return nodes;
    }

    public ChooseFormDialog set(List<String> path, AliasTable alias) {

        for (int i = 0; i < path.size(); i++) {
            String nodeName = path.get(i);
            if (i > 0 && !path.get(0).equals(I18N.CONSTANTS.geography())) {
                nodeName = alias.createAlias(nodeName);
            }

            Set<FluentElement> fluentElements = nodes().get(nodeName);
            if (fluentElements != null) {
                fluentElements.iterator().next().clickWhenReady(); // click first
            } else {
                throw new RuntimeException("ChooseFormDialog : Unable to find node with name: " + nodeName);
            }
        }
        return save();
    }

    public ChooseFormDialog save() {
        modal.accept();
        return this;
    }
}
