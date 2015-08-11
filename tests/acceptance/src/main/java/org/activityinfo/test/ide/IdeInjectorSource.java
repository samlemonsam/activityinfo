package org.activityinfo.test.ide;
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

import com.google.inject.Guice;
import com.google.inject.Injector;
import cucumber.runtime.java.guice.ScenarioScope;
import cucumber.runtime.java.guice.impl.ScenarioModule;
import cucumber.runtime.java.guice.impl.SequentialScenarioScope;
import org.activityinfo.test.driver.DriverModule;
import org.activityinfo.test.sut.SystemUnderTest;
import org.activityinfo.test.webdriver.WebDriverModule;

/**
 * @author yuriyz on 08/11/2015.
 */
public class IdeInjectorSource implements cucumber.runtime.java.guice.InjectorSource {

    private ScenarioScope scope = new SequentialScenarioScope();

    @Override
    public Injector getInjector() {
        return Guice.createInjector(
                new SystemUnderTest(),
                new WebDriverModule(),
                new DriverModule(),
                new ScenarioModule(scope));
    }
}
