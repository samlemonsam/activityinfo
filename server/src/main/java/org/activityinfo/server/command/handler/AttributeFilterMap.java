package org.activityinfo.server.command.handler;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;

import java.util.Collection;
import java.util.Set;

/**
 * Maps attribute filter ids to their attribute group id and name.
 *
 * Attribute filters are _SERIALIZED_ as only the integer ids of the required attributes,
 * but they are actually applied by _NAME_ to all forms in the query.
 *
 */
public class AttributeFilterMap {

    private Multimap<String, String> attributeFilters = HashMultimap.create();

    public AttributeFilterMap(Filter filter, Iterable<FormTree> formTrees) {
        Set<Integer> attributeIds = filter.getRestrictions(DimensionType.Attribute);
        if(attributeIds.isEmpty()) {
            return;
        }

        for (FormTree formTree : formTrees) {
            for (FormTree.Node node : formTree.getLeaves()) {
                if(node.isEnum()) {
                    EnumType type = (EnumType) node.getType();
                    for (EnumItem enumItem : type.getValues()) {
                        int attributeId = CuidAdapter.getLegacyIdFromCuid(enumItem.getId());
                        if(attributeIds.contains(attributeId)) {
                            attributeFilters.put(node.getField().getLabel(), enumItem.getLabel());
                            attributeIds.remove(attributeId);
                            if(attributeIds.isEmpty()) {
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    public Set<String> getFilteredFieldNames() {
        return attributeFilters.keySet();
    }

    public Collection<String> getFilteredValues(String fieldName) {
        return attributeFilters.get(fieldName);
    }
}
