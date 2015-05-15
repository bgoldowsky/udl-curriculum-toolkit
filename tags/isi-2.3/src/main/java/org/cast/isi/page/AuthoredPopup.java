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

import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.cast.cwm.components.ShyLabel;
import org.cast.cwm.xml.XmlSection;
import org.cast.cwm.xml.XmlSectionModel;
import org.cast.cwm.xml.transform.FilterElements;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISIXmlComponent;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
public class AuthoredPopup extends ISIBasePage {
	
	private static final long serialVersionUID = 1L;
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
		add(new Label("pageTitle", ISIApplication.get().getPageTitleBase() + " :: " + (new StringResourceModel("AuthoredPopup.pageTitle", this, null, "Shared Content").getString())));

		// get the content based on the id and the msection
		ISIXmlComponent xmlContent = new ISIXmlComponent("xmlContent", mSection, "student");
		String object = "//*[@id='" + xmlId + "']";
		xmlContent.setTransformParameter(FilterElements.XPATH, object);
		add(xmlContent);
				
		String popupTitle = getPopupTitle();
		add(new ShyLabel("popupTitle", new Model<String>(popupTitle)));
		add(ISIApplication.get().getToolbar("tht", this));
	}	

	/**
	 * Get the title for the popup based on the content retrieved.
	 * @return
	 */
	private String getPopupTitle() {
		// get the element by xmlId
		Element xmlElement = mSection.getObject().getElement().getOwnerDocument().getElementById(xmlId);
		
		// check for a title attribute
		if (xmlElement.getAttributeNS(null, "title") != null  && !xmlElement.getAttributeNS(null, "title").isEmpty()) {
			return xmlElement.getAttributeNS(null, "title");
		}
				
		// check if there is a header (h1-h9) or a bridgehead
		NodeList items = xmlElement.getChildNodes();
		 for (int i=0; i < items.getLength(); i++) {
			 Node node = (Node) items.item(i);
			 if ((node.getNodeName().toLowerCase().matches("h[0-9]") && node.getNodeName().length() == 2) ||
					 (node.getNodeName().toLowerCase().equals("bridgehead"))) {
				 if (node.getTextContent() != null && !node.getTextContent().isEmpty()) {
					 return node.getTextContent();
				 }
			 }
		 }	
	
		 // fall back to the title of page
		XmlSection section = mSection.getObject();
		return section.getTitle();
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
		return param.get("callingPageDetail").toString();
	}

	@Override
	public String getPageViewDetail() {
		return xmlId;
	}
}