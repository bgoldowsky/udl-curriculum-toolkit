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
package org.cast.isi.component;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.cast.cwm.components.ClassAttributeModifier;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISIXmlSection;

/**
 * A link to a particular page, specified by (zero-based!) page number.
 * 
 * @author bgoldowsky
 *
 */
public class PageNumberLink extends BookmarkablePageLink<Void> {
	
	private static final long serialVersionUID = 1L;

	public PageNumberLink(String id, int targetPageNum) {
		super(id, ISIApplication.get().getReadingPageClass());
		ISIXmlSection target = ISIApplication.get().getPageNum(targetPageNum);
		if (target!=null)
			getPageParameters().add("loc", target.getContentLoc().getLocation());
		else {
			setEnabled(false);
			this.add(new ClassAttributeModifier("off"));
		}
	}

}
