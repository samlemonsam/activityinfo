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
package org.activityinfo.i18n.tools.po;

import org.activityinfo.i18n.tools.model.Term;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.HashSet;
import java.util.Set;

@JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
public class PoTerm implements Term {
  public static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss");

  @JsonProperty("term")
  private String key;
  private String context;
  private DateTime created;
  private DateTime updated;
  private String reference;

  @JsonSerialize(include= JsonSerialize.Inclusion.NON_EMPTY)
  private final Set<String> tags = new HashSet<>();

  public PoTerm() {
  }

  public PoTerm(Term term) {
    this.key = term.getKey();
  }

  public String getKey() {
    return key;
  }

  public void setKey(String term) {
    this.key = term;
  }

  public String getContext() {
    return context;
  }

  public void setContext(String context) {
    this.context = context;
  }

  public DateTime getCreated() {
    return created;
  }

  public void setCreated(DateTime created) {
    this.created = created;
  }

  @JsonSetter
  public void setCreated(String created) {
    this.created = DATE_TIME_FORMAT.parseDateTime(created);
  }

  public DateTime getUpdated() {
    return updated;
  }

  public void setUpdated(DateTime updated) {
    this.updated = updated;
  }

  @JsonSetter
  public void setUpdated(String updated) {
    if(!updated.isEmpty()) {
      this.updated = DATE_TIME_FORMAT.parseDateTime(updated);
    }
  }

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public Set<String> getTags() {
    return tags;
  }


}
