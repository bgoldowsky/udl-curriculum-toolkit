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
package org.cast.isi.page;

import com.google.inject.Inject;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.cast.cwm.xml.XmlDocument;
import org.cast.cwm.xml.XmlSection;
import org.cast.cwm.xml.service.IXmlService;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISIXmlSection;
import org.cast.isi.data.ContentLoc;

import java.io.Serializable;

public class SectionLinkFactory implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private IXmlService xmlService;

	
	public SectionLinkFactory() {
		super();
        Injector.get().inject(this);
	}

	/** Return the real page that should be linked to when a link to a 
	 * section is requested.  This can be the section itself, but can also be
	 * for example the first page if the given section is a reading with
	 * multiple pages or a parent section if this is a sub-page
	 * 
	 * @param sec the section that is being linked to
	 * @return the nearest child or parent that returns true to isPage()
	 */
	public ISIXmlSection sectionLinkDest (ISIXmlSection sec) {
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

	public BookmarkablePageLink<ISIStandardPage> linkTo (String wicketId, XmlSection sec, final String fragment) {
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
		link.getPageParameters().add("loc", loc.getLocation());
		return link;
	}

	public BookmarkablePageLink<ISIStandardPage> linkToPage (String wicketId, XmlSection sec) {
		ISIXmlSection page = null;
		if (sec != null)
			page = ((ISIXmlSection) sec).getPageAncestor();
		if (page != null && !page.equals(sec))
			return linkTo (wicketId, page, sec.getId());
		else
			return linkTo(wicketId, sec, null);
	}

	/**
	 * Convenience methods to create a link to a page
	 */
	
	public BookmarkablePageLink<ISIStandardPage> linkTo (String wicketId, String file, String id) {
		XmlDocument document = xmlService.getDocument(file);
		return linkToPage (wicketId, document==null ? null : document.getById(id));
	}

}
