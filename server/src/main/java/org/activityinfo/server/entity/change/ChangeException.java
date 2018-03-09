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
package org.activityinfo.server.entity.change;

import javax.validation.ConstraintViolation;
import java.util.Set;

public class ChangeException extends RuntimeException {

    private ChangeFailureType failureType;
    private String property;
    private Set<? extends ConstraintViolation<?>> violations;

    public ChangeException(ChangeFailureType failureType) {
        super(failureType.toString());
        this.failureType = failureType;
    }

    public ChangeException(ChangeFailureType failureType, Exception cause) {
        super(failureType.toString(), cause);
        this.failureType = failureType;
    }

    public ChangeException(ChangeFailureType failureType, String property) {
        super(failureType + " (" + property + ")");
        this.failureType = failureType;
        this.property = property;
    }

    public ChangeException(String property, Set<? extends ConstraintViolation<?>> violations) {
        super(ChangeFailureType.CONSTRAINT_VIOLATION + " (" + property + ": " + toString(violations) + ")");
        this.failureType = ChangeFailureType.CONSTRAINT_VIOLATION;
        this.property = property;
        this.violations = violations;
    }

    private static String toString(Set<? extends ConstraintViolation<?>> violations) {
        StringBuilder sb = new StringBuilder();
        for (ConstraintViolation<?> violation : violations) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(violation.getMessage());
        }
        return sb.toString();
    }

    public ChangeException(Exception e) {
        this(ChangeFailureType.SERVER_FAULT, e);
    }

    public ChangeFailureType getFailureType() {
        return failureType;
    }

    public String getProperty() {
        return property;
    }
}
