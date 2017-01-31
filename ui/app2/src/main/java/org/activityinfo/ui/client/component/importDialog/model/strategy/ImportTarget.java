package org.activityinfo.ui.client.component.importDialog.model.strategy;

import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;

/**
 * Some fields can have multiple binding "sites" to which columns
 * can be bound. For example, a GeographicPoint can accept two columns -
 * a latitude and a longitude - as an import source.
 */
public class ImportTarget {

    private final FormField formField;
    private final TargetSiteId site;
    private final String label;
    private final ResourceId formClassId;

    public ImportTarget(FormField formField, TargetSiteId site, String label, ResourceId formClassId) {
        this.formField = formField;
        this.site = site;
        this.label = label;
        this.formClassId = formClassId;
    }

    public ResourceId getFormClassId() {
        return formClassId;
    }

    public FormField getFormField() {
        return formField;
    }

    public TargetSiteId getSite() {
        return site;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label + "[" + site + "@" + formField.getId() + "]";
    }
}
