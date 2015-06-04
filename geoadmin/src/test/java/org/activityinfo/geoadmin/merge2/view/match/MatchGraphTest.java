package org.activityinfo.geoadmin.merge2.view.match;

import org.activityinfo.geoadmin.merge2.model.ImportModel;
import org.activityinfo.geoadmin.merge2.state.ResourceStoreStub;
import org.activityinfo.geoadmin.merge2.view.ImportView;
import org.activityinfo.geoadmin.merge2.view.profile.FormProfile;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class MatchGraphTest {



    public static final int COLUMN_WIDTH = 25;
    private ImportModel model;
    private MatchTable matchTable;
    private ImportView importView;
    private FormProfile sourceForm;
    private FormProfile targetForm;
    private MatchGraph graph;
    private KeyFieldPairSet keyFields;

    @Before
    public void setUp() throws IOException {
        ResourceStoreStub resourceStore = new ResourceStoreStub();

        ResourceId sourceId = ResourceStoreStub.COMMUNE_SOURCE_ID;
        ResourceId targetId = ResourceStoreStub.COMMUNE_TARGET_ID;

        sourceForm = FormProfile.profile(resourceStore, sourceId).get();
        targetForm = FormProfile.profile(resourceStore, targetId).get();

        keyFields = KeyFieldPairSet.matchKeys(sourceForm, targetForm);

        graph = new MatchGraph(keyFields);
    }

    @Test
    @Ignore
    public void missingValues() throws IOException {

        int vinarySource = findSourceIndex("COMMUNE", "Vinany");

        graph.findCandidatesForSource(vinarySource);
        
        dumpCandidatesForSource(vinarySource);
        
        graph.findBestTargetForSource(vinarySource);
        
        
    }
    
    @Test
    public void optimalTest() throws IOException {
        int sourceIndex = findSourceIndex("COMMUNE", "Komajia");
        graph.findCandidatesForSource(sourceIndex);
        int targetIndex = graph.findBestTargetForSource(sourceIndex);
        
        assertThat(targetForm.getField("Name").getView().getString(targetIndex), equalTo("Komajia"));
    }

    private void dumpCandidatesForSource(int sourceIndex) {
        
        Collection<Integer> candidates = graph.getCandidatesForSource(sourceIndex);

        for (Integer targetIndex : candidates) {
            for (KeyFieldPair keyField : keyFields) {
                System.out.print(String.format("[%s = %s]",
                        keyField.getTargetField().getLabel(),
                        keyField.getTargetField().getView().getString(targetIndex)));

            }
            System.out.println();
        }
        
    }

    private int findSourceIndex(String fieldName, String fieldValue) {
        ColumnView view = sourceForm.getField(fieldName).getView();
        for(int i=0;i<view.numRows();++i) {
            if(fieldValue.equals(view.getString(i))) {
                return i;
            }
        }
        throw new IllegalArgumentException(fieldValue);
    }


}