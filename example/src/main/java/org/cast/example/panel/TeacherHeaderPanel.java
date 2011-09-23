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
package org.cast.example.panel;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.cast.cwm.components.ClassAttributeModifier;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISISession;

public class TeacherHeaderPanel extends ExampleHeaderPanel {

	private static final long serialVersionUID = 1L;

	public TeacherHeaderPanel(String id) {
		super(id);

		BookmarkablePageLink<Void> manageClassesLink = new BookmarkablePageLink<Void>("manageClassesLink", ISIApplication.get().getManageClassesPageClass());
		ISIApplication.get().setLinkProperties(manageClassesLink);
		add(manageClassesLink);
		
		if (ISISession.get().getStudentModel() == null) {
			setTeacherLinks();
		}
	}
	
	@Override
	public void onBeforeRender() {
				
		Class<? extends Page> pageClass = getPage().getClass();
		if (ISIApplication.get().getManageClassesPageClass().isAssignableFrom(pageClass)) {
			WebMarkupContainer link = (WebMarkupContainer) get("manageClassesLink");
			link.add(new ClassAttributeModifier("current"));
		}
		
		super.onBeforeRender();
	}
	
	public void setTeacherLinks() {
		// teacher links for notebook whiteboard and glossary should be turned off and disabled if there is no student selected
		super.glossaryLink.setEnabled(false);
		super.notebookLink.setEnabled(false);
		super.whiteboardLink.setEnabled(false);
		
		super.glossaryLink.add(new ClassAttributeModifier("off")); 
		super.notebookLink.add(new ClassAttributeModifier("off")); 
		super.whiteboardLink.add(new ClassAttributeModifier("off")); 
	}

}
