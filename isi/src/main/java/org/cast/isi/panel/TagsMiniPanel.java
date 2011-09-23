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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.cast.cwm.data.PersistedObject;
import org.cast.cwm.tag.component.TagPanel;
import org.cast.isi.ISIApplication;

public class TagsMiniPanel extends TagPanel {

	private static final long serialVersionUID = 1L;
	private List<Component> updatingComponents = new ArrayList<Component>();

	public TagsMiniPanel(String id, PersistedObject target) {
		super(id, target, ISIApplication.get().getTagLinkBuilder());
		AjaxFallbackLink<Object> close = new AjaxFallbackLink<Object>("close") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				if (target!=null) {
					for (Component c : updatingComponents)
						target.addComponent(c);
				}
			}
			
		};
		add(close);
		remove("tagsSubHeader");
	}

	public List<Component> getUpdatingComponents() {
		return updatingComponents;
	}

	public void setUpdatingComponents(List<Component> updatingComponents) {
		this.updatingComponents = updatingComponents;
	}
	
	public void addUpdatingComponent(Component c) {
		if (c == null)
			throw new IllegalArgumentException("Don't add null compoennt");
		this.updatingComponents.add(c);
	}
	
	public void removeUpdatingComponent(Component c) {
		this.updatingComponents.remove(c);
	}
}
