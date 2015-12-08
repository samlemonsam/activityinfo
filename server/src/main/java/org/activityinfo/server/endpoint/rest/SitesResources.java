package org.activityinfo.server.endpoint.rest;

import com.bedatadriven.rebar.time.calendar.LocalDate;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.activityinfo.legacy.shared.command.*;
import org.activityinfo.legacy.shared.command.result.MonthlyReportResult;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.model.type.NarrativeType;
import org.activityinfo.model.type.attachment.AttachmentType;
import org.activityinfo.model.type.attachment.AttachmentValue;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.server.command.DispatcherSync;
import org.activityinfo.server.util.monitoring.Timed;
import org.codehaus.jackson.JsonGenerator;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

public class SitesResources {

    private final DispatcherSync dispatcher;

    public SitesResources(DispatcherSync dispatcher) {
        this.dispatcher = dispatcher;
    }

    @GET 
    @Timed(name = "api.rest.sites")
    @Produces(MediaType.APPLICATION_JSON)
    public String query(@QueryParam("activity") List<Integer> activityIds,
                        @QueryParam("database") List<Integer> databaseIds,
                        @QueryParam("indicator") List<Integer> indicatorIds,
                        @QueryParam("partner") List<Integer> partnerIds,
                        @QueryParam("attribute") List<Integer> attributeIds,
                        @QueryParam("location") List<Integer> locationIds,
                        @QueryParam("format") String format) throws IOException {

        Filter filter = new Filter();
        filter.addRestriction(DimensionType.Activity, activityIds);
        filter.addRestriction(DimensionType.Database, databaseIds);
        filter.addRestriction(DimensionType.Indicator, indicatorIds);
        filter.addRestriction(DimensionType.Partner, partnerIds);
        filter.addRestriction(DimensionType.Attribute, attributeIds);
        filter.addRestriction(DimensionType.Location, locationIds);

        List<SiteDTO> sites = dispatcher.execute(new GetSites(filter)).getData();

        StringWriter writer = new StringWriter();
        JsonGenerator json = Jackson.createJsonFactory(writer);

        writeJson(sites, json);

        return writer.toString();
    }


    @GET 
    @Path("/points")
    @Timed(name = "api.rest.sites.points")
    public Response queryPoints(@QueryParam("activity") List<Integer> activityIds,
                                @QueryParam("database") List<Integer> databaseIds,
                                @QueryParam("callback") String callback) throws IOException {

        Filter filter = new Filter();
        filter.addRestriction(DimensionType.Activity, activityIds);
        filter.addRestriction(DimensionType.Database, databaseIds);
    
        List<SiteDTO> sites = dispatcher.execute(new GetSites(filter)).getData();

        StringWriter writer = new StringWriter();
        JsonGenerator json = Jackson.createJsonFactory(writer);
        writeGeoJson(sites, json);

        if (Strings.isNullOrEmpty(callback)) {
            return Response.ok(writer.toString()).type("application/json; charset=UTF-8").build();
        } else {
            return Response.ok(callback + "(" + writer.toString() + ");")
                           .type("application/javascript; charset=UTF-8")
                           .build();
        }
    }


    private void writeJson(List<SiteDTO> sites, JsonGenerator json) throws IOException {
        json.writeStartArray();

        Map<Integer, ActivityFormDTO> forms = Maps.newHashMap();

        for (SiteDTO site : sites) {
            json.writeStartObject();
            json.writeNumberField("id", site.getId());
            json.writeNumberField("activity", site.getActivityId());
            json.writeNumberField("timestamp", site.getTimeEdited());

            // write start / end date if applicable
            if (site.getDate1() != null && site.getDate2() != null) {
                json.writeStringField("startDate", site.getDate1().toString());
                json.writeStringField("endDate", site.getDate2().toString());
            }

            // write the location as a separate object
            json.writeObjectFieldStart("location");
            json.writeNumberField("id", site.getLocationId());
            json.writeStringField("name", site.getLocationName());
            json.writeStringField("code", site.getLocationAxe());

            if (site.hasLatLong()) {
                json.writeFieldName("latitude");
                json.writeNumber(site.getLatitude());
                json.writeFieldName("longitude");
                json.writeNumber(site.getLongitude());
            }
            json.writeEndObject();

            json.writeObjectFieldStart("partner");
            json.writeNumberField("id", site.getPartnerId());
            json.writeStringField("name", site.getPartnerName());
            json.writeEndObject();

            if (site.getProject() != null) {
                json.writeNumberField("projectId", site.getProject().getId());
            }

            // write attributes as a series of ids
            Set<Integer> attributes = getAttributeIds(site);
            if (!attributes.isEmpty()) {
                json.writeFieldName("attributes");
                json.writeStartArray();
                for (Integer attributeId : attributes) {
                    json.writeNumber(attributeId);
                }
                json.writeEndArray();
            }

            // write indicators
            Set<Integer> indicatorIds = getIndicatorIds(site);
            if (!indicatorIds.isEmpty()) {
                json.writeObjectFieldStart("indicatorValues");
                for (Integer indicatorId : indicatorIds) {
                    Object indicatorValue = site.getIndicatorValue(indicatorId);
                    if (indicatorValue instanceof Double) {
                        json.writeNumberField(Integer.toString(indicatorId), (Double) indicatorValue);
                    } else if (indicatorValue instanceof String) {

                        String stringValue = (String) indicatorValue;

                        if (Strings.isNullOrEmpty(stringValue)) {
                            ActivityFormDTO form = forms.get(site.getActivityId());
                            if(form == null) {
                                form = dispatcher.execute(new GetActivityForm(site.getActivityId()));
                                forms.put(form.getId(), form);
                            }
                            if (form.getIndicatorById(indicatorId).getType() == AttachmentType.TYPE_CLASS) {
                                json.writeObjectField(Integer.toString(indicatorId), AttachmentValue.fromJson(stringValue).asRecord());
                            } else {
                                json.writeStringField(Integer.toString(indicatorId), stringValue);
                            }
                        } else {
                            json.writeStringField(Integer.toString(indicatorId), stringValue);
                        }

                    } else if (indicatorValue instanceof LocalDate) {
                        json.writeStringField(Integer.toString(indicatorId), indicatorValue.toString());
                    } else if (indicatorValue instanceof Boolean) {
                        json.writeStringField(Integer.toString(indicatorId), indicatorValue.toString());
                    }
                }
                json.writeEndObject();
            }

            // comments
            if (!Strings.isNullOrEmpty(site.getComments())) {
                json.writeFieldName("comments");
                json.writeString(site.getComments());
            }

            json.writeEndObject();
        }
        json.writeEndArray();
        json.close();
    }

    private void writeGeoJson(List<SiteDTO> sites, JsonGenerator json) throws IOException {

        json.writeStartObject();
        json.writeStringField("type", "FeatureCollection");
        json.writeArrayFieldStart("features");
        
        Map<Integer, ActivityFormDTO> forms = Maps.newHashMap();
        
        for (SiteDTO site : sites) {
            if (site.hasLatLong()) {
                json.writeStartObject();
                json.writeStringField("type", "Feature");
                json.writeNumberField("id", site.getId());

                ActivityFormDTO form = forms.get(site.getActivityId());
                if(form == null) {
                    form = dispatcher.execute(new GetActivityForm(site.getActivityId()));
                    forms.put(form.getId(), form);
                }

                // write out the properties object
                json.writeObjectFieldStart("properties");
                json.writeStringField("locationName", site.getLocationName());
                json.writeStringField("partnerName", site.getPartnerName());
                if (!Strings.isNullOrEmpty(site.getComments())) {
                    json.writeStringField("comments", site.getComments());
                }

                json.writeNumberField("activity", site.getActivityId());
                if (!Strings.isNullOrEmpty(form.getCategory())) {
                    json.writeStringField("activityCategory", form.getCategory());
                }
                json.writeStringField("activityName", form.getName());

                // write start / end date if applicable
                if (site.getDate1() != null && site.getDate2() != null) {
                    json.writeStringField("startDate", site.getDate1().toString());
                    json.writeStringField("endDate", site.getDate2().toString());
                }

                // write indicators
                json.writeObjectFieldStart("indicators");
                for (IndicatorDTO indicator : form.getIndicators()) {
                    Object value = getTypedValue(site, indicator);
                    if(value instanceof String) {
                        json.writeStringField(indicator.getName(), (String)value);
                    } else if(value instanceof Number) {
                        json.writeNumberField(indicator.getName(), ((Number) value).doubleValue());
                    }
                }
                json.writeEndObject();
                
                // write attributes 
                for (AttributeGroupDTO group : form.getAttributeGroups()) {
                    Set<String> values = new HashSet<>();
                    for (AttributeDTO attribute : group.getAttributes()) {
                        if(site.get(attribute.getPropertyName()) == Boolean.TRUE) {
                            values.add(attribute.getName());
                        }
                    }
                    if(!values.isEmpty()) {
                        json.writeObjectFieldStart(group.getName());
                        for (String value : values) {
                            json.writeBooleanField(value, true);
                        }
                        json.writeEndObject();
                    }
                }

                json.writeEndObject();

                // write out the geometry object
                json.writeObjectFieldStart("geometry");
                json.writeStringField("type", "Point");
                json.writeArrayFieldStart("coordinates");
                json.writeNumber(site.getX());
                json.writeNumber(site.getY());
                json.writeEndArray();
                json.writeEndObject();

                json.writeEndObject();
            }
        }

        json.writeEndArray();
        json.writeEndObject();
        json.close();
    }

    /**
     * Returns an indicator value IFF the value matches the declared type of the indicator,
     * otherwise null.
     */
    private Object getTypedValue(SiteDTO site, IndicatorDTO indicator) {
        Object value = site.get(indicator.getPropertyName());
        if(indicator.getType() instanceof QuantityType.TypeClass) {
            if(value instanceof Number) {
                return value;
            }
        } else if(indicator.getType() == TextType.TYPE_CLASS || 
                  indicator.getType() == NarrativeType.TYPE_CLASS) {
            if(value instanceof String) {
                return value;
            }
        }
        return null;
    }

    private Set<Integer> getIndicatorIds(SiteDTO site) {
        Set<Integer> ids = Sets.newHashSet();
        for (String propertyName : site.getPropertyNames()) {
            if (propertyName.startsWith(IndicatorDTO.PROPERTY_PREFIX) && site.get(propertyName) != null) {
                ids.add(IndicatorDTO.indicatorIdForPropertyName(propertyName));
            }
        }
        return ids;
    }

    private Set<Integer> getAttributeIds(SiteDTO site) {
        Set<Integer> ids = Sets.newHashSet();
        for (String propertyName : site.getPropertyNames()) {
            if (propertyName.startsWith(AttributeDTO.PROPERTY_PREFIX)) {
                int attributeId = AttributeDTO.idForPropertyName(propertyName);
                boolean value = site.get(propertyName, false);
                if (value) {
                    ids.add(attributeId);
                }
            }
        }
        return ids;
    }

    @GET
    @Path("{id}/monthlyReports")
    @Produces("application/json")
    @Timed(name = "api.rest.sites.monthly_reports")
    public String queryMonthlyReports(@PathParam("id") int siteId) throws IOException {

        GetMonthlyReports command = new GetMonthlyReports(siteId, new Month(0,1), new Month(Integer.MAX_VALUE, 12));
        MonthlyReportResult result = dispatcher.execute(command);

        // list all months
        Set<String> monthNames = Sets.newHashSet();
        for(IndicatorRowDTO row : result.getData()) {
            for(String propertyName : row.getPropertyNames()) {
                if(propertyName.startsWith("M")) {
                    monthNames.add(propertyName);
                }
            }
        }

        // write out results per month
        StringWriter writer = new StringWriter();
        JsonGenerator json = Jackson.createJsonFactory(writer);

        json.writeStartObject();
        for(String monthName : monthNames) {
            json.writeArrayFieldStart(formatMonth(monthName));

            for(IndicatorRowDTO row : result.getData()) {
                if(row.get(monthName) instanceof Number) {
                    json.writeStartObject();
                    Number value = row.get(monthName);
                    json.writeNumberField("indicatorId", row.getIndicatorId());
                    json.writeStringField("indicatorName", row.getIndicatorName());
                    json.writeNumberField("value", value.doubleValue());
                    json.writeEndObject();
                }
            }
            json.writeEndArray();
        }
        json.writeEndObject();
        json.close();

        return writer.toString();
    }

    @Path("/cube")
    public CubeResource getCube() {
        return new CubeResource(dispatcher);
    }


    private String formatMonth(String propertyName) {
        Month month = Month.parseMonth(propertyName.substring(1));
        String monthName = month.getYear() + "-";
        if(month.getMonth() < 10) {
            monthName += "0";
        }
        monthName += month.getMonth();
        return monthName;
    }

}
