package org.activityinfo.i18n.shared;

/**
 * Defines available application locales.
 */
public enum ApplicationLocale {

    EN("English"),
    ES("Español"),
    FR("Français"),
    NL("Nederlands"),
    AR("العربية");
    
    private String localizedName;

    ApplicationLocale(String localizedName) {
        this.localizedName = localizedName;
    }

    public String getLocalizedName() {
        return localizedName;
    }
    
    public String getCode() {
        return name().toLowerCase();
    }

    public static ApplicationLocale fromCode(String localeCode) {
        return valueOf(localeCode.toUpperCase());
    }
}
