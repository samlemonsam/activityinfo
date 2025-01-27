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
package org.activityinfo.legacy.shared.reports.model;

import org.activityinfo.legacy.shared.model.AdminLevelDTO;
import org.activityinfo.legacy.shared.model.IndicatorDTO;
import org.activityinfo.legacy.shared.reports.content.TreeNode;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alex Bertram
 */
public class TableColumn extends TreeNode<TableColumn> {

    public enum SortOrder {
        Ascending,
        Descending
    }

    private String label;
    private String property;
    private Integer propertyQualifyingId;
    private SortOrder order;
    private List<TableColumn> children = new ArrayList<TableColumn>(0);

    public TableColumn() {
    }

    public TableColumn(String label) {
        this.label = label;
    }

    public TableColumn(String label, String property) {
        this.label = label;
        this.property = property;
    }

    @Override @XmlAttribute
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @XmlTransient
    public boolean isOrderAscending() {
        return order == SortOrder.Ascending;
    }

    @XmlAttribute
    public SortOrder getOrder() {
        return order;
    }

    public void setOrder(SortOrder order) {
        this.order = order;
    }

    @Override @XmlElement(name = "column")
    public List<TableColumn> getChildren() {
        return children;
    }

    public void setChildren(List<TableColumn> children) {
        this.children = children;
    }

    public void addChild(TableColumn column) {
        children.add(column);
    }

    @Override @XmlTransient
    public boolean isLeaf() {
        return children.size() == 0;
    }

    @XmlAttribute(name = "source")
    public String getProperty() {
        return property;
    }

    @XmlAttribute(name = "sourceId")
    public Integer getPropertyQualifyingId() {
        return propertyQualifyingId;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public void setPropertyQualifyingId(Integer propertyQualifyingId) {
        this.propertyQualifyingId = propertyQualifyingId;
    }

    /**
     * @return the name of the property in SiteDTO
     */
    public String getSitePropertyName() {

        // support legacy models that used their own special names
        if ("site.date1".equals(property)) {
            return "date1";
        } else if ("site.date2".equals(property)) {
            return "date2";
        } else if ("site.comments".equals(property)) {
            return "comments";
        } else if ("location.name".equals(property)) {
            return "locationName";
        } else if ("location.axe".equals(property)) {
            return "locationAxe";
        } else if ("indicator".equals(property)) {
            return IndicatorDTO.getPropertyName(propertyQualifyingId);
        } else if ("admin".equals(property)) {
            return AdminLevelDTO.getPropertyName(propertyQualifyingId);
        } else {
            return property;
        }
    }
}
