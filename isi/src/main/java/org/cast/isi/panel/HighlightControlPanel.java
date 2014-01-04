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

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeAction;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeActions;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.cast.cwm.data.User;
import org.cast.cwm.data.component.highlight.HighlightDisplayPanel;
import org.cast.cwm.service.HighlightService.HighlightType;
import org.cast.cwm.service.ICwmSessionService;
import org.cast.cwm.service.IHighlightService;
import org.cast.cwm.service.IUserPreferenceService;
import org.cast.cwm.xml.XmlSectionModel;
import org.cast.isi.behavior.HighlightStateChangeBehavior;
import org.cast.isi.data.ContentLoc;

import com.google.inject.Inject;

/**
 * A page-level control panel for highlights.  The basic controls are handled
 * via javascript.  The bulk of this panel is actually support content and
 * the ability to edit an application wide "Highlight Label."
 * 
 * The highlighting javascript requires two other components: a {@link HighlightDisplayPanel},
 * which includes hidden form fields to transmit data about highlights, and a 
 * {@link NoHighlightsModal} modal message which needs to be attached
 * as a direct child of the page.  This panel will check for both in onBeforeRender
 * and raise an error if they are not found.
 * 
 * @author jbrookover
 *
 */
@AuthorizeActions(actions = { @AuthorizeAction(action="RENDER", roles={"STUDENT"})})
public class HighlightControlPanel extends Panel {
	
	private static final long serialVersionUID = 1L;

	@Inject
	protected IHighlightService highlightService;

	@Inject
	protected IUserPreferenceService preferenceService;
	
	@Inject
	protected ICwmSessionService cwmSessionService;

	public HighlightControlPanel(String id, ContentLoc loc, XmlSectionModel mSection) {
		super(id);
		setMarkupId(IHighlightService.GLOBAL_CONTROL_ID);
		setOutputMarkupId(true);
		
		IModel<User> mUser = cwmSessionService.getUserModel();
		Boolean prefValue = preferenceService.getUserPreferenceBoolean(mUser, "highlightOn");
		boolean highlightOn = (prefValue==null) ? false : prefValue;		
		String highlightColor = "";
		
		// determine if any highlighter is on  - pass ON state into the correct controller below
		if (highlightOn) {
			highlightColor = preferenceService.getUserPreferenceString(mUser, "highlightColor");
		}

		HighlightController highlightController;
		for (HighlightType type : highlightService.getHighlighters()) {
			highlightController = new HighlightController(type.getColor().toString(), type, loc, mSection);
			add(highlightController);
			if (highlightOn && highlightColor.equals(Character.toString(type.getColor()))) {
				highlightController.setHighlightOn(true);
			}
		}
		
		// add the behavior that will track js side changes to the highlight state
		add(new HighlightStateChangeBehavior());
	}
	
	@Override
	protected void onBeforeRender() {
		Object hdpFound = getPage().visitChildren(HighlightDisplayPanel.class, new IVisitor<HighlightDisplayPanel, Object>() {
            public void component(HighlightDisplayPanel object, IVisit<Object> visit) {
                visit.stop(object);
            }
        });
		
		if (hdpFound == null)
			throw new IllegalStateException("HighlightControlPanel must be on the same page as a HighlightDisplayPanel.");
		
		Object nhpFound = getPage().visitChildren(NoHighlightModal.class, new IVisitor<NoHighlightModal, Object>() {
            public void component(NoHighlightModal object, IVisit<Object> visit) {
                visit.stop(object);
            }
        });

		if (nhpFound == null)
			throw new IllegalStateException("HighlightControlPanel must be on the same page as a NoHighlightModal.");
		
		super.onBeforeRender();
	}

}