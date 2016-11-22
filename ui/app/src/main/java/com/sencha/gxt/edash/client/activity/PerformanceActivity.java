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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sencha.gxt.edash.client.LoggingAsyncCallback;
import com.sencha.gxt.edash.client.place.PerformancePlace;
import com.sencha.gxt.edash.client.view.PerformanceView;
import com.sencha.gxt.edash.shared.EdashServiceAsync;
import com.sencha.gxt.edash.shared.model.CompanyStats;
import com.sencha.gxt.edash.shared.model.Performance;
import com.sencha.gxt.edash.shared.model.ReportDownload;

import javax.inject.Inject;
import java.util.List;

public class PerformanceActivity extends BaseActivity<PerformancePlace> implements PerformanceView.Delegate {

  @Inject
  private PerformanceView view;

  @Inject
  private EdashServiceAsync service;

  @Override
  public void start(final AcceptsOneWidget acceptsOneWidget, EventBus eventBus) {
    view.setDelegate(this);
    acceptsOneWidget.setWidget(view);


    service.getAllPerformance(new LoggingAsyncCallback<List<Performance>>() {
      @Override
      public void onSuccess(List<Performance> result) {
        view.setData(result);
        view.setCompany(getPlace().getCompany());
      }
    });

  }

  @Override
  public void getStatements(final AsyncCallback<List<ReportDownload>> callback) {
    service.getReportDownloads(callback);
  }

  @Override
  public void getCompanyStats(String companyFilter, AsyncCallback<CompanyStats> callback) {
    service.getCompanyStats(companyFilter, callback);
  }

  @Override
  public void setPlace(PerformancePlace place) {
    super.setPlace(place);
    if (place.getCompany() == null || "".equals(place.getCompany())) {
      return;
    }
    view.setCompany(place.getCompany());
  }
}
