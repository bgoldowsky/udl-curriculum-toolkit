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

import org.apache.wicket.markup.html.basic.Label;
import org.cast.isi.ISIApplication;

/**
 * A {@link QuickFlipForm} that also displays the total number of pages.
 * 
 * @author bgoldowsky
 *
 */
public class QuickFlipWithTotal extends QuickFlipForm {

	private static final long serialVersionUID = 1L;

	public QuickFlipWithTotal(String id, Boolean addLabel, Integer currentPage) {
		super(id, addLabel, currentPage);

		add(new Label("pageCount", String.valueOf(ISIApplication.get().getPageCount())));
	}

}
