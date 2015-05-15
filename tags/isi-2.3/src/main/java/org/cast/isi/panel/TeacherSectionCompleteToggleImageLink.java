/*
 * Copyright 2011-2015 CAST, Inc.
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
import org.cast.cwm.data.User;
import org.cast.cwm.xml.XmlSection;
import org.cast.isi.data.SectionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeacherSectionCompleteToggleImageLink extends SectionCompleteToggleImageLink {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(TeacherSectionCompleteToggleImageLink.class);

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param id wicket id
	 * @param model a model containing the section to be checked/toggled
	 */
	public TeacherSectionCompleteToggleImageLink(String id, IModel<XmlSection> model) {
		super(id, model);
	}
	
	/**
	 * Constructor
	 * 
	 * @param id wicket id
	 * @param model a model containing the section to be checked/toggled
	 * @param targetUserModel the user to be marked/unmarked as completing the section
	 */
	public TeacherSectionCompleteToggleImageLink(String id,
			IModel<XmlSection> model, IModel<User> targetUserModel) {
		super(id, model, targetUserModel);
	}

	@Override
	protected void handleClick() {
		User student = getUser();
		boolean newState = !isComplete();
		sectionService.setReviewed(student, sectionContentLocation, newState);
		if (isLockResponse())
			sectionService.setLocked(student, sectionContentLocation, newState);
	}

	
	// in this case isComplete means the teacher has reviewed
	protected boolean isComplete() {
		SectionStatus status = sectionService.getSectionStatus(getUser(), getLocation());			
		if (status == null)
			return false;
		return status.getReviewed();
	}	
}