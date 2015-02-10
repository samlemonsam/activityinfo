package org.activityinfo.test.pageobject.web.reports;

import com.google.common.collect.Iterables;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.gxt.GxtGrid;
import org.activityinfo.test.pageobject.gxt.GxtPanel;
import org.activityinfo.test.pageobject.gxt.GxtTree;
import org.activityinfo.test.pageobject.web.components.GridData;

import java.util.List;

public class PivotTableEditor {
    
    private FluentElement container;

    public PivotTableEditor(FluentElement container) {
        this.container = container;
    }

    private GxtTree indicatorTree() {
        return GxtPanel.find(container, "Indicators").tree();
    }

    private GxtTree dimensionTree() {
        return GxtPanel.find(container, "Dimensions").tree();
    }

    private GxtGrid dataTable() {
        return GxtGrid.findGrids(container).first().get();
    }


    public void selectMeasure(String name) {
        indicatorTree().search(name).get().setChecked(true);
    }


    public void selectDimensions(List<String> rowDimensions, List<String> columnDimensions) {
        dimensionTree().setChecked(Iterables.concat(rowDimensions, columnDimensions));
    }
    
    public GridData extractData() {

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        GxtGrid grid = dataTable();
        GridData data = grid.extractData();
        System.out.println(data);
        return data;
    }
}
