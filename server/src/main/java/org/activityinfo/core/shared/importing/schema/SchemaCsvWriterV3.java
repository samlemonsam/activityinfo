package org.activityinfo.core.shared.importing.schema;


import com.google.common.annotations.VisibleForTesting;
import org.activityinfo.legacy.shared.model.ActivityDTO;
import org.activityinfo.legacy.shared.model.ActivityFormDTO;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.SubFormKind;
import org.activityinfo.model.formTree.BatchFormClassProvider;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.subform.SubFormReferenceType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SchemaCsvWriterV3 {


    public static final String MULTIPLE_SELECT = "Multiple Select";
    public static final String SINGLE_SELECT = "Single Select";

    private class FieldContext {

        private UserDatabaseDTO db;
        private FormClass formClass;
        private FormField subFormField;
        private SubFormKind subFormKind;

        public FieldContext(UserDatabaseDTO db, FormClass formClass) {
            this.db = db;
            this.formClass = formClass;
        }
        
        public FieldContext subForm(FormField field, FormClass subFormClass) {
            FieldContext context = new FieldContext(db, formClass);
            context.subFormField = field;
            context.subFormKind = subFormClass.getSubFormKind();
            return context;
        }
    };

    
    public enum Column {
        DATABASE_ID("DatabaseId") {
            @Override
            public Object value(FieldContext context, FormField field, EnumItem enumItem) {
                return context.db.getId();
            }
        },
        
        DATABASE_NAME("DatabaseName") {
            @Override
            public Object value(FieldContext context, FormField field, EnumItem enumItem) {
                return context.db.getName();
            }
        },

        FORM_VERSION("FormVersion") {
            @Override
            public Object value(FieldContext context, FormField field, EnumItem enumItem) {
                return "3.0";
            }
        },
        
        FORM_ID("FormId") {
            @Override
            public Object value(FieldContext context, FormField field, EnumItem enumItem) {
                return context.formClass.getId().asString();
            }
        },
        
        FORM_NAME("FormName") {
            @Override
            public Object value(FieldContext context, FormField field, EnumItem enumItem) {
                return context.formClass.getLabel();
            }
        },
        
        SUBFORM("SubForm") {
            @Override
            public Object value(FieldContext context, FormField field, EnumItem enumItem) {
                if(context.subFormField != null) {
                    return context.subFormField.getLabel();
                }
                return null;
            }
        },
        
        SUBFORM_TYPE("SubFormType") {
            @Override
            public Object value(FieldContext context, FormField field, EnumItem enumItem) {
                if(context.subFormKind != null) {
                    return context.subFormKind.name().toLowerCase();
                }
                return null;
            }
        },
        
        FIELD_ID("FieldId") {
            @Override
            public Object value(FieldContext context, FormField field, EnumItem enumItem) {
                return field.getId();
            }
        },
  
        FIELD_CODE("FieldCode") {
            @Override
            public Object value(FieldContext context, FormField field, EnumItem enumItem) {
                return field.getCode();
            }
        },
        
        FIELD_TYPE("FieldType") {
            @Override
            public Object value(FieldContext context, FormField field, EnumItem enumItem) {
                if(field.getType() instanceof EnumType) {
                    if(((EnumType) field.getType()).getCardinality() == Cardinality.MULTIPLE) {
                        return MULTIPLE_SELECT;
                    } else {
                        return SINGLE_SELECT;
                    }
                }
                return field.getType().getTypeClass().getId();
            }
        },

        FIELD_NAME("FieldName") {
            @Override
            public Object value(FieldContext context, FormField field, EnumItem enumItem) {
                return field.getLabel();
            }
        },
        
        FIELD_DESCRIPTION("FieldDescription") {
            @Override
            public Object value(FieldContext context, FormField field, EnumItem enumItem) {
                return field.getDescription();
            }
        },

        FIELD_REQUIRED("Required") {
            @Override
            public Object value(FieldContext context, FormField field, EnumItem enumItem) {
                return field.isRequired();
            }
        },
        
        FIELD_UNITS("Units") {
            @Override
            public Object value(FieldContext context, FormField field, EnumItem enumItem) {
                if(field.getType() instanceof QuantityType) {
                    return ((QuantityType) field.getType()).getUnits();
                } else {
                    return null;
                }
            }
        },
        
        FIELD_EXPR("Expression") {
            @Override
            public Object value(FieldContext context, FormField field, EnumItem enumItem) {
                if(field.getType() instanceof CalculatedFieldType) {
                    return ((CalculatedFieldType) field.getType()).getExpression();
                }
                return null;
            }
        },
        
        FIELD_RANGE("References") {
            @Override
            public Object value(FieldContext context, FormField field, EnumItem enumItem) {
                if(field.getType() instanceof ReferenceType) {
                    ReferenceType type = (ReferenceType) field.getType();
                    if(type.getRange().size() > 0) {
                        ResourceId formId = type.getRange().iterator().next();
                        return formId.asString();
                    }
                }
                return null;
            }
        },
        
        ENUM_ITEM("Choice Id") {
            @Override
            public Object value(FieldContext context, FormField field, EnumItem enumItem) {
                if(enumItem != null) {
                    return enumItem.getId();
                }
                return null;
            }
        },
        
        ENUM_LABEL("Choice Label") {
            @Override
            public Object value(FieldContext context, FormField field, EnumItem enumItem) {
                if(enumItem != null) {
                    return enumItem.getLabel();
                }
                return null;
            }
        };
        
        private String header;

        Column(String header) {
            this.header = header;
        }

        public String getHeader() {
            return header;
        }

        public abstract Object value(FieldContext context, FormField field, EnumItem enumItem);
    }



    private final CsvWriter csv = new CsvWriter();
    private BatchFormClassProvider catalog;

    public SchemaCsvWriterV3(BatchFormClassProvider catalog) {
        this.catalog = catalog;
    }

    public void writeForms(UserDatabaseDTO db) {
        List<ResourceId> formIds = new ArrayList<>();

        for (ActivityDTO activity : db.getActivities()) {
            if(activity.getReportingFrequency() == ActivityFormDTO.REPORT_ONCE) {
                formIds.add(CuidAdapter.activityFormClass(activity.getId()));
            }
        }

        writeForms(db, formIds);
    }

    @VisibleForTesting
    void writeForms(UserDatabaseDTO db, List<ResourceId> formIds) {
        writeHeaders();
        Map<ResourceId, FormClass> formClasses = catalog.getFormClasses(formIds);
        for (ResourceId formId : formIds) {
            FormClass formClass = formClasses.get(formId);
            if(formClass == null) {
                throw new IllegalStateException("No FormClass for " + formId);
            }
            writeForm(db, formClass);
        }
    }

    private void writeHeaders() {
        Column[] columns = Column.values();
        Object[] headers = new Object[columns.length];

        for (int i = 0; i < columns.length; i++) {
            headers[i] = columns[i].getHeader();
        }

        csv.writeLine(headers);
    }

    private void writeForm(UserDatabaseDTO db, FormClass formClass) {
        FieldContext context = new FieldContext(db, formClass);
        List<FormField> fields = formClass.getFields();
        
        for (FormField field : fields) {
            if(field.getType() instanceof EnumType) {
                writeEnumItems(context, field, ((EnumType) field.getType()).getValues());
            } else if(field.getType() instanceof SubFormReferenceType) {
                writeSubForm(context, field);
            } else if(!isBuiltinField(formClass, field)) {
                writeField(context, field);
            }
        }
    }

    private boolean isBuiltinField(FormClass formClass, FormField field) {
        if(formClass.getId().getDomain() != CuidAdapter.ACTIVITY_DOMAIN) {
            return false;
        }
        int activityId = CuidAdapter.getLegacyIdFromCuid(formClass.getId());
        return field.getId().equals(CuidAdapter.partnerField(activityId)) ||
                field.getId().equals(CuidAdapter.projectField(activityId));
    }

    private void writeSubForm(FieldContext context, FormField field) {
        SubFormReferenceType fieldType = (SubFormReferenceType) field.getType();
        FormClass subFormClass = catalog.getFormClass(fieldType.getClassId());
        FieldContext subFormContext = context.subForm(field, subFormClass);

        for (FormField subField : subFormClass.getFields()) {
            if (subField.getType() instanceof EnumType) {
                writeEnumItems(subFormContext, subField, ((EnumType) subField.getType()).getValues());
            } else {
                writeField(subFormContext, subField);
            }
        }
    }

    private void writeField(FieldContext context, FormField field) {
        writeRow(context, field, null);
    }

    private void writeEnumItems(FieldContext context, FormField field, List<EnumItem> values) {
        if(values.size() == 0) {
            writeRow(context, field, null);
        } else {
            for (EnumItem value : values) {
                writeRow(context, field, value);
            }
        }
    }

    private void writeRow(FieldContext context, FormField field, EnumItem value) {
        Column[] columns = Column.values();
        Object[] values = new Object[columns.length];

        for (int i = 0; i < columns.length; i++) {
            values[i] = columns[i].value(context, field, value);
        }
        csv.writeLine(values);
    }
    

    public String toString() {
        return csv.toString();
    }

}