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

import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.component.DialogBorder;

public abstract class RemoveDialog extends Panel {
	
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
	
	public Behavior getClickToOpenBehavior() {
		return db.getClickToOpenBehavior();
	}
	
	public Behavior getClickToCloseBehavior() {
		return db.getClickToCloseBehavior();
	}

	protected abstract void removeObject();
	protected abstract String getDialogTitle();
	protected abstract String getDialogText();

}
