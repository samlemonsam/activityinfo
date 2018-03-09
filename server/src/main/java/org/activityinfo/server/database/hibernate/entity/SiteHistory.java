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
package org.activityinfo.server.database.hibernate.entity;

import javax.persistence.*;

@Entity
public class SiteHistory implements java.io.Serializable {
    private static final long serialVersionUID = 4677949631016278657L;

    private int id;
    private Site site;
    private User user;
    private String json;
    private long timeCreated;
    private boolean initial;

    public SiteHistory() {
    }

    @Id @Column(name = "id", unique = true, nullable = false) @GeneratedValue(strategy = GenerationType.AUTO)
    public int getId() {
        return this.id;
    }

    public void setId(int siteId) {
        this.id = siteId;
    }

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "siteId", nullable = false)
    public Site getSite() {
        return this.site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "userId", nullable = false)
    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Lob
    public String getJson() {
        return this.json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    @Basic
    public long getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(long timeCreated) {
        this.timeCreated = timeCreated;
    }

    /**
     * @return true if this sitehistory entry was created when the site itself
     * was created. False if the entry is an update, or baseline
     * 'repair' record.
     */
    @Basic
    public boolean isInitial() {
        return this.initial;
    }

    public void setInitial(boolean isInitial) {
        this.initial = isInitial;
    }

}
