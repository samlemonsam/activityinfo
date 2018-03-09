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
package org.activityinfo.ui.client.widget.dialog;

import java.util.List;

/**
 * @author yuriyz on 11/13/2014.
 */
public class StraightForwardPageSwitcher implements PageSwitcher {

    private final List<WizardPage> pages;
    private WizardPage currentPage = null;

    public StraightForwardPageSwitcher(List<WizardPage> pages) {
        this.pages = pages;
    }

    @Override
    public WizardPage getNext() {
        if (currentPageIndex() == -1) {
            currentPage = pages.get(0);
        } else {
            currentPage = pages.get(currentPageIndex() + 1);
        }

        return currentPage;
    }

    public WizardPage getCurrentPage() {
        return currentPage;
    }

    @Override
    public boolean hasPrevious() {
        return currentPageIndex() > 0;
    }

    @Override
    public boolean hasNext() {
        return currentPageIndex() < (pages.size() - 1);
    }

    @Override
    public WizardPage getPrevious() {
        if (currentPageIndex() == -1) {
            throw new RuntimeException("Illegal state of page switcher, current page index is -1.");
        }
        return pages.get(currentPageIndex() - 1);
    }

    @Override
    public boolean isFinishPage() {
        return currentPageIndex() == (pages.size() - 1);
    }

    private int currentPageIndex() {
        if (currentPage == null) {
            return -1;
        }
        return pages.indexOf(currentPage);
    }


}
