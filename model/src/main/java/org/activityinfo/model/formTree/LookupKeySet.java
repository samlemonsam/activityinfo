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
package org.activityinfo.model.formTree;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.TypedFormRecord;
import org.activityinfo.model.formula.CompoundExpr;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.formula.SymbolNode;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.SerialNumberType;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.promise.Maybe;

import java.util.*;

/**
 * Set of keys that can be used to lookup a value for a reference field.
 *
 * <p>Fields of type {@link ReferenceType} refer to Records in other Forms. In order to be select
 * a value, users need a way to "look up" the ID of a Record. We could of course ask users to the enter
 * the auto-generated RecordId, but that would be difficult.</p>
 *
 * <p>One intuitive way to look up Records is to use the fields that form designers have identified as "keys".</p>
 *
 * <p>In the simplest case, a Form will have a single text key field. Such a reference field will have a single
 * {@link LookupKey}. The user can select one value from the {@code LookupKey} and that will be sufficient to
 * uniquely identify the record.</p>
 *
 * <p>In other cases, a Form might have two text keys, such as "Last Name" and "First Name". Neither key is
 * sufficient to identify a record uniquely, but together they can be used to look up a record.</p>
 */
public class LookupKeySet {

    private List<LookupKey> lookupKeys = new ArrayList<>();

    /**
     * Map from a referenced form to it's leaf lookup key.
     *
     * For example, if you have a reference field that includes [PROVINCE, TERRITORY, SECTOR]
     * in its reference range, that means that _either_ a PROVINCE can be selected, _or_ a TERRITORY _or_ a SECTOR
     * can be selected.
     *
     * If those three forms have the following keys:
     *
     * <pre>
     *     PROVINCE:  k0[PROVINCE.name]
     *     TERRITORY: k0[PROVINCE.name], k1[TERRITORY.name]
     *     SECTOR:    k0[PROVINCE.name], k1[TERRITORY.name], k2[SECTOR.name]
     * </pre>
     *
     * Then the "leaf key" of the PROVINCE form is k0, while the leaf key of TERRITORY is k1, and
     * the leaf key of SECTOR is k2.
     *
     */
    private Map<ResourceId, LookupKey> leafKeyMap = HashBiMap.create();

    /**
     * Maps each visited form to its leaf key.
     *
     * <p>For reference fields with multiple forms included in the range, the keys may overlap. We want
     * to
     *
     */
    private Map<ResourceId, LookupKey> formMap = new HashMap<>();

    private Multimap<ResourceId, ResourceId> parentMap = HashMultimap.create();

    private FormTree formTree;
    private String referenceFieldLabel;

    public LookupKeySet(FormTree formTree, FormField referenceField) {
        this.formTree = formTree;
        this.referenceFieldLabel = referenceField.getLabel();

        ReferenceType referenceType = (ReferenceType) referenceField.getType();
        for (ResourceId referenceFormId : referenceType.getRange()) {
            if (!formTree.getFormMetadata(referenceFormId).isAccessible()) {
                continue;
            }
            leafKeyMap.put(referenceFormId, addLevels(formTree.getFormClass(referenceFormId)));
            leafKeyMap.get(referenceFormId).setLeaf(true);
        }

        // Now that we know how many keys there are and from which forms,
        // we can compose labels
        for (LookupKey lookupKey : lookupKeys) {
            composeLabel(lookupKey);
        }
    }

    private LookupKey addLevels(FormClass formClass) {

        ResourceId formId = formClass.getId();

        if(formMap.containsKey(formId)) {
            return formMap.get(formId);
        }

        // if serial number is present, we use that exclusively.
        Optional<FormField> serialNumberField = findSerialNumberField(formClass);
        if(serialNumberField.isPresent()) {
            LookupKey lookupKey = serialNumberLevel(formClass, serialNumberField.get());
            lookupKeys.add(lookupKey);
            formMap.put(formId, lookupKey);
            return lookupKey;
        }

        List<LookupKey.ParentKey> parentKeys = Lists.newArrayList();

        // If there is a reference key, then we climb the reference tree recursively.
        Optional<FormField> referenceKey = findReferenceKey(formClass);
        if(referenceKey.isPresent()) {
            ReferenceType referenceType = (ReferenceType) referenceKey.get().getType();
            for(ResourceId referencedFormId : referenceType.getRange()) {
                FormClass referencedFormClass = formTree.getFormClass(referencedFormId);
                parentMap.put(formId, referencedFormId);
                parentKeys.add(new LookupKey.ParentKey(addLevels(referencedFormClass), referenceKey.get().getId().asString()));
            }
        }

        LookupKey lookupKey = null;

        // Now check for text key fields
        for (FormField formField : formClass.getFields()) {
            if(isTextLikeKey(formField)) {
                lookupKey = textKeyLevel(formClass, parentKeys, formField);
                lookupKeys.add(lookupKey);
                parentKeys.clear();
                parentKeys.add(new LookupKey.ParentKey(lookupKey,null));
            }
        }


        // If this form's only key is a reference to another form, then
        // it has a 1-to-1 relatonship with that form, and we don't need our own key
        if(lookupKey == null && parentKeys.size() == 1) {
            return parentKeys.get(0).getKey();
        }

        // If there are really no other key fields, then use the autogenerated id as a key
        if (lookupKey == null) {
            lookupKey = idLevel(formClass);
            if (!parentKeys.isEmpty()) {
                lookupKey.addParentLevels(parentKeys);
            }
            lookupKeys.add(lookupKey);
        }

        formMap.put(formId, lookupKey);

        return lookupKey;
    }

    private boolean isTextLikeKey(FormField formField) {
        return formField.isKey() &&
                (formField.getType() instanceof TextType ||
                        formField.getType() instanceof EnumType);
    }


    private int nextKeyIndex() {
        return lookupKeys.size() + 1;
    }


    private LookupKey serialNumberLevel(FormClass form, FormField field) {
        return new LookupKey(nextKeyIndex(), form, field);
    }

    private LookupKey textKeyLevel(FormClass form, List<LookupKey.ParentKey> parentKeys, FormField formField) {
        return new LookupKey(nextKeyIndex(), parentKeys, form, formField);
    }

    private LookupKey idLevel(FormClass formSchema) {
        return new LookupKey(nextKeyIndex(), formSchema);
    }

    private Optional<FormField> findSerialNumberField(FormClass formClass) {
        for (FormField formField : formClass.getFields()) {
            if(formField.getType() instanceof SerialNumberType) {
                return Optional.of(formField);
            }
        }
        return Optional.absent();
    }

    private Optional<FormField> findReferenceKey(FormClass formClass) {
        for (FormField formField : formClass.getFields()) {
            if(formField.isKey() && formField.getType() instanceof ReferenceType) {
                return Optional.of(formField);
            }
        }
        return Optional.absent();
    }


    private void composeLabel(LookupKey lookupKey) {
        StringBuilder label = new StringBuilder();

        if (formMap.size() > 1) {
            // If there are keys that live on multiple forms,
            // we need to distinguish them by their form name
            label.append(lookupKey.getFormLabel());
        } else {
            // If there is only one form referenced,
            // we always use the referencing field's label
            label.append(Strings.nullToEmpty(referenceFieldLabel));
        }

        if(lookupKey.getFieldLabel() == null) {
            label.append(" ID");
        } else {
            if(!label.toString().equals(lookupKey.getFieldLabel())) {
                label.append(" ");
                label.append(lookupKey.getFieldLabel());
            }
        }

        lookupKey.keyLabel = label.toString();
    }

    public List<LookupKey> getLookupKeys() {
        return lookupKeys;
    }

    /**
     * @return a List of LookupKeys with no children.
     */
    public Collection<LookupKey> getLeafKeys() {
        return leafKeyMap.values();
    }


    /**
     * Composes a human-readable label for a record reference.
     */
    public Maybe<String> label(RecordTree tree, RecordRef ref) {
        Optional<TypedFormRecord> potentialRecord = tree.getRecord(ref).getIfVisible();
        if (potentialRecord.isPresent()) {
            TypedFormRecord record = potentialRecord.get();
            for (LookupKey lookupKey : lookupKeys) {
                if (record.getFormId().equals(lookupKey.getFormId())) {
                    return Maybe.of(lookupKey.label(record));
                }
            }
        }
        return Maybe.notFound();
    }

    public LookupKey getKey(int i) {
        return lookupKeys.get(i);
    }

    public LookupKey getLeafKey(ResourceId referencedFormId) {
        LookupKey lookupKey = leafKeyMap.get(referencedFormId);
        assert lookupKey != null : "No leaf key for " + referencedFormId;
        return lookupKey;
    }

    public Set<ResourceId> getAncestorForms(ResourceId formId) {
        Set<ResourceId> set = new HashSet<>();
        collectAncestors(Collections.singletonList(formId), set);
        return set;
    }

    private void collectAncestors(Collection<ResourceId> formIds, Set<ResourceId> set) {
        for (ResourceId formId : formIds) {
            Collection<ResourceId> parents = parentMap.get(formId);
            set.addAll(parents);
            collectAncestors(parents, set);
        }
    }

    public Map<LookupKey, FormulaNode> getKeyFormulas(FormulaNode baseField) {

        if(leafKeyMap.size() == 1) {
            return Iterables.getOnlyElement(getLeafKeys()).getKeyFormulas(baseField);

        } else {
            // For the case where a field can refer to one of several different forms,
            // query the keys using the syntax [formId].[keyFieldId]

            Map<LookupKey, FormulaNode> keyMap = new HashMap<>();
            for (LookupKey lookupKey : lookupKeys) {
                keyMap.put(lookupKey, new CompoundExpr(new SymbolNode(lookupKey.getFormId()), lookupKey.getKeyField()));
            }

            return keyMap;
        }
    }

    public Map<LookupKey, FormulaNode> getKeyFormulas(ResourceId fieldId) {
        return getKeyFormulas(new SymbolNode(fieldId));
    }

    public boolean hasLeafKey(ResourceId referencedFormId) {
        return leafKeyMap.containsKey(referencedFormId);
    }
}