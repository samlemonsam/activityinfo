package org.activityinfo.ui.client.analysis.viewModel;

import com.google.common.base.Optional;
import org.activityinfo.model.analysis.AnalysisModel;
import org.activityinfo.model.analysis.AnalysisUpdate;
import org.activityinfo.model.analysis.TypedAnalysis;
import org.activityinfo.promise.Maybe;

public class WorkingModel<T extends AnalysisModel> {

    private String id;
    private Optional<String> label;
    private Optional<String> parentId;
    private T model;
    private final boolean dirty;

    public WorkingModel(String id, Maybe<TypedAnalysis<T>> analysis, DraftMetadata draftMetadata, Optional<T> draftModel, T emptyModel) {
        this.id = id;
        this.label = draftMetadata.getLabel().or(analysis.getIfVisible().transform(a -> a.getLabel()));
        this.parentId = draftMetadata.getFolderId().or(analysis.getIfVisible().transform(a -> a.getParentId()));
        this.model = draftModel.or(analysis.getIfVisible().transform(a -> a.getModel())).or(emptyModel);
        this.dirty = isDirty(analysis.transform(a -> a.getLabel()), draftMetadata.getLabel()) ||
                     isDirty(analysis.transform(a -> a.getParentId()), draftMetadata.getFolderId()) ||
                     isDirty(analysis.transform(a -> a.getModel()), draftModel);

    }

    private static <T> boolean isDirty(Maybe<T> saved, Optional<T> draft) {
        if(!draft.isPresent()) {
            return false;
        }
        if(!saved.isVisible()) {
            return true;
        }
        return !saved.get().equals(draft.get());
    }

    public boolean isDirty() {
        return dirty;
    }

    public String getId() {
        return id;
    }

    public Optional<String> getLabel() {
        return label;
    }

    public Optional<String> getParentId() {
        return parentId;
    }

    public T getModel() {
        return model;
    }

    public AnalysisUpdate buildUpdate() {
        AnalysisUpdate update = new AnalysisUpdate();
        update.setId(id);
        update.setModel(model.toJson());
        update.setType(model.getTypeId());
        update.setLabel(label.get());
        update.setParentId(parentId.get());
        return update;
    }
}
