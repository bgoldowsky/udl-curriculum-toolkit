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

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.cast.cwm.data.component.CollapseToggleLink;
import org.cast.cwm.data.component.highlight.HighlightDisplayPanel;
import org.cast.cwm.tag.component.TagPanel;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISISession;
import org.cast.isi.ISIXmlSection;
import org.cast.isi.data.ContentElement;
import org.cast.isi.data.PromptType;
import org.cast.isi.panel.NoHighlightModal;
import org.cast.isi.panel.ResponseList;
import org.cast.isi.panel.TeacherSectionCompleteTogglePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AuthorizeInstantiation("TEACHER")
public class TeacherReading extends Reading implements IHeaderContributor {

	private static final long serialVersionUID = 1L;

	protected static final Logger log = LoggerFactory.getLogger(TeacherReading.class);
	
	public TeacherReading(final PageParameters parameters) {
		// If there is no student model, tell super constructor that we won't be displaying XML content.
		super(parameters, ISISession.get().getStudentModel() != null);
	}

	protected boolean userIsStudent() {
		return false;
	}

	@Override
	protected void addSectionCompleteToggle(ISIXmlSection section) {
		TeacherSectionCompleteTogglePanel sectionCompleteTogglePanel = new TeacherSectionCompleteTogglePanel("toggleCompletePanel", mSection, mTargetUser);
		sectionCompleteTogglePanel.setVisibilityEnabled(showXmlContent);
		add(sectionCompleteTogglePanel);
	}

	// panels are not visible without student
	@Override
	public void addHighlightPanel() {	
		if (showXmlContent) {
			super.addHighlightPanel();
			((HighlightDisplayPanel) get("highlightDisplayPanel")).setReadOnly(true);

		} else {
			add(new WebMarkupContainer("highlightToggleLink").setVisible(false));
			add(new WebMarkupContainer("highlightControlPanel"));
			add(new WebMarkupContainer("highlightDisplayPanel"));
			add(new NoHighlightModal("noHighlightModal"));
		}		
	}

	
	// Differs from superclass version in that it creates a READ-ONLY page notes area with no edit buttons
	// view the student's notes
	protected void addNotesPanel () {
		boolean pageNotesOn = ISIApplication.get().isPageNotesOn();

		CollapseToggleLink pageNotesToggleLink = new CollapseToggleLink("pageNotesToggleLink", "pageNotesToggleLink", "pageNotesToggle");
		add(pageNotesToggleLink);
		pageNotesToggleLink.setVisible(pageNotesOn && showXmlContent);

		setPageNotesMetadata();
		WebMarkupContainer responseButtons = new WebMarkupContainer("responseButtons");
		add(responseButtons);
		responseButtons.setVisible(false);
		
		mNotesPrompt = responseService.getOrCreatePrompt(PromptType.PAGE_NOTES, loc);
		ResponseList responseList = new ResponseList ("responseList", mNotesPrompt, pageNotesMetadata, loc, mTargetUser);
		responseList.setContext("pagenote.teacher");
		responseList.setAllowNotebook(false);
		responseList.setAllowEdit(false);
		responseList.setAllowWhiteboard(ISIApplication.get().isWhiteboardOn());
		add(responseList);
	}

	@SuppressWarnings("static-access")
	@Override
	protected void addQuestionsPanel () {
		boolean myQuestionsOn = ISIApplication.get().isMyQuestionsOn();

		CollapseToggleLink myQuestionsToggleLink = new CollapseToggleLink("myQuestionsToggleLink", "myQuestionsToggleLink", "myQuestionsToggle");
		add(myQuestionsToggleLink);
		myQuestionsToggleLink.setVisible(myQuestionsOn && showXmlContent);

    	WebMarkupContainer questionContainer = new WebMarkupContainer("questionContainer");
		questionContainer.setOutputMarkupId(true);
    	add(questionContainer);
		
    	PopupSettings questionPopupSettings = ISIApplication.get().questionPopupSettings;
    	QuestionListView questionList = new QuestionListView("questionList", ISIApplication.get().getQuestionPopupPageClass(), questionPopupSettings, null, 
    			(ISISession.get().getTargetUserModel().getObject().getId()));  
		questionContainer.add(questionList);
		questionContainer.add(new WebMarkupContainer("qButtonVisible").setVisible(false));
				
		// teachers can't add new Qs
		add(new WebMarkupContainer("newQuestion").setVisible(false));
	}
	
	// Differs from superclass version in that it may omit the panel (if no student selected),
	// and will create a READ-ONLY tagging panel to view student's tags.
	protected void addTaggingPanel () {
		boolean myTagsOn = ISIApplication.get().isTagsOn();

		CollapseToggleLink myTagsToggleLink = new CollapseToggleLink("myTagsToggleLink", "myTagsToggleLink", "myTagsToggle");
		add(myTagsToggleLink);
		myTagsToggleLink.setVisible(myTagsOn && showXmlContent);

		if (showXmlContent) {
			ContentElement ce = responseService.getOrCreateContentElement(loc).getObject();
			add(new TagPanel("tagPanel", ce, ISIApplication.get().getTagLinkBuilder(), ISISession.get().getStudentModel().getObject()));
		} else {
			add(new WebMarkupContainer("tagPanel"));
		}
	}

	

}