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
package org.activityinfo.geoadmin.merge2.view.swing.match.select;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import org.activityinfo.geoadmin.merge2.model.InstanceMatch;
import org.activityinfo.geoadmin.merge2.view.ImportView;
import org.activityinfo.geoadmin.merge2.view.match.KeyFieldPairSet;
import org.activityinfo.geoadmin.merge2.view.match.MatchRow;
import org.activityinfo.geoadmin.merge2.view.match.MatchSide;
import org.activityinfo.geoadmin.merge2.view.match.MatchTable;
import org.activityinfo.geoadmin.merge2.view.swing.match.MatchStepPanel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.StatefulValue;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Allows the user to choose a match for an instance in the opposing collection.
 */
public class SelectDialog extends JDialog {


    private final JTable candidateTable;
    private final JTable headerTable;
    private final ImportView viewModel;
    
    private MatchSide fromSide;
    private int fromIndex;
    private final KeyFieldPairSet keyFields;
    private final CandidateTableModel candidateModel;

    private StatefulValue<Boolean> showAll = new StatefulValue<>(false);

    public SelectDialog(MatchStepPanel parent,
                        final ImportView viewModel,
                        final int matchRowIndex,
                        final MatchSide fromSide) {
        
        super(SwingUtilities.getWindowAncestor(parent), "Choose", ModalityType.APPLICATION_MODAL);
        this.viewModel = viewModel;
        setSize(450, 350);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        
        JPanel toolBar = new JPanel();
        toolBar.setLayout(new FlowLayout());
        
        final MatchTable matchTable = viewModel.getMatchTable();
        final MatchRow matchRow = matchTable.get(matchRowIndex);
        keyFields = matchTable.getKeyFields();

        this.fromSide = fromSide;
        this.fromIndex = matchRow.getRow(fromSide);
        
        /*
         * The header table shows the single row that were are looking to match against
         */
        HeaderTableModel headerModel = new HeaderTableModel(keyFields, matchRow, fromSide);



        /*
         * The rest of of the model shows match candidates
         */

        Observable<List<Integer>> candidates = showAll.transform(new Function<Boolean, List<Integer>>() {
            @Nullable
            @Override
            public List<Integer> apply(@Nullable Boolean showAll) {
                if(showAll == Boolean.FALSE) {
                    return matchTable.getGraph().get().getParetoFrontier(fromIndex, fromSide);
                } else {
                    return viewModel.getProfile(fromSide.opposite()).get().getRowIndexSequence();
                }
            }
        });


        candidateModel = new CandidateTableModel(matchTable.getGraph().get(), fromIndex, fromSide, candidates);

        headerTable = new JTable( headerModel );
        headerTable.setRowSelectionAllowed(false);
        headerTable.setCellSelectionEnabled(false);
     

        candidateTable = new JTable(candidateModel);
        candidateTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        candidateTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        candidateTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2) {
                    updateMatchToSelection();
                }
            }
        });

        int selectInstanceIndex = matchRow.getRow(fromSide.opposite());
        if(selectInstanceIndex != -1) {
            int selectedCandidateIndex = candidateModel.instanceIndexToCandidateRow(selectInstanceIndex);
            int selectedViewRow = candidateTable.convertRowIndexToView(selectedCandidateIndex);
            candidateTable.setRowSelectionInterval(selectedViewRow, selectedViewRow);
        }
        
        headerTable.getColumnModel().addColumnModelListener(new SyncingColumnListener(headerTable, candidateTable));
        
        JScrollPane headerScroll = new JScrollPane(headerTable);
        headerScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        headerScroll.setVerticalScrollBar(new HiddenScrollBar(headerScroll.getVerticalScrollBar()));

        JScrollPane candidateScroll = new JScrollPane(candidateTable) {
            public void setColumnHeaderView(Component view) {} // work around
        };
        headerScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        candidateScroll.getHorizontalScrollBar().addAdjustmentListener(
                new SyncingAdjustmentListener(headerScroll.getHorizontalScrollBar()));

        candidateScroll.setPreferredSize(new Dimension(400, 100));
        headerScroll.setPreferredSize(new Dimension(400, 40));  // Hmm...
        
        getContentPane().add(headerScroll, BorderLayout.NORTH);
        getContentPane().add(candidateScroll, BorderLayout.CENTER);


        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        
        JButton changeButton = new JButton("Match");
        changeButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateMatchToSelection();
                setVisible(false);
            }
        });
        
        JButton unmatchButton = new JButton("No match");
        unmatchButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                unmatch();
                setVisible(false);
            }
        });

        final JCheckBox showAllCheckBox = new JCheckBox("Show all");
        showAllCheckBox.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                showAll.updateIfNotEqual(showAllCheckBox.isSelected());
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(changeButton);
        buttonPanel.add(unmatchButton);
        buttonPanel.add(showAllCheckBox);

        add(buttonPanel, BorderLayout.PAGE_END);
    }

    private void updateMatchToSelection() {
        int viewRow = candidateTable.getSelectedRow();
        int candidateIndex = candidateTable.convertRowIndexToModel(viewRow);
        int targetIndex = candidateModel.candidateRowToInstanceIndex(candidateIndex);

        updateMatch(targetIndex);
    }


    private void unmatch() {
        ResourceId resourceId = keyFields.getForm(fromSide).getRowId(fromIndex);
        InstanceMatch explicitMatch;
        if(fromSide == MatchSide.TARGET) {
            explicitMatch = new InstanceMatch(Optional.<ResourceId>absent(), Optional.of(resourceId));
        } else {
            explicitMatch = new InstanceMatch(Optional.of(resourceId), Optional.<ResourceId>absent());
        }
       
        viewModel.getModel().getInstanceMatchSet().add(explicitMatch);
    }


    private void updateMatch(int matchedIndex) {

        ResourceId fromId = keyFields.getForm(fromSide).getRowId(fromIndex);
        ResourceId toId = keyFields.getForm(fromSide.opposite()).getRowId(matchedIndex);
        
        InstanceMatch newMatch;
        if(fromSide == MatchSide.SOURCE) {
            newMatch = new InstanceMatch(fromId, toId);
        } else {
            newMatch = new InstanceMatch(toId, fromId);
        }
        
        viewModel.getModel().getInstanceMatchSet().add(newMatch);
        
        setVisible(false);
    }


}
