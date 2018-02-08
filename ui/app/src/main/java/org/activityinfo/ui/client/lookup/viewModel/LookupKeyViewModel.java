package org.activityinfo.ui.client.lookup.viewModel;

import com.google.common.base.Optional;
import org.activityinfo.model.formTree.LookupKey;
import org.activityinfo.observable.Observable;

import java.util.List;

public class LookupKeyViewModel {

    private LookupKey lookupKey;
    private final Observable<Boolean> enabled;
    private final Observable<Optional<String>> selectedKey;
    private final Observable<List<String>> choices;

    LookupKeyViewModel(LookupKey lookupKey,
                       Observable<Boolean> enabled,
                       Observable<Optional<String>> selectedKey,
                       Observable<List<String>> choices) {

        this.lookupKey = lookupKey;
        this.selectedKey = selectedKey;
        this.enabled = enabled;
        this.choices = choices;
    }

    public boolean isLeaf() {
        return lookupKey.getChildLevels().isEmpty();
    }

    public Observable<Optional<String>> getSelectedKey() {
        return selectedKey;
    }

    public Observable<Boolean> isEnabled() {
        return enabled;
    }

    public Observable<List<String>> getChoices() {
        return choices;
    }

    public String getKeyLabel() {
        return lookupKey.getKeyLabel();
    }

    public LookupKey getLookupKey() {
        return lookupKey;
    }
}
