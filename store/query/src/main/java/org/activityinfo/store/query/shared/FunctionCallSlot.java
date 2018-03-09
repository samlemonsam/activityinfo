package org.activityinfo.store.query.shared;

import com.google.common.collect.Lists;
import org.activityinfo.model.formula.diagnostic.FormulaException;
import org.activityinfo.model.formula.functions.ColumnFunction;
import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.EmptyColumnView;

import java.util.List;

class FunctionCallSlot implements Slot<ColumnView> {
    private final List<Slot<ColumnView>> argumentSlots;
    private ColumnFunction function;

    public FunctionCallSlot(ColumnFunction function, List<Slot<ColumnView>> argumentSlots) {
        this.function = function;
        this.argumentSlots = argumentSlots;

    }

    @Override
    public ColumnView get() {
        List<ColumnView> arguments = Lists.newArrayList();
        for (Slot<ColumnView> argument : argumentSlots) {
            ColumnView view = argument.get();
            if (view == null) {
                throw new IllegalStateException();
            }
            arguments.add(view);
        }
        try {
            return function.columnApply(arguments.get(0).numRows(), arguments);
        } catch (FormulaException e) {
            int numRows = arguments.get(0).numRows();
            return new EmptyColumnView(ColumnType.STRING, numRows);
        }
    }
}
