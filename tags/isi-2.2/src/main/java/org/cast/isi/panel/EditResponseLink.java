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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.Response;
import org.cast.cwm.data.ResponseMetadata;
import org.cast.isi.data.ContentLoc;

public class EditResponseLink extends AjaxLink<Response> {

	Component placeholder;
	final ResponseMetadata metadata; 
	final ContentLoc loc;
	
	private static final long serialVersionUID = 1L;

	public EditResponseLink(String id, IModel<Response> model, ResponseMetadata metadata, ContentLoc loc, Component placeholder) {
		super(id, model);
		this.placeholder = placeholder;
		this.metadata = metadata;
		this.loc = loc;
	}

	@Override
	public void onClick(final AjaxRequestTarget target) {
		ResponseEditor editor = new ResponseEditor(placeholder.getId(), getModel(), metadata, loc);
		placeholder.replaceWith(editor);
		target.add(editor);
	}

}

