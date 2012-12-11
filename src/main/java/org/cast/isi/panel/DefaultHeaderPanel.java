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

import lombok.Getter;
import lombok.Setter;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.cast.cwm.components.ClassAttributeModifier;
import org.cast.isi.ISIApplication;
import org.cast.isi.panel.GlossaryLink;
import org.cast.isi.panel.HeaderPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This header panel implements the top level navigation for this application.  It adds
 * both the tabular buttons and the buttons for popup windows.
 * 
 * @author lynnmccormack
 *
 */
public class DefaultHeaderPanel extends HeaderPanel {
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(DefaultHeaderPanel.class);
	
	@Getter @Setter
	BookmarkablePageLink<Page> notebookLink, whiteboardLink;
	
	@Getter @Setter
	GlossaryLink glossaryLink;
	
	public DefaultHeaderPanel(String id, PageParameters parameters) {
		super(id, parameters);
	}

	@Override
	public void addButtons() {

		ISIApplication application = ISIApplication.get();
		BookmarkablePageLink<Void> homeLink = new BookmarkablePageLink<Void>("homeLink", application.getHomePage());
		application.setLinkProperties(homeLink);
		add(homeLink);
		
		BookmarkablePageLink<Void> contentsLink = new BookmarkablePageLink<Void>("contentsLink", application.getReadingPageClass());
		application.setLinkProperties(contentsLink);
		add(contentsLink);
		
		BookmarkablePageLink<Void> rcLink = new BookmarkablePageLink<Void>("myResponseCollectionsLink", application.getResponseCollectionsPageClass());
		rcLink.setVisible(application.isResponseCollectionsOn());
		application.setLinkProperties(rcLink);
		add(rcLink);

		BookmarkablePageLink<Void> questionsLink = new BookmarkablePageLink<Void>("myQuestionsLink", application.getMyQuestionsPageClass());
		questionsLink.setVisible(application.isMyQuestionsOn());
		application.setLinkProperties(questionsLink);
		add(questionsLink);

		BookmarkablePageLink<Void> tagsLink = new BookmarkablePageLink<Void>("myTagsLink", application.getTagsPageClass());
		tagsLink.setVisible(application.isTagsOn());
		application.setLinkProperties(tagsLink);
		add(tagsLink);

		notebookLink = new BookmarkablePageLink<Page>("notebookLink", application.getNotebookPageClass());
		application.setLinkProperties(notebookLink);
		notebookLink.setVisible(application.isNotebookOn());
		add(notebookLink);

		whiteboardLink = new BookmarkablePageLink<Page>("whiteboardLink", application.getWhiteboardPageClass());
		application.setLinkProperties(whiteboardLink);
		whiteboardLink.setVisible(application.isWhiteboardOn());
		add(whiteboardLink);
		
		glossaryLink = new GlossaryLink("glossaryLink", null);
		application.setLinkProperties(glossaryLink);
		glossaryLink.setVisible(application.isGlossaryOn());
		add(glossaryLink);
		

	}

	@Override
	public void onBeforeRender() {
		
		ISIApplication application = ISIApplication.get();

		// Set the selected button to "current" button based on parent page
		Class<? extends Page> pageClass = getPage().getClass();
		String prefix = null;
		
		if (application.getHomePage().isAssignableFrom(pageClass))
			prefix="home";
		else if (application.getReadingPageClass().isAssignableFrom(pageClass))
			prefix = "contents";
		else if (application.getResponseCollectionsPageClass().isAssignableFrom(pageClass))
			prefix= "myResponseCollections";
		else if (application.getMyQuestionsPageClass().isAssignableFrom(pageClass))
			prefix = "myQuestions";
		else if (application.getTagsPageClass().isAssignableFrom(pageClass))
			prefix = "myTags";
		
		if (prefix != null) {
			WebMarkupContainer link = (WebMarkupContainer) get(prefix + "Link");
			link.add(new ClassAttributeModifier("current"));
		}
		
		super.onBeforeRender();
	}
	
}