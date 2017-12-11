package chdc.frontend.client;

import chdc.frontend.client.dashboard.DashboardPlace;
import chdc.frontend.client.entry.DataEntryPlace;
import chdc.frontend.client.table.TablePlace;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceHistoryMapper;

/**
 * Maps the history token (the part following the "#" in the URL) to the
 * corresponding {@link Place} object.
 *
 * For example, if the URL chdc.ngo-safety.org/app/#entry/incident/cx324hdr3y, then
 * this mapper will return a new {@link DataEntryPlace} for the form 'incident' and the
 * record id 'cx324hdr3y'.
 *
 */
public class ChdcPlaceHistoryMapper implements PlaceHistoryMapper {

    @Override
    public Place getPlace(String token) {

        String parts[] = token.split("/");

        if(parts[0].equals(DashboardPlace.SLUG)) {
            return new DashboardPlace();

        } else if(token.startsWith(DataEntryPlace.SLUG)) {
            return new DataEntryPlace(parts[1], parts[2]);

        } else if(token.startsWith(TablePlace.SLUG)) {
            return new TablePlace(parts[1]);
        }
        return null;
    }

    @Override
    public String getToken(Place place) {
        return place.toString();
    }
}
