package org.activityinfo.test.pageobject.odk;

public class BlankForm {
    private String name;
    private String checkBoxRef;

    public BlankForm(String name, String checkBoxRef) {
        this.name = name;
        this.checkBoxRef = checkBoxRef;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCheckBoxRef() {
        return checkBoxRef;
    }

    public void setCheckBoxRef(String checkBoxRef) {
        this.checkBoxRef = checkBoxRef;
    }
}
