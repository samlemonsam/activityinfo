package org.activityinfo.geoadmin.merge2.state;

import org.activityinfo.geoadmin.merge2.MergeModelStore;
import org.activityinfo.geoadmin.merge2.view.swing.MergeDialog;
import org.activityinfo.model.legacy.CuidAdapter;

import java.io.IOException;

/**
 * Created by alex on 22-5-15.
 */
public class MergeDialogRunner {

    public static void main(String[] args) throws IOException {
        ResourceStore resourceStore = new ResourceStoreStub();

        MergeModelStore modelStore = new MergeModelStore(resourceStore,
                CuidAdapter.adminLevelFormClass(3),
                CuidAdapter.adminLevelFormClass(3));

        MergeDialog dialog = new MergeDialog(modelStore);
        dialog.setVisible(true);

    }
}
