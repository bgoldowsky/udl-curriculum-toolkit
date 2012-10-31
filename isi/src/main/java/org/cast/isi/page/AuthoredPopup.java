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

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.cast.cwm.xml.XmlSectionModel;
import org.cast.cwm.xml.transform.FilterElements;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISIXmlComponent;
public class AuthoredPopup extends ISIBasePage {
	
	protected String xmlId;
	protected XmlSectionModel mSection;	
	protected PageParameters param;

	/**
	 * Popup window that displays an author specified xml content based on a supplied xml id
	 * 
	 * @param _param - used to specify calling page details
	 * @param _xmlId - the id within the xml file
	 * @param _mSection - the section model
	 */
	public AuthoredPopup(PageParameters _param, String _xmlId, XmlSectionModel _mSection) {
		super(_param);
		this.xmlId = _xmlId;
		this.mSection = _mSection;
		this.param = _param;
		add(new Label("pageTitle", ISIApplication.get().getPageTitleBase() + " :: " + "SOME TEXT?"));

		ISIXmlComponent xmlContent = new ISIXmlComponent("xmlContent", mSection, "student");
		String object = "//*[@id='" + xmlId + "']";
		xmlContent.setTransformParameter(FilterElements.XPATH, object);
		add(xmlContent);

		add(ISIApplication.get().getToolbar("tht", this));
	}	

	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
		renderThemeCSS(response, "css/window.css");
		renderThemeCSS(response, "css/window_print.css", "print");
	}
	
	@Override
	public String getPageType() {
		return "authoredpopup";
	}

	@Override
	public String getPageName() {
		return param.getString("callingPageDetail");
	}

	@Override
	public String getPageViewDetail() {
		return xmlId;
	}
}