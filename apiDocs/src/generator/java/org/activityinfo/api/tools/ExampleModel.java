package org.activityinfo.api.tools;

public class ExampleModel {
    
    private String language;
    private String source;

    public ExampleModel() {
    }

    public ExampleModel(String language, String source) {
        this.language = language;
        this.source = source;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
