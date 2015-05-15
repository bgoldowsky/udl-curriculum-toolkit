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

import lombok.Getter;
import lombok.Setter;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.cast.cwm.data.component.DialogBorder;

/**
 * A dialog panel that is shown in the right column of the page, 
 * aligned vertically with the button that was pressed to open it.
 *  
 * @author bgoldowsky
 *
 */
public class SidebarDialog extends DialogBorder {

	private static final long serialVersionUID = 1L;
	
	@Getter @Setter
	protected String verticalReferencePointId;
	
	/**
	 * Construct.
	 * 
	 * @param id wicket ID
	 * @param model model
	 * @param verticalReferencePointId the markupId of the element to be used for vertical alignment
	 */
	public SidebarDialog(String id, String title, String verticalReferenceId) {
		this(id, new Model<String>(title), verticalReferenceId);
	}
	
	public SidebarDialog (String id, IModel<String> mTitle, String verticalReferenceId) {
		super(id, mTitle);
		this.verticalReferencePointId = verticalReferenceId;
		this.setMasking(false);
		this.setShowMoveLink(true);
	}

	@Override
	protected String getPositioningString() {
		if (verticalReferencePointId != null)
			return "matchVerticalPosition('" + verticalReferencePointId + "', \'" + contentContainer.getMarkupId() + "\');";
		else
			return "";
	}

}
