package org.activityinfo.ui.client.component.importDialog;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Interface to import wizard pages
 */
public interface ImportPage extends IsWidget {

    boolean isValid();

    /**
     *
     * @return true if this page has a next step
     */
    boolean hasNextStep();

    /**
     *
     * @return true if this page has an (internal) previous step
     */
    boolean hasPreviousStep();

    void start();

    void fireStateChanged();

    void nextStep();

    void previousStep();


}
