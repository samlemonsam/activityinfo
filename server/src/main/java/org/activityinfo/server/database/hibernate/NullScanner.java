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
package org.activityinfo.server.database.hibernate;

import org.hibernate.ejb.packaging.NamedInputStream;
import org.hibernate.ejb.packaging.Scanner;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Collections;
import java.util.Set;

/**
 * Ensures that NO scanning is done on startup by Hibernate
 */
public class NullScanner implements Scanner {
    @Override
    public Set<Package> getPackagesInJar(URL jartoScan, Set<Class<? extends Annotation>> annotationsToLookFor) {
        return Collections.emptySet();
    }

    @Override
    public Set<Class<?>> getClassesInJar(URL jartoScan, Set<Class<? extends Annotation>> annotationsToLookFor) {
        return Collections.emptySet();
    }

    @Override
    public Set<NamedInputStream> getFilesInJar(URL jartoScan, Set<String> filePatterns) {
        return Collections.emptySet();
    }

    @Override
    public Set<NamedInputStream> getFilesInClasspath(Set<String> filePatterns) {
        return Collections.emptySet();
    }

    @Override
    public String getUnqualifiedJarName(URL jarUrl) {
        throw new UnsupportedOperationException();
    }
}
