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
