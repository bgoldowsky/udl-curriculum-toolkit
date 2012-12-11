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

import lombok.Setter;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.link.Link;
import org.cast.cwm.xml.XmlSectionModel;
import org.cast.isi.ISIApplication;
import org.cast.isi.page.AuthoredPopup;
import org.cast.isi.page.ISIStandardPage;
/**
 * An Link which when clicked creates a {@link AuthoredPopup} in a new window
 */
public class AuthoredPopupLink extends Link<Void> {
	private static final long serialVersionUID = 1L;
	
	@Setter
	private String xmlId;	
	
	@Setter
	private XmlSectionModel mSection;


	public AuthoredPopupLink(String id, String _xmlId, XmlSectionModel _mSection) {
		super(id);
		this.xmlId = _xmlId;
		this.mSection = _mSection;
		this.setPopupSettings(ISIApplication.authoredPopupSettings);
	}

	@Override
	public void onClick() {
		PageParameters pageParameters = new PageParameters();
		String callingPageDetail = ((ISIStandardPage)getPage()).getPageName();
		pageParameters.add("callingPageDetail", callingPageDetail);
		AuthoredPopup authoredPopup = new AuthoredPopup(pageParameters, xmlId, mSection);
		setResponsePage(authoredPopup);
	}
}