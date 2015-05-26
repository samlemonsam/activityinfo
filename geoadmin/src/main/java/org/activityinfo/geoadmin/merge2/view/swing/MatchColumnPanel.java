package org.activityinfo.geoadmin.merge2.view.swing;

import org.activityinfo.geoadmin.merge2.model.ImportModel;
import org.activityinfo.geoadmin.merge2.view.model.FormMapping;
import org.activityinfo.geoadmin.merge2.view.model.SourceFieldMapping;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Observer;
import org.activityinfo.observable.Subscription;

import javax.swing.*;
import java.awt.*;


public class MatchColumnPanel extends StepPanel {
    
    private java.awt.List sourceList;
    private java.awt.List targetList;
    
    private java.util.List<Subscription> subscriptions;
    private JLabel sourceHeader;
    private JLabel targetHeader;
    private Subscription sourceSubscription;
    private Subscription targetSubscription;

    public MatchColumnPanel(ImportModel modelStore) {
        setLayout(new GridBagLayout());

        addStepTitle();
        addStepDescription();
        addSourceList(modelStore);
        addTargetList(modelStore);

        validate();
        
    }

    private void addStepTitle() {
        JLabel title = new JLabel("Step 1. Match columns");
        title.setFont(new Font(title.getFont().getName(), Font.BOLD, 16));

        GridBagConstraints constraints = constraints(0, 0);
        constraints.gridwidth = 3;
        
        add(title, constraints);
    }


    private void addStepDescription() {
        JLabel description = new JLabel("First, match the columns or fields from the import source" +
                " to those in the existing form.");

        GridBagConstraints constraints = constraints(0, 1);
        constraints.gridwidth = 3;
        constraints.insets = new Insets(5, 0, 10, 0);

        add(description, constraints);
    }

    private void addSourceList(ImportModel modelStore) {
        sourceHeader = new JLabel();
        add(sourceHeader, constraints(0, 2));

        sourceList = new List();

        GridBagConstraints listConstraints = constraints(0, 3);
        listConstraints.weightx = 1;
        listConstraints.weighty = 1;
        listConstraints.fill = GridBagConstraints.BOTH;

        add(sourceList, listConstraints);
        
        sourceSubscription = modelStore.getFieldMapping().subscribe(new Observer<FormMapping>() {
            @Override
            public void onChange(Observable<FormMapping> mapping) {
                if(mapping.isLoading()) {
                    sourceHeader.setText("Loading...");
                    sourceList.removeAll();
                } else {
                    sourceHeader.setText(mapping.get().getSource().getLabel());
                    for (SourceFieldMapping fieldMapping : mapping.get().getMappings()) {
                        sourceList.add(fieldMapping.toString());
                    }
                }
            }
        });
    }

    private void addTargetList(ImportModel modelStore) {
        
        targetHeader = new JLabel();
        add(targetHeader, constraints(2, 2));

        GridBagConstraints listConstraints = constraints(2, 3);
        listConstraints.weightx = 1;
        listConstraints.weighty = 1;
        listConstraints.fill = GridBagConstraints.BOTH;
        
        targetList = new List();
        add(targetList, listConstraints);

        targetSubscription = modelStore.getTargetTree().subscribe(new Observer<FormTree>() {
            @Override
            public void onChange(Observable<FormTree> formTree) {
                if(formTree.isLoading()) {
                    targetHeader.setText("Loading...");
                    targetList.removeAll();
                } else {
                    targetHeader.setText(formTree.get().getRootFormClass().getLabel());
                    for (FormTree.Node node : formTree.get().getLeaves()) {
                        targetList.add(node.debugPath());
                    }
                }
            }
        });
    }

    private GridBagConstraints constraints(int x, int y) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.fill =  GridBagConstraints.HORIZONTAL;
        return constraints;
    }


    @Override
    public void stop() {
        sourceSubscription.unsubscribe();
        targetSubscription.unsubscribe();
    }
}
