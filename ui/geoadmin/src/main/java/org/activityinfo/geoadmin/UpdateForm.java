package org.activityinfo.geoadmin;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.opengis.feature.type.PropertyDescriptor;

public class UpdateForm extends JPanel {


    private ImportSource source;
    private JComboBox nameCombo;
    private JComboBox codeCombo;
    private JTextField sourceUrlField;
    private JTextArea messageTextArea;

    public UpdateForm(ImportSource source) {
        super(new MigLayout());

        this.source = source;

        nameCombo = new JComboBox(source.getAttributeNames());
        nameCombo.setSelectedIndex(guessNameColumn());

        codeCombo = new JComboBox(source.getAttributeNames());
        codeCombo.setSelectedIndex(guessCodeColumn());

        if (codeCombo.getSelectedIndex() == nameCombo.getSelectedIndex()) {
            codeCombo.setSelectedItem(null);
        }

        sourceUrlField = new JTextField();
        PreferenceBinder.bind("source_url_" + source.getMd5Hash(), sourceUrlField);

        messageTextArea = new JTextArea();
        PreferenceBinder.bind("message_" + source.getMd5Hash(), messageTextArea);


        add(new JLabel("Name Attribute"));
        add(nameCombo, "width 160!, wrap");

        add(new JLabel("Code Attribute"));
        add(codeCombo, "width 160!, wrap");

        add(new JLabel("Source URL"));
        add(sourceUrlField, "wrap, growx");

        add(new JLabel("Commit Message"));
        add(messageTextArea, "wrap, growx");

    }

    public PropertyDescriptor getNameProperty() {
        return source.getAttributes().get(nameCombo.getSelectedIndex());
    }

    public PropertyDescriptor getCodeProperty() {
        if (codeCombo.getSelectedItem() == null) {
            return null;
        }
        return source.getAttributes().get(codeCombo.getSelectedIndex());
    }

    private int guessNameColumn() {
        return new ColumnGuesser()
            .forPattern("[A-Za-z-' ]+")
            .favoringUniqueValues()
            .findBest(source);
    }

    private int guessCodeColumn() {
        return new ColumnGuesser()
            .forPattern("[0-9]+")
            .favoringUniqueValues()
            .findBest(source);
    }

    public String getSourceUrl() {
        return sourceUrlField.getText();
    }

    public String getMessage() {
        return messageTextArea.getText();
    }
}
