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
package com.sencha.gxt.edash.client.activity;


import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sencha.gxt.edash.client.LoggingAsyncCallback;
import com.sencha.gxt.edash.client.place.KpiOverviewPlace;
import com.sencha.gxt.edash.client.view.KpiOverviewView;
import com.sencha.gxt.edash.shared.EdashServiceAsync;
import com.sencha.gxt.edash.shared.model.Kpi;

import javax.inject.Inject;
import java.util.List;

public class KpiOverviewActivity extends BaseActivity<KpiOverviewPlace> implements KpiOverviewView.Delegate {


  @Inject
  private KpiOverviewView view;

  @Inject
  private EdashServiceAsync service;

  @Override
  public void start(AcceptsOneWidget acceptsOneWidget, EventBus eventBus) {
    logger.fine("test from activity");
    view.setDelegate(this);
    acceptsOneWidget.setWidget(view);

    service.getKpiData(new LoggingAsyncCallback<List<Kpi>>() {
      @Override
      public void onSuccess(List<Kpi> result) {
        view.setKpiData(result);
      }
    });
  }


  @Override
  public void setPlace(KpiOverviewPlace place) {
    super.setPlace(place);

    view.setMetric(place.getMetric());
  }
}
