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

import org.apache.wicket.PageParameters;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.cast.cwm.admin.EventLog;

@AuthorizeInstantiation("RESEARCHER")

public class ISIEventLog extends EventLog {
	
	public ISIEventLog(final PageParameters params) {
		super(params);	
	}
	
// FIXME put back these extra columns
//	@Override
//	protected IDataColumn[] makeColumns() {
//		IDataColumn[] columns = new IDataColumn[7];
//		IDataColumn[] standardCol = super.makeColumns();
//		columns[0] = standardCol[0]; // Date
//		columns[1] = standardCol[1]; // User
//		columns[2] = new PropertyDataColumn("Student", "target.username", "target.username");
//		columns[3] = standardCol[2]; // Event type
//		columns[4] = standardCol[3]; // Page
//		columns[5] = standardCol[4]; // Details
//		columns[6] = standardCol[5]; // Responses
//		return columns;
//	}
}