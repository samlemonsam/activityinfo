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
package org.activityinfo.server.command.handler.pivot;

import org.activityinfo.legacy.shared.command.result.Bucket;
import org.activityinfo.legacy.shared.model.IndicatorDTO;
import org.activityinfo.legacy.shared.reports.content.DimensionCategory;
import org.activityinfo.legacy.shared.reports.model.Dimension;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Accumulates a total value for an individual pivot cell. 
 * 
 * <p>Dependending on the type of the indicator, it counts distinct sites, or sums/averages indicator values.</p>
 */
public class Accumulator {

  private final Map<Dimension, DimensionCategory> key;
  private final Set<Integer> distinctSiteIds = new HashSet<>(0);
  
  private int aggregationMethod;

  private double sum;
  private int valueCount;
  

  public Accumulator(Map<Dimension, DimensionCategory> key, int aggregationMethod) {
    this.key = key;
    this.aggregationMethod = aggregationMethod;
  }
  
  public void addValue(double value) {
    valueCount++;
    sum += value;
  }
  
  public void maybeUpdateAggregationMethod(int aggregationMethod) {
    // When combining average and sum indicators, 
    // choose the sum aggregation method iff there is one or more indicators with sum aggregation
    if(aggregationMethod == IndicatorDTO.AGGREGATE_SUM) {
      this.aggregationMethod = IndicatorDTO.AGGREGATE_SUM;
    }
  }
  
  public void addSite(int id) {
    distinctSiteIds.add(id);
  }


  public Bucket createBucket() {
    
    double totalValue = sum + distinctSiteIds.size();
    int totalCount = valueCount + distinctSiteIds.size();
    
    return new Bucket(totalValue, totalCount, aggregationMethod, key);
  }

  public void addCount(int i) {
    valueCount ++;
    sum += 1.0;
  }
}
