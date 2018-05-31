package org.activityinfo.server.pipeline;

import com.google.appengine.tools.pipeline.PipelineService;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.model.pipeline.AdditionJobDescriptor;
import org.activityinfo.model.pipeline.PipelineJobDescriptor;
import org.activityinfo.server.pipeline.job.AdditionJob;

import javax.persistence.EntityManager;

public class PipelineJobFactory {

    private Provider<EntityManager> entityManager;

    @Inject
    public PipelineJobFactory(Provider<EntityManager> entityManager) {
        this.entityManager = entityManager;
    }

    public String create(String type, PipelineJobDescriptor jobDescriptor, PipelineService pipeline) {
        switch(type) {
            case AdditionJobDescriptor.TYPE:
                AdditionJob addJob = new AdditionJob();
                AdditionJobDescriptor addDescriptor = (AdditionJobDescriptor) jobDescriptor;
                return pipeline.startNewPipeline(addJob, addDescriptor.getA(), addDescriptor.getB());
            default:
                break;
        }
        throw new IllegalArgumentException(type);
    }

}
