/*
 * Copyright 2011 CAST, Inc.
 *
 * This file is part of the CAST Wicket Modules:
 * see <http://code.google.com/p/cast-wicket-modules>.
 *
 * The CAST Wicket Modules are free software: you can redistribute and/or
 * modify them under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * The CAST Wicket Modules are distributed in the hope that they will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cast.isi.page;

import lombok.Getter;
import lombok.Setter;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.cast.cwm.xml.XmlDocument;
import org.cast.cwm.xml.XmlSection;
import org.cast.cwm.xml.XmlSectionModel;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISIXmlComponent;
import org.cast.isi.data.ContentLoc;

public class LongDescription extends ISIBasePage {
	
	private String imageId;
	@Getter @Setter protected ContentLoc loc;
	
	/** 
	 * Long Description popup window for a given page and image ID.
	 * @param parameters
	 */
	public LongDescription(final PageParameters parameters) {
		super(parameters);
		imageId = parameters.getString("img");
		loc = new ContentLoc(parameters.getString("loc"));
		
		add(new Label("pageTitle", ISIApplication.get().getPageTitleBase() + " :: Image Description"));

		XmlSection ld = null;
		if (parameters.containsKey("text")) {
			add (new Label("text", parameters.getString("text")));
		} else if (loc != null) {
			XmlDocument content = loc.getSection().getXmlDocument();
		   	if (content != null)
		   		ld = content.getLongDescSection(imageId);
	    	if (ld != null)
	    		add (new ISIXmlComponent("text", new XmlSectionModel(ld), "student"));
	    	else
				add (new Label("text", "No image description found"));
		}
	}
	
	@Override
	public String getPageType() {
		return "longdesc";
	}
	
	@Override
	public String getPageName() {
		return getPage().getPageParameters().getString("loc");
		
	}
	@Override
	public String getPageViewDetail() {
		return imageId;
	}

}
