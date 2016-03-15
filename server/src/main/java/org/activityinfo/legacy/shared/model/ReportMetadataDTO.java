package org.activityinfo.legacy.shared.model;

/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.RpcMap;
import com.google.common.collect.Maps;
import org.activityinfo.legacy.shared.reports.model.EmailDelivery;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * One-to-one DTO for the
 * {@link org.activityinfo.server.database.hibernate.entity.ReportDefinition}
 * domain class
 *
 * @author Alex Bertram
 */
public final class ReportMetadataDTO extends BaseModelData implements DTO {

    private List<String> subscribers;
    /**
     * Dummy reference to assure that GWT includes ReportFrequency is included
     * in the list of classes to serialize.
     */
    private EmailDelivery freq;

    public ReportMetadataDTO() {
        setEmailDelivery(EmailDelivery.NONE);
    }

    /**
     * @return this ReportDefinition's id
     */
    public int getId() {
        return (Integer) get("id");
    }

    /**
     * Sets the id of this ReportDefinition
     */
    public void setId(int id) {
        set("id", id);
    }

    /**
     * Sets the title of this ReportDefinition
     */
    public void setTitle(String title) {
        set("title", title);
    }

    /**
     * @return the title of this ReportDefinition
     */
    public String getTitle() {
        return get("title");
    }

    /**
     * @return true if the current user permission to edit this report
     * definition
     */
    public boolean isEditAllowed() {
        return get("editAllowed", false);
    }

    /**
     * Sets the permission of the current user to edit this report definition
     */
    public void setEditAllowed(boolean allowed) {
        set("editAllowed", allowed);
    }

    /**
     * @return the name of the User who owns this ReportDefinition
     */
    public String getOwnerName() {
        return get("ownerName");
    }

    /**
     * Sets the name of the User who own this ReportDefinition
     */
    public void setOwnerName(String name) {
        set("ownerName", name);
    }

    /**
     * Sets whether the current user is the owner of this ReportDefintion
     */
    public void setAmOwner(boolean amOwner) {
        set("amOwner", amOwner);
    }

    /**
     * @return true if the current user is the owner of this ReportDefinition
     */
    public boolean getAmOwner() {
        return (Boolean) get("amOwner");
    }

    /**
     * @return the ReportFrequency of this ReportDefinition
     */
    public EmailDelivery getEmailDelivery() {
        return get("emailDelivery", EmailDelivery.NONE);
    }

    /**
     * Sets the ReportFrequency of this ReportDefinition
     */
    public void setEmailDelivery(EmailDelivery frequency) {
        set("emailDelivery", frequency);
    }

    /**
     * @return the day of the month [1, 31] on which this ReportDefinition is to
     * be published
     */
    public Integer getDay() {
        return get("day", 0);
    }

    /**
     * Sets the day of the month on which this ReportDefinition is to be
     * published.
     */
    public void setDay(Integer day) {
        set("day", day);
    }

    public List<String> getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(List<String> subscribers) {
        this.subscribers = subscribers;
    }

    public void setDashboard(boolean dashboard) {
        set("dashboard", dashboard);
    }

    public boolean isDashboard() {
        return get("dashboard", false);
    }

    private void writeObject(ObjectOutputStream o)
            throws IOException {

        o.writeObject(freq);
        o.writeObject(subscribers);
        o.writeBoolean(allowNestedValues);
        o.writeObject(Maps.newHashMap(map.getTransientMap()));
    }

    private void readObject(ObjectInputStream o)
            throws IOException, ClassNotFoundException {

        freq = (EmailDelivery) o.readObject();
        subscribers = (List) o.readObject();
        allowNestedValues = o.readBoolean();

        HashMap<String,Object> readMap = (HashMap<String, Object>) o.readObject();
        map = new RpcMap();

        if (readMap != null && !readMap.isEmpty()) {
            for (Map.Entry<String, Object> entry : readMap.entrySet()) {
                map.put(entry.getKey(), entry.getValue());
            }
        }
    }

}
