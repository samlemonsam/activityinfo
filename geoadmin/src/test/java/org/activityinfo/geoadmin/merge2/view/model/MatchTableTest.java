package org.activityinfo.geoadmin.merge2.view.model;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import org.activityinfo.geoadmin.merge2.model.ImportModel;
import org.activityinfo.geoadmin.merge2.model.InstanceMatch;
import org.activityinfo.geoadmin.merge2.state.ResourceStoreStub;
import org.activityinfo.geoadmin.merge2.view.ImportView;
import org.activityinfo.geoadmin.merge2.view.match.MatchTable;
import org.activityinfo.geoadmin.merge2.view.match.MatchTableColumn;
import org.activityinfo.geoadmin.merge2.view.match.UnmatchedColumn;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.MockObserver;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.TableObserver;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class MatchTableTest {


    public static final int COLUMN_WIDTH = 25;
    private ImportModel model;
    private MatchTable matchTable;
    private ImportView importView;

    @Before
    public void setUp() throws IOException {
        ResourceStoreStub resourceStore = new ResourceStoreStub();
        model = new ImportModel(
                ResourceStoreStub.GADM_PROVINCE_SOURCE_ID,
                ResourceStoreStub.REGION_TARGET_ID);

        importView = new ImportView(resourceStore, model);
        matchTable = importView.getMatchTable();
        matchTable.subscribe(new MockTableObserver());
    }
    
    
    @Test
    public void resolveMatch() {
        ResourceId sourceId = ResourceId.valueOf("MDG_adm2.7");   // Amoron'i mania
        ResourceId targetId = ResourceId.valueOf("z0000041751");  // Amoron i Mania

        assertThat(waitFor(matchTable.getUnresolvedCount()), equalTo(2));

        model.getInstanceMatchSet().add(new InstanceMatch(sourceId, targetId));

        assertThat(matchTable.getUnresolvedCount().get(), equalTo(1));
        
        dump(matchTable);
    }

    private <T> T waitFor(Observable<T> observable) {
        if(observable.isLoading()) {
            observable.subscribe(new MockObserver<T>());
        }
        assert !observable.isLoading();
        return observable.get();
    }


    public void dump(MatchTable table) {
        
        List<MatchTableColumn> columns = table.getColumns().get();
        int rowCount = matchTable.getRowCount();

        System.out.print(formatCol("sourceId"));
        System.out.print(formatCol("targetId"));
        for (MatchTableColumn column : columns) {
            if(!(column instanceof UnmatchedColumn)) {
                System.out.print(formatCol(column.getHeader()));
            }
        }
        System.out.println();
        
        for(int i=0;i<rowCount;++i) {
            System.out.print(formatCol(table.get(i).getSourceId()));
            System.out.print(formatCol(table.get(i).getTargetId()));
            for (MatchTableColumn column : columns) {
                if(!(column instanceof UnmatchedColumn)) {
                    System.out.print(formatCol(column.getValue(i)));
                }
            }
            System.out.println();
        }
    }

    private String formatCol(Optional<?> id) {
        if(id.isPresent()) {
            return formatCol(id.get().toString());
        } else {
            return formatCol("");
        }
    }

    private String formatCol(String cell) {
        String cellString = Strings.nullToEmpty(cell);
        if(cellString.length() > (COLUMN_WIDTH-1)) {
            return cellString.substring(0, COLUMN_WIDTH-1) + " ";
        } else {
            return Strings.padEnd(cellString, COLUMN_WIDTH, ' ');
        }
    }


    private static class MockTableObserver implements TableObserver {

        @Override
        public void onRowsChanged() {
            
        }

        @Override
        public void onRowChanged(int index) {

        }
    }
    
}