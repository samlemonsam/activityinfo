package org.activityinfo.ui.client.style.table;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;

/**
 * Application styles for the CellTable
 */
public class CellTableResources implements CellTable.Resources {


    public static final CellTableResources INSTANCE = new CellTableResources();

    @Override
    public ImageResource cellTableFooterBackground() {
        // not referenced in our CSS so should not be called
        throw new UnsupportedOperationException();
    }

    @Override
    public ImageResource cellTableHeaderBackground() {
        // not referenced in our CSS so should not be called
        throw new UnsupportedOperationException();
    }

    @Override
    public ImageResource cellTableLoading() {
        return DataGridResources.INSTANCE.dataGridLoading();
    }

    @Override
    public ImageResource cellTableSelectedBackground() {
        // not referenced in our CSS so should not be called
        throw new UnsupportedOperationException();
    }

    @Override
    public ImageResource cellTableSortAscending() {
        return DataGridResources.INSTANCE.dataGridSortAscending();
    }

    @Override
    public ImageResource cellTableSortDescending() {
        return DataGridResources.INSTANCE.dataGridSortDescending();
    }


    @Override
    public CellTable.Style cellTableStyle() {
        return GWT.create(CellTableStyle.class);
    }

    private static class StyleAdapter implements CellTable.Style {

        @Override
        public String cellTableCell() {
            return DataGridResources.INSTANCE.dataGridStyle().dataGridCell();
        }

        @Override
        public String cellTableEvenRow() {
            return DataGridResources.INSTANCE.dataGridStyle().dataGridEvenRow();
        }

        @Override
        public String cellTableEvenRowCell() {
            return DataGridResources.INSTANCE.dataGridStyle().dataGridEvenRowCell();
        }

        @Override
        public String cellTableFirstColumn() {
            return DataGridResources.INSTANCE.dataGridStyle().dataGridFirstColumn();
        }

        @Override
        public String cellTableFirstColumnFooter() {
            return DataGridResources.INSTANCE.dataGridStyle().dataGridFirstColumnFooter();
        }

        @Override
        public String cellTableFirstColumnHeader() {
            return DataGridResources.INSTANCE.dataGridStyle().dataGridFirstColumnHeader();
        }

        @Override
        public String cellTableFooter() {
            return DataGridResources.INSTANCE.dataGridStyle().dataGridFooter();
        }

        @Override
        public String cellTableHeader() {
            return DataGridResources.INSTANCE.dataGridStyle().dataGridHeader();
        }

        @Override
        public String cellTableHoveredRow() {
            return DataGridResources.INSTANCE.dataGridStyle().dataGridHoveredRow();
        }

        @Override
        public String cellTableHoveredRowCell() {
            return DataGridResources.INSTANCE.dataGridStyle().dataGridHoveredRowCell();
        }

        @Override
        public String cellTableKeyboardSelectedCell() {
            return DataGridResources.INSTANCE.dataGridStyle().dataGridKeyboardSelectedCell();
        }

        @Override
        public String cellTableKeyboardSelectedRow() {
            return DataGridResources.INSTANCE.dataGridStyle().dataGridKeyboardSelectedRow();
        }

        @Override
        public String cellTableKeyboardSelectedRowCell() {
            return DataGridResources.INSTANCE.dataGridStyle().dataGridKeyboardSelectedRowCell();
        }

        @Override
        public String cellTableLastColumn() {
            return DataGridResources.INSTANCE.dataGridStyle().dataGridLastColumn();
        }

        @Override
        public String cellTableLastColumnFooter() {
            return DataGridResources.INSTANCE.dataGridStyle().dataGridLastColumnFooter();
        }

        @Override
        public String cellTableLastColumnHeader() {
            return DataGridResources.INSTANCE.dataGridStyle().dataGridLastColumnHeader();
        }

        @Override
        public String cellTableLoading() {
            return "loading";
        }

        @Override
        public String cellTableOddRow() {
            return DataGridResources.INSTANCE.dataGridStyle().dataGridOddRow();
        }

        @Override
        public String cellTableOddRowCell() {
            return DataGridResources.INSTANCE.dataGridStyle().dataGridOddRowCell();
        }

        @Override
        public String cellTableSelectedRow() {
            return DataGridResources.INSTANCE.dataGridStyle().dataGridSelectedRow();
        }

        @Override
        public String cellTableSelectedRowCell() {
            return DataGridResources.INSTANCE.dataGridStyle().dataGridSelectedRowCell();
        }

        @Override
        public String cellTableSortableHeader() {
            return DataGridResources.INSTANCE.dataGridStyle().dataGridSortableHeader();
        }

        @Override
        public String cellTableSortedHeaderAscending() {
            return DataGridResources.INSTANCE.dataGridStyle().dataGridSortedHeaderAscending();
        }

        @Override
        public String cellTableSortedHeaderDescending() {
            return DataGridResources.INSTANCE.dataGridStyle().dataGridSortedHeaderDescending();
        }

        @Override
        public String cellTableWidget() {
            return DataGridResources.INSTANCE.dataGridStyle().dataGridWidget();
        }

        @Override
        public boolean ensureInjected() {
            return DataGridResources.INSTANCE.dataGridStyle().ensureInjected();
        }

        @Override
        public String getText() {
            return DataGridResources.INSTANCE.dataGridStyle().getText();
        }

        @Override
        public String getName() {
            return "CellTableStyle";
        }
    }
}
