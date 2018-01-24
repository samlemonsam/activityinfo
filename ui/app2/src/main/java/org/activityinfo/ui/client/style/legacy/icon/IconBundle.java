package org.activityinfo.ui.client.style.legacy.icon;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface IconBundle extends ClientBundle {
    IconImageBundle ICONS = GWT.create(IconImageBundle.class);

    ImageResource add();

    ImageResource delete();

    ImageResource editPage();

    ImageResource save();

    @ClientBundle.Source("arrow_merge.png")
    ImageResource importIcon();

    ImageResource database();

    ImageResource design();

    ImageResource addDatabase();

    ImageResource editDatabase();

    ImageResource excel();

    ImageResource activity();

    ImageResource addActivity();

    ImageResource user();

    ImageResource addUser();

    ImageResource deleteUser();

    /*
     * @return Icon for a user group
     */
    ImageResource group();

    ImageResource table();

    ImageResource report();

    ImageResource curveChart();

    ImageResource map();

    ImageResource filter();

    @ClientBundle.Source(value = "key.png")
    ImageResource login();

    ImageResource cancel();

    ImageResource barChart();

    @ClientBundle.Source(value = "keyboard.png")
    ImageResource dataEntry();

    @ClientBundle.Source(value = "ruler.png")
    ImageResource indicator();

    ImageResource attributeGroup();

    ImageResource attribute();

    ImageResource refresh();

    @ClientBundle.Source(value = "wrench_orange.png")
    ImageResource setup();

    ImageResource mapped();

    ImageResource unmapped();

    ImageResource image();

    ImageResource msword();

    ImageResource pdf();

    ImageResource pieChart();

    ImageResource unchecked();

    ImageResource offline();

    ImageResource sync();

    ImageResource up();

    ImageResource down();

    ImageResource remove();

    @ClientBundle.Source(value = "cog.png")
    ImageResource create();

    @ClientBundle.Source(value = "page_edit.png")
    ImageResource rename();

    @ClientBundle.Source(value = "table_edit.png")
    ImageResource select();

    @ClientBundle.Source(value = "information.png")
    ImageResource info();

    ImageResource collapse();

    @ClientBundle.Source(value = "project.png")
    ImageResource project();

    @ClientBundle.Source(value = "LockedPeriod.png")
    ImageResource lockedPeriod();

    @ClientBundle.Source(value = "lockSmall.png")
    ImageResource lockedPeriodSmall();

    @ClientBundle.Source(value = "partner.png")
    ImageResource partner();

    @ClientBundle.Source(value = "empty.png")
    ImageResource empty();

    @ClientBundle.Source(value = "applyFilter.png")
    ImageResource applyFilter();

    @ClientBundle.Source(value = "site.png")
    ImageResource site();

    @ClientBundle.Source(value = "none.png")
    ImageResource none();

    @ClientBundle.Source(value = "indicators.png")
    ImageResource indicators();

    ImageResource location();

    ImageResource edit();

    ImageResource time();

    ImageResource note();

    ImageResource csv();

    ImageResource dashboard();

    ImageResource marker();

    @ClientBundle.Source(value = "useLocation16.png")
    ImageResource useLocation();

    ImageResource list();

    ImageResource folder();

    ImageResource printer();

    ImageResource email();

    ImageResource star();

    ImageResource starWhite();

    ImageResource link();

    ImageResource page();

    @ClientBundle.Source(value = "html_add.png")
    ImageResource embed();

    ImageResource text();

    @ClientBundle.Source(value = "application_form.png")
    ImageResource form();
}
