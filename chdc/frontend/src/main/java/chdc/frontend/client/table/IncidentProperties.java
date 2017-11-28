package chdc.frontend.client.table;

import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

import java.util.Date;

public interface IncidentProperties extends PropertyAccess<IncidentModel> {

    ModelKeyProvider<IncidentModel> id();

    ValueProvider<IncidentModel, String> narrative();

    ValueProvider<IncidentModel, String> time();

    ValueProvider<IncidentModel, Date> date();

    ValueProvider<IncidentModel, String> perpetrator();

}
