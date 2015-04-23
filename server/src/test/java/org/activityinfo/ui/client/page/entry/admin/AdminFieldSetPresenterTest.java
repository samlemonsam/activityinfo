package org.activityinfo.ui.client.page.entry.admin;

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

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import org.activityinfo.legacy.shared.command.GetAdminEntities;
import org.activityinfo.legacy.shared.command.result.AdminEntityResult;
import org.activityinfo.legacy.shared.model.ActivityFormDTO;
import org.activityinfo.legacy.shared.model.AdminEntityDTO;
import org.activityinfo.legacy.shared.model.AdminLevelDTO;
import org.activityinfo.legacy.shared.model.DTOs.*;
import org.activityinfo.legacy.shared.reports.util.mapping.Extents;
import org.activityinfo.ui.client.dispatch.DispatcherStub;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.activityinfo.legacy.shared.model.DTOs.*;
import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class AdminFieldSetPresenterTest {

    private DispatcherStub dispatcher = new DispatcherStub();
    private AdminFieldSetPresenter presenter;
    private Listener<AdminLevelSelectionEvent> selectionListener;
    private Listener<LevelStateChangeEvent> levelStateChangeListener;
    private Listener<BoundsChangedEvent> boundsListener;

    @Before
    public void setupDispatcher() {
        dispatcher.setResult(new GetAdminEntities(PROVINCE.getId()), PROVINCES);
        dispatcher.setResult(
                new GetAdminEntities(TERRITOIRE.getId(), NORD_KIVU.getId()),
                NORD_KIVU_TERRITOIRES);
        dispatcher.setResult(
                new GetAdminEntities(TERRITOIRE.getId(), SUD_KIVU.getId()),
                SUD_KIVU_TERRITOIRES);
    }

    @Before
    public void setupListeners() {
        selectionListener = createNiceMock("selectionListener", Listener.class);
        levelStateChangeListener = createNiceMock("levelStateChangeListener",
                Listener.class);
        boundsListener = createNiceMock("boundsListener", Listener.class);

        replay(selectionListener, levelStateChangeListener, boundsListener);
    }

    @Test
    public void testSetSite() throws Exception {
        expectSelections(PEAR.WATALINA_CENTER_IN_BENI.getAdminEntities()
                .values());
        expectEnabledEvents(TERRITOIRE, SECTEUR);

        presenterForActivity(PEAR.NFI_DISTRIBUTION);

        presenter.setSelection(PEAR.WATALINA_CENTER_IN_BENI);

        // Verify that setting a sites results in the correct values being
        // sent to the view, and that the correct combos are enabled
        verify(selectionListener, levelStateChangeListener);

        assertSelected(NORD_KIVU);
        assertLevelIsEnabled(PROVINCE);
        assertLevelIsEnabled(TERRITOIRE);
        assertLevelIsEnabled(SECTEUR);

        // VERIFY that the correct command has been set for combos
        verifyLoad(TERRITOIRE, NORD_KIVU_TERRITOIRES);
    }

    @Test
    public void testInitBlank() throws Exception {

        presenterForActivity(PEAR.NFI_DISTRIBUTION);
        presenter.setSelection(PEAR.SITE_WITH_NO_ADMIN_LEVELS);

        assertLevelIsEnabled(PROVINCE);
        assertLevelIsDisabled(TERRITOIRE);
        assertLevelIsDisabled(SECTEUR);
    }

    @Test
    public void testCascade() throws Exception {

        presenterForActivity(PEAR.NFI_DISTRIBUTION);
        presenter.setSelection(PEAR.SITE_WITH_NO_ADMIN_LEVELS);

        verifyLoad(PROVINCE, PROVINCES);

        // VERIFY that a change to the province reconfigures the territory
        // loader
        presenter.setSelection(1, NORD_KIVU);

        verifyLoad(TERRITOIRE, NORD_KIVU_TERRITOIRES);
    }

    @Test
    public void testCascadeReplace() throws Exception {

        presenterForActivity(PEAR.NFI_DISTRIBUTION);

        // SETUP selection
        presenter.setSelection(PEAR.WATALINA_CENTER_IN_BENI);

        // VERIFY that change to province correctly cascades
        presenter.setSelection(PROVINCE.getId(), SUD_KIVU);

        assertLevelIsEmpty(TERRITOIRE);
        assertLevelIsEmpty(SECTEUR);
        assertLevelIsDisabled(SECTEUR);

        verifyLoad(TERRITOIRE, SUD_KIVU_TERRITOIRES);
    }

    @Test
    public void testBounds() {

        expectBounds(BENI.getBounds(), BENI.getName());

        presenterForActivity(PEAR.NFI_DISTRIBUTION);

        presenter.setSelection(PEAR.WATALINA_CENTER_IN_BENI);

        verify(boundsListener);

        assertThat(presenter.getBounds(), equalTo(BENI.getBounds()));
        assertThat(presenter.getBoundsName(), equalTo(BENI.getName()));
    }

    @Test
    public void testBoundsChange() {

        presenterForActivity(PEAR.NFI_DISTRIBUTION);

        presenter.setSelection(PEAR.WATALINA_CENTER_IN_BENI);

        expectBounds(MASISI.getBounds(), MASISI.getName());

        presenter.setSelection(TERRITOIRE.getId(), MASISI);

        verify(boundsListener);
    }

    /**
     * Regression test for bug
     */
    @Test
    public void testChange3rdLevelAdmin() {

        presenterForActivity(PEAR.NFI_DISTRIBUTION);

        // VERIFY: changing one adminlevel works properlty
        presenter.setSelection(PEAR.WATALINA_CENTER_IN_BENI);
        presenter.setSelection(SECTEUR.getId(), RUIZI);

        assertThat(presenter.getAdminEntity(PROVINCE), equalTo(NORD_KIVU));
        assertThat(presenter.getAdminEntity(TERRITOIRE), equalTo(BENI));
        assertThat(presenter.getAdminEntity(SECTEUR), equalTo(RUIZI));
    }
    
    @Test
    public void testSortingAdminLevel() {
        AdminLevelDTO country = new AdminLevelDTO();
        country.setId(0);
        country.setName("Country");
        
        AdminLevelDTO state = new AdminLevelDTO(); 
        state.setId(1);
        state.setName("state");
        state.setParentLevelId(0);
        
        AdminLevelDTO district = new AdminLevelDTO();
        district.setId(2);
        district.setParentLevelId(1);
        district.setName("District");
        
        AdminLevelDTO city = new AdminLevelDTO();
        city.setId(3);
        city.setParentLevelId(2);
        city.setName("City");
        
        AdminLevelDTO region = new AdminLevelDTO();
        region.setId(4);
        region.setParentLevelId(0);
        region.setName("Region");
        
        AdminLevelDTO northRegion = new AdminLevelDTO();
        northRegion.setId(5);
        northRegion.setParentLevelId(4);
        northRegion.setName("North region");
        
        AdminLevelDTO southRegion = new AdminLevelDTO();
        southRegion.setId(6);
        southRegion.setParentLevelId(4);
        southRegion.setName("south region");
        
        AdminLevelDTO southEastArea = new AdminLevelDTO();
        southEastArea.setId(7);
        southEastArea.setParentLevelId(6);
        southEastArea.setName("South east area");

        List<AdminLevelDTO> levels = new ArrayList<AdminLevelDTO>();
        levels.add(city);
        levels.add(southEastArea);
        levels.add(region);
        levels.add(northRegion);
        levels.add(southRegion);
        levels.add(country);
        levels.add(state);
        levels.add(district);
        
        ArrayList<AdminLevelDTO> sortedLevels = sort(levels);
        
        assertTrue("country before state", sortedLevels.indexOf(country) <  sortedLevels.indexOf(state));
        assertTrue("state before district", sortedLevels.indexOf(state)   <  sortedLevels.indexOf(district));
        assertTrue("district before city", sortedLevels.indexOf(district) < sortedLevels.indexOf(city));

        assertTrue("country before region", sortedLevels.indexOf(country) < sortedLevels.indexOf(region));
        assertTrue("region before 'north region'", sortedLevels.indexOf(region) < sortedLevels.indexOf(northRegion));
        assertTrue("'north region' before 'south region", sortedLevels.indexOf(northRegion) < sortedLevels.indexOf(southRegion));
        assertTrue("'south region' before 'southEastArea", sortedLevels.indexOf(southRegion) < sortedLevels.indexOf(southEastArea));
    }

    private void expectSelections(Collection<AdminEntityDTO> values) {
        resetToDefault(selectionListener);
        for (AdminEntityDTO entity : values) {
            selectionListener.handleEvent(eq(new AdminLevelSelectionEvent(
                    entity.getLevelId(), entity)));
        }
        replay(selectionListener);
    }

    private void expectEnabledEvents(AdminLevelDTO... levels) {
        resetToDefault(levelStateChangeListener);
        for (AdminLevelDTO level : levels) {
            levelStateChangeListener.handleEvent(eq(new LevelStateChangeEvent(
                    level.getId(), true)));
        }
        replay(levelStateChangeListener);
    }

    private void expectBounds(Extents bounds, String name) {
        resetToDefault(boundsListener);
        boundsListener.handleEvent(eq(new BoundsChangedEvent(bounds, name)));
        replay(boundsListener);
    }

    private void assertSelected(AdminEntityDTO entity) {
        assertThat("levelId=" + entity.getLevelId() + " selection",
                presenter.getAdminEntity(entity.getLevelId()), equalTo(entity));
    }

    private void assertLevelIsEmpty(AdminLevelDTO level) {
        assertThat(level.getName() + " is empty",
                presenter.getAdminEntity(level.getId()), is(nullValue()));
    }

    private void assertLevelIsEnabled(AdminLevelDTO level) {
        assertTrue(level.getName() + "is enabled",
                presenter.isLevelEnabled(level));
    }

    private void assertLevelIsDisabled(AdminLevelDTO level) {
        assertFalse(level.getName() + "is disabled",
                presenter.isLevelEnabled(level));
    }

    private void verifyLoad(AdminLevelDTO level,
                            AdminEntityResult expectedEntities) {
        ListStore<AdminEntityDTO> store = presenter.getStore(level.getId());
        store.getLoader().load();

        assertThat("number of entities loaded for " + level.getName()
                + " combo", store.getModels(), equalTo(expectedEntities.getData()));
    }

    private void presenterForActivity(ActivityFormDTO activity) {
        presenter = new AdminFieldSetPresenter(dispatcher,
                activity.getLocationType().getCountryBounds(),
                activity.getAdminLevels());
        presenter.addListener(AdminLevelSelectionEvent.TYPE, selectionListener);
        presenter.addListener(LevelStateChangeEvent.TYPE,
                levelStateChangeListener);
        presenter.addListener(BoundsChangedEvent.TYPE, boundsListener);
    }
    
    private ArrayList<AdminLevelDTO> sort(List<AdminLevelDTO> levels2) {
        ArrayList<AdminLevelDTO> sortedList = new ArrayList<>();
        ArrayList<AdminLevelDTO> sorterList = new ArrayList<>();
       
        for (AdminLevelDTO level : levels2) {
            if (level.getParentLevelId()== null) {
               sorterList.add(level);
               sortedList.add(level);
            }
        }
        while(levels2.size() != sortedList.size()) {
            ArrayList<AdminLevelDTO> tempList = new ArrayList<>();
            for(AdminLevelDTO dto : sorterList) {
                for(AdminLevelDTO e: levels2) {
                    if(e.getParentLevelId() != null) {
                        if(e.getParentLevelId().equals(dto.getId())) {
                            tempList.add(e);
                        }
                    }
                }
            }
            sortedList.addAll(tempList);
            sorterList.clear();
            sorterList.addAll(tempList);
        }
        return sortedList;
    }
}
