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

import lombok.Getter;
import lombok.Setter;
import net.databinder.auth.hib.AuthDataSession;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.PropertyModel;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.cwm.data.component.SessionExpireWarningDialog;
import org.cast.cwm.xml.XmlDocument;
import org.cast.cwm.xml.XmlSection;
import org.cast.cwm.xml.service.XmlService;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISISession;
import org.cast.isi.ISIXmlSection;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.panel.TeacherSubHeaderPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base page for ISI application.  All non-popup pages should extend this page.
 * It will include the header and footer panels and any other common feature
 * across these pages.
 * 
 * @author jacobbrookover
 *
 */
abstract public class ISIStandardPage extends ISIBasePage {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ISIStandardPage.class);
	
	@Getter @Setter protected ContentLoc loc = null;

	public ISIStandardPage(final PageParameters parameters) {
		super(parameters);
		commonInit(parameters);
	}
	
	public void commonInit(PageParameters parameters) {
		add(new Label("pageTitle", new PropertyModel<String>(this, "pageTitle")));
		
		add(ISIApplication.get().getHeaderPanel("headerPanel", parameters).setOutputMarkupId(true));
		// If teacher, then add a sub header panel
		if (ISISession.get().getUser().getRole().subsumes(Role.TEACHER)) {
			add(new TeacherSubHeaderPanel("teacherSubHeader", parameters));
		} else {
			add(new WebMarkupContainer("teacherSubHeader").setVisible(false));			
		}

		add(ISIApplication.get().getFooterPanel("footerPanel", parameters));
		add(new ISISessionExpireWarningDialog("sessionWarning"));
		
		addToolbar("tht");
	}
	
	/**
	 * Adds the application's default toolbar (with features like text-to-speech, dictionary, etc).
	 * Pages can override this method to use a different (or no) toolbar.
	 */
	protected void addToolbar (String id) {
		add (ISIApplication.get().getToolbar(id, this));
	}
	
	/** 
	 * By default returns null.  Override to provide more detail.
	 * 
	 * @see org.cast.isi.page.IEventInfoProvider#getPageViewDetail()
	 */
	public String getPageViewDetail() {
		return null;
	}
	
	public Class<? extends WebPage>getHomePage() {
		return getISIApplication().getHomePage();
	}
	
	/**
	 * Convenience methods to create a link to a page
	 */
	
	public static BookmarkablePageLink<ISIStandardPage> linkTo (String wicketId, String file, String id) {
		XmlDocument document = XmlService.get().getDocument(file);
		return linkTo (wicketId, document==null ? null : document.getById(id));
	}
	
	public static BookmarkablePageLink<ISIStandardPage> linkTo (String wicketId, XmlSection sec) {
		ISIXmlSection page = null;
		if (sec != null)
			page = ((ISIXmlSection) sec).getPageAncestor();
		if (page != null && !page.equals(sec))
			return linkTo (wicketId, page, sec.getId());
		else
			return linkTo(wicketId, sec, null);
	}
	
	public static BookmarkablePageLink<ISIStandardPage> linkTo (String wicketId, XmlSection sec, final String fragment) {
		if (sec == null) {
			// No section to link to, return an invisible component
			BookmarkablePageLink<ISIStandardPage> link = new BookmarkablePageLink<ISIStandardPage>(wicketId, ISIStandardPage.class);
			link.setVisible(false);
			return link;
		}
		sec = sectionLinkDest ((ISIXmlSection) sec);
		Class<? extends ISIStandardPage> pageType = ISIApplication.get().getReadingPageClass();
		ContentLoc loc = new ContentLoc (sec);
		BookmarkablePageLink<ISIStandardPage> link = new BookmarkablePageLink<ISIStandardPage>(wicketId, pageType) {
			private static final long serialVersionUID = 1L;

			@Override
			protected CharSequence appendAnchor(ComponentTag tag, CharSequence url) {
				if (fragment==null || url.toString().indexOf('#')>-1)
					return super.appendAnchor(tag, url);
				return url + "#" + fragment;
			}
			
		};
		link.setParameter("loc", loc.getLocation());
		return link;
	}
	
	/** Return the real page that should be linked to when a link to a 
	 * section is requested.  This can be the section itself, but can also be
	 * for example the first page if the given section is a reading with
	 * multiple pages or a parent section if this is a sub-page
	 * 
	 * @param sec the section that is being linked to
	 * @return the nearest child or parent that returns true to isPage()
	 */
	public static ISIXmlSection sectionLinkDest (ISIXmlSection sec) {
		ISIXmlSection result = null;
		if (sec.isPage()) { // If this is a page, return it
			result = sec;
		} else if (sec.hasChildren()) { // If this has children, see if one of them is a page
			result = sectionLinkDest ((ISIXmlSection) sec.getChild(0));
		}
		if (result == null) {
			result = sec.getPageAncestor(); // If all else fails, return the page that contains this section
		}
		return result;
	}
		
	public void renderHead(final IHeaderResponse response) {
		response.renderCSSReference(new ResourceReference("/css/main.css"));
		super.renderHead(response);		
	}
	
	/** 
	 * Return Person who is logged in and viewing this page
	 * @return Person object
	 */
	public User getUser() {
		return (User) AuthDataSession.get().getUser();
	}
	
	public ISIApplication getISIApplication() {
		return (ISIApplication) getApplication();
	}	

	
	/**
	 * TODO: do we want to override behavior of this warning at all?
	 *
	 */
	protected class ISISessionExpireWarningDialog extends SessionExpireWarningDialog {

		private static final long serialVersionUID = 1L;
		
		public ISISessionExpireWarningDialog(String id) {
			super(id);
		}
		
	}
	
}
