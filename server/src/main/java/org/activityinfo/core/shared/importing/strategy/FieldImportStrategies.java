package org.activityinfo.core.shared.importing.strategy;

import com.google.common.collect.Lists;
import org.activityinfo.core.client.type.converter.JsConverterFactory;
import org.activityinfo.core.shared.type.converter.ConverterFactory;
import org.activityinfo.model.formTree.FormTree;

import java.util.List;


public class FieldImportStrategies {

    private static FieldImportStrategies INSTANCE;

    private List<FieldImportStrategy> strategies = Lists.newArrayList();

    private FieldImportStrategies(ConverterFactory converterFactory) {
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
    public static FieldImportStrategies get(ConverterFactory converterFactory) {
        if(INSTANCE == null) {
            INSTANCE = new FieldImportStrategies(converterFactory);
        }
        return INSTANCE;
    }

    public static FieldImportStrategies get() {
        return get(JsConverterFactory.get());
    }

}
