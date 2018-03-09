/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.ui.client.component.form.field.hierarchy;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.dispatch.ResourceLocator;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Models a hierarchy of choices for the user
 */
public class Hierarchy {

    private Map<ResourceId, Level> levelMap = Maps.newHashMap();
    private List<Level> roots = Lists.newArrayList();
    private List<Level> levels = Lists.newArrayList();

    public static Promise<Hierarchy> get(final ResourceLocator resourceLocator, ReferenceType type) {
        return Promise.map(type.getRange(), new Function<ResourceId, Promise<FormClass>>() {
            @Override
            public Promise<FormClass> apply(@Nullable ResourceId input) {
                return resourceLocator.getFormClass(input);
            }
        }).then(new Function<List<FormClass>, Hierarchy>() {
            @Nullable
            @Override
            public Hierarchy apply(@Nullable List<FormClass> input) {
                return new Hierarchy(input);
            }
        });
    }


    public static Hierarchy get(FormTree.Node rootField) {
        Map<ResourceId, FormClass> formClasses = new HashMap<>();
        for (FormTree.Node childNode : rootField.getChildren()) {
            FormClass childForm = childNode.getDefiningFormClass();
            if(!formClasses.containsKey(childForm.getId())) {
                formClasses.put(childForm.getId(), childForm);
            }
        }
        return new Hierarchy(Lists.<FormClass>newArrayList(formClasses.values()));
    }

    public Hierarchy(List<FormClass> rangeFormClasses) {

        // Find all of the form class here
        for(FormClass formClass : rangeFormClasses) {
            if(!levelMap.containsKey(formClass.getId())) {
                levelMap.put(formClass.getId(), new Level(formClass));
            }
        }

        // Assign parents...
        for(Level level : levelMap.values()) {
            if(!level.isRoot()) {
                level.parent = levelMap.get(level.parentFormId);
                level.parent.children.add(level);
            }
        }

        // find roots
        for(Level level : levelMap.values()) {
            if(level.isRoot()) {
                roots.add(level);
            }
        }

        // breadth first search to establish presentation order
        establishPresentationOrder(roots);
    }

    public List<Level> getRoots() {
        return roots;
    }


    /**
     * @return  the Level associated with the given {@code formClassId}
     */
    public Level getLevel(ResourceId formClassId) {
        return levelMap.get(formClassId);
    }

    private void establishPresentationOrder(List<Level> parents) {
        for(Level child : parents) {
            this.levels.add(child);
            establishPresentationOrder(child.children);
        }
    }

    /**
     *
     * @return a list of levels in this tree, in topologically sorted order
     */
    public List<Level> getLevels() {
        return levels;
    }

    public boolean hasLevel(ResourceId classId) {
        return levelMap.containsKey(classId);
    }

}
