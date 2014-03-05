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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.cast.cwm.service.IEventService;
import org.cast.isi.ISIApplication;
import org.cast.isi.page.ISIStandardPage;

import com.google.inject.Inject;

public class MiniGlossaryLink extends AjaxLink<String>{

	private static final long serialVersionUID = 1L;
	private MiniGlossaryModal glossaryModal;

	@Inject
	private IEventService eventService;

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
		glossaryModal.setModel(ISIApplication.get().getGlossary().getEntryById(getModelObject()));
		glossaryModal.getDialog().setVerticalReferencePointId(this.getMarkupId());
		target.add(glossaryModal);
		target.appendJavaScript(glossaryModal.getDialog().getOpenString());
		eventService.saveEvent("miniglossary:view", getModelObject(), ((ISIStandardPage) getPage()).getPageName());
	}

}
