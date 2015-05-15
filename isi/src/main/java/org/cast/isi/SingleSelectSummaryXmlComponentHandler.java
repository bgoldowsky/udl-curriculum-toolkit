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
package org.cast.isi;

import com.google.inject.Inject;
import org.apache.wicket.Component;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.cast.cwm.CwmSession;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.Response;
import org.cast.cwm.xml.XmlSectionModel;
import org.cast.cwm.xml.service.IXmlService;
import org.cast.isi.ISIXmlComponent.AttributeRemover;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.data.PromptType;
import org.cast.isi.panel.CorrectDelayedSingleSelectSummaryPanel;
import org.cast.isi.panel.CorrectImmediateSingleSelectSummaryPanel;
import org.cast.isi.panel.IncorrectSingleSelectSummaryPanel;
import org.cast.isi.panel.NoAnswerSingleSelectSummaryPanel;
import org.cast.isi.service.IISIResponseService;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
public class SingleSelectSummaryXmlComponentHandler {

	@Inject 
	IISIResponseService responseService;

	@Inject
	protected IXmlService xmlService;
	
	public SingleSelectSummaryXmlComponentHandler() {
        Injector.get().inject(this);
	}
	
	public Component makeComponent(final String wicketId, final Element elt, XmlSectionModel model, boolean noAnswer) {
		WebMarkupContainer container = new WebMarkupContainer(wicketId);
		container.add(new AttributeRemover("type", "rgid"));

		boolean delayed = isDelayFeedback(model);
		String responseGroupId = elt.getAttribute("rgid");
		String collectionName = elt.getAttribute("group");
		List<Response> responses = getResponses(findPrompt(model, responseGroupId, collectionName));

		NodeList wicketNodes = xmlService.getWicketNodes((Element) elt, false);

		for (int i = 0; i < wicketNodes.getLength(); i++) {
			Element itemElt = (Element) wicketNodes.item(i);
			String responseId = responseGroupId + "_" + getXmlId(itemElt);
			List<Response> matchingResponses = getResponsesForChoice(responses, responseId);

			container.add(makeChoiceComponent(getWicketId(itemElt), isCorrect(itemElt), delayed, noAnswer, matchingResponses));
		}
		
		return container;
	}

	private Component makeChoiceComponent(String itemWicketId, boolean correct,
			boolean delayed, boolean noAnswer, List<Response> matchingResponses) {
		if (correct && delayed) {
			return new CorrectDelayedSingleSelectSummaryPanel(itemWicketId, matchingResponses);
		}
		else if (correct && !delayed) {
			return new CorrectImmediateSingleSelectSummaryPanel(itemWicketId, mapScores(matchingResponses));
		}
		else if (noAnswer) {
			return new NoAnswerSingleSelectSummaryPanel(itemWicketId, matchingResponses);
		}
		else {
			return new IncorrectSingleSelectSummaryPanel(itemWicketId, matchingResponses);
		}
	}

	private List<Response> getResponsesForChoice(List<Response> responses,
			String responseId) {
		List<Response> matchingResponses = new ArrayList<Response>();
		for (Response response: responses) {
			if (responseId.equals(response.getResponseData().getText()))
				matchingResponses.add(response);
		}
		return matchingResponses;
	}

	private String getXmlId(Element itemElt) {
		return itemElt.getAttribute("xmlId");
	}

	private boolean isCorrect(Element itemElt) {
		return Boolean.valueOf(itemElt.getAttribute("correct"));
	}

	private String getWicketId(Element itemElt) {
		return itemElt.getAttributeNS(xmlService.getNamespaceContext().getNamespaceURI("wicket"), "id");
	}

	private List<Response> getResponses(IModel<Prompt> mPrompt) {
		return responseService.getResponsesForPeriod(mPrompt, CwmSession.get().getCurrentPeriodModel()).getObject();
	}

	private IModel<Prompt> findPrompt(XmlSectionModel model, String responseGroupId, String collectionName) {
		return responseService.getOrCreatePrompt(PromptType.SINGLE_SELECT, new ContentLoc(model.getObject()), responseGroupId, collectionName);
	}

	private boolean isDelayFeedback(XmlSectionModel model) {
		ISIXmlSection section = (ISIXmlSection) model.getObject();
		return (section != null) && section.isDelayFeedback();
	}

	private Map<Integer, List<Response>> mapScores(List<Response> responses) {
		Map<Integer, List<Response>> sorted = new HashMap<Integer, List<Response>>();
		for (Response response: responses) {
			Integer tries = response.getScore()==0 ? 0 : response.getTries();
			if (sorted.get(tries) == null)
				sorted.put(tries, new LinkedList<Response>());
			sorted.get(tries).add(response);
		}
		return sorted;
	}

}
