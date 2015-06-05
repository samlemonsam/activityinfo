package org.activityinfo.geoadmin.merge2.view.swing.match;

import com.google.common.base.Optional;
import org.activityinfo.geoadmin.match.MatchLevel;
import org.activityinfo.geoadmin.merge2.view.match.*;
import org.activityinfo.geoadmin.merge2.view.swing.MatchColors;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;


public class CellRenderers {
    
    private MatchTable matchTable;
    
    private final TexturePaint unmatchedPaint = createUnmatchedPaint();

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
            } else {
                c.setBackground(Color.WHITE);
                c.setForeground(Color.BLACK);
            }
            return c;
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (matched) {
                super.paintComponent(g);
            } else {
                paintUnmatchedCell(g);
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
