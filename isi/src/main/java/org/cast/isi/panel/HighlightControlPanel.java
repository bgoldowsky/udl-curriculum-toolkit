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

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.component.highlight.HighlightDisplayPanel;
import org.cast.cwm.service.HighlightService;
import org.cast.cwm.service.HighlightService.HighlightType;
import org.cast.cwm.service.IHighlightService;
import org.cast.cwm.xml.XmlSectionModel;
import org.cast.isi.ISISession;
import org.cast.isi.data.ContentLoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class HighlightControlPanel extends Panel {
	
	protected static final Logger log = LoggerFactory.getLogger(HighlightControlPanel.class);
	private static final long serialVersionUID = 1L;
	protected boolean isTeacher;
	protected ContentLoc loc;
	
	public HighlightControlPanel(String id, ContentLoc loc, XmlSectionModel mSection) {
		super(id);
		this.loc = loc;
		setMarkupId(IHighlightService.GLOBAL_CONTROL_ID);
		setOutputMarkupId(true);

		isTeacher = ISISession.get().getUser().getRole().subsumes(Role.TEACHER);

		for (HighlightType type : HighlightService.get().getHighlighters()) {
			add(new HighlightController(type.getColor().toString(), type, loc, mSection));
		}
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