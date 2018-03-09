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
package org.activityinfo.test.driver.mail.mailinator;

import org.junit.Test;

import static org.activityinfo.test.driver.mail.mailinator.MessageHeader.parseEmail;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


public class MessageHeaderTest {

    @Test
    public void testEmail() {
        assertThat(parseEmail("\"ActivityInfo Notifications\" <notifications@activityinfo.org> "), equalTo("notifications@activityinfo.org"));
        assertThat(parseEmail("ActivityInfo Notifications <bob@gmail.com>"), equalTo("bob@gmail.com"));
        assertThat(parseEmail("bob@gmail.com"), equalTo("bob@gmail.com"));

    }
    
    

}