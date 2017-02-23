package org.activityinfo.geoadmin;


import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PasswordForm {

    public interface Callback {
        void ok(String username, String password);
    }

    private JTextField usernameInput;
    private JPasswordField passwordInput;

    public PasswordForm() {
    }

    public void show(final Callback callback) {
        // Basic form create
        final JDialog frame = new JDialog();
        frame.setTitle("Login");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setSize(300,150);

        // Creating the grid
        JPanel panel = new JPanel(new MigLayout());
        frame.getContentPane().add(panel, BorderLayout.NORTH);

        usernameInput = new JTextField(25);
        passwordInput = new JPasswordField(25);

        panel.add(new JLabel("Email:"));
        panel.add(usernameInput, "wrap");

        panel.add(new JLabel("Password:"));
        panel.add(passwordInput, "wrap");

        JButton loginInput = new JButton("Login");
        loginInput.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                frame.setVisible(false);
                callback.ok(usernameInput.getText(), new String(passwordInput.getPassword()));
            }
        });
        panel.add(loginInput);
        frame.setVisible(true);
    }

}
