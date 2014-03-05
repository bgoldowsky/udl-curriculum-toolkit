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

import java.util.Date;

import org.apache.wicket.datetime.PatternDateConverter;
import org.apache.wicket.datetime.markup.html.basic.DateLabel;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class ISIDateLabel extends DateLabel {

	private static final long serialVersionUID = 1L;

	private static final String dateFormat = "M/d/yy h:mm a";
	
	public ISIDateLabel (String id, IModel<Date> mDate) {
		super(id, mDate, new PatternDateConverter(dateFormat, true));
	}

	public ISIDateLabel(String id, Date date) {
		this(id, new Model<Date>(date));
	}
}

