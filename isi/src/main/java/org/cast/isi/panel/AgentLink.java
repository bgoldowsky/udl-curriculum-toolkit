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
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.cast.cwm.service.EventService;
import org.cast.isi.page.ISIBasePage;

public class AgentLink extends SidebarDialog {
	private static final long serialVersionUID = 1L;
	private String responseAreaId;
	private String title;

	public AgentLink(String id, final String title, final String responseAreaId) {
		super(id, title, null);
		
		this.responseAreaId = responseAreaId;
		this.title = title;
		
		WebMarkupContainer link = new WebMarkupContainer("link");
		link.add(this.getClickToOpenBehavior());
		link.setOutputMarkupId(true);
		add(link);
		this.setVerticalReferencePointId(link.getMarkupId()); // Make dialog come up alongside the button.
		
		link.add (new Label("title", title));
	}
	
	@Override
	protected void logOpenEvent(AjaxRequestTarget target) {
		EventService.get().saveEvent("dialog:agent", title + ":" + responseAreaId, ((ISIBasePage) getPage()).getPageName());
	}
}