/*
 * Copyright 2011 CAST, Inc.
 *
 * This file is part of the UDL Curriculum Toolkit:
 * see <http://code.google.com/p/udl-curriculum-toolkit>.
 *
 * The UDL Curriculum Toolkit is free software: you can redistribute and/or
 * modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * The UDL Curriculum Toolkit is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cast.isi.panel;

import org.apache.wicket.model.IModel;

/**
 * Navigation Panels can extend this, giving them the ability to visit other types of navigation panels 
 * without knowing what is on the page.
 * 
 * @author jbrookover
 *
 */
public abstract class AbstractNavBar<T> extends ISIPanel {

	private static final long serialVersionUID = 1L;

	public AbstractNavBar(String id) {
		super(id);
	}

	public AbstractNavBar(String id, IModel<? extends T> model) {
		super(id, model);
	}
	
	/**
	 * Gets model
	 * 
	 * @return model
	 */
	@SuppressWarnings("unchecked")
	public final IModel<T> getModel()
	{
		return (IModel<T>)getDefaultModel();
	}

	/**
	 * Sets model
	 * 
	 * @param model
	 */
	public final void setModel(IModel<T> model)
	{
		setDefaultModel(model);
	}

	/**
	 * Gets model object
	 * 
	 * @return model object
	 */
	@SuppressWarnings("unchecked")
	public final T getModelObject()
	{
		return (T) getDefaultModelObject();
	}

	/**
	 * Sets model object
	 * 
	 * @param object
	 */
	public final void setModelObject(T object)
	{
		setDefaultModelObject(object);
	}

}
