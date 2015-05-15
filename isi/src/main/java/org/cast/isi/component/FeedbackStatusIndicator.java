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
package org.cast.isi.component;

import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.cast.cwm.components.Icon;
import org.cast.isi.panel.ResponseFeedbackButtonPanel;
/**
 * 
 * Determine if there is feedback within this toggle area.  Show indicator
 * based on the priority of the feedback (red first, then gray).  
 *
 */
public class FeedbackStatusIndicator extends Panel implements IDisplayFeedbackStatus {
	private static final long serialVersionUID = 1L;

	public FeedbackStatusIndicator(String id) {
		super(id);
		setOutputMarkupPlaceholderTag(true);
	}

	@Override
	protected void onBeforeRender() {
		ResponseFeedbackButtonPanelVisitor responseVisitor = new ResponseFeedbackButtonPanelVisitor();
		getParent().visitChildren(ResponseFeedbackButtonPanel.class, responseVisitor);
		if (responseVisitor.state == null) {
			addOrReplace(new EmptyPanel("feedbackStatusIcon").setVisible(false));
		} else if (responseVisitor.state.equals("old")) {
			addOrReplace(new Icon("feedbackStatusIcon", "img/icons/envelope_large_old.png", 
					new ResourceModel("feedback.indicatorText.previousFeedback.alt").getObject(), 
					new ResourceModel("feedback.indicatorText.previousFeedback.title").getObject()));
		} else {
			addOrReplace(new Icon("feedbackStatusIcon", "img/icons/envelope_large_new.png", 
					new ResourceModel("feedback.indicatorText.newFeedback.alt").getObject(), 
					new ResourceModel("feedback.indicatorText.newFeedback.title").getObject()));
		}
		super.onBeforeRender();
	}	
}