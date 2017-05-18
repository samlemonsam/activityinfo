package org.activityinfo.io.xlsform;

import com.google.common.base.Strings;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FormClassProvider;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.NarrativeType;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.model.type.time.LocalDateType;
import org.apache.poi.hssf.usermodel.HSSFCreationHelper;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Writes a ActivityInfo {@link org.activityinfo.model.form.FormClass} as 
 * an XLS form
 */
public class XlsFormBuilder {

    private static final int TYPE_COLUMN = 0;
    private static final int NAME_COLUMN = 1;
    private static final int LABEL_COLUMN = 2;
    private static final int UNITS_COLUMN = 3;
    private static final int REQUIRED_COLUMN = 4;
    private static final int RELEVANT_COLUMN = 5;
    private static final int CALCULATION_FIELD = 6;
    
    private static final int CHOICES_LIST_NAME_COLUMN = 0;
    private static final int CHOICES_NAME_COLUMN = 1;
    private static final int CHOICES_LABEL = 2;

    private final HSSFCreationHelper creationHelper;
    private final HSSFWorkbook book;
    private final HSSFSheet surveySheet;
    private final HSSFSheet choicesSheet;

    private int nextFieldRow;
    private int nextChoiceRow;

    private final FormClassProvider formClassProvider;

    public XlsFormBuilder(FormClassProvider formClassProvider) {
        this.formClassProvider = formClassProvider;

        book = new HSSFWorkbook();
        creationHelper = book.getCreationHelper();
        
        surveySheet = book.createSheet("survey");
        choicesSheet = book.createSheet("choices");

        addSurveySheetHeaders();
        addChoiceSheetHeaders();
        
        nextFieldRow = 1;
        nextChoiceRow = 1;
        
    }

    private void addSurveySheetHeaders() {
        HSSFRow headerRow = surveySheet.createRow(0);

        headerRow.createCell(TYPE_COLUMN).setCellValue("type");
        headerRow.createCell(NAME_COLUMN).setCellValue("name");
        headerRow.createCell(LABEL_COLUMN).setCellValue("label");
        headerRow.createCell(UNITS_COLUMN).setCellValue("units");
        headerRow.createCell(REQUIRED_COLUMN).setCellValue("required");
        headerRow.createCell(RELEVANT_COLUMN).setCellValue("relevant");
        headerRow.createCell(CALCULATION_FIELD).setCellValue("calculation");
    }

    private void addChoiceSheetHeaders() {
        HSSFRow headerRow = choicesSheet.createRow(0);
        headerRow.createCell(CHOICES_LIST_NAME_COLUMN).setCellValue("list name");
        headerRow.createCell(CHOICES_NAME_COLUMN).setCellValue("name");
        headerRow.createCell(CHOICES_LABEL).setCellValue("label");
    }
    
    public void build(ResourceId formClassId) {
        writeFields(formClassProvider.getFormClass(formClassId));
    }
    
    public void write(OutputStream outputStream) throws IOException {
        book.write(outputStream);
    }

    private void writeFields(FormClass formClass) {
        for (FormField field : formClass.getFields()) {
            if(field.getType() instanceof SubFormReferenceType) {
                writeSubForm(field);
            } else {
                writeSimpleField(field);
            }
        }
    }

    private void writeSubForm(FormField field) {
        SubFormReferenceType subFormType = (SubFormReferenceType) field.getType();
        FormClass formClass = formClassProvider.getFormClass(subFormType.getClassId());
        
        HSSFRow beginRow = surveySheet.createRow(nextFieldRow++);
        beginRow.createCell(TYPE_COLUMN).setCellValue("begin repeat");
        beginRow.createCell(NAME_COLUMN).setCellValue(field.getCode());
        
        writeFields(formClass);
        
        HSSFRow endRow = surveySheet.createRow(nextFieldRow++);
        endRow.createCell(TYPE_COLUMN).setCellValue("end repeat");
    }

    private void writeSimpleField(FormField field) {
        HSSFRow fieldRow = surveySheet.createRow(nextFieldRow++);

        String name = field.getCode();
        if(Strings.isNullOrEmpty(name)) {
            name = field.getId().asString();
        }
        
        fieldRow.createCell(NAME_COLUMN).setCellValue(name);
        fieldRow.createCell(LABEL_COLUMN).setCellValue(field.getLabel());
        fieldRow.createCell(REQUIRED_COLUMN).setCellValue(field.isRequired() ? "yes" : "no");

        FieldType type = field.getType();
        if(type instanceof QuantityType) {
            QuantityType quantityType = (QuantityType) field.getType();
            fieldRow.createCell(TYPE_COLUMN).setCellValue(XlsFormTypes.DECIMAL);
            fieldRow.createCell(UNITS_COLUMN).setCellValue(quantityType.getUnits());
            
        } else if(type instanceof TextType) {
            fieldRow.createCell(TYPE_COLUMN).setCellValue(XlsFormTypes.TEXT);

        } else if(type instanceof NarrativeType) {
            fieldRow.createCell(TYPE_COLUMN).setCellValue("narrative");
            
        } else if(type instanceof CalculatedFieldType) {
            CalculatedFieldType calculatedType = (CalculatedFieldType) field.getType();
            fieldRow.createCell(TYPE_COLUMN).setCellValue(XlsFormTypes.CALCULATE);
            fieldRow.createCell(CALCULATION_FIELD)
                    .setCellValue(calculatedType.getExpression());

        } else if(type instanceof LocalDateType) {
            fieldRow.createCell(TYPE_COLUMN).setCellValue(XlsFormTypes.DATE);

        } else if(type instanceof EnumType) {
            EnumType enumType = (EnumType) type;
            String typeName;
            if (enumType.getCardinality() == Cardinality.SINGLE) {
                typeName = XlsFormTypes.SELECT_ONE;
            } else {
                typeName = XlsFormTypes.SELECT_MULTIPLE;
            }
            String listName = name;
            fieldRow.createCell(TYPE_COLUMN).setCellValue(typeName + " " + listName);
            addChoices(listName, enumType);
        }
        
        if(field.getRelevanceConditionExpression() != null) {
            fieldRow.createCell(RELEVANT_COLUMN).setCellValue(field.getRelevanceConditionExpression());
        }
    }

    private void addChoices(String listName, EnumType enumType) {
        for (EnumItem enumItem : enumType.getValues()) {
            HSSFRow row = choicesSheet.createRow(nextChoiceRow++);
            row.createCell(CHOICES_LIST_NAME_COLUMN).setCellValue(listName);
            row.createCell(CHOICES_NAME_COLUMN).setCellValue(enumItem.getId().asString());
            row.createCell(CHOICES_LABEL).setCellValue(enumItem.getLabel());
        }
    }
}
