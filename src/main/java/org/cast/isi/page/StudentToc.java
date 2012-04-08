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

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.cast.cwm.components.ClassAttributeModifier;
import org.cast.cwm.indira.IndiraImage;
import org.cast.cwm.indira.IndiraImageComponent;
import org.cast.cwm.xml.XmlDocument;
import org.cast.cwm.xml.XmlSection;
import org.cast.cwm.xml.XmlSectionModel;
import org.cast.isi.CollapseBoxBehavior;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISISession;
import org.cast.isi.ISITagLinkBuilder;
import org.cast.isi.ISIXmlComponent;
import org.cast.isi.ISIXmlSection;
import org.cast.isi.data.ClassMessage;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.panel.QuickFlipForm;
import org.cast.isi.panel.TagCloudTocPanel;
import org.cast.isi.service.ISIResponseService;
import org.cast.isi.service.SectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AuthorizeInstantiation("STUDENT")
public class StudentToc extends ISIStandardPage {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(StudentToc.class);
	protected List<String> locsWithUnread;
	protected List<String> locsWithMessages;
	
	private int pageNum = 0; // Global page count for entire curriculum
	
	private transient ISIXmlSection currentPage;
	private transient ISIXmlSection currentRootSection;
		
	public StudentToc(PageParameters parameters) {
		super(parameters);

		pageTitle = (new StringResourceModel("StudentTOC.pageTitle", this, null, "Home").getString());
		setPageTitle(pageTitle);
		
		if (loc == null)
			loc = ISIApplication.get().getBookmarkLoc();

		currentPage = (loc == null ? null : loc.getSection());
		if (currentPage != null)
			currentRootSection = ISIXmlSection.getRootSection(currentPage); // "level1"
		
		String sectionLevel = ISIApplication.get().getSectionElement();

		// Jump to a certain page
		add(new QuickFlipForm("quickFlipForm", true));
		
		// Loads the Class Message for this period
		WebMarkupContainer classMessageBox = new WebMarkupContainer("classMessageBox");
		add(classMessageBox);
		ClassMessage m = ISIResponseService.get().getClassMessage(ISISession.get().getCurrentPeriodModel());
		if (m == null) {
			classMessageBox.add(new Label("classMessage", new ResourceModel("classMessage")));
		} else {
			classMessageBox.add(new Label("classMessage", m.getMessage()));
		}
		classMessageBox.setVisible(ISIApplication.get().isClassMessageOn());

		// Loads a list of locations that have Unread Messages and Regular Messages
		locsWithUnread = ISIResponseService.get().getPagesWithNotes(ISISession.get().getUser(), true);
		locsWithMessages = ISIResponseService.get().getPagesWithNotes(ISISession.get().getUser());
		
		WebMarkupContainer tagsBox = new WebMarkupContainer("tagsBox");
		add(tagsBox);
		tagsBox.setVisible(ISIApplication.get().isTagsOn());
		tagsBox.add(new WebMarkupContainer("tagCollapseToggle").add(new CollapseBoxBehavior("onclick", "tagpanel:studenttoc", getPageName())));
		tagsBox.add(new TagCloudTocPanel("tagcloud", getTagLinkBuilder()));
		
		// This is the "Chapter" level
	   	RepeatingView chapterRepeater = new RepeatingView("chapterRepeater");
	   	add(chapterRepeater);
	   	
	   	for (XmlDocument doc : ISIApplication.get().getStudentContent()) { // For each XML document
	   		// FIXME: Calling getLastModified here has the side effect of checking whether the XML
	   		// has been modified, and if so, updating it.  This is mysterious and should be 
	   		// replaced with a more transparent mechanism or at least a better name for the method.
	   		doc.getLastModified();
	   		for (XmlSection rs : doc.getTocSection().getChildren()) { // For each chapter in the document
	   			ISIXmlSection rootSection = (ISIXmlSection) rs;
	   			WebMarkupContainer rootSectionContainer = new WebMarkupContainer(chapterRepeater.newChildId());
	   			chapterRepeater.add(rootSectionContainer);
	   			// Chapter that contains the bookmark is open by default.
	   			if (rootSection.equals(currentRootSection))
	   				rootSectionContainer.add(new ClassAttributeModifier("open"));
	   			rootSectionContainer.add(new Label("chapterTitle", rootSection.getTitle()));
	   			ISIXmlComponent xml = new ISIXmlComponent("chapterContent", new XmlSectionModel(rootSection), "toc");
	   			xml.setTransformParameter("sectionLevel", sectionLevel);
	   			if (currentPage != null)
	   				xml.setTransformParameter("current", currentPage.getSectionAncestor().getId());
				rootSectionContainer.add(xml);				
	   		}
	   	}
	}
	
	/**
	 * Returns a list view of the pages in a section.  Override
	 * and return an invisible component to hide pages.
	 * 
	 * @param id wicket id of component
	 * @param list list of XmlSections that are pages
	 * @return
	 */
	public WebMarkupContainer getPageList(String id, List<XmlSection> list) {
		
		ListView<XmlSection> listView = new ListView<XmlSection>(id) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<XmlSection> item) {
				XmlSection sec3 = item.getModelObject();
				ContentLoc loc = new ContentLoc(sec3);
				BookmarkablePageLink<ISIStandardPage> link = linkTo("link", sec3);
				if (locsWithUnread.contains(loc.getLocation()))
					link.add(new Image("messageIcon", new ResourceReference("img/icons/envelope_new.png")));
				else if (locsWithMessages.contains(loc.getLocation()))
					link.add(new Image("messageIcon", new ResourceReference("img/icons/envelope_old.png")));
				else
					link.add(new WebMarkupContainer("messageIcon").setVisible(false));
				item.add(link);
				link.add(new Label("number", String.valueOf(++pageNum)).setRenderBodyOnly(true));
			}
			
		};
		
		if (list == null || list.isEmpty())
			listView.setVisible(false);
		else
			listView.setList(list);
		return listView;
	}
	

	/**
	 * Provides an image indicating whether a section has been completed or not.
	 * 
	 * @param id wicket id
	 * @param completed true if the section has been completed, false otherwise.
	 * @return the appropriate image component for the 'completed' parameter
	 */
	public Component getCompletedImage(String id, IModel<XmlSection> model) {
		IndiraImageComponent image = new IndiraImageComponent(id);
		ISIXmlSection currentSection = (ISIXmlSection) model.getObject();
		
		Boolean isComplete = SectionService.get().getSectionStatusMap(getUser()).get(new ContentLoc(currentSection).getLocation());

		if (isComplete == null)
			isComplete = false;
		
		if (isComplete) {
			image.setDefaultModelObject(IndiraImage.get("img/icons/check_done.png"));
			image.setTitleText("Section Completed");
			image.setAltText("Section Completed");
		} else {
			image.setVisible(false);
		}
		return image;
	}
	
	/**
	 * Provides a custom {@link ISITagLinkBuilder} for the tag panel
	 * on this TOC page.
	 * 
	 * @return
	 */
	public ISITagLinkBuilder getTagLinkBuilder() {
		return ISIApplication.get().getTagLinkBuilder();
	}
	
	public String getPageType() {
		return "studenttoc";
	}

	public String getPageName() {
		return null;
	}
}