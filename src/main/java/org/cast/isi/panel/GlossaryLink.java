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

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.Strings;
import org.cast.isi.ISIApplication;

/**
 * 
 * This GlossaryLink is a link to a modal popup window with a mini glossary definition.  This may
 * or may not be used based on how the glossary links are configured for the application.
 * 
 */
public class GlossaryLink extends BookmarkablePageLink<String> {

	private static final long serialVersionUID = 1L;

	public GlossaryLink(String id, IModel<String> model) {
		super(id, ISIApplication.get().getGlossaryPageClass());
		setModel(model);
	}
	
	@Override
	protected void onBeforeRender() {
		super.onBeforeRender();
		if (!Strings.isEmpty(getModelObject())) {
			getPageParameters().add("word", getModelObject());
			getPageParameters().add("link", "mini");
		}
	}

	@Override
	protected CharSequence appendAnchor(ComponentTag tag, CharSequence url) {
		String word = getModelObject();
		if (Strings.isEmpty(word) || url.toString().indexOf('#')>-1)
			return super.appendAnchor(tag, url);
		Character initial = Character.toLowerCase(word.charAt(0));

		if (!Character.isLetter(initial)) 
			initial = '#';
		return url + "#" + initial;
	}
	
}