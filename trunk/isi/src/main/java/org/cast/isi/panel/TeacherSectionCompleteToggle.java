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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.cast.cwm.xml.XmlSection;
import org.cast.isi.data.ContentLoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeacherSectionCompleteToggle extends SectionCompleteToggleComponent {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(TeacherSectionCompleteToggle.class);

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param id wicket id
	 * @param location the xmlSection to be checked/toggled
	 */
	public TeacherSectionCompleteToggle(String id, IModel<XmlSection> mSection) {
		super(id, mSection);
		setOutputMarkupId(true);
	}
	
	@Override
	public void onClick (final AjaxRequestTarget target) {	
		sectionService.setReviewed(getUser(), new ContentLoc(location), !isComplete());
		if (target != null) {
			getPage().visitChildren(TeacherSectionCompleteToggle.class, new IVisitor<TeacherSectionCompleteToggle>() {
				public Object component(TeacherSectionCompleteToggle component) {
					if (getLocation().equals(component.getLocation()))
						target.addComponent(component);
					return CONTINUE_TRAVERSAL;
				}
			});
		}
	}

	
	// in this case isComplete means the teacher has reviewed
	public boolean isComplete() {
		Boolean isComplete = sectionService.sectionIsReviewed(getUser(), location);			
		if (isComplete == null)
			isComplete = false;
		return isComplete;
	}	
}