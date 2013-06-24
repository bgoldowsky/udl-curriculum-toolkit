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

import lombok.Getter;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.cast.cwm.IInputStreamProvider;
import org.cast.cwm.IRelativeLinkSource;
import org.cast.cwm.xml.XmlSectionModel;
import org.cast.isi.page.ISIBasePage;
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
	private static final Logger log = LoggerFactory.getLogger(AnnotatedImageComponent.class);

	protected Element elt;
	protected StringBuffer hotSpotString;
	protected String annotatedImageComponentId;
	protected XmlSectionModel xmlSectionModel;

	public AnnotatedImageComponent(String id, Element elt, XmlSectionModel xmlSectionModel) {
		super(id);
		this.elt = elt;
		this.annotatedImageComponentId = elt.getAttributeNS(null,"annotatedImageId");
		this.xmlSectionModel = xmlSectionModel;
		
	}

	public void renderHead(IHeaderResponse response) {
		// FIXME: this is dependent on the named JS/CSS file being supplied by the application in the expected location.
		ISIBasePage.renderThemeJS(response, "js/jquery/jquery.annotate.js");
		ISIBasePage.renderThemeCSS(response, "css/annotation.css");
		// The js call that will set up the annotated image.  The hotSpotString contains all the information
		// needed for the annotated image.
		response.renderOnLoadJavaScript("{$(\"#" + annotatedImageComponentId + "\").annotateImage({" +
                "viewAnnotations: true, " +
                "editable: false, " +
                "useAjax: false, " +
                "hoverShow: false, " +
                "notes: [ " + hotSpotString + "] } )}");
	}
	
	/**
	 * Visits HotSpotComponet children and records the attributes needed to
	 * call the js correctly for this image.  Each child record will create a 
	 * hotspot on the annotated image
	 */
	public class HotSpotVisitor implements IVisitor<HotSpotComponent, Void> {
		public StringBuffer hotSpotDetails;
		@Getter private int count = 0;
		
		public HotSpotVisitor(StringBuffer hotSpotDetails) {
			this.hotSpotDetails = hotSpotDetails;
		}

        /*
		public Object component(HotSpotComponent component) {
			
			if (count != 0 )
				hotSpotDetails.append(", ");
			
			hotSpotDetails.append("{ \"top\": " + component.top + ", ");
			hotSpotDetails.append("\"left\": " + component.left + ", ");
			hotSpotDetails.append("\"width\": " + component.width + ", ");
			hotSpotDetails.append("\"height\": " + component.height + ", ");
			hotSpotDetails.append("\"text\": " + "\"" + component.title + "\"" + ", ");
	
			if (component.imgSrc != null && !component.imgSrc.trim().equals("")) {
				// find the url of the image
				Resource xmlFile = xmlSectionModel.getObject().getXmlDocument().getXmlFile();  
				ResourceReference imageResourceRef = ((IRelativeLinkSource)xmlFile).getRelativeReference(component.imgSrc);
				String imageUrl = RequestCycle.get().urlFor(imageResourceRef).toString();
				if (imageUrl.equals(null))
						log.warn("The URL for the hotspot image {} is not found", imageUrl);
				
				hotSpotDetails.append("\"useImg\": " + "\"true" + "\"" + ", ");
				hotSpotDetails.append("\"imgSrc\": " + "\"" + imageUrl + "\"" + ", ");
			}
			hotSpotDetails.append("\"xmlId\": " + "\"" + component.xmlId + "\"" + ", ");
			hotSpotDetails.append("\"id\": " + "\"" + component.hotSpotId + "\"" + " }");
			
			count++;
			
			return CONTINUE_TRAVERSAL;
		}

        */
        public void component(HotSpotComponent component, IVisit visit) {
            if (count != 0 )
                hotSpotDetails.append(", ");

            hotSpotDetails.append("{ \"top\": " + component.top + ", ");
            hotSpotDetails.append("\"left\": " + component.left + ", ");
            hotSpotDetails.append("\"width\": " + component.width + ", ");
            hotSpotDetails.append("\"height\": " + component.height + ", ");
            hotSpotDetails.append("\"text\": " + "\"" + component.title + "\"" + ", ");

            if (component.imgSrc != null && !component.imgSrc.trim().equals("")) {
                // find the url of the image
                IInputStreamProvider xmlFile = xmlSectionModel.getObject().getXmlDocument().getXmlFile();
                ResourceReference imageResourceRef = ((IRelativeLinkSource)xmlFile).getRelativeReference(component.imgSrc);
                // heikki TODO: pay attention to this in testing
                String imageUrl = RequestCycle.get().mapUrlFor(imageResourceRef, new PageParameters()).toString();
                if (imageUrl.equals(null)){
                    log.warn("The URL for the hotspot image {} is not found", imageUrl);
                }
                hotSpotDetails.append("\"useImg\": " + "\"true" + "\"" + ", ");
                hotSpotDetails.append("\"imgSrc\": " + "\"" + imageUrl + "\"" + ", ");
            }
            hotSpotDetails.append("\"xmlId\": " + "\"" + component.xmlId + "\"" + ", ");
            hotSpotDetails.append("\"id\": " + "\"" + component.hotSpotId + "\"" + " }");

            count++;

            //return CONTINUE_TRAVERSAL;
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