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
package org.activityinfo.geoadmin.merge2.view.swing;

import com.google.common.base.Optional;
import org.activityinfo.geoadmin.match.MatchLevel;

import java.awt.*;

/**
 * Defines colors to use for visualizing matches. 
 *
 * <p>Constructed using <a href="http://colorbrewer2.org/">colorbrewer2.org</a></p>
 */
public class MatchColors {

    public static final Color MATCH_EXACT = Color.decode("#ffeda0");
    public static final Color MATCH_WARNING = Color.decode("#feb24c");
    public static final Color MATCH_POOR = Color.decode("#f03b20");

    public static final Color NON_KEY_TEXT_COLOR = Color.decode("#333333");

    public static void update(Component c, Optional<MatchLevel> matchConfidence) {
        // If this is a matching, color code the confidence level
        if (matchConfidence.isPresent()) {
            switch (matchConfidence.get()) {
                case EXACT:
                    c.setBackground(MatchColors.MATCH_EXACT);
                    c.setForeground(Color.BLACK);
                    break;
                case WARNING:
                    c.setBackground(MatchColors.MATCH_WARNING);
                    c.setForeground(Color.BLACK);
                    break;
                case POOR:
                    c.setBackground(MatchColors.MATCH_POOR);
                    c.setForeground(Color.WHITE);
                    break;
            }
        } else {
            c.setBackground(Color.WHITE);
            c.setForeground(NON_KEY_TEXT_COLOR);
        }
    }
}
