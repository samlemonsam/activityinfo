package org.activityinfo.server.endpoint.rest;

import com.google.common.collect.Lists;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.server.command.CommandTestCase2;
import org.activityinfo.server.database.OnDataSet;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(InjectionSupport.class)
@OnDataSet("/dbunit/sites-simple1.db.xml")
public class SitesResourcesTest extends CommandTestCase2 {

    private static class QueryParameters {
        private final List<Integer> activityIds = Lists.newArrayList();
        private final List<Integer> databaseIds = Lists.newArrayList();
        private final List<Integer> indicatorIds = Lists.newArrayList();
        private final List<Integer> partnerIds = Lists.newArrayList();
        private final List<Integer> attributeIds = Lists.newArrayList();
        private final List<Integer>  locationIds = Lists.newArrayList();
        private final List<Integer> siteIds = Lists.newArrayList();
        private final Boolean legacy = true;
        private String format = null;
    }

    @Test
    public void indicatorsArePresent() throws IOException {
        QueryParameters parameters = new QueryParameters();
        parameters.databaseIds.add(2);

        String json = query(parameters);

        System.out.println(json);

        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getJsonFactory();
        JsonParser jp = factory.createJsonParser(json);
        ArrayNode array = (ArrayNode) mapper.readTree(jp);

        assertThat(array.size(), equalTo(3));

        JsonNode site6 = getSiteById(array, 6);
        assertThat(site6.path("id").asInt(), equalTo(6));
        assertThat(site6.path("activity").asInt(), equalTo(4));

        double indicatorValue = site6.path("indicatorValues").path("6").asDouble();
        assertThat(indicatorValue, equalTo(70d));
    }

    @Test
    public void filterByPartner() throws IOException {
        final int partnerId = 2;

        QueryParameters parameters = new QueryParameters();
        parameters.partnerIds.add(partnerId);
        String json = query(parameters);

        System.out.println(json);

        final ArrayNode jsonNode = (ArrayNode) Jackson.createJsonMapper().readTree(json);

        assertEquals(jsonNode.get(0).path("partner").path("id").asInt(), partnerId);
    }

    public String query(QueryParameters parameters) throws IOException {
        return new SitesResources(getDispatcherSync()).query(
                parameters.activityIds, parameters.databaseIds, parameters.indicatorIds, parameters.partnerIds,
                parameters.attributeIds, parameters.locationIds,
                parameters.siteIds,
                parameters.legacy,
                parameters.format
        );
    }

    protected JsonNode getSiteById(ArrayNode array, int siteId) {
        for (JsonNode node : array) {
            if (node.get("id").asInt() == siteId) {
                return node;
            }
        }
        throw new AssertionError("No site json object with id " + siteId);
    }
}
