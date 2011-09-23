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

import net.databinder.auth.hib.AuthDataSession;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.StringResourceModel;
import org.cast.cwm.data.User;
import org.cast.isi.ISIApplication;

/**
 * Basic Header Panel for ISI Applications.  Subclasses
 * implement {@link #addButtons()} to add their application
 * specific set of buttons.
 * 
 * This panel is constructed in {@link ISIApplication#getHeaderPanel(String)}.
 * 
 * @author jbrookover
 *
 */
public abstract class HeaderPanel extends ISIPanel {

	private static final long serialVersionUID = 1L;

	public HeaderPanel(String id) {
		super(id);
		addCommonComponents();
		addButtons();
	}

	protected void addCommonComponents() {
		add(new Label("applicationTitle", new StringResourceModel("applicationTitle", this, null)));
		add(new Label("applicationSubTitle", new StringResourceModel("applicationSubTitle", this, null)));
		
		User user = (User) AuthDataSession.get().getUser();
		add(new Label("userName", (user == null ? new StringResourceModel("unknownUserName", this, null).getString() : user.getFullName())));			

		add(new ISIApplication.LogoutLink("logoutLink"));
	}
	
	public abstract void addButtons();

}