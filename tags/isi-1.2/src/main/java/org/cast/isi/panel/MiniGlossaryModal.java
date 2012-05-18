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

import lombok.Getter;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.cast.cwm.glossary.IGlossaryEntry;
import org.cast.cwm.xml.ICacheableModel;
import org.cast.cwm.xml.IXmlPointer;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISIXmlComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiniGlossaryModal extends Panel {

	@Getter
	private SidebarDialog dialog;
	
	private String contentPage;
	private GlossaryLink glossaryLink;
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ISIXmlComponent.class);
	private static final long serialVersionUID = 1L;

	public MiniGlossaryModal(String id, String contentPage) {
		super(id, new Model<String>(null));
		this.contentPage = contentPage;
		setOutputMarkupId(true);

		dialog = new SidebarDialog("dialogBorder", new PropertyModel<String>(this, "modelObject.headword"), null);
		dialog.setMoveContainer(this);
		add(dialog);
		
		dialog.getBodyContainer().add(new Label("xmlEntry", "Loading content..."));
		glossaryLink = new GlossaryLink("fullGlossaryLink", new PropertyModel<String>(this, "modelObject.identifier"));
		ISIApplication.get().setLinkProperties(glossaryLink);
		dialog.getBodyContainer().add(glossaryLink);
	}
	
	@SuppressWarnings("unchecked")
	public IModel<? extends IGlossaryEntry> getModel() {
		return (IModel<? extends IGlossaryEntry>) getDefaultModel();
	}
	
	public IGlossaryEntry getModelObject() {
		return getModel() != null ? getModel().getObject() : null;
	}
	
	public void setModel (IModel<? extends IGlossaryEntry> model) {
		setDefaultModel(model);
	}

	@Override
	public void onBeforeRender() {
		ICacheableModel<? extends IXmlPointer> xmlPointer = null;
		if (getModelObject() != null)
			xmlPointer = getModelObject().getXmlPointer();

		if (xmlPointer != null) {
			ISIXmlComponent xmlComponent = new ISIXmlComponent("xmlEntry", xmlPointer, "glossary");
			xmlComponent.setTransformParameter("mini", "true");
			dialog.getBodyContainer().replace(xmlComponent);
		} else {
			dialog.getBodyContainer().replace(new Label("xmlEntry", new ResourceModel("isi.miniGlossary.error", "No such glossary word")));			
		}
		super.onBeforeRender();
	}

}
