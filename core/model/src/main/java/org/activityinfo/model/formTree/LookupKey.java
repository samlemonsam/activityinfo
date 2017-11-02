package org.activityinfo.model.formTree;

import org.activityinfo.model.expr.CompoundExpr;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.SerialNumber;
import org.activityinfo.model.type.SerialNumberType;
import org.activityinfo.model.type.primitive.HasStringValue;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A level within a hierarchy of lookup choices.
 */
public class LookupKey {

    private int keyIndex;

    private LookupKey parentLevel;

    private SymbolExpr parentFieldId;

    private ResourceId formId;

    private String levelLabel;

    /**
     * This key's field id.
     */
    private SymbolExpr fieldId;

    private List<LookupKey> childLevels = new ArrayList<>();

    LookupKey(int keyIndex,
              String parentFieldId,
              LookupKey parentLevel,
              ResourceId formId,
              String levelLabel,
              String fieldId) {
        this.keyIndex = keyIndex;
        if(parentFieldId == null) {
            this.parentFieldId = null;
        } else {
            this.parentFieldId = new SymbolExpr(parentFieldId);
        }
        this.parentLevel = parentLevel;
        this.formId = formId;
        this.levelLabel = levelLabel;
        this.fieldId = new SymbolExpr(fieldId);

        if(parentLevel != null) {
            parentLevel.childLevels.add(this);
        }
    }

    LookupKey(int keyIndex, ResourceId formId, String levelLabel, String fieldId) {
        this(keyIndex, null, null, formId, levelLabel, fieldId);
    }

    public int getKeyIndex() {
        return keyIndex;
    }

    public boolean isRoot() {
        return parentLevel == null;
    }

    public boolean isLeaf() {
        return childLevels.isEmpty();
    }

    public String getLevelLabel() {
        return levelLabel;
    }

    public ResourceId getFormId() {
        return formId;
    }

    public SymbolExpr getKeyField() {
        return fieldId;
    }

    public LookupKey getParentLevel() {
        assert parentLevel != null;
        return parentLevel;
    }

    public List<LookupKey> getParentLevels() {
        List<LookupKey> parents = new ArrayList<>();
        collectParents(parents);
        return parents;
    }

    private void collectParents(List<LookupKey> parents) {
        if(!this.isRoot()) {
            parents.add(getParentLevel());
            getParentLevel().collectParents(parents);
        }
    }

    public List<LookupKey> getChildLevels() {
        return childLevels;
    }

    @Override
    public String toString() {
        return "[" + levelLabel + ": " +  formId + "." + fieldId + "]";
    }

    private void collectKeys(@Nullable ExprNode baseField, Map<LookupKey, ExprNode> keys) {
        keys.put(this, join(baseField, fieldId));

        if(!isRoot()) {
            parentLevel.collectKeys(join(baseField, parentFieldId), keys);
        }
    }

    private ExprNode join(@Nullable ExprNode base, SymbolExpr field) {
        if(base == null) {
            return field;
        } else {
            return new CompoundExpr(base, field);
        }
    }

    public Map<LookupKey, ExprNode> getKeyFormulas() {
        Map<LookupKey, ExprNode> keys = new HashMap<>();
        collectKeys(null, keys);
        return keys;
    }

    public ExprNode getParentKey() {
        return join(parentFieldId, parentLevel.getKeyField());
    }

    public String label(FormInstance record) {
        if(fieldId.getName().equals(ColumnModel.ID_SYMBOL)) {
            return record.getId().asString();
        }

        FieldValue fieldValue = record.get(ResourceId.valueOf(fieldId.getName()));
        if(fieldValue == null) {
            return record.getId().asString();
        }

        if(fieldValue instanceof SerialNumber) {
            SerialNumberType type = new SerialNumberType();
            return type.format(((SerialNumber) fieldValue));

        } else {
            return ((HasStringValue) fieldValue).asString();
        }
    }
}
