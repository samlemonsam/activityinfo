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
package org.activityinfo.fixtures;

import com.google.inject.Injector;
import org.junit.internal.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.List;

public class InjectDependencies extends Statement {

    private Statement inner;
    private Injector injector;
    private SimpleScope scope;
    private Object test;

    public InjectDependencies(Statement inner, Injector injector,
                              SimpleScope scope, Object test) {
        this.inner = inner;
        this.scope = scope;
        this.test = test;
        this.injector = injector;
    }

    @Override
    public void evaluate() throws Throwable {
        List<Throwable> errors = new ArrayList<Throwable>();
        errors.clear();
        try {
            System.out.println("=============++> Entering Test Scope");

            scope.enter();
            injector.injectMembers(test);
            inner.evaluate();
        } catch (Throwable e) {
            errors.add(e);
        } finally {
            try {
                
                System.out.println("=============++> Exiting Test Scope");
                scope.exit();

            } catch (Throwable e) {
                errors.add(e);
            }
        }
        MultipleFailureException.assertEmpty(errors);
    }
}
