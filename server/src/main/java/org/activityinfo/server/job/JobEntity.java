package org.activityinfo.server.job;

import com.google.gson.JsonParser;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Unindex;
import org.activityinfo.model.job.JobDescriptor;
import org.activityinfo.model.job.JobRequest;
import org.activityinfo.model.job.JobResult;
import org.activityinfo.model.job.JobState;

import java.util.Date;

/**
 * A long-running server side job executed on behalf of the user.
 */
@Entity(name = "Job")
public class JobEntity {

    @Id
    private Long id;


    /**
     * The type of job
     */
    @Index
    private String type;

    @Index
    private long userId;

    /**
     * Json-encoded job descriptor.
     */
    @Unindex
    private String descriptor;

    @Unindex
    private JobState state;

    @Index
    private Date startTime;

    @Unindex
    private Date completionTime;

    @Unindex
    private String locale;

    @Unindex
    private String result;

    public JobEntity() {
    }

    public JobEntity(long userId) {
        this.userId = userId;
    }


    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(String description) {
        this.descriptor = description;
    }

    public JobDescriptor parseDescriptor() {
        return JobRequest.parseDescriptor(getType(), new JsonParser().parse(getDescriptor()).getAsJsonObject());
    }

    public JobState getState() {
        return state;
    }

    public void setState(JobState state) {
        this.state = state;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(Date completionTime) {
        this.completionTime = completionTime;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public JobResult parseResult() {
        if(result == null) {
            return null;
        }
        JobDescriptor descriptor = parseDescriptor();
        return descriptor.parseResult(new JsonParser().parse(getResult()).getAsJsonObject());
    }
}
