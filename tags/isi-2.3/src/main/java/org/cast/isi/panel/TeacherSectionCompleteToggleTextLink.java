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

public class TeacherSectionCompleteToggleTextLink extends
		SectionCompleteToggleTextLink {

	private static final long serialVersionUID = 1L;

	public TeacherSectionCompleteToggleTextLink(String id,
			IModel<XmlSection> pageSectionXmlModel, IModel<User> targetUserModel) {
		super(id, pageSectionXmlModel, targetUserModel);
	}
	
	@Override
	public boolean isEnabled() {
		return isComplete();
	}
	
	@Override
	protected String getLabelText() {
		if (isReviewed())
			return getStringResource("isi.sectionToggleLinks.linkText.markNotReviewed", "Mark Section Not Reviewed");
		else if (isComplete())
			return getStringResource("isi.sectionToggleLinks.linkText.markReviewed", "Mark Section Reviewed");
		else 
			return getStringResource("isi.sectionToggleLinks.linkText.incomplete", "Incomplete Student Work");
	}

	@Override
	protected boolean isComplete() {
		SectionStatus status = sectionService.getSectionStatus(getUser(), sectionContentLocation);			
		if (status == null)
			return false;
		return status.getCompleted();
	}
	
	protected boolean isReviewed() {
		SectionStatus status = sectionService.getSectionStatus(getUser(), sectionContentLocation);			
		if (status == null)
			return false;
		return status.getReviewed();
	}
	
	@Override
	protected void handleClick() {
		User student = getUser();
		boolean newState = !isReviewed();
		sectionService.setReviewed(student, sectionContentLocation, newState);
		if (isLockResponse())
			sectionService.setLocked(student, sectionContentLocation, newState);
	}


}
