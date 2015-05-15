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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.cast.cwm.xml.XmlSectionModel;
import org.cast.cwm.xml.transform.FilterElements;
import org.cast.isi.data.ContentElement;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.data.ISIPrompt;
import org.cast.isi.data.ISIResponse;
import org.cast.isi.data.PromptType;
import org.cast.isi.panel.ResponseViewer;

public class ResponseViewerFactory {

	public Component makeResponseViewComponent(
			String wicketId, IModel<ISIResponse> model) {
		ISIResponse response = model.getObject();
		ISIPrompt prompt = (ISIPrompt) response.getPrompt();
		PromptType type = prompt.getType();
		if (type == PromptType.SINGLE_SELECT) {
			return makeXmlComponentViewer(wicketId, prompt);
		}
		else {
			return makeResponseViewer(wicketId, model);
		}
	}

	private Component makeResponseViewer(String wicketId, IModel<ISIResponse> model) {
		ResponseViewer viewer = new ResponseViewer(wicketId,
				model, 
				500, 500);
		viewer.setShowDateTime(false);
		return viewer;
	}

	private Component makeXmlComponentViewer(String wicketId, ISIPrompt prompt) {
		ISIXmlSection section = prompt.getContentElement().getContentLocObject().getSection();
		ISIXmlComponent xml = new ISIXmlComponent(wicketId,new XmlSectionModel(section), "view-response" );
		xml.setTransformParameter(FilterElements.XPATH, String.format("//dtb:responsegroup[@id='%s']", prompt.getContentElement().getXmlId()));
		xml.setTransformParameter("lock-response", (section != null) && section.isLockResponse());
		xml.setTransformParameter("delay-feedback", (section != null) && section.isDelayFeedback());
		xml.setOutputMarkupId(true);
		return xml;
	}

	public Component makeQuestionTextComponent(String wicketId, ISIPrompt prompt) {
		PromptType type = prompt.getType();
		if ((type == PromptType.SINGLE_SELECT) || (type == PromptType.RESPONSEAREA))
			return makeXmlQuestionTextComponent(wicketId, prompt);
		else if (type == PromptType.PAGE_NOTES) 
			return new Label(wicketId, "<em>Page Notes</em>").setEscapeModelStrings(false);
		else
			return new Label(wicketId, "Question Not Available");
	}

	private Component makeXmlQuestionTextComponent(String wicketId, ISIPrompt prompt) {
		ContentElement contentElement = prompt.getContentElement();
		ContentLoc contentLocObject = contentElement.getContentLocObject();
		ISIXmlSection section = contentLocObject.getSection();
		ISIXmlComponent xml = new ISIXmlComponent(wicketId,new XmlSectionModel(section), "student" );
		String xmlId = contentElement.getXmlId();
		xml.setTransformParameter(FilterElements.XPATH, String.format("//dtb:responsegroup[@id='%s']//dtb:prompt", xmlId));
		xml.setOutputMarkupId(true);
		return xml;
	}

}
