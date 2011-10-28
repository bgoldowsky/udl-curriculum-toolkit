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

import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

public class SlideShowComponent extends WebMarkupContainer implements IHeaderContributor{
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(SlideShowComponent.class);
	
	protected String slideShowId;
	protected Element elt;

	public SlideShowComponent(String id, Element elt) {
		super(id);
		this.slideShowId = elt.getAttribute("id");
		log.debug("ADDING THE SLIDESHOW WITH ID = {}", slideShowId);
	}

	public void renderHead(IHeaderResponse response) {
		response.renderJavascriptReference(new ResourceReference("/js/jquery/jquery.seqSlideshow.js"));
		// The js call that will set up the annotated image.  The hotSpotString contains all the information
		// needed for the annotated image.
		response.renderOnDomReadyJavascript("{$(\"#" + slideShowId + "\").seqSlideshow();}");
		log.debug("RUNNING THE JS NEEDED FOR THE SLIDESHOW ID = {}", slideShowId);
	}

}
