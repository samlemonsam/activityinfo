/**
 * Sencha GXT 4.0.0 - Sencha for GWT
 * Copyright (c) 2006-2015, Sencha Inc.
 *
 * licensing@sencha.com
 * http://www.sencha.com/products/gxt/license/
 *
 * ================================================================================
 * Evaluation/Trial License
 * ================================================================================
 * This version of Sencha GXT is licensed commercially for a limited period for
 * evaluation purposes only. Production use or use beyond the applicable evaluation
 * period is prohibited under this license.
 *
 * Please see the Sencha GXT Licensing page at:
 * http://www.sencha.com/products/gxt/license/
 *
 * For clarification or additional options, please contact:
 * licensing@sencha.com
 * ================================================================================
 *
 *
 *
 *
 *
 *
 *
 * ================================================================================
 * Disclaimer
 * ================================================================================
 * THIS SOFTWARE IS DISTRIBUTED "AS-IS" WITHOUT ANY WARRANTIES, CONDITIONS AND
 * REPRESENTATIONS WHETHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE
 * IMPLIED WARRANTIES AND CONDITIONS OF MERCHANTABILITY, MERCHANTABLE QUALITY,
 * FITNESS FOR A PARTICULAR PURPOSE, DURABILITY, NON-INFRINGEMENT, PERFORMANCE AND
 * THOSE ARISING BY STATUTE OR FROM CUSTOM OR USAGE OF TRADE OR COURSE OF DEALING.
 * ================================================================================
 */
package com.sencha.gxt.edash.client.widget.grid;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.widget.core.client.grid.GroupingView;

public abstract class GroupingViewExt<M> extends GroupingView<M> {

  public GroupingViewExt() {
    super();

    this.scrollOffset = 0;
  }

  @Override
  public void toggleGroup(int index, boolean expanded) {
    super.toggleGroup(index, expanded);
  }

  @Override
  public NodeList<Element> getGroups() {
    return super.getGroups();
  }

  @Override
  public int getGroupIndex(XElement group) {
    return super.getGroupIndex(group);
  }


  protected void onRowOut(int rowIndex) {
    super.onRowOut(getRow(rowIndex));
  }

  public void onRowOutSync(GroupingViewExt<M> view, Element row) {
    super.onRowOut(row);
    int rowIndex = findRowIndex(row);
    view.onRowOut(rowIndex);
  }


  protected void onRowOver(int rowIndex) {
    super.onRowOver(getRow(rowIndex));
  }

  public void onRowOverSync(GroupingViewExt<M> view, Element row) {
    super.onRowOver(row);
    int rowIndex = findRowIndex(row);
    view.onRowOver(rowIndex);
  }
}
