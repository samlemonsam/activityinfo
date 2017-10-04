package org.activityinfo.legacy.shared.command;

public class OldGetSites extends GetSites {

    public OldGetSites(GetSites command) {
        if (command.isFetchAllIndicators()) {
            setFetchAllIndicators(command.isFetchAllIndicators());
        } else {
            if (command.getFetchIndicators() != null) {
                setFetchIndicators(command.getFetchIndicators());
            }
        }
        setFetchAllReportingPeriods(command.isFetchAllReportingPeriods());
        setFetchAttributes(command.isFetchAttributes());
        setFetchComments(command.isFetchComments());
        setFetchDates(command.isFetchDates());
        setFetchLinks(command.isFetchLinks());
        setFetchLocation(command.isFetchLocation());
        setFetchPartner(command.isFetchPartner());
        setFilter(command.getFilter());
        setOffset(command.getOffset());
        if (command.getSeekToSiteId() != null) {
            setSeekToSiteId(command.getSeekToSiteId());
        }
        setLimit(command.getLimit());
        setSortInfo(command.getSortInfo());
        setFetchAdminEntities(command.isFetchAdminEntities());

        setLegacyFetch(command.isLegacyFetch());
    }

}
