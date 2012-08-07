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
package org.cast.isi.page;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.cast.cwm.data.ResponseMetadata;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.models.PromptModel;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISISession;
import org.cast.isi.data.Question;
import org.cast.isi.panel.ResponseButtons;
import org.cast.isi.panel.ResponseList;
import org.cast.isi.service.QuestionService;

/**
 * This popup window shows all the responses to a given question passed in
 * via page parameters.
 * 
 * @author lynnmccormack
 *
 */
public class QuestionPopup extends ISIBasePage {
	protected ResponseMetadata questionsMetadata;

	protected boolean isTeacher;
	
	public QuestionPopup (final PageParameters param) {
		super(param);
		setQuestionsMetadata(questionsMetadata);
		setDefaultModel(QuestionService.get().getQuestionModelById(param.getLong("question")));	
		Question question = (Question) getDefaultModelObject();
		add(new Label("pageTitle", ISIApplication.get().getPageTitleBase() + " :: " + question.getText()));
		add(new Label("question", question.getText()));

		// set teacher flag and target user
		isTeacher = ISISession.get().getUser().getRole().subsumes(Role.TEACHER);

		addResponses(question);
		add(ISIApplication.get().getToolbar("tht", this));
	}

	protected void addResponses(Question question) {
		PromptModel mPrompt = new PromptModel(question.getPrompt());
		ResponseList responseList = new ResponseList("responses", mPrompt, questionsMetadata, null, null);
		add(responseList);
		String context = "questions" + (isTeacher ? ".teacher" : ""  );
		responseList.setContext(context);
		responseList.setAllowNotebook(false);
		responseList.setAllowWhiteboard(false);
		responseList.setAllowEdit(!isTeacher);
		
		ResponseButtons responseButtons = new ResponseButtons("responseButtons", mPrompt, questionsMetadata, null);
		responseButtons.setContext("questions");
		add(responseButtons);
		responseButtons.setVisible(!isTeacher);
	}
	
	public void renderHead(final IHeaderResponse response) {
		response.renderCSSReference(new ResourceReference("/css/window.css"));
		response.renderCSSReference(new ResourceReference("/css/window_print.css"), "print");
		super.renderHead(response);
	}
	
	public void setQuestionsMetadata(ResponseMetadata questionsMetadata) {
		this.questionsMetadata = ISIApplication.get().getResponseMetadata();
	}

	@Override
	public String getPageName() {
		return getPage().getPageParameters().getString("callingPageName");
	}

	@Override
	public String getPageType() {
		return "myquestionspopup";
	}

	@Override
	public String getPageViewDetail() {
		return ((Question) getDefaultModelObject()).getText();
	}
}