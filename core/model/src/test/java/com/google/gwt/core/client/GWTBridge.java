package com.google.gwt.core.client;
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

/**
 * CalendarUtil class uses client GWT.isClient() instead of shared GWT.isClient() which
 * lead to NoClassDefFoundError during unit tests. This is dummy hack to avoid the exception.
 *
 * java.lang.NoClassDefFoundError: com/google/gwt/core/client/GWTBridge
 * at com.google.gwt.user.datepicker.client.CalendarUtil.<clinit>(CalendarUtil.java:35)
 *
 * @author yuriyz on 01/14/2016.
 */
public abstract class GWTBridge extends com.google.gwt.core.shared.GWTBridge {
}
