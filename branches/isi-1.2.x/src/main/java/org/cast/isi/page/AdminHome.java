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
package org.cast.isi.page;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.cast.cwm.CwmSession;
import org.cast.cwm.data.Role;
import org.cast.isi.ISIApplication;

@AuthorizeInstantiation("RESEARCHER")
public class AdminHome extends org.cast.cwm.admin.AdminHome {

	public AdminHome(PageParameters parameters) {
		super(parameters);
	}

	@Override
	protected List<Component> homePageComponents() {
		List<Component> list = super.homePageComponents();
		if (CwmSession.get().getUser().getRole().equals(Role.RESEARCHER)) {
			list.add(new BookmarkablePageLink<ISIStandardPage>("link", ISIApplication.get().getTocPageClass(Role.TEACHER))
					.add(new Label("label", "Teacher Interface")));
		}
		return list;
	}

}
