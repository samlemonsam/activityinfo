package org.activityinfo.geoadmin.merge2.view.match;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import org.activityinfo.geoadmin.merge2.model.ImportModel;
import org.activityinfo.geoadmin.merge2.state.ResourceStoreStub;
import org.activityinfo.geoadmin.merge2.view.ImportView;
import org.activityinfo.geoadmin.merge2.view.profile.FormProfile;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.apache.commons.math3.linear.RealMatrix;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
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
    public void pcaTest() {

        for (KeyFieldPair keyField : keyFields) {
            System.out.println(keyField.getTargetField().getLabel());
        }
        
        InstanceMatrix similarityMatrix = new InstanceMatrix(keyFields);
        
        ColumnView sourceRegion = sourceForm.getField("REGION").getView();
        ColumnView sourceRegionCode = sourceForm.getField("REG_PCODE").getView();
        
        ColumnView targetRegion = targetForm.getField("Region.Name").getView();
        ColumnView targetRegionCode = targetForm.getField("Region.Code").getView();
        
        int regionNameKey = findKeyIndex("Region.Name");
        int regionCodeKey = findKeyIndex("Region.Code");
        
        int rowIndex = 0;
        printLoop: for (int i = 0; i < similarityMatrix.getRowCount(); i++) {
            for (int j = 0; j < similarityMatrix.getColumnCount(); j++) {
                System.out.println(format("[%3.0f: %6s <> %6s][%3.0f: %25s <> %25s]",
                        similarityMatrix.score(i, j, regionCodeKey) * 100d,
                        sourceRegionCode.getString(i),
                        targetRegionCode.getString(j),
                        
                        similarityMatrix.score(i, j, regionNameKey) * 100d,
                        sourceRegion.getString(i),
                        targetRegion.getString(j)));
                rowIndex++;
                if(rowIndex > 1000) {
                    break printLoop;
                }
            }
        }
        
        // Now try our online version
        Stopwatch stopwatch = Stopwatch.createStarted();
        RealMatrix covMatrix2 = similarityMatrix.computeCovarianceMatrix();
        
        stopwatch.stop();

        System.out.println(format("Covariance matrix computed in %d seconds: %8.7f",
                stopwatch.elapsed(TimeUnit.SECONDS),
                covMatrix2.getEntry(regionCodeKey, regionNameKey)));

        System.out.println(covMatrix2);


//      
//        EigenDecomposition eigenDecomposition = new EigenDecomposition(covMatrix2);
//        eigenDecomposition.
//        RealMatrix eigenVectors = eigenDecomposition.getV();
        
//        System.out.println(eigenVectors);
//
//        keyFields.
        
    }

    @Test
    public void missingValues() throws IOException {

        int sourceIndex = findSourceIndex("COMMUNE", "Vinany");

        graph.buildParetoFrontierForSource(sourceIndex);
        
        dumpCandidatesForSource(sourceIndex);
        
        
    }
    
    @Test
    public void optimalTest() throws IOException {
        int sourceIndex = findSourceIndex("COMMUNE", "Komajia");
        graph.buildParetoFrontierForSource(sourceIndex);

        dumpCandidatesForSource(sourceIndex);

        Collection<MatchGraph.Candidate> frontier = graph.getParetoFrontierForSource(sourceIndex);
        
        assertThat(frontier.size(), equalTo(1));

        MatchGraph.Candidate optimal = Iterables.getOnlyElement(frontier);

        assertThat(targetForm.getField("Name").getView().getString(optimal.getTargetIndex()), equalTo("Komajia"));
    }
    
    @Test
    public void mandritsara() {
        int sourceIndex = findSourceIndex("COMMUNE", "Mandritsara");
        graph.buildParetoFrontierForSource(sourceIndex);
        
        dumpCandidatesForSource(sourceIndex);
    }
    
    @Test
    public void bestTest() {
        graph.build();
        
        int sourceIndex = findTargetIndex("Name", "Komajia");
        int targetIndex = graph.getBestMatchForTarget(sourceIndex);
        
        assertThat(targetIndex, equalTo(900000));

    }

    private void dumpCandidatesForSource(int sourceIndex) {
        
        Collection<MatchGraph.Candidate> candidates = graph.getParetoFrontierForSource(sourceIndex);

        for (MatchGraph.Candidate candidate : candidates) {
            for (KeyFieldPair keyField : keyFields) {
                System.out.print(format("[%s = %s]",
                        keyField.getTargetField().getLabel(),
                        keyField.getTargetField().getView().getString(candidate.getTargetIndex())));

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

    private int findTargetIndex(String fieldName, String fieldValue) {
        ColumnView view = targetForm.getField(fieldName).getView();
        for(int i=0;i<view.numRows();++i) {
            if(fieldValue.equals(view.getString(i))) {
                return i;
            }
        }
        throw new IllegalArgumentException(fieldValue);
    }


    private int findKeyIndex(String fieldName) {
        int keyIndex = 0;
        for (int i = 0; i < keyFields.size(); i++) {
            if (keyFields.get(i).getTargetField().getLabel().equals(fieldName)) {
                return i;
            }
        }
        throw new IllegalArgumentException(fieldName);
    }

}