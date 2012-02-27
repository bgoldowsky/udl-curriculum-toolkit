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
package org.cast.isi.component;

import lombok.Getter;
import lombok.Setter;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.model.IModel;

/**
 * A simple radio button.  It can be told whether it is the correct
 * answer.
 * 
 * @author jbrookover
 */
public class SingleSelectItem extends WebMarkupContainer {

	private static final long serialVersionUID = 1L;
	
	@Getter @Setter private boolean correct = false;
	
	public SingleSelectItem(String id, IModel<String> model) {
		this(id, model, false);
	}

	public SingleSelectItem(String id, IModel<String> model, boolean correct) {
		super(id, model);
		this.correct = correct;
		Radio<String> radio = new Radio<String>("radio", model);
		add(radio);
		add(new FormComponentLabel("label", radio));
		
	}

	/**
	 * Is this item currently selected?
	 * @return true if so
	 */
	public boolean isSelected() {
		@SuppressWarnings("unchecked")
		Radio<String> radio = (Radio<String>) get("radio");
		return (radio.getDefaultModelObject().equals(findParent(RadioGroup.class).getDefaultModelObject()));
	}
	
	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		tag.remove("correct"); // Remove correct attribute designated by XML
	}
	
	@SuppressWarnings("unchecked")
	public IModel<String> getModel() {
		return (IModel<String>) getDefaultModel();
	}
	
}
