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
package com.sencha.gxt.edash.client.place;


import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class KpiOverviewPlace extends BasePlace {

  public static enum PerformanceMetric {
    CLICKS("clicks"),
    WON("redemption"),
    SALES("sales"),
    GOALS_MET("goalsmet");

    private String type;

    private PerformanceMetric(String type) {
      this.type = type;
    }

    public String getType() {
      return type;
    }
  }


  private PerformanceMetric metric;

  public KpiOverviewPlace() {
    this.metric = PerformanceMetric.CLICKS;
  }

  public KpiOverviewPlace(PerformanceMetric metric) {
    this.metric = metric;
  }

  public PerformanceMetric getMetric() {
    return metric;
  }

  @Prefix("kpi")
  public static class Tokenizer implements PlaceTokenizer<KpiOverviewPlace> {
    @Override
    public String getToken(KpiOverviewPlace place) {
      return place.getMetric().name();
    }

    @Override
    public KpiOverviewPlace getPlace(String token) {
      PerformanceMetric metric;
      try {
        metric = PerformanceMetric.valueOf(token);
      } catch (IllegalArgumentException e) {
        metric = PerformanceMetric.CLICKS;
      }

      return new KpiOverviewPlace(metric);
    }
  }

}
