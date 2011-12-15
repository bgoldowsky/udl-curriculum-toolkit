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

import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;

public class TextHelpToolbar extends ISIPanel implements IHeaderContributor {
	
	private static final long serialVersionUID = 1L;

	public TextHelpToolbar(String id) {
		super(id);
		WebMarkupContainer script = new WebMarkupContainer("script");
		add(script);
		script.add(new SimpleAttributeModifier("src", RequestCycle.get().urlFor(new ResourceReference("/js/toolbar.js"))));
	}

	public void renderHead(IHeaderResponse response) {
		response.renderCSSReference(new ResourceReference("/css/toolbar.css"));
	} 
}
