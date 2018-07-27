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
package org.activityinfo.analysis.pivot.viewModel;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Ordering;
import org.activityinfo.analysis.ParsedFormula;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formula.CompoundExpr;
import org.activityinfo.model.formula.SymbolNode;
import org.activityinfo.model.formula.functions.date.MonthFunction;
import org.activityinfo.model.formula.functions.date.QuarterFunction;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.time.LocalDateType;
import org.activityinfo.model.analysis.pivot.DateLevel;
import org.activityinfo.model.analysis.pivot.DimensionMapping;
import org.activityinfo.model.analysis.pivot.DimensionModel;

import java.util.*;

/**
 * Models a measure that is part of the analysis,
 * based on the user's model as well as metadata
 */
public class EffectiveMapping {
    private int index;
    private DimensionModel model;
    private DimensionMapping mapping;
    private ParsedFormula formula;

    private boolean multiValued;

    public EffectiveMapping(FormTree formTree, int index, DimensionModel model, DimensionMapping mapping) {
        this.index = index;
        this.model = model;
        this.mapping = mapping;
        if(this.mapping != null) {
            this.formula = new ParsedFormula(formTree, mapping.getFormula());
            if(this.formula.isValid()) {
                if(this.formula.getResultType() instanceof EnumType) {
                    EnumType type = (EnumType) this.formula.getResultType();
                    if(type.getCardinality() == Cardinality.MULTIPLE) {
                        multiValued = true;
                    }
                }
            }
        }
    }

    public String getId() {
        return model.getId();
    }

    public int getIndex() {
        return index;
    }

    /**
     *
     * @return true if this is a "fixed" dimension like Measure or Statistic that is independent of
     * the fields aggregated.
     */
    public boolean isFixed() {
        return model.getId().equals(DimensionModel.MEASURE_ID) ||
               model.getId().equals(DimensionModel.STATISTIC_ID);
    }

    public boolean isMultiValued() {
        return multiValued;
    }

    /**
     * @return true if this dimension has a single value for each row.
     */
    public boolean isSingleValued() {
        return !isFixed() && !multiValued;
    }

    public DimensionMapping getMapping() {
        return mapping;
    }

    public ParsedFormula getFormula() {
        return formula;
    }

    public boolean isDate() {
        if(this.mapping != null) {
            if (this.formula.isValid()) {
                return this.formula.getResultType() instanceof LocalDateType;
            }
        }
        return false;
    }

    public List<ColumnModel> getRequiredColumns() {

        if(multiValued) {
            List<ColumnModel> columns = new ArrayList<>();
            EnumType enumType = (EnumType) this.formula.getResultType();
            for (EnumItem enumItem : enumType.getValues()) {
                ColumnModel columnModel = new ColumnModel();
                columnModel.setId(getColumnId(enumItem));
                columnModel.setFormula(new CompoundExpr(
                        this.formula.getRootNode(),
                        new SymbolNode(enumItem.getId())));
                columns.add(columnModel);
            }
            return columns;
        }

        if(this.mapping != null && this.formula.isValid()) {
            ColumnModel columnModel = new ColumnModel();
            columnModel.setId(getColumnId());
            columnModel.setFormula(this.formula.getFormula());
            return Collections.singletonList(columnModel);
        }
        return Collections.emptyList();
    }

    private String getColumnId() {
        return "d" + index;
    }

    private String getColumnId(EnumItem item) {
        return getColumnId() + "_" + item.getId().asString();
    }

    public DimensionReader createReader(ColumnSet columnSet) {

        String missingCategory = computeMissingCategory();

        ColumnView columnView = columnSet.getColumnView(getColumnId());
        if(columnView == null) {
            return row -> missingCategory;
        }

        Function<String, String> map = createMap();


        return row -> {
            String category = columnView.getString(row);
            if(category == null) {
                return missingCategory;
            }
            return map.apply(category);
        };
    }

    private String computeMissingCategory() {
        if(model.getMissingIncluded()) {
            return getMissingLabel();
        } else {
            // Observations with a missing category will be excluded from the analysis
            return null;
        }
    }

    private String getMissingLabel() {
        return model.getMissingLabel().orElse(I18N.CONSTANTS.none());
    }

    private Function<String, String> createMap() {
        if(formula.getResultType() instanceof LocalDateType) {
            Optional<DateLevel> dateLevel = model.getDateLevel();
            if(dateLevel.isPresent()) {
                switch (dateLevel.get()) {
                    case YEAR:
                        return EffectiveMapping::year;
                    case MONTH:
                        return EffectiveMapping::month;
                    case QUARTER:
                        return EffectiveMapping::quarter;
                }
            }
        }
        return Functions.identity();
    }

    private static String month(String date) {
        return Integer.toString(MonthFunction.fromIsoString(date));
    }

    private static String quarter(String date) {
        int month = MonthFunction.fromIsoString(date);
        return "Q" + QuarterFunction.fromMonth(month);
    }

    private static String year(String date) {
        return date.substring(0, 4);
    }

    public MultiDim createMultiDimSet(ColumnSet columnSet) {
        EnumType enumType = (EnumType) this.formula.getResultType();

        int numItems = enumType.getValues().size();
        int numCategories;

        if(model.getMissingIncluded()) {
            // If we are included cases with no values set, then consider it
            // a category on its own
            numCategories = numItems + 1;
        } else {
            numCategories = numItems;
        }

        String[] labels = new String[numCategories];
        BitSet[] bitSets = new BitSet[numCategories];
        List<EnumItem> values = enumType.getValues();

        for (int j = 0; j < numItems; j++) {

            BitSet bitSet = new BitSet();
            EnumItem enumItem = values.get(j);
            ColumnView columnView = columnSet.getColumnView(getColumnId(enumItem));

            assert columnView != null;
            assert columnView.getType() == ColumnType.BOOLEAN;

            for (int i = 0; i < columnView.numRows(); i++) {
                if (columnView.getBoolean(i) == ColumnView.TRUE) {
                    bitSet.set(i, true);
                }
            }
            labels[j] = enumItem.getLabel();
            bitSets[j] = bitSet;
        }

        if(model.getMissingIncluded()) {
            labels[numCategories - 1] = getMissingLabel();
            bitSets[numCategories - 1] = computeMissingBitSet(columnSet.getNumRows(), numItems, bitSets);
        }

        return new MultiDim(this.index, labels, bitSets);
    }

    /**
     * Computes the bitSet of cases that have NO values sent in any of the
     * {@code numItems}
     *
     * @param numRows the number of rows.
     * @param numItems the number of enum items
     * @param bitSets an array of bitSets, one for each enum item.
     * @return a bitSet where a bit is set to true for each row that has NO categories set to true.
     */
    private BitSet computeMissingBitSet(int numRows, int numItems, BitSet[] bitSets) {

        // Initialize missing bitSet to 1 for all rows
        BitSet missing = new BitSet(numRows);
        missing.set(0, numRows);

        // Now set missing=false when a category is set.
        for (int i = 0; i < numItems; i++) {
            missing.andNot(bitSets[i]);
        }

        return missing;
    }


    public Comparator<String> getCategoryComparator() {
        return Ordering.natural();
    }


}
