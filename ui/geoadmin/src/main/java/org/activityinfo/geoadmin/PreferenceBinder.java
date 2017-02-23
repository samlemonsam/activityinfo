package org.activityinfo.geoadmin;

import java.util.prefs.Preferences;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

public class PreferenceBinder implements DocumentListener {

    private static final Preferences PREFS = Preferences.userNodeForPackage(GeoAdmin.class);

    private String key;
    private JTextComponent component;

    public PreferenceBinder(String key, JTextComponent component) {
        this.key = key;
        this.component = component;
    }

    private void onChanged() {
        PREFS.put(key, component.getText());
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        onChanged();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        onChanged();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        onChanged();
    }

    public static void bind(String key, JTextComponent field) {
        field.setText(PREFS.get(key, null));
        field.getDocument().addDocumentListener(new PreferenceBinder(key, field));
    }
}
