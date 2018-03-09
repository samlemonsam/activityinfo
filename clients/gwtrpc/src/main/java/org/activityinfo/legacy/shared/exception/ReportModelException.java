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
package org.activityinfo.legacy.shared.exception;

import org.activityinfo.legacy.shared.reports.model.ReportElement;

/**
 * Exception relating to the (mal)definition of a report model
 *
 * @author Alex Bertram
 */
public class ReportModelException extends RuntimeException {

    public ReportModelException() {
        super();
    }

    public ReportModelException(String message, Throwable cause, ReportElement element) {
        super(appendElementDetails(message, element), cause);
    }

    public ReportModelException(String message, ReportElement element) {
        super(appendElementDetails(message, element));
    }

    public ReportModelException(Throwable cause, ReportElement element) {
        super(appendElementDetails("", element), cause);
    }

    private static String appendElementDetails(String message, ReportElement element) {
        try {
            message += "In " + element.getClass().toString() + ", ";
            if (element.getTitle() == null) {
                message += " untitled";
            } else {
                message += " titled '" + element.getTitle() + "'.";
            }
        } catch (Exception ignored) {
        }
        return message;
    }
}
