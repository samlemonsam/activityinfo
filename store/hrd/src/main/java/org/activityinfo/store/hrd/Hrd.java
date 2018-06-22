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
package org.activityinfo.store.hrd;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Work;
import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.store.hrd.entity.*;

/**
 * Gateway to ObjectifyService that ensures entity classes are registered
 */
public class Hrd {
    static {
        LocaleProxy.initialize();
        ObjectifyService.register(FormEntity.class);
        ObjectifyService.register(FormRecordEntity.class);
        ObjectifyService.register(FormRecordSnapshotEntity.class);
        ObjectifyService.register(FormColumnStorage.class);
        ObjectifyService.register(FormSchemaEntity.class);
        ObjectifyService.register(AnalysisEntity.class);
        ObjectifyService.register(AnalysisSnapshotEntity.class);
        ObjectifyService.register(AuthTokenEntity.class);
    }

    public static Objectify ofy() {
        return ObjectifyService.ofy();
    }

    public static <T> T run(Work<T> work) {
        return ObjectifyService.run(work);
    }
}
