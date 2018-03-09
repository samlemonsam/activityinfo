/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
