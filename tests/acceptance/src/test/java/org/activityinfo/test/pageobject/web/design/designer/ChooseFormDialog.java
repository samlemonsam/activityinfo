package org.activityinfo.test.pageobject.web.design.designer;

import org.activityinfo.test.driver.AliasTable;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.api.XPathBuilder;
import org.activityinfo.test.pageobject.bootstrap.BsModal;

import java.util.List;

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

    public ChooseFormDialog set(List<String> path, AliasTable alias) {
        for (int i = 0; i < path.size(); i++) {
            select(path.get(i));
        }
        return save();
    }

    private void select(String nodeName) {
        FluentElement formElement = modal.form().getForm();

        formElement.find().div(XPathBuilder.withText(nodeName)).clickWhenReady();
    }

    public ChooseFormDialog save() {
        modal.accept();
        return this;
    }
}
