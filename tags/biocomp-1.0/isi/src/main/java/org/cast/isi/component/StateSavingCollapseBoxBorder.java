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

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.model.ResourceModel;
import org.cast.cwm.components.ClassAttributeModifier;
import org.cast.cwm.service.IUserPreferenceService;
import org.cast.isi.ISISession;

import com.google.inject.Inject;

/**
 * A collapse box border that can retrieve and save its state.
 * 
 * @author lynnmccormack
 *
 */
public class StateSavingCollapseBoxBorder extends Border {
	private static final long serialVersionUID = 1L;

	@Inject
	IUserPreferenceService userPreferenceService;

	/**
	 * @param id - wicket id
	 * @param userPreferenceName - unique name for a user preference, stored in db, also connects to "userPreferenceName.title" in properties file
	 * @param collapseBoxId - Needed when the collapse box requires an id for CSS purposes.  The highlighter collapse box requires a specific id.
	 * @param pageName - usually supplied by the page - can be null, used for event logging only
	 */
	public StateSavingCollapseBoxBorder(String id, String userPreferenceName, String collapseBoxId, String pageName) {
		super(id);

		WebMarkupContainer collapseBox = new WebMarkupContainer("collapseBox");
		addToBorder(collapseBox);
		
		// add the id, if it has been supplied
		if (collapseBoxId != null && !collapseBoxId.isEmpty()) {
			collapseBox.setOutputMarkupId(true).setMarkupId(collapseBoxId);
		}
		
		// if the preference exists in the db, add or remove the css class "open"
		Boolean toggleState = userPreferenceService.getUserPreferenceBoolean(ISISession.get().getUserModel(), userPreferenceName);
		if (toggleState != null) {
			collapseBox.add(new ClassAttributeModifier("open", !toggleState));
		}		

		WebMarkupContainer collapseBoxToggle = new WebMarkupContainer("collapseBoxToggle");
		collapseBox.add(collapseBoxToggle);
		
		// add event logging behavior
		collapseBoxToggle.add(new StateSavingCollapseBoxBehavior(userPreferenceName, pageName, userPreferenceName));
		
		// title of collapse box is in the application .properties file
		collapseBoxToggle.add(new Label("collapseBoxLabel", new ResourceModel(userPreferenceName+".title", "default collapse box label")));		
	}
}