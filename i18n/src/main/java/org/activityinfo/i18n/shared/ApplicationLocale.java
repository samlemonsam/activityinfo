package org.activityinfo.i18n.shared;

/**
 * Defines available application locales.
 */
public enum ApplicationLocale {

    // ORDER:
    // 1) English first.
    // 2) All remaining languages with Latin script, sorted alphabetically
    // 3) Languages written with Arabic script, sorted alphabetically
    // 4) Additional writing systems?

    EN("English"),
    ES("Español"),
    EL("Ελληνικά"),
    FR("Français"),
    NL("Nederlands"),
    TR("Türkçe"),
    VI("Vietnamese"),
    AR("العربية"),
    FA("فارسی");

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
