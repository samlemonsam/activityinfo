package org.activityinfo.server.command.handler.pivot;

import org.activityinfo.legacy.shared.command.result.Bucket;
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
  private final int aggregationMethod;
  private final Set<Integer> distinctSiteIds = new HashSet<>(0);

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
