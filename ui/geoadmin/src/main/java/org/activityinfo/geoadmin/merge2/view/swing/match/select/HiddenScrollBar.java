package org.activityinfo.geoadmin.merge2.view.swing.match.select;

import javax.swing.*;
import java.awt.*;

class HiddenScrollBar extends JScrollBar {


    public HiddenScrollBar(JScrollBar scrollBar) {
        setPreferredSize(scrollBar.getPreferredSize());
    }

    public void paint(Graphics g) {}

    @Override
    public void repaint(long tm, int x, int y, int width, int height) {}


}
