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

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;
import org.cast.cwm.components.ClassAttributeModifier;
import org.cast.cwm.data.component.DialogBorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * A hotSpotComponent is added to an image.  It is a rectangle with defined shape and position.
 * Clicking this hotSpot will pop up a modal with additional detail related to the larger image 
 * that is being annotated (child html is found in the <annotated> tag).  This Component works 
 * in conjunction with @AnnotatedImageComponent@.
 * 
 * @author lynnmccormack
 *
 */
public class HotSpotComponent extends DialogBorder {
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(HotSpotComponent.class);

	protected String hotSpotId = "";
	protected String xmlId = "";
	protected String top = "";
	protected String left = "";
	protected String width = "";
	protected String height = "";
	protected String imgSrc = "";
	protected String title = ""; 

	/**
	 * @param id
	 * @param elt
	 */
	public HotSpotComponent(String id, Element elt) {
		super(id, new Model<String>(elt.getAttributeNS(null,"title")));

		this.hotSpotId = getContentContainer().getMarkupId();
		this.xmlId = elt.getAttributeNS(null,"id");
		this.top = (elt.getAttributeNS(null,"top").trim().equals("") ? "0" : elt.getAttributeNS(null,"top"));
		this.left = elt.getAttributeNS(null,"left");
		this.width = elt.getAttributeNS(null,"width");
		this.height = elt.getAttributeNS(null,"height");
		this.title = elt.getAttributeNS(null,"title");
		this.imgSrc = elt.getAttributeNS(null,"imgSrc");
		
		this.setShowMoveLink(true);

		// setup needed to return focus to calling hotspot; id for hotspot
		// link is created in js
		this.setFocusOverrideSelector("#" + hotSpotId + "_hs");
	}

	@Override
	protected void onInitialize() {
		AnnotatedImageComponent container = findParent(AnnotatedImageComponent.class);
		if (container == null)
			throw new IllegalStateException("each hotspot must have an Annotated Image ancestor");

		// add the appropriate css class to the modal
		WebMarkupContainer contentContainer = this.getContentContainer();
		contentContainer.add(new ClassAttributeModifier("modalSize_hotspot"));
		
		super.onInitialize();
	}

}