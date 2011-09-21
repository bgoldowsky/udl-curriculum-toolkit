/*
 * Copyright 2011 CAST, Inc.
 *
 * This file is part of the CAST Wicket Modules:
 * see <http://code.google.com/p/cast-wicket-modules>.
 *
 * The CAST Wicket Modules are free software: you can redistribute and/or
 * modify them under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * The CAST Wicket Modules are distributed in the hope that they will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cast.isi.panel;

import java.util.Collections;
import java.util.List;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.component.DialogBorder;

public abstract class RemoveDialog extends Panel {
	
	static {
		List<ResourceReference> noStylesheets = Collections.emptyList();
		DialogBorder.setStyleReferences(noStylesheets);
	}

	protected final DialogBorder db;
	
	private static final long serialVersionUID = 1L;
	
	public RemoveDialog(String id, IModel<?> model) {
		super(id);
		this.setDefaultModel(model);
		
		db = new DialogBorder("dialogBorder", getDialogTitle());
		add(db);

		db.getBodyContainer().add(new Label("text", getDialogText()));
		db.getBodyContainer().add(new WebMarkupContainer("cancelLink").add(db.getClickToCloseBehavior()));

		db.getBodyContainer().add(new Link<Void>("confirmLink") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick() {
				removeObject();
			}
		});
	}
	
	public IBehavior getClickToOpenBehavior() {
		return db.getClickToOpenBehavior();
	}
	
	public IBehavior getClickToCloseBehavior() {
		return db.getClickToCloseBehavior();
	}

	protected abstract void removeObject();
	protected abstract String getDialogTitle();
	protected abstract String getDialogText();

}
