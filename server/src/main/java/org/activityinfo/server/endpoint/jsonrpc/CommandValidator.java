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
package org.activityinfo.server.endpoint.jsonrpc;

import org.activityinfo.legacy.shared.command.Command;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

/**
 * Validates {@code Command} instances deserialized from JSON
 */
public class CommandValidator {


    private final Validator validator;

    public CommandValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    public void assertValid(Command command) {
        Set<ConstraintViolation<Command>> violations = validate(command);
        if(!violations.isEmpty()) {
            throw new BadRpcRequest(composeMessage(violations));
        }
    }

    public Set<ConstraintViolation<Command>> validate(Command command) {
        return validator.validate(command);
    }

    private String composeMessage(Set<ConstraintViolation<Command>> violations) {
        StringBuilder sb = new StringBuilder();
        sb.append("There were errors in your request.");
        for (ConstraintViolation<?> violation : violations) {
            sb.append("\n");
            sb.append(violation.getPropertyPath()).append(": ").append(violation.getMessage());
        }
        return sb.toString();
    }
}
