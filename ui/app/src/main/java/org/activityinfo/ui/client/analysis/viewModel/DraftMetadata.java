package org.activityinfo.ui.client.analysis.viewModel;

import com.google.common.base.Optional;
import org.immutables.value.Value;

@Value.Immutable
public abstract class DraftMetadata {

    public abstract Optional<String> getLabel();
    public abstract Optional<String> getFolderId();

}
