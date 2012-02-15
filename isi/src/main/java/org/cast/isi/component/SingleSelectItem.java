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

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.model.IModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple radio button.  It can be told whether it is the correct
 * answer.
 * 
 * @author jbrookover
 */
public class SingleSelectItem extends Radio<String> {

	private static final long serialVersionUID = 1L;
	@Getter @Setter private boolean correct = false;
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(SingleSelectItem.class);
	
	
	public SingleSelectItem(String id, IModel<String> model) {
		this(id, model, false);
	}

	public SingleSelectItem(String id, IModel<String> model, boolean correct) {
		super(id, model);
		this.correct = correct;
	}
	
	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		tag.remove("correct"); // Remove correct attribute designated by XML
	}
	
	/**
	 * A simple container that adds a 'for' attribute with the value
	 * of the accompanying {@link SingleSelectItem} and sets its model
	 * object to the same..
	 * 
	 */
	public static class SingleSelectItemLabel extends WebMarkupContainer {

		private static final long serialVersionUID = 1L;
		private String label;

		/**
		 * A simple container that adds a 'for' attribute with the value
		 * of the accompanying {@link SingleSelectItem} and sets its model
		 * object to the same.
		 * 
		 * @param id wicket id
		 * @param label the actual label (to set the model object of the radio button)
		 */
		public SingleSelectItemLabel(String id, String label) {
			super(id);
			this.label = label;
		}
		
		@Override
		protected void onBeforeRender() {
			super.onBeforeRender();
			String radioId = "selectItem_" + getId().substring("selectItemLabel_".length());
			SingleSelectItem radio = (SingleSelectItem) getParent().get(radioId); // Associated Radio button
			add(new SimpleAttributeModifier("for", radio.getMarkupId())); // Associate this label with Radio button
			radio.setModelObject(label); // Set modelObject of Radio button to this label.
		}
	}
}
