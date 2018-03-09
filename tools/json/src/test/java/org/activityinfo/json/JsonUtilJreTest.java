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
package org.activityinfo.json;

import org.activityinfo.json.impl.JsonUtil;

/**
 * Run {@link JsonUtilGwtTest} in the JVM
 */
public class JsonUtilJreTest extends JsonUtilGwtTest {

  @Override
  public String getModuleName() {
    return null;
  }

  @Override
  public void testNative() {
    // No native things to test in JRE
  }


  public void testQuote() {
    String badString = "\bThis\"is\ufeff\ta\\bad\nstring\u2029\u2029";
    assertEquals("\"\\bThis\\\"is\\ufeff\\ta\\\\bad\\nstring"
        + "\\u2029\\u2029\"", JsonUtil.quote(badString));
  }
}
