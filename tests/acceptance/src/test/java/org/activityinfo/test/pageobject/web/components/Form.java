package org.activityinfo.test.pageobject.web.components;


import org.joda.time.LocalDate;

public abstract class Form {

    public abstract void fillTextField(String label, String value);

    public abstract void fillDateField(String label, LocalDate date);
    
    public abstract void select(String label, String itemLabel);
}
