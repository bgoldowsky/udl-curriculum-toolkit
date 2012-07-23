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
package org.cast.example;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.cast.cwm.CwmSession;
import org.cast.cwm.data.Role;
import org.cast.cwm.xml.XmlSection;
import org.cast.isi.ISIApplication;
import org.cast.isi.page.AdminHome;
import org.cast.isi.page.ISIStandardPage;
import org.cast.isi.page.Reading;
import org.cast.isi.page.TeacherReading;
import org.cast.isi.panel.AbstractNavBar;
import org.cast.isi.panel.FooterPanel;
import org.cast.isi.panel.HeaderPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleApplication extends ISIApplication {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ISIApplication.class);
	
	
	@Override
	protected void init() {
		super.init();

	}

	/** Return the class of page that should be used to display a given XmlSection.
	 *  If it is not a valid page, return null.
	 * 
	 * Rules for this application: a level2, 3, or 4 element can be a page.
	 * If the level2 or 3 has children, then those will be the pages and the 
	 * parent is not.
	 * 
	 * @param sec an ISIXmlSection
	 * @return A subclass of ISIPage.
	 */
	@Override
	public Class<? extends ISIStandardPage> getReadingPageClass() {
		if (Role.TEACHER.equals(CwmSession.get().getUser().getRole()))
			return TeacherReading.class;
		else
			return Reading.class;
	}

	/* (non-Javadoc)
	 * @see org.cast.isi.ISIApplication#getPageTitleBase()
	 */
	@Override
	public String getPageTitleBase() {
		return (new StringResourceModel("pageTitleBase", new Model<String>("pageTitleBase"), null, "Example")).getString();
	}

	@Override
	public Class<? extends WebPage> getHomePage(Role role) {
		if (role.equals(Role.ADMIN) || role.equals(Role.RESEARCHER)) 
			return AdminHome.class;
		if (role.equals(Role.TEACHER) || role.equals(Role.STUDENT))
			return getTocPageClass(role);
		return getSignInPageClass();
	}

	@Override
	public String getAppId() {
		return (new StringResourceModel("applicationId", new Model<String>("applicationId"), null, "ExampleApplication")).getString();
	}
}