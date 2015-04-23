package org.activityinfo.model.expr.eval;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.expr.diagnostic.AmbiguousSymbolException;
import org.activityinfo.model.expr.diagnostic.SymbolNotFoundException;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Maps symbol names to fields in a FormClass
 */
public class FormSymbolTable {

    private Map<String, FormField> idMap = Maps.newHashMap();
    private Multimap<String, FormField> codeMap = HashMultimap.create();
    private Multimap<String, FormField> labelMap = HashMultimap.create();


    public FormSymbolTable(FormClass formClass) {
        for (FormField field : formClass.getFields()) {

            // ID has first priority
            FormField prevValue = idMap.put(field.getId().asString(), field);
            assert prevValue == null : "Duplicated id [" + field.getId() + "] in FormClass " + formClass.getId();

            // Then we try codes
            if(field.hasCode()) {
                codeMap.put(field.getCode().toLowerCase(), field);
            }

            // And finally labels, if they're unique
            labelMap.put(field.getLabel().toLowerCase(), field);
        }
    }

    public FormSymbolTable(List<FormField> fields) {
        for (FormField field : fields) {

            // ID has first priority
            idMap.put(field.getId().asString(), field);

            // Then we try codes
            if(field.hasCode()) {
                codeMap.put(field.getCode().toLowerCase(), field);
            }

            // And finally labels, if they're unique
            labelMap.put(field.getLabel().toLowerCase(), field);
        }
    }

    public FormField resolveFieldById(String id) {
        FormField field = idMap.get(id);
        if(field == null) {
            throw new IllegalArgumentException(id);
        }
        return field;
    }

    public FormField resolveSymbol(SymbolExpr symbolExpr) {
        return resolveSymbol(symbolExpr.getName());
    }

    public FormField resolveSymbol(String name) {
        FormField field = idMap.get(name);
        if(field != null) {
            return field;
        }
        Collection<FormField> matching = codeMap.get(name.toLowerCase());
        if(matching.isEmpty()) {
            // as last resort, try matching against label
            matching = labelMap.get(name.toLowerCase());
        }

        if (matching.size() > 1) {
            throw new AmbiguousSymbolException(name);
        } else if (matching.isEmpty()) {
            throw new SymbolNotFoundException(name);
        }

        return Iterables.getOnlyElement(matching);
    }
}
