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

import com.google.inject.Inject;
import org.apache.wicket.Component;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.cast.cwm.components.ClassAttributeModifier;
import org.cast.cwm.components.Icon;
import org.cast.cwm.xml.XmlDocument;
import org.cast.cwm.xml.XmlSection;
import org.cast.cwm.xml.XmlSectionModel;
import org.cast.isi.CollapseBoxBehavior;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISISession;
import org.cast.isi.ISITagLinkBuilder;
import org.cast.isi.ISIXmlComponent;
import org.cast.isi.ISIXmlSection;
import org.cast.isi.component.QuickFlipForm;
import org.cast.isi.data.ClassMessage;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.panel.TagCloudTocPanel;
import org.cast.isi.service.IFeatureService;
import org.cast.isi.service.IISIResponseService;
import org.cast.isi.service.ISectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@AuthorizeInstantiation("STUDENT")
public class StudentToc extends ISIStandardPage {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(StudentToc.class);
	protected List<String> locsWithUnread;
	protected List<String> locsWithMessages;
	
	private int pageNum = 0; // Global page count for entire curriculum
	
	private transient ISIXmlSection currentPage;
	private transient ISIXmlSection currentRootSection;
		
	@Inject
	protected ISectionService sectionService;

	@Inject
	protected IFeatureService featureService;

	@Inject
	protected IISIResponseService responseService;
	
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
		ClassMessage m = responseService.getClassMessage(ISISession.get().getCurrentPeriodModel());
		if (m == null) {
			classMessageBox.add(new Label("classMessage", new ResourceModel("classMessage")));
		} else {
			classMessageBox.add(new Label("classMessage", m.getMessage()));
		}
		classMessageBox.setVisible(ISIApplication.get().isClassMessageOn());

		// Loads a list of locations that have Unread Messages and Regular Messages
		locsWithUnread = responseService.getPagesWithNotes(ISISession.get().getUser(), true);
		locsWithMessages = responseService.getPagesWithNotes(ISISession.get().getUser());
		
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
	   			xml.setTransformParameter("sectionToggleLinks", getSectionToggleParameter());
	   			xml.setTransformParameter("sectionLevel", sectionLevel);
	   			if (currentPage != null)
	   				xml.setTransformParameter("current", currentPage.getSectionAncestor().getId());
				rootSectionContainer.add(xml);				
	   		}
	   	}
	}

	private String getSectionToggleParameter() {
		return Boolean.toString(featureService.isTocSectionTogglesOn());
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
				BookmarkablePageLink<ISIStandardPage> link = new SectionLinkFactory().linkToPage("link", sec3);
				if (locsWithUnread.contains(loc.getLocation()))
					link.add(new Icon("messageIcon", "img/icons/envelope_new.png"));
				else if (locsWithMessages.contains(loc.getLocation()))
					link.add(new Icon("messageIcon", "img/icons/envelope_old.png"));
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
	 * @param model
	 * @return the appropriate image component for the 'completed' parameter
	 */
	public Component getCompletedImage(String id, IModel<XmlSection> model) {
		ISIXmlSection currentSection = (ISIXmlSection) model.getObject();
		
		Boolean isComplete = sectionService.getSectionStatusMap(getUser()).get(new ContentLoc(currentSection).getLocation());

		if (isComplete == null)
			isComplete = false;

		Icon image = new Icon("img/icons/check_done.png", "Section Completed");
		if (!isComplete)
			image.setVisible(false);
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