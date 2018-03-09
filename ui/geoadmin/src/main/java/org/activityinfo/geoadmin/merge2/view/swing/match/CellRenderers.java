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
package org.activityinfo.geoadmin.merge2.view.swing.match;

import org.activityinfo.geoadmin.merge2.view.match.*;
import org.activityinfo.geoadmin.merge2.view.swing.MatchColors;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;


public class CellRenderers {
    
    private MatchTable matchTable;
    
    private final TexturePaint unmatchedPaint = createUnmatchedPaint();
    
    private Font strikeThroughFont;

    public CellRenderers(MatchTable table) {
        this.matchTable = table;
    }
    
    public TableCellRenderer rendererFor(MatchTableColumn column) {
        if(column instanceof ResolutionColumn) {
            return new Resolution();
        } else if(column instanceof SeparatorColumn) {
            return new Separator();
        } else  {
            return new DataCellRenderer(column);
        }
    }

    private static class Resolution extends DefaultTableCellRenderer {
        public Resolution() {
            setHorizontalAlignment(CENTER);
        }
    }
    
    private static class Separator extends DefaultTableCellRenderer {
        public Separator() {
            setBackground(Color.LIGHT_GRAY);
            setHorizontalAlignment(CENTER);
        }
    }
    
    
    private class DataCellRenderer extends DefaultTableCellRenderer {
        
        private final MatchTableColumn columnModel;

        /**
         * True if the current row being painted is matched
         */
        private boolean matched;
        
        public DataCellRenderer(MatchTableColumn columnModel) {
            this.columnModel = columnModel;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            int matchIndex = table.convertRowIndexToModel(row);
            MatchRow matchRow = matchTable.get(matchIndex);
            MatchSide side = columnModel.getSide().get();

            matched = matchRow.isMatched(side);
            if(matchRow.isMatched()) {
                MatchColors.update(c, columnModel.getMatchConfidence(matchIndex));
            } else if(isSource()) {
                // Show empty
                c.setBackground(Color.WHITE);
                c.setForeground(Color.BLACK);
            } else {
                // Unmatched targets will be deleted from the dataset
                // Render as striken-through text
                if(strikeThroughFont == null) {
                    Map<TextAttribute, Object> attributes = new HashMap<>();
                    attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
                    strikeThroughFont = c.getFont().deriveFont(attributes);
                }
                
                c.setFont(strikeThroughFont);
                c.setBackground(Color.LIGHT_GRAY);
                c.setForeground(Color.BLACK);
            }
            return c;
        }

        private boolean isSource() {
            return columnModel.getSide().get() == MatchSide.SOURCE;
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (!matched && isSource()) {
                paintUnmatchedCell(g);
            } else {
                super.paintComponent(g);
            } 
        }

        private void paintUnmatchedCell(Graphics g) {
            if(g != null) {
                Graphics scratchGraphics = g.create();
                try {
                    Graphics2D g2d = (Graphics2D) scratchGraphics;
                    g2d.setPaint(unmatchedPaint);
                    g2d.fill(new Rectangle2D.Double(0, 0, getWidth(), getHeight()));

                } finally {
                    scratchGraphics.dispose();
                }
            }
        }
    }

    private static TexturePaint createUnmatchedPaint() {
        BufferedImage bufferedImage =
                new BufferedImage(5, 5, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = bufferedImage.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, 5, 5);
        g2.setColor(Color.GRAY);
        g2.drawLine(0, 5, 5, 0); // /

        // paint with the texturing brush
        Rectangle2D rect = new Rectangle2D.Double(0, 0, 5, 5);
        return new TexturePaint(bufferedImage, rect);
    }
}
