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

import com.google.common.base.Strings;
import org.activityinfo.legacy.shared.command.AddTarget;
import org.activityinfo.legacy.shared.command.Command;
import org.activityinfo.legacy.shared.model.TargetDTO;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;

import javax.validation.ConstraintViolation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;

public class CommandValidatorTest {

    private CommandValidator validator;

    @Before
    public void setUp() throws Exception {
        validator = new CommandValidator();
    }

    @Test
    public void missingRootProperty() {
        
        AddTarget command = new AddTarget();
        assertThat(validator.validate(command), hasItems(
                forProperty("databaseId"), 
                forProperty("target")));
    }
    
    @Test
    public void missingDates() {

        TargetDTO target = new TargetDTO();
        AddTarget command = new AddTarget();
        command.setTarget(target);

        assertThat(validator.validate(command), hasItems(
                forProperty("target.name"),
                forProperty("target.fromDate"),
                forProperty("target.toDate")));
    }
    
    @Test
    public void nameTooLong() {
        TargetDTO target = new TargetDTO();
        target.setName(Strings.repeat("xoxoxo", 2000));
        AddTarget command = new AddTarget();
        command.setTarget(target);
        
        assertThat(validator.validate(command), hasItems(
                forProperty("target.name")));
    }
    
    private Matcher<ConstraintViolation<Command>> forProperty(final String name) {
        return new TypeSafeMatcher<ConstraintViolation<Command>>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("violation of constraints for ").appendValue(name);
            }

            @Override
            protected boolean matchesSafely(ConstraintViolation<Command> violation) {
                return name.equals(violation.getPropertyPath().toString());
            }
        };
    }
    

}