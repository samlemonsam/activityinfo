package org.activityinfo.ui.client.analysis.view;

import com.sencha.gxt.data.shared.ListStore;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.analysis.viewModel.AnalysisViewModel;
import org.activityinfo.ui.client.analysis.viewModel.EffectiveMeasure;
import org.activityinfo.ui.client.analysis.viewModel.EffectiveModel;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MeasureListItemStore extends ListStore<MeasureListItem> {

    private static final Logger LOGGER = Logger.getLogger(MeasureListItemStore.class.getName());

    public MeasureListItemStore(AnalysisViewModel viewModel) {
        super(MeasureListItem::getId);

        Observable<EffectiveModel> effectiveModel = viewModel.getEffectiveModel();
        assert effectiveModel != null;

        Observable<List<MeasureListItem>> measures = effectiveModel.transform(em -> {
            List<MeasureListItem> list = new ArrayList<>();
            for (EffectiveMeasure effectiveMeasure : em.getMeasures()) {
                MeasureListItem li = new MeasureListItem(effectiveMeasure);
                list.add(li);
            }
            return list;
        });

        measures.subscribe(observer -> {
            clear();
            if(observer.isLoaded()) {
                replaceAll(observer.get());
            }
        });
    }
}
