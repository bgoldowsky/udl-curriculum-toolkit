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

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.cast.cwm.tag.ITagLinkBuilder;
import org.cast.cwm.tag.model.Tag;
import org.cast.isi.page.Tags;

public class ISITagLinkBuilder implements ITagLinkBuilder {

	private static final long serialVersionUID = 1L;

	public WebMarkupContainer buildLink(String id, Tag tag) {
		BookmarkablePageLink<Page> bpl = new BookmarkablePageLink<Page>(id, Tags.class);
		bpl.getPageParameters().add("tag", tag.getName());
		return bpl;
	}

    public WebMarkupContainer buildLink(String id, Tag tag, PageParameters parameters) {
		BookmarkablePageLink<Page> link = new BookmarkablePageLink<Page>(id, Tags.class, parameters);
		link.getPageParameters().add("tag", tag.getName());
		return link;
	}
}
