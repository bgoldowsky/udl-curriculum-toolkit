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
package org.cast.isi;

import lombok.Getter;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.cast.cwm.indira.FileResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * This image contains annotations or @HotSpotComponent@ children that define the hotSpots that
 * are potentially visible when this image is on a page.  This component works with jquery.annotate.js 
 * to create the hotspots.
 * 
 * @author lynnmccormack
 *
 */
public class AnnotatedImageComponent extends WebMarkupContainer implements IHeaderContributor {
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(AnnotatedImageComponent.class);

	protected Element elt;
	protected StringBuffer hotSpotString;
	protected String annotatedImageComponentId;

	public AnnotatedImageComponent(String id, Element elt) {
		super(id);
		this.elt = elt;
		this.annotatedImageComponentId = elt.getAttribute("annotatedImageId");
	}

	public void renderHead(IHeaderResponse response) {
		response.renderJavascriptReference(new ResourceReference("/js/jquery/jquery-ui-1.8.13.custom.min.js"));
		response.renderJavascriptReference(new ResourceReference("/js/jquery/jquery.annotate.js"));
		response.renderCSSReference(new ResourceReference("/css/annotation.css"));
		// The js call that will set up the annotated image.  The hotSpotString contains all the information
		// needed for the annotated image.
		response.renderOnDomReadyJavascript("{$(\"#" + annotatedImageComponentId + "\").annotateImage({" +
            "editable: false, " +
            "useAjax: false, " +
            "notes: [ " + hotSpotString + "] } )}");
	}
	
	/**
	 * Visits HotSpotComponet children and records the attributes needed to
	 * call the js correctly for this image.  Each child record will create a 
	 * hotspot on the annotated image
	 */
	public class HotSpotVisitor implements IVisitor<HotSpotComponent> {
		
		private static final long serialVersionUID = 1L;
		public StringBuffer hotSpotDetails;
		@Getter private int count = 0;
		
		public HotSpotVisitor(StringBuffer hotSpotDetails) {
			this.hotSpotDetails = hotSpotDetails;
		}

		public Object component(HotSpotComponent component) {
			
			if (count != 0 )
				hotSpotDetails.append(", ");
			
			hotSpotDetails.append("{ \"top\": " + component.top + ", ");
			hotSpotDetails.append("\"left\": " + component.left + ", ");
			hotSpotDetails.append("\"width\": " + component.width + ", ");
			hotSpotDetails.append("\"height\": " + component.height + ", ");
			hotSpotDetails.append("\"text\": " + "\"" + component.title + "\"" + ", ");
	
			if ((component.icon).equals("true")) {
				hotSpotDetails.append("\"icon\": " + "\"" + component.icon + "\"" + ", ");
				hotSpotDetails.append("\"iconImg\": " + "\"" + FileResourceManager.get().getUrl(component.iconImg) + "\"" + ", ");
				hotSpotDetails.append("\"iconClass\": " + "\"" + component.iconClass + "\"" + ", ");
			}
			hotSpotDetails.append("\"id\": " + "\"" + component.hotSpotId + "\"" + " }");
			
			count++;
			
			return CONTINUE_TRAVERSAL;
		} 
		
	}

	/*
	 * this is when the js will get called  
	 */
	@Override
	protected void onBeforeRender() {
		super.onBeforeRender();

		final StringBuffer hotSpots = new StringBuffer("");
		HotSpotVisitor visitor = new HotSpotVisitor(hotSpots);
		visitChildren(HotSpotComponent.class, visitor);
		hotSpotString = visitor.hotSpotDetails;
	}
	
}