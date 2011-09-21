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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.cast.cwm.glossary.IGlossaryEntry;
import org.cast.cwm.service.EventService;
import org.cast.isi.ISIApplication;
import org.cast.isi.page.ISIStandardPage;

public class MiniGlossaryLink extends AjaxLink<String>{

	private static final long serialVersionUID = 1L;
	private MiniGlossaryModal glossaryModal;

	public MiniGlossaryLink(String id, IModel<String> model, MiniGlossaryModal glossaryModal) {
		super(id, model);
		this.glossaryModal = glossaryModal;
		setOutputMarkupId(true);
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		tag.remove("word");
	}
	
	@Override
	public void onClick(AjaxRequestTarget target) {
		// Set model of mini glossary panel to the model of the glossary entry
		glossaryModal.setModel(ISIApplication.get().getGlossary().getEntryById(getDefaultModelObjectAsString()));
		glossaryModal.getDialog().setVerticalReferencePointId(this.getMarkupId());
		target.addComponent(glossaryModal);
		target.appendJavascript(glossaryModal.getDialog().getOpenString());
		EventService.get().saveEvent("miniglossary:view", getModelObject(), ((ISIStandardPage) getPage()).getPageName());
	}

}
