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
