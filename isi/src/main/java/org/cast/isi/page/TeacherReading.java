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
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.cast.cwm.components.ClassAttributeModifier;
import org.cast.cwm.data.component.highlight.HighlightDisplayPanel;
import org.cast.cwm.tag.component.TagPanel;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISISession;
import org.cast.isi.ISIXmlSection;
import org.cast.isi.component.StateSavingCollapseBoxBehavior;
import org.cast.isi.component.StateSavingCollapseBoxBorder;
import org.cast.isi.data.ContentElement;
import org.cast.isi.data.PromptType;
import org.cast.isi.panel.ResponseList;
import org.cast.isi.panel.TeacherSectionCompleteTogglePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AuthorizeInstantiation("TEACHER")
public class TeacherReading extends Reading implements IHeaderContributor {

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


	// Differs from superclass version in that it creates a READ-ONLY page notes area with no edit buttons
	// view the student's notes
	protected void addNotesPanel () {
		setPageNotesMetadata();
		mNotesPrompt = responseService.getOrCreatePrompt(PromptType.PAGE_NOTES, loc);
		StateSavingCollapseBoxBorder noteBox = new StateSavingCollapseBoxBorder("noteBox", "noteToggle", null, getPageName());
		add(noteBox);
		noteBox.setVisible(showXmlContent);
		ResponseList responseList = new ResponseList ("responseList", mNotesPrompt, pageNotesMetadata, loc, mTargetUser);
		responseList.setContext("pagenote.teacher");
		responseList.setAllowNotebook(false);
		responseList.setAllowEdit(false);
		responseList.setAllowWhiteboard(ISIApplication.get().isWhiteboardOn());
		noteBox.add(responseList);
		WebMarkupContainer responseButtons = new WebMarkupContainer("responseButtons");
		noteBox.add(responseButtons);
		responseButtons.setVisible(false);
		noteBox.setVisible(ISIApplication.get().isPageNotesOn());
	}

	
	// Differs from superclass version in that it may omit the panel (if no student selected),
	// and will create a READ-ONLY tagging panel to view student's tags.
	protected void addTaggingPanel () {
		if (showXmlContent) {
			WebMarkupContainer tagBox = new WebMarkupContainer("tagBox");
			add(tagBox);
			Boolean toggleState = userPreferenceService.getUserPreferenceBoolean(ISISession.get().getUserModel(), "tagToggle");
			if (toggleState != null) {
				tagBox.add(new ClassAttributeModifier("open", !toggleState));
			}				
			tagBox.setVisible(ISIApplication.get().isTagsOn());

			StateSavingCollapseBoxBehavior behavior = new StateSavingCollapseBoxBehavior("tagToggle", getPageName(), "tagToggle");
			tagBox.add(new WebMarkupContainer("tagBoxToggle").add(behavior));

			ContentElement ce = responseService.getOrCreateContentElement(loc).getObject();
			tagBox.add(new TagPanel("tagPanel", ce, ISIApplication.get().getTagLinkBuilder(), ISISession.get().getStudentModel().getObject()));
		} else {
			add (new WebMarkupContainer("tagBox")
				.add(new WebMarkupContainer("tagBoxToggle"))
				.add(new WebMarkupContainer("tagPanel"))
				.setVisible(false));
		}
	}

	
	// panels are not visible without student
	@Override
	public void addHighlightPanel() {	
		if (showXmlContent) {
			super.addHighlightPanel();
			((HighlightDisplayPanel) get("highlightDisplayPanel")).setReadOnly(true);

		} else {
			add (new WebMarkupContainer("highlightBox")
				.add (new WebMarkupContainer("highlightControlPanel"))
				.setVisible(false));
			add (new WebMarkupContainer("highlightDisplayPanel").setVisible(false));
		}		
	}

	
	@SuppressWarnings("static-access")
	@Override
	protected void addQuestionsPanel () {
		StateSavingCollapseBoxBorder questionBox = new StateSavingCollapseBoxBorder("questionBox", "questionToggle", null, getPageName());
		add(questionBox);
		questionBox.setVisible(ISIApplication.get().isMyQuestionsOn() && (showXmlContent));
    	questionContainer = new WebMarkupContainer("questionContainer");
		questionContainer.setOutputMarkupId(true);
    	questionBox.add(questionContainer);
		PopupSettings questionPopupSettings = ISIApplication.get().questionPopupSettings;
    	questionList = new QuestionListView("question", QuestionPopup.class, questionPopupSettings, null, 
    			(ISISession.get().getTargetUserModel().getObject().getId()));  
		questionContainer.add(questionList);
		questionContainer.add(new WebMarkupContainer("qButtonVisible").setVisible(false));
				
		// teachers can't add new Qs
		add(new WebMarkupContainer("newQuestion").setVisible(false));
	}

}