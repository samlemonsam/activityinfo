package org.activityinfo.ui.client.page.common.toolbar;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.SplitButton;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.ui.client.page.report.ReportDesignPage;
import org.activityinfo.ui.client.style.legacy.icon.IconImageBundle;

import java.util.Arrays;
import java.util.List;

/**
 * SplitButton that provides users with a choice to "Save" or "Save As"
 */
public class SaveMenuButton extends SplitButton {

    private Menu menu;
    private SelectionListener<MenuEvent> menuListener;
    private ReportDesignPage.SaveCallback callback;
    private List<SaveMethod> methods;

    public SaveMenuButton() {
        this.setText(I18N.CONSTANTS.save());

        this.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                if (callback != null) {
                    callback.save(methods.get(0));
                }
            }
        });

        menuListener = new SelectionListener<MenuEvent>() {
            @Override
            public void componentSelected(MenuEvent menuEvent) {
                callback.save((SaveMethod) menuEvent.getItem().getData("method"));
            }
        };

        menu = new Menu();
        setMenu(menu);

        setMethods(Arrays.asList(SaveMethod.values()));
    }

    public void setMethods(List<SaveMethod> methods) {
        this.methods = methods;
        setIcon(IconImageBundle.ICONS.save());
        menu.removeAll();
        for (SaveMethod method : methods) {
            MenuItem word = new MenuItem(formatLabel(method), IconImageBundle.ICONS.save(), menuListener);
            word.setData("method", method);
            menu.add(word);
        }
    }

    public SaveMenuButton setCallback(ReportDesignPage.SaveCallback callback) {
        this.callback = callback;
        return this;
    }

    private String formatLabel(SaveMethod method) {
        switch (method) {
            case SAVE:
                return I18N.CONSTANTS.save();
            case SAVEAS:
                return I18N.CONSTANTS.saveAs();
        }
        throw new IllegalArgumentException("Undefined save method [" + method.name() + "]");
    }


}
