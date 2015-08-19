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
