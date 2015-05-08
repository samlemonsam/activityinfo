
SELECT * FROM indicatorvalue V
LEFT JOIN indicator I ON (I.indicatorId = V.IndicatorId)
LEFT JOIN reportingperiod P ON (P.reportingPeriodId=V.ReportingPeriodId)
LEFT JOIN site S ON (P.siteId=S.SiteId)
WHERE S.dateDeleted IS NOT NULL 
 AND  S.activityId=13702
 AND  S.