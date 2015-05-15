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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

public class ProgressBar extends Panel {

	private static final long serialVersionUID = 1L;

	private int percent;
	
	public ProgressBar(String id, int percent) {
		super(id);
		this.percent = percent;
		add(new Container("container"));
	}

	private class Container extends WebMarkupContainer {

		private static final long serialVersionUID = 1L;

		private Container(String id) {
			super(id);
			add(new Label("percent", String.format("%d%%", percent)));
			add(new AttributeModifier("style", true, getStyleModel()));
		}

		private Model<String> getStyleModel() {
			return new Model<String>(String.format("width: %d%%", percent));
		}
	}

}
