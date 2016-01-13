package org.activityinfo.i18n.tools.po;

/**
 * A term to update.
 * 
 * @see <a href="https://poeditor.com/localization/files/json">PoEditor.com documentation</a>
 */
public class PoTermUpdate {

    /**
     * The term key
     */
    private String term;


    /**
     * The translation of the term
     */
    private String definition;

    public PoTermUpdate() {
    }

    public PoTermUpdate(String key, String defaultTranslation) {
        this.term = key;
        this.definition = defaultTranslation;
    }


    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }
}
