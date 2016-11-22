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
package com.sencha.gxt.edash.shared;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.edash.shared.model.CompanyStats;
import com.sencha.gxt.edash.shared.model.FullProfitLoss;
import com.sencha.gxt.edash.shared.model.Kpi;
import com.sencha.gxt.edash.shared.model.News;
import com.sencha.gxt.edash.shared.model.Performance;
import com.sencha.gxt.edash.shared.model.ProfitLoss;
import com.sencha.gxt.edash.shared.model.ReportDownload;

import java.util.List;

/**
 *
 */
public interface EdashServiceAsync {
  void getAllNews(AsyncCallback<List<News>> async);

  void getAllPerformance(AsyncCallback<List<Performance>> async);

  void getCompanyStats(String companyName, AsyncCallback<CompanyStats> async);

  void getReportDownloads(AsyncCallback<List<ReportDownload>> async);

  void getKpiData(AsyncCallback<List<Kpi>> async);

  void getProfitLoss(AsyncCallback<ProfitLoss> callback);
}
