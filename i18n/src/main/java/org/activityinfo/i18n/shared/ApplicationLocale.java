package org.activityinfo.i18n.shared;

public enum ApplicationLocale {

    EN("English"),
    FR("Français"),
    ES("Espanol"),
    AR("العربية");
    
    private String abbreviation;
    private String localizedName;

    ApplicationLocale(String localizedName) {
        this.localizedName = localizedName;
    }

    public String getLocalizedName() {
        return localizedName;
    }
}
