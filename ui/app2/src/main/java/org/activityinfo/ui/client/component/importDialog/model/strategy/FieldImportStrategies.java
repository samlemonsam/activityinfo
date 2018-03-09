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
package org.activityinfo.ui.client.component.importDialog.model.strategy;

import com.google.common.collect.Lists;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.ui.client.component.importDialog.model.type.converter.FieldParserFactory;
import org.activityinfo.ui.client.dispatch.type.JsConverterFactory;

import java.util.List;


public class FieldImportStrategies {

    private static FieldImportStrategies INSTANCE;

    private List<FieldImportStrategy> strategies = Lists.newArrayList();

    private FieldImportStrategies(FieldParserFactory converterFactory) {
        strategies.add(new GeographicPointImportStrategy(converterFactory));
        strategies.add(new SingleClassReferenceStrategy());
        strategies.add(new DataFieldImportStrategy(converterFactory));
        strategies.add(new HierarchyReferenceStrategy());
        strategies.add(new EnumImportStrategy());
    }

    public FieldImportStrategy forField(FormTree.Node fieldNode) {
        for (FieldImportStrategy strategy : strategies) {
            if (strategy.accept(fieldNode)) {
                return strategy;
            }
        }
        throw new UnsupportedOperationException();
    }

    // server side may provide own convertor here explicitly : JvmConverterFactory.get()
    public static FieldImportStrategies get(FieldParserFactory converterFactory) {
        if(INSTANCE == null) {
            INSTANCE = new FieldImportStrategies(converterFactory);
        }
        return INSTANCE;
    }

    public static FieldImportStrategies get() {
        return get(JsConverterFactory.get());
    }

}
