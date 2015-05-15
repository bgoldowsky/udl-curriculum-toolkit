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
package org.cast.isi.panel;

import lombok.Getter;
import lombok.Setter;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.cast.cwm.components.ClassAttributeModifier;
import org.cast.isi.ISIApplication;
import org.cast.isi.page.Login;

/**
 * This header panel implements the top level navigation for this application.  It adds
 * both the tabular buttons and the buttons for popup windows.
 * 
 * @author lynnmccormack
 * @author bgoldowsky
 *
 */
public class GuestHeaderPanel extends HeaderPanel {
	
	private static final long serialVersionUID = 1L;
	
	@Getter @Setter
	GlossaryLink glossaryLink;
	
	public GuestHeaderPanel(String id, PageParameters parameters) {
		super(id, parameters);
	}
	
	@Override
	public void addUserInfo() {
		add(new BookmarkablePageLink<Login>("loginLink", ISIApplication.get().getSignInPageClass()));
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
		
		if (prefix != null) {
			WebMarkupContainer link = (WebMarkupContainer) get(prefix + "Link");
			link.add(new ClassAttributeModifier("current", false));
		}
		
		super.onBeforeRender();
	}
	
}