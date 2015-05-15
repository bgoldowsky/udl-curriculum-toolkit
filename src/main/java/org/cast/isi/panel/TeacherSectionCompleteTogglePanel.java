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


public class TeacherSectionCompleteTogglePanel extends SectionCompleteTogglePanel {

	private static final long serialVersionUID = 1L;

	public TeacherSectionCompleteTogglePanel(String id,
			XmlSectionModel mSection, IModel<User> mTargetUser) {
		super(id, mSection, mTargetUser);
	}

	@Override
	protected void addSectionToggleButton() {
		add(new TeacherSectionCompleteToggleTextLink("toggleComplete", mSection, mTargetUser));
	}

	@Override
	protected String getInstructions() {
		if (isReviewed())
			return getStringResource("isi.sectionToggleLinks.instructions.markNotReviewed", "Mark Section Not Reviewed");
		else if (isComplete())
			return getStringResource("isi.sectionToggleLinks.instructions.markReviewed", "Mark Section Reviewed");
		else 
			return getStringResource("isi.sectionToggleLinks.instructions.incomplete", "Incomplete Student Work");
	}

	protected boolean isReviewed() {
		SectionStatus status = sectionService.getSectionStatus(getUser(), sectionContentLocation);			
		if (status == null)
			return false;
		return status.getReviewed();
	}

	@Override
	protected boolean showCompletedImage() {
		return isReviewed();
	}

}
