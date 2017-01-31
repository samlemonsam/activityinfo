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
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * @author yuriyz on 2/12/14.
 */
public interface ImageResources extends ClientBundle {

    public static final ImageResources ICONS = (ImageResources) GWT.create(ImageResources.class);

    @Source("add.png")
    ImageResource add();

    @Source("delete.png")
    ImageResource delete();

    @Source("edit.png")
    ImageResource edit();

    @Source("arrow_merge.png")
    ImageResource editBulk();

    @Source("undo.png")
    ImageResource undo();

    @Source("undo32.png")
    ImageResource undo32();

    @Source("redo32.png")
    ImageResource redo32();

    @Source("redo.png")
    ImageResource redo();

    @Source("up.png")
    ImageResource up();

    @Source("down.png")
    ImageResource down();

    @Source("add_folder.png")
    ImageResource addFolder();

    @Source("add_folder32.png")
    ImageResource addFolder32();

    @Source("progress.gif")
    ImageResource progress();

    @Source("cog.png")
    ImageResource configure();

    @Source("filter.png")
    ImageResource filter();

    @Source("points2.png")
    ImageResource moveLeft();

    @Source("rappels2.png")
    ImageResource moveRight();

}
