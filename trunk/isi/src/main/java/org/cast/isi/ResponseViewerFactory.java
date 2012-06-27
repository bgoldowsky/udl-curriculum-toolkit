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
package org.cast.isi;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.cast.cwm.xml.XmlSectionModel;
import org.cast.cwm.xml.transform.FilterElements;
import org.cast.isi.data.ISIPrompt;
import org.cast.isi.data.ISIResponse;
import org.cast.isi.data.PromptType;
import org.cast.isi.panel.ResponseViewer;

public class ResponseViewerFactory {

	public Component makeResponseViewComponent(
			IModel<ISIResponse> model) {
		ISIResponse response = model.getObject();
		ISIPrompt prompt = (ISIPrompt) response.getPrompt();
		PromptType type = prompt.getType();
		ResponseViewerFactory factory = this;
		if (type == PromptType.SINGLE_SELECT) {
			return factory.makeXmlComponentViewer(prompt);
		}
		else {
			return factory.makeResponseViewer(model);
		}
	}

	private Component makeResponseViewer(IModel<ISIResponse> model) {
		ResponseViewer viewer = new ResponseViewer("response",
				model, 
				500, 500);
		viewer.setShowDateTime(false);
		return viewer;
	}

	private Component makeXmlComponentViewer(ISIPrompt prompt) {
		ISIXmlSection section = prompt.getContentElement().getContentLocObject().getSection();
		ISIXmlComponent xml = new ISIXmlComponent("response",new XmlSectionModel(section), "view-response" );
		xml.setTransformParameter(FilterElements.XPATH, String.format("//dtb:responsegroup[@id='%s']", prompt.getContentElement().getXmlId()));
		xml.setTransformParameter("lock-response", (section != null) && section.isLockResponse());
		xml.setTransformParameter("delay-feedback", (section != null) && section.isDelayFeedback());
		xml.setOutputMarkupId(true);
		return xml;
	}

}
