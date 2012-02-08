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
import org.cast.isi.CollapseBoxBehavior;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISISession;
import org.cast.isi.data.ContentElement;
import org.cast.isi.data.PromptType;
import org.cast.isi.panel.HighlightControlPanel;
import org.cast.isi.panel.ResponseList;
import org.cast.isi.service.ISIResponseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AuthorizeInstantiation("TEACHER")
public class TeacherReading extends Reading implements IHeaderContributor {

	protected static final Logger log = LoggerFactory.getLogger(TeacherReading.class);
	
	public TeacherReading(final PageParameters parameters) {
		// If there is no student model, tell super constructor that we won't be displaying XML content.
		super(parameters, ISISession.get().getStudentModel() != null);
	}

	
	// Differs from superclass version in that it creates a READ-ONLY page notes area with no edit buttons
	// view the student's notes
	protected void addNotesPanel () {
		setPageNotesMetadata();
		mNotesPrompt = ISIResponseService.get().getOrCreatePrompt(PromptType.PAGE_NOTES, loc);
		WebMarkupContainer notesbox = new WebMarkupContainer("notesbox") {
			private static final long serialVersionUID = 1L;
			@Override
			protected void onBeforeRender() {
				super.onBeforeRender();
				if (mNotesPrompt != null || !(mNotesPrompt.getObject().getResponses().size() == 0))
					this.add(new ClassAttributeModifier("open"));
			}
		};
		add(notesbox);
		notesbox.setVisible(showXmlContent);
		notesbox.add(new WebMarkupContainer("collapseBoxToggle").add(new CollapseBoxBehavior("onclick", "pagenotes", getPageName())));
		ResponseList responseList = new ResponseList ("responseList", mNotesPrompt, pageNotesMetadata, loc, null);
		responseList.setContext("teacher.pagenote");
		responseList.setAllowNotebook(false);
		responseList.setAllowEdit(false);
		responseList.setAllowWhiteboard(ISIApplication.get().isWhiteboardOn());
		notesbox.add(responseList);
		WebMarkupContainer responseButtons = new WebMarkupContainer("responseButtons");
		notesbox.add(responseButtons);
		responseButtons.setVisible(false);
		notesbox.setVisible(ISIApplication.get().isPageNotesOn());
	}

	
	// Differs from superclass version in that it may omit the panel (if no student selected),
	// and will create a READ-ONLY tagging panel to view student's tags.
	protected void addTaggingPanel () {
		if (showXmlContent) {
			ContentElement ce = ISIResponseService.get().getOrCreateContentElement(loc).getObject();
			WebMarkupContainer tagsBox = new WebMarkupContainer("tagsBox");
			add(tagsBox);
			tagsBox.setVisible(ISIApplication.get().isTagsOn());
			tagsBox.add(new WebMarkupContainer("tagCollapseToggle").add(new CollapseBoxBehavior("onclick", "tagpanel:reading", getPageName())));
			tagsBox.add(new TagPanel("tagPanel", ce, ISIApplication.get().getTagLinkBuilder(), ISISession.get().getStudentModel().getObject()));
		} else {
			add (new WebMarkupContainer("tagsBox")
				.add(new WebMarkupContainer("tagCollapseToggle"))
				.add(new WebMarkupContainer("tagPanel"))
				.setVisible(false));
		}
	}

	
	// panels are not visible without student
	@Override
	public void addHighlightPanel() {	
		if (showXmlContent) {
			add(new HighlightControlPanel("highlightControlPanel", ISIResponseService.get().getOrCreatePrompt(PromptType.HIGHLIGHTLABEL, loc), mSection)
											.setVisible(ISIApplication.get().isHighlightsPanelOn()));
			HighlightDisplayPanel highlightDisplayPanel = new HighlightDisplayPanel("highlightDisplayPanel", ISIResponseService.get().getOrCreatePrompt(PromptType.PAGEHIGHLIGHT, loc), 
																ISISession.get().getStudentModel());
			add(highlightDisplayPanel);
			highlightDisplayPanel.setVisible(ISIApplication.get().isHighlightsPanelOn());
			highlightDisplayPanel.setReadOnly(true);

		} else {
			add (new WebMarkupContainer("highlightControlPanel").setVisible(false));
			add (new WebMarkupContainer("highlightDisplayPanel").setVisible(false));
		}		
	}

	
	@SuppressWarnings("static-access")
	@Override
	protected void addQuestionsPanel () {
    	// Always open this panel if there are questions
		WebMarkupContainer questionBox = new WebMarkupContainer("questionBox") {
			private static final long serialVersionUID = 1L;
			@Override
			protected void onBeforeRender() {
				super.onBeforeRender();
				if (questionList.size() > 0) {
					this.add(new ClassAttributeModifier("open"));
				}
			}
		};
		add(questionBox);
		questionBox.setVisible(ISIApplication.get().isMyQuestionsOn() && (showXmlContent));
		questionBox.add(new WebMarkupContainer("questionBoxToggle")
					.add(new CollapseBoxBehavior("onclick", "questionspanel", getPageName())));
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