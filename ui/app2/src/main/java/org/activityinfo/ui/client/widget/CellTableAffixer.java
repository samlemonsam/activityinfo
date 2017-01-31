package org.activityinfo.ui.client.widget;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.ScrollPanel;

/**
 * Adds affix behavior to table.
 *
 * @author yuriyz on 4/7/14.
 */
public class CellTableAffixer {

    //    private static final Logger LOGGER = Logger.getLogger(CellTableAffixer.class.getName());
    public static final String AFFIX_CLASS_NAME = "affix";

    private final CellTable table;
    private final ScrollPanel scrollAncestor;
    private final CellTableHeaderWidthApplier widthApplier;

    private boolean affixed = false;

    public CellTableAffixer(final CellTable table) {
        this.table = table;
        this.scrollAncestor = table.getScrollAncestor();
        this.widthApplier = new CellTableHeaderWidthApplier(table);

        table.getEventBus().addHandler(CellTable.ScrollEvent.TYPE, new CellTable.ScrollHandler() {
            @Override
            public void onScroll(CellTable.ScrollEvent p_event) {
                handleScroll(p_event);
            }
        });

        widthApplier.saveHeaderWidthInformation();
    }

    public CellTableHeaderWidthApplier getWidthApplier() {
        return widthApplier;
    }

    private void handleScroll(CellTable.ScrollEvent event) {
        final int verticalScroll = event.getVerticalScrollPosition();

        final int tableTop = table.getAbsoluteTop();
        boolean shouldAffix = verticalScroll > tableTop;

        if (affixed == shouldAffix) {
            return;
        }

        affixed = shouldAffix;
        if (shouldAffix) {
            setAffix();
        } else {
            clearAffix();
        }
    }

    private void setAffix() {
        table.getTableHeadElement().addClassName(AFFIX_CLASS_NAME);
        table.getTableHeadElement().getStyle().setTop(scrollAncestor != null ? scrollAncestor.getAbsoluteTop() : 0, Style.Unit.PX);
        widthApplier.restoreHeaderWidthInformation();
    }

    private void clearAffix() {
        table.getTableHeadElement().removeClassName(AFFIX_CLASS_NAME);
        table.getTableHeadElement().getStyle().clearTop();
        widthApplier.clearHeaderWidthInformation();
    }

    public void forceAffix() {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                if (affixed) {
                    setAffix();
                }
            }
        });
    }
}
