/**
 * Application icons
 */
package org.activityinfo.ui.client.style.legacy.icon;

/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * Provides access to the application's icons through GWT's magic ImageBundle
 * generator.
 */
public class IconImageBundle  {
    public static final IconImageBundle ICONS = new IconImageBundle();

    private final IconBundle bundle;

    public IconImageBundle() {
        bundle = GWT.create(IconBundle.class);
    }

    public AbstractImagePrototype add() { return AbstractImagePrototype.create(bundle.add()); }

    public AbstractImagePrototype delete() { return AbstractImagePrototype.create(bundle.delete()); }

    public AbstractImagePrototype editPage() { return AbstractImagePrototype.create(bundle.editPage()); }

    public AbstractImagePrototype save() { return AbstractImagePrototype.create(bundle.save()); }

    public AbstractImagePrototype importIcon() { return AbstractImagePrototype.create(bundle.importIcon()); }

    public AbstractImagePrototype database() { return AbstractImagePrototype.create(bundle.database()); }

    public AbstractImagePrototype design() { return AbstractImagePrototype.create(bundle.design()); }

    public AbstractImagePrototype addDatabase() { return AbstractImagePrototype.create(bundle.addDatabase()); }

    public AbstractImagePrototype editDatabase() { return AbstractImagePrototype.create(bundle.editDatabase()); }

    public AbstractImagePrototype excel() { return AbstractImagePrototype.create(bundle.excel()); }

    public AbstractImagePrototype activity() { return AbstractImagePrototype.create(bundle.activity()); }

    public AbstractImagePrototype addActivity() { return AbstractImagePrototype.create(bundle.addActivity()); }

    public AbstractImagePrototype user() { return AbstractImagePrototype.create(bundle.user()); }

    public AbstractImagePrototype addUser() { return AbstractImagePrototype.create(bundle.addUser()); }

    public AbstractImagePrototype deleteUser() { return AbstractImagePrototype.create(bundle.deleteUser()); }

    /*
     * @return Icon for a user group
     */
    public AbstractImagePrototype group() { return AbstractImagePrototype.create(bundle.group()); }

    public AbstractImagePrototype table() { return AbstractImagePrototype.create(bundle.table()); }

    public AbstractImagePrototype report() { return AbstractImagePrototype.create(bundle.report()); }

    public AbstractImagePrototype curveChart() { return AbstractImagePrototype.create(bundle.curveChart()); }

    public AbstractImagePrototype map() { return AbstractImagePrototype.create(bundle.map()); }

    public AbstractImagePrototype filter() { return AbstractImagePrototype.create(bundle.filter()); }

    public AbstractImagePrototype login() { return AbstractImagePrototype.create(bundle.login()); }

    public AbstractImagePrototype cancel() { return AbstractImagePrototype.create(bundle.cancel()); }

    public AbstractImagePrototype barChart() { return AbstractImagePrototype.create(bundle.barChart()); }

    public AbstractImagePrototype dataEntry() { return AbstractImagePrototype.create(bundle.dataEntry()); }

    public AbstractImagePrototype indicator() { return AbstractImagePrototype.create(bundle.indicator()); }

    public AbstractImagePrototype attributeGroup() { return AbstractImagePrototype.create(bundle.attributeGroup()); }

    public AbstractImagePrototype attribute() { return AbstractImagePrototype.create(bundle.attribute()); }

    public AbstractImagePrototype refresh() { return AbstractImagePrototype.create(bundle.refresh()); }

    public AbstractImagePrototype setup() { return AbstractImagePrototype.create(bundle.setup()); }

    public AbstractImagePrototype mapped() { return AbstractImagePrototype.create(bundle.mapped()); }

    public AbstractImagePrototype unmapped() { return AbstractImagePrototype.create(bundle.unmapped()); }

    public AbstractImagePrototype image() { return AbstractImagePrototype.create(bundle.image()); }

    public AbstractImagePrototype msword() { return AbstractImagePrototype.create(bundle.msword()); }

    public AbstractImagePrototype pdf() { return AbstractImagePrototype.create(bundle.pdf()); }

    public AbstractImagePrototype pieChart() { return AbstractImagePrototype.create(bundle.pieChart()); }

    public AbstractImagePrototype unchecked() { return AbstractImagePrototype.create(bundle.unchecked()); }

    public AbstractImagePrototype offline() { return AbstractImagePrototype.create(bundle.offline()); }

    public AbstractImagePrototype sync() { return AbstractImagePrototype.create(bundle.sync()); }

    public AbstractImagePrototype up() { return AbstractImagePrototype.create(bundle.up()); }

    public AbstractImagePrototype down() { return AbstractImagePrototype.create(bundle.down()); }

    public AbstractImagePrototype remove() { return AbstractImagePrototype.create(bundle.remove()); }

    public AbstractImagePrototype create() { return AbstractImagePrototype.create(bundle.create()); }

    public AbstractImagePrototype rename() { return AbstractImagePrototype.create(bundle.rename()); }

    public AbstractImagePrototype select() { return AbstractImagePrototype.create(bundle.select()); }

    public AbstractImagePrototype info() { return AbstractImagePrototype.create(bundle.info()); }

    public AbstractImagePrototype collapse() { return AbstractImagePrototype.create(bundle.collapse()); }

    public AbstractImagePrototype project() { return AbstractImagePrototype.create(bundle.project()); }

    public AbstractImagePrototype lockedPeriod() { return AbstractImagePrototype.create(bundle.lockedPeriod()); }

    public AbstractImagePrototype lockedPeriodSmall() { return AbstractImagePrototype.create(bundle.lockedPeriodSmall()); }

    public AbstractImagePrototype partner() { return AbstractImagePrototype.create(bundle.partner()); }

    public AbstractImagePrototype empty() { return AbstractImagePrototype.create(bundle.empty()); }

    public AbstractImagePrototype applyFilter() { return AbstractImagePrototype.create(bundle.applyFilter()); }

    public AbstractImagePrototype site() { return AbstractImagePrototype.create(bundle.site()); }

    public AbstractImagePrototype none() { return AbstractImagePrototype.create(bundle.none()); }

    public AbstractImagePrototype indicators() { return AbstractImagePrototype.create(bundle.indicators()); }

    public AbstractImagePrototype location() { return AbstractImagePrototype.create(bundle.location()); }

    public AbstractImagePrototype edit() { return AbstractImagePrototype.create(bundle.edit()); }

    public AbstractImagePrototype time() { return AbstractImagePrototype.create(bundle.time()); }

    public AbstractImagePrototype note() { return AbstractImagePrototype.create(bundle.note()); }

    public AbstractImagePrototype csv() { return AbstractImagePrototype.create(bundle.csv()); }

    public AbstractImagePrototype dashboard() { return AbstractImagePrototype.create(bundle.dashboard()); }

    public AbstractImagePrototype marker() { return AbstractImagePrototype.create(bundle.marker()); }

    public AbstractImagePrototype useLocation() { return AbstractImagePrototype.create(bundle.useLocation()); }

    public AbstractImagePrototype list() { return AbstractImagePrototype.create(bundle.list()); }

    public AbstractImagePrototype folder() { return AbstractImagePrototype.create(bundle.folder()); }

    public AbstractImagePrototype printer() { return AbstractImagePrototype.create(bundle.printer()); }

    public AbstractImagePrototype email() { return AbstractImagePrototype.create(bundle.email()); }

    public AbstractImagePrototype star() { return AbstractImagePrototype.create(bundle.star()); }

    public AbstractImagePrototype starWhite() { return AbstractImagePrototype.create(bundle.starWhite()); }

    public AbstractImagePrototype link() { return AbstractImagePrototype.create(bundle.link()); }

    public AbstractImagePrototype page() { return AbstractImagePrototype.create(bundle.page()); }

    public AbstractImagePrototype embed() { return AbstractImagePrototype.create(bundle.embed()); }

    public AbstractImagePrototype text() { return AbstractImagePrototype.create(bundle.text()); }

    public AbstractImagePrototype form() { return AbstractImagePrototype.create(bundle.form()); }

}
