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

import org.apache.wicket.Component;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.User;
import org.cast.cwm.xml.XmlSection;


public abstract class SectionCompleteToggleImageLink extends SectionCompleteToggleLink {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param id wicket id
	 * @param model a model containing the section to be checked/toggled
	 * @param targetUserModel the user to be marked/unmarked as completing the section
	 */
	public SectionCompleteToggleImageLink(String id,
			IModel<XmlSection> model, IModel<User> targetUserModel) {
		super(id, model, targetUserModel);
	}

	/**
	 * Constructor
	 * 
	 * @param id wicket id
	 * @param model a model containing the section to be checked/toggled
	 */
	public SectionCompleteToggleImageLink(String id, IModel<XmlSection> model) {
		super(id, model);
	}

	@Override
	public void onBeforeRender() {
		addOrReplace(getImage());
		super.onBeforeRender();
	}

	protected Image getImage() {
		if (isComplete())
			return new DoneImage("doneImg");
		else return new NotDoneImage("doneImg");
	}

	private void addAttribute(Component component, String name, String value) {
		component.add(new SimpleAttributeModifier(name, value));
	}

	public class DoneImage extends Image {

		private static final long serialVersionUID = 1L;

		public DoneImage(String id) {
			super(id, "/img/icons/check_done.png");
			addAttribute(this, "alt", "Finished");
			addAttribute(this, "title", "Finished");
		}

	}

	public class NotDoneImage extends Image {

		private static final long serialVersionUID = 1L;

		public NotDoneImage(String id) {
			super(id, "/img/icons/check_notdone.png");
			addAttribute(this, "alt", "Not Finished");
			addAttribute(this, "title", "Not Finished");
		}

	}


}