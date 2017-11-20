package org.activityinfo.test.driver.mail.mailinator;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * An item in the mailinator inbox
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class MessageHeader {
    @JsonProperty("seconds_ago")
    private long secondsAgo;
    
    private String id;
    private String to;
    private long time;
    private String subject;
    
    @JsonProperty("fromfull")
    private String fromFull;
    
    @JsonProperty("origfrom")
    private String originalFrom;
    
    @JsonProperty("been_read")
    private boolean read;

    public long getSecondsAgo() {
        return secondsAgo;
    }

    public void setSecondsAgo(long secondsAgo) {
        this.secondsAgo = secondsAgo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getFromFull() {
        return fromFull;
    }

    public void setFromFull(String fromFull) {
        this.fromFull = fromFull;
    }

    public String getOriginalFrom() {
        return originalFrom;
    }

    public void setOriginalFrom(String originalFrom) {
        this.originalFrom = originalFrom;
    }

    public String getFrom() {
        if(!Strings.isNullOrEmpty(fromFull)) {
            return fromFull;
        }
        if(!Strings.isNullOrEmpty(originalFrom)) {
            return parseEmail(originalFrom);
        }
        throw new IllegalStateException("Could not read from header from message " + id);
    }

    /**
     * Parse an email in the form "Sender Name <sender@host.com>". Will also also accept
     * "sender@host.com"
     * 
     * @return the email address
     */
    @VisibleForTesting
    static String parseEmail(String headerValue) {
        StringBuilder email = new StringBuilder();
        boolean quoted = false;
        boolean inEmail = false;

        for (int i = 0; i < headerValue.length(); i++) {
            char c = headerValue.charAt(i);
            if(quoted) {
                if(c == '"') {
                    quoted = false;
                }
            
            } else {
                if(c == '"') {
                    quoted = true;
                } else if(c == '<') {
                    inEmail = true;
                } else if(c == '>') {
                    inEmail = false;
                
                } else {
                    if(inEmail) {
                        email.append(c);
                    }
                }
            }
        }
        
        if(email.length() > 0) {
            return email.toString();
        }
        
        // If there was no text between <> tokens, assume
        // this is a plain address if there is @ somewhere
        if(headerValue.contains("@")) {
            return headerValue.trim();
        }
        
        // Otherwise, give up
        throw new IllegalArgumentException("Could not extract address from header [" + headerValue + "]");
    }
    
    
    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }


    @Override
    public String toString() {
        return new StringBuilder().append("{")
                .append(id).append(" ")
                .append(fromFull).append(" ")
                .append(subject).append("}")
                .toString();
    }
}
