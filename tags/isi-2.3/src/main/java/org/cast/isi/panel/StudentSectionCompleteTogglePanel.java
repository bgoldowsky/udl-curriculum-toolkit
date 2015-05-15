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
import org.cast.cwm.xml.XmlSectionModel;
import org.cast.isi.data.SectionStatus;

public class StudentSectionCompleteTogglePanel extends SectionCompleteTogglePanel {

	private static final long serialVersionUID = 1L;

	public StudentSectionCompleteTogglePanel(String id,
			XmlSectionModel mSection, IModel<User> mTargetUser) {
		super(id, mSection, mTargetUser);
	}

	@Override
	protected void addSectionToggleButton() {
		add(new StudentSectionCompleteToggleTextLink("toggleComplete", mSection, mTargetUser));
	}

	@Override
	protected String getInstructions() {
		if (isLocked())
			return getStringResource("isi.sectionToggleLinks.instructions.locked","");
		else if (isComplete())
			return getStringResource("isi.sectionToggleLinks.instructions.markIncomplete","");
		else 
			return getStringResource("isi.sectionToggleLinks.instructions.markComplete","");
	}

	protected boolean isLocked() {
		SectionStatus status = sectionService.getSectionStatus(getUser(), sectionContentLocation);			
		if (status == null)
			return false;
		return status.getLocked();
	}

	@Override
	protected boolean showCompletedImage() {
		return isComplete();
	}
	
}
