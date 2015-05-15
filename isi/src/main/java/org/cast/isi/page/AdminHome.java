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
package org.cast.isi.page;

import java.util.Map;

import org.apache.wicket.Page;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.cast.cwm.CwmSession;
import org.cast.cwm.data.Role;
import org.cast.isi.ISIApplication;

@AuthorizeInstantiation("RESEARCHER")
public class AdminHome extends org.cast.cwm.admin.AdminHome {

	private static final long serialVersionUID = 1L;

	public AdminHome(PageParameters parameters) {
		super(parameters);
	}

	@Override
	protected Map<String,Class<? extends Page>> getHomePageLinkMap() {
		Map<String, Class<? extends Page>> map = super.getHomePageLinkMap();
		if (CwmSession.get().getUser().getRole().equals(Role.RESEARCHER)) {
			map.put("Teacher Interface", ISIApplication.get().getTocPageClass(Role.TEACHER));
		}
		return map;
	}

}
