package org.activityinfo.geoadmin.merge2.view.swing;

import org.activityinfo.geoadmin.merge2.view.ImportView;
import org.activityinfo.geoadmin.merge2.view.mapping.FormMapping;
import org.activityinfo.geoadmin.merge2.view.mapping.ReferenceFieldMapping;
import org.activityinfo.geoadmin.merge2.view.swing.lookup.LookupStep;
import org.activityinfo.geoadmin.merge2.view.swing.match.MatchStep;
import org.activityinfo.geoadmin.model.GeoAdminClient;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Observer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;


public class ImportDialog extends JFrame {

    private GeoAdminClient client;
    private final ImportView viewModel;

    private List stepList;
    private JPanel stepPanel;

    private java.util.List<Step> steps;

    public ImportDialog(GeoAdminClient client, ImportView viewModel) {
        super("Merge");
        this.client = client;
        this.viewModel = viewModel;
        setSize(650, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        steps = new ArrayList<>();
        stepPanel = new JPanel(new BorderLayout());

        stepList = new List();
        stepList.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                stepPanel.removeAll();
                stepPanel.add(steps.get(stepList.getSelectedIndex()).createView(), BorderLayout.CENTER);
                stepPanel.validate();
            }
        });

        viewModel.getMapping().subscribe(new Observer<FormMapping>() {
            @Override
            public void onChange(Observable<FormMapping> formMapping) {
                updateStepList(formMapping);
            }
        });

        JButton okButton = new JButton("Import");
        okButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runImport();
            }
        });
        JButton cancelButton = new JButton("Cancel");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        buttonPanel.add(cancelButton);
        buttonPanel.add(okButton);

        getContentPane().add(stepList, BorderLayout.WEST);
        getContentPane().add(stepPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.PAGE_END);
    }


    private void updateStepList(Observable<FormMapping> formMapping) {
        steps.clear();
        steps.add(new MatchStep(viewModel));

        if (!formMapping.isLoading()) {
            for (ReferenceFieldMapping referenceFieldMapping : formMapping.get().getReferenceFieldMappings()) {
                steps.add(new LookupStep(viewModel, referenceFieldMapping));
            }
        }

        stepList.removeAll();

        for (Step step : steps) {
            stepList.add(step.getLabel());
        }
    }

    private void runImport() {
        Observable<Integer> unresolvedCount = viewModel.getMatchTable().getUnresolvedCount();
        if (!unresolvedCount.isLoading()) {
            if (unresolvedCount.get() > 0) {
                JOptionPane.showMessageDialog(null, String.format("There are %d unresolved matches. " +
                        "Please resolve these first.", unresolvedCount.get()), "Unresolved matches", JOptionPane.PLAIN_MESSAGE);
            } else {
                executeImport();
            }
        }
    }

    private void executeImport() {
        viewModel.runUpdate(client);


        this.setVisible(false);
    }
}