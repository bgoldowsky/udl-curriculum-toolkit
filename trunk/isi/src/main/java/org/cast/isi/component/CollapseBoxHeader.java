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
package org.cast.isi.component;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.cast.isi.CollapseBoxBehavior;
import org.cast.isi.page.ISIBasePage;

public class CollapseBoxHeader extends WebMarkupContainer {
	private static final long serialVersionUID = 1L;
	
	String boxSequence;

	public CollapseBoxHeader(String id, String _boxSequence) {
		super(id);
		boxSequence = _boxSequence;
	}	
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new CollapseBoxBehavior("onclick", "support:" + boxSequence, ((ISIBasePage) getPage()).getPageName()));
	}
}