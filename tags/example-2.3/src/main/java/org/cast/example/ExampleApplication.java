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
package org.cast.example;

import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.cast.isi.ISIApplication;

public class ExampleApplication extends ISIApplication {
	
	
	@Override
	public String getPageTitleBase() {
		return (new StringResourceModel("pageTitleBase", new Model<String>("pageTitleBase"), null, "Example")).getString();
	}

	@Override
	public String getAppId() {
		return (new StringResourceModel("applicationId", new Model<String>("applicationId"), null, "ExampleApplication")).getString();
	}
}