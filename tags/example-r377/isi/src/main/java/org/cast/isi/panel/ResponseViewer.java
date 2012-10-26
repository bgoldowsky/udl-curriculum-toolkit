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
package org.cast.isi.panel;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.cast.cwm.components.ClassAttributeModifier;
import org.cast.cwm.components.Icon;
import org.cast.cwm.components.ShyLabel;
import org.cast.cwm.data.Response;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISIDateLabel;
import org.cast.isi.page.ISIStandardPage;

public class ResponseViewer extends org.cast.cwm.data.component.ResponseViewer {

	@Getter @Setter
	private boolean showContentPageLink;
	
	@Getter @Setter
	private boolean showDateTime;
	
	private BookmarkablePageLink<ISIStandardPage> sectionLink;
	private WebComponent sectionIcon;
	private ISIDateLabel date;
	
	private static int MAX_WIDTH = 700;
	private static int MAX_HEIGHT = 700; 
	
	private static final long serialVersionUID = 1L;

	/**
	 * Create a response viewer to view a Response object.  Image responses will be resized to MAX_WIDTH x MAX_HEIGHT.
	 * 
	 * @param id - the wicket Id
	 * @param model - the model wrapping a Response object
	 */
	public ResponseViewer(String id, IModel<? extends Response> model) {
		this(id, model, MAX_WIDTH, MAX_HEIGHT);
	}
	
	/**
	 * Create a response viewer to view a Response object.  Image responses will be resized to fit in the provided dimensions.
	 * 
	 * @param id
	 * @param model - the model wrapping a Response object
	 * @param w - maximum width of an image response
	 * @param h - maximum height of an image response
	 */
	public ResponseViewer(String id, IModel<? extends Response> model, Integer w, Integer h) {
		super(id, model, w, h);
	
		// Title associated with this response
		add(new ShyLabel("responseTitle", new PropertyModel<String>(model, "title")));

		// Last-updated timestamp
		date = new ISIDateLabel("date", new PropertyModel<Date>(model, "lastUpdated"));
		date.setVisible(showDateTime);
		add(date);

		// ContentPage cpage = resp.getOrigPost().getEvent().getContentPage();  //FIXME 
		// If flagged, show the link to the content page associated with this response
		// TODO: Hack to check and see if the content page is not a glossary page.
// FIXME fix these compile errors and bring this back -
//		if (cpage != null && !cpage.getName().startsWith("glossary")) {
//			sectionLink = ISIPage.linkTo("sectionLink",(new ContentLoc(cpage.getName())).getSection());
//			sectionLink.add(new Label("sectionTitle", (new ContentLoc(cpage.getName())).getSection().getSectionAncestor().getTitle()));
//			sectionIcon = ISIApplication.get().iconFor((new ContentLoc(cpage.getName())).getSection().getSectionAncestor(), "_small");
//
//		} else {
			sectionLink = new BookmarkablePageLink<ISIStandardPage>("sectionLink", ISIApplication.get().getHomePage());
			sectionLink.add(new Label("sectionTitle", "Home"));
			sectionIcon = new Icon("icon", "img/icons/home_small.png");
//		}

			sectionLink.add(new ClassAttributeModifier("sectionLink"));
			add(sectionIcon);
			add(sectionLink);

// FIXME -- drawings should be zoomable
//			// Containers for the two images (can add captions in the future)
//			WebMarkupContainer imageThumb = new WebMarkupContainer("imageThumb");
//			WebMarkupContainer imageDetail = new WebMarkupContainer("imageDetail");
//			imageThumb.setOutputMarkupPlaceholderTag(true);
//			imageDetail.setOutputMarkupPlaceholderTag(true);
//
//			fragment.add(imageThumb);
//			fragment.add(imageDetail);
//
//			// Set IDs to a common format
//			String idString = String.valueOf(resp.getId());
//			imageThumb.setMarkupId("image_" + idString);
//			imageDetail.setMarkupId("imageDetail_" + idString);
//
//			// Add Small version of image
//			imageThumb.add(new ScaledNonCachingImage("image", new HibernateObjectModel<ExtendedResponseDatum>(er), maxW, maxH));
//
//			// Add Large version of image
//			imageDetail.add(new ScaledNonCachingImage("image", new HibernateObjectModel<ExtendedResponseDatum>(er), -1, -1));
//
//			// Add expand link
//			imageThumb.add(new WebMarkupContainer("expandButton").add(new SimpleAttributeModifier("onclick", "showImageDetail('" + idString + "', true); return false;")));
//
//			// Add collapse link
//			imageDetail.add(new WebMarkupContainer("collapseButton").add(new SimpleAttributeModifier("onclick", "showImageDetail('" + idString + "', false); return false;")));

	}
	
	public void onConfigure() {
		date.setVisible(showDateTime);
		sectionLink.setVisible(showContentPageLink);
		sectionIcon.setVisible(showContentPageLink);
	}

}