package org.activityinfo.ui.client.page.config.mvp;

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

import org.activityinfo.legacy.shared.model.DTO;

import java.util.List;

/*
 * M: the model to perform C/U/D actions upon
 * L: a List of models to display in a list
 * P: the parent model containing the list of models
 */
@Deprecated
public interface ListView<M extends DTO, P extends DTO> extends View<M> {

    public void setParent(P parent);

    public void setItems(List<M> items);
}
