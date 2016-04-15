package org.activityinfo.model.form;

import com.google.common.base.Strings;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.resource.Record;
import org.activityinfo.model.resource.ResourceId;

import javax.annotation.Nonnull;

/**
 * Created by yuriyz on 4/15/2016.
 */
public class FormLabel extends FormElement {

    private final ResourceId id;
    private String label;
    private boolean visible = true;

    public FormLabel(ResourceId id) {
        this(id, null);
    }

    public FormLabel(ResourceId id, String label) {
        this.id = id;
        this.label = label;
    }

    @Override
    public ResourceId getId() {
        return id;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public FormLabel setLabel(String label) {
        this.label = label;
        return this;
    }

    public boolean isVisible() {
        return visible;
    }

    public FormLabel setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    @Override
    public Record asRecord() {
        Record record = new Record();
        record.set("id", id.asString());
        record.set("label", label);
        record.set("type", "label");
        record.set("visible", visible);
        return record;
    }

    public static FormLabel fromRecord(@Nonnull Record record) {
        return new FormLabel(ResourceId.valueOf(record.getString("id")))
                .setLabel(Strings.nullToEmpty(record.isString("label")))
                .setVisible(record.getBoolean("visible", true));
    }

    public enum TextStyle {
        PLAIN("plain", I18N.CONSTANTS.plain(), "", ""),
        BOLD("bold", I18N.CONSTANTS.bold(), "<b>", "</b>");

        private String value;
        private String label;
        private String htmlStartTag;
        private String htmlEndTag;

        TextStyle(String value, String label, String htmlStartTag, String htmlEndTag) {
            this.value = value;
            this.label = label;
            this.htmlStartTag = htmlStartTag;
            this.htmlEndTag = htmlEndTag;
        }

        public String getValue() {
            return value;
        }

        public String getLabel() {
            return label;
        }

        public ResourceId getResourceId() {
            return ResourceId.valueOf(value);
        }

        public static TextStyle fromValue(String textStyle) {
            for (TextStyle style : TextStyle.values()) {
                if (style.getValue().equalsIgnoreCase(textStyle)) {
                    return style;
                }
            }

            return null;
        }

        public String applyStyle(String html) {
            return getHtmlStartTag() + html + getHtmlEndTag();
        }

        public String getHtmlStartTag() {
            return htmlStartTag;
        }

        public String getHtmlEndTag() {
            return htmlEndTag;
        }
    }
}
