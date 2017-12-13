package chdc.frontend.client;

import chdc.frontend.client.entry.DataEntryPlace;
import chdc.frontend.client.table.TablePlace;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceHistoryMapper;

public class ChdcPlaceHistoryMapper implements PlaceHistoryMapper {

    @Override
    public Place getPlace(String token) {
        if(token.startsWith("entry")) {
            return new DataEntryPlace();
        } else if(token.startsWith("table")) {
            return new TablePlace();
        }
        return null;
    }

    @Override
    public String getToken(Place place) {
        return place.toString();
    }
}
