package org.activityinfo.model.formula.eval;

import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formula.SymbolNode;
import org.activityinfo.model.formula.diagnostic.AmbiguousSymbolException;
import org.activityinfo.model.formula.diagnostic.SymbolNotFoundException;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;

/**
 * Maps symbol names to fields in a FormClass
 */
public class FormSymbolTable {
    
    private FormClass formClass;
    private Map<String, FormField> idMap = Maps.newHashMap();
    private Multimap<String, FormField> codeMap = HashMultimap.create();
    private Multimap<String, FormField> labelMap = HashMultimap.create();


    public FormSymbolTable(@Nonnull FormClass formClass) {
        this.formClass = formClass;
        for (FormField field : formClass.getFields()) {

            // ID has first priority
            FormField prevValue = idMap.put(field.getId().asString(), field);
            assert prevValue == null : "Duplicated id [" + field.getId() + "] in FormClass " + formClass.getId();

            // Then we try codes
            if(field.hasCode()) {
                codeMap.put(field.getCode().toLowerCase(), field);
            }

            // And finally labels, if they're unique
            if(field.getLabel() != null) {
                labelMap.put(field.getLabel().toLowerCase(), field);
            }
        }
    }

    public FormSymbolTable(Iterable<FormField> fields) {
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

    public FormField resolveSymbol(SymbolNode symbolNode) {
        return resolveSymbol(symbolNode.getName());
    }

    public FormField resolveSymbol(String name) {
        
        if(name.equals("@parent")) {
            Optional<FormField> parentField = formClass.getParentField();
            if(parentField.isPresent()) {
                return parentField.get();
            } else {
                throw new RuntimeException("Form " + this.formClass.getLabel() +
                        " [" + this.formClass.getId() + "] is not a subform.");
            }
        }
        
        Optional<FormField> match = tryResolveSymbol(name);
        if (match.isPresent()) {
            return match.get();
        } else {
            throw new SymbolNotFoundException(name);
        }
    }
    
    public Optional<FormField> tryResolveSymbol(String name) {
        FormField field = idMap.get(name);
        if(field != null) {
            return Optional.of(field);
        }
        Collection<FormField> matching = codeMap.get(name.toLowerCase());
        if(matching.isEmpty()) {
            // as last resort, try matching against label
            matching = labelMap.get(name.toLowerCase());
        }

        if (matching.size() > 1) {
            throw new AmbiguousSymbolException(name);
        } else if (matching.isEmpty()) {
           return Optional.absent();
        } else {
            return Optional.of(Iterables.getOnlyElement(matching));
        }
    }

    public Optional<FormField> tryResolveSymbol(SymbolNode value) {
        return tryResolveSymbol(value.getName());
    }
}
