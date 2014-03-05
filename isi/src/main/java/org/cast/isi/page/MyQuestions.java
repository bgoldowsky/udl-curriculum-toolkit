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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.string.Strings;
import org.cast.cwm.data.ResponseMetadata;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.component.DeletePersistedObjectDialog;
import org.cast.cwm.data.models.PromptModel;
import org.cast.cwm.data.models.UserModel;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISISession;
import org.cast.isi.data.Question;
import org.cast.isi.panel.ResponseButtons;
import org.cast.isi.panel.ResponseList;
import org.cast.isi.service.IQuestionService;
import org.cast.isi.validator.QuestionNameValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;


/**  
 * Page to show and allow editing of all of a student's questions and evidence.
 * This page is very similar to QuestionPopup, combined with the Add New Question
 * dialog that appears on reading pages.
 * @author bgoldowsky
 *
 */
@AuthorizeInstantiation("STUDENT")
public class MyQuestions extends ISIStandardPage {

	private static final long serialVersionUID = 1L;

	private QuestionListView questionLister = null;
	private Question selectedQuestion;
	private IModel<Question> mSelectedQuestion;
	private WebMarkupContainer questionContainer;
	private WebMarkupContainer questionTitleContainer;
	private EditQuestionTitleForm editQuestionTitleForm;
	private UserModel mUser;
	private boolean isTeacher = false;
	protected ResponseMetadata questionsMetadata;

	@Inject
	protected IQuestionService questionService;

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(MyQuestions.class);

	public MyQuestions (PageParameters parameters) {
		super(parameters);

		setQuestionsMetadata(questionsMetadata);
		String pageTitleEnd = (new StringResourceModel("MyQuestions.pageTitle", this, null, "My Questions").getString());
		setPageTitle(pageTitleEnd);

		// set teacher flag and target user
		isTeacher = ISISession.get().getUser().getRole().subsumes(Role.TEACHER);
		if (isTeacher) {
			mUser = new UserModel(ISISession.get().getStudent());
		} else {
			mUser = new UserModel(ISISession.get().getUser());			
		}

		selectedQuestion = null;
        Long questionId = -1l;
        StringValue o = parameters.get("question");
        if(o.toString() != null) {
            questionId = Long.parseLong(o.toString());
        }

		mSelectedQuestion = questionService.getQuestionModelById(questionId);
		if (questionId != -1) {
			selectedQuestion = mSelectedQuestion.getObject();
			if (!selectedQuestion.getActive()) {
				setDefaultModel(new Model<Question>(null));
				selectedQuestion = null;
			}
		}

		// components on the left side
		questionContainer = new WebMarkupContainer("questionContainer");
		questionContainer.setOutputMarkupId(true);
		add(questionContainer);
		if (mUser.getObject() != null) {
			questionLister = new QuestionListView("question", ISIApplication.get().getMyQuestionsPageClass(), null, selectedQuestion);
			questionContainer.add(questionLister);
		} else {
			questionContainer.add(new WebMarkupContainer("question").add(new WebMarkupContainer("link")));
			questionContainer.setVisible(false);
		}
		questionContainer.add(new WebMarkupContainer("newQuestionButton").setVisible(!isTeacher));
		add(questionContainer);
		add(new NewQuestionForm("newQuestion"));

		// components on the right side of the form
		add(new WebMarkupContainer("noStudentSelected").setVisible(mUser.getObject() == null));
		WebMarkupContainer noQuestionSelected = new WebMarkupContainer("noQuestionSelected");
		add(noQuestionSelected);
		WebMarkupContainer noQuestions = new WebMarkupContainer("noQuestions");
		add(noQuestions);
		if (mUser.getObject() != null) {
			noQuestionSelected.setVisible((questionId==-1) && (!questionLister.getList().isEmpty()));
			noQuestions.setVisible((questionId==-1) && (questionLister.getList().isEmpty()));
		} else {
			noQuestionSelected.setVisible(false);
			noQuestions.setVisible(false);
		}
		questionTitleContainer = new WebMarkupContainer("questionTitleContainer");
		questionTitleContainer.setOutputMarkupPlaceholderTag(true);
		editQuestionTitleForm = new EditQuestionTitleForm("editQuestionTitleForm");
		add(questionTitleContainer);
		add(editQuestionTitleForm);

		if (selectedQuestion != null) {

			// Show Question Title
			questionTitleContainer.add(new Label("title", new PropertyModel<Question>(selectedQuestion, "text")));

			// only allow user created questions to be edited
			AjaxFallbackLink<Object> edit = new AjaxFallbackLink<Object>("edit") {
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget target) {
					questionTitleContainer.setVisible(false);
					editQuestionTitleForm.setVisible(true);
					editQuestionTitleForm.setModel(new CompoundPropertyModel<Question>(selectedQuestion));
					if (target != null) {
						target.add(questionTitleContainer);
						target.add(editQuestionTitleForm);
					}
				}
			};
			if (selectedQuestion.getOwner() == null || !selectedQuestion.getOwner().equals(mUser.getObject()) || isTeacher) {
				edit.setVisible(false);				
			}
			questionTitleContainer.add(edit);

			PromptModel mPrompt = new PromptModel(selectedQuestion.getPrompt());

			ResponseButtons responseButtons = new ResponseButtons("responseButtons", mPrompt, questionsMetadata, loc);
			responseButtons.setVisible(!isTeacher);
			add(responseButtons);

			ResponseList responseList = new ResponseList("responseList", mPrompt, questionsMetadata, loc, mUser);
			String context = "questions" + (isTeacher ? ".teacher" : ""  );
			responseList.setContext(context);
			responseList.setAllowEdit(!isTeacher);
			responseList.setAllowNotebook(false);
			responseList.setAllowWhiteboard(false);
			add(responseList);

		} else {
			questionTitleContainer.setVisible(false);
			add(new WebMarkupContainer("responseButtons").setVisible(false));
			add(new WebMarkupContainer("responseList").setVisible(false));
		}
	}

	protected class NewQuestionForm extends Form<Object> {
		private static final long serialVersionUID = 1L;
		Model<String> textModel = new Model<String>("");
		private FeedbackPanel feedback;

		public NewQuestionForm(String id) {
			super(id);
			TextArea<String> questionTextArea = new TextArea<String>("text", textModel);
			questionTextArea.add(new QuestionNameValidator(null))
					.setRequired(true)
					.add(new AttributeModifier("maxlength", "250"));
			add(new FormComponentLabel("questionLabel", questionTextArea));
			add(questionTextArea);
			add(new AjaxButton("submit") {
				private static final long serialVersionUID = 1L;

				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					super.onSubmit();
					String qstr = ((String)textModel.getObject());
					textModel.setObject("");
					if (qstr != null)
						qstr = qstr.trim();
					if (!Strings.isEmpty(qstr)) {
						if (qstr.length() > 250)
							qstr = qstr.substring(0, 250);
						questionService.createQuestion(mUser, qstr, getPageName());
						questionLister.doQuery();
						target.add(questionContainer);
						target.add(NewQuestionForm.this);
						target.add(feedback);
					}
					target.appendJavaScript("$('#newQuestionModal').hide();");
				}
				@Override
				protected void onError(AjaxRequestTarget target, Form<?> form) {
					super.onError(target, form);
					if (target != null)
						target.add(feedback);
				}				
			});

			add(feedback = new FeedbackPanel("feedback", new ContainerFeedbackMessageFilter(NewQuestionForm.this)));
			feedback.setOutputMarkupPlaceholderTag(true);
		}
	}

	protected class EditQuestionTitleForm extends Form<Question> {
		private static final long serialVersionUID = 1L;

		public EditQuestionTitleForm(String id) {
			super(id);
			this.setOutputMarkupPlaceholderTag(true);
			this.setVisible(false);
			addContent();
		}

		private void addContent() {
			TextField<String> questionName = new TextField<String>("text");
			questionName.add(new AttributeModifier("maxlength", "250"));
			questionName.add(new QuestionNameValidator(selectedQuestion));
			questionName.setRequired(true);
			add(questionName);

			add(new AjaxFallbackLink<Object>("cancel") {
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget target) {
					questionTitleContainer.setVisible(true);
					editQuestionTitleForm.setVisible(false);
					if (target != null) {
						target.add(questionTitleContainer);
						target.add(editQuestionTitleForm);	
					}				
				}
			});

			add(new AjaxSubmitLink("save") {
				private static final long serialVersionUID = 1L;

				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					questionTitleContainer.setVisible(true);
					editQuestionTitleForm.setVisible(false);
					if (target != null) {
						target.add(questionTitleContainer);
						target.add(editQuestionTitleForm);	
						target.add(questionContainer);
					}
				}

				@Override
				protected void onError(AjaxRequestTarget target, Form<?> form) {
					if (target != null) {
						target.add(editQuestionTitleForm);
					}
				}
			});

			FeedbackPanel f = new FeedbackPanel("feedback", new ContainerFeedbackMessageFilter(this));
			f.setMaxMessages(1);
			add(f);

			DeletePersistedObjectDialog<Question> dialog = new DeletePersistedObjectDialog<Question>("deleteModal", mSelectedQuestion) {
				private static final long serialVersionUID = 1L;

				@Override
				protected void deleteObject() {
					questionService.deleteQuestion(getModel(), getPageName());
					questionLister.doQuery();
					setResponsePage(ISIApplication.get().getMyQuestionsPageClass());
				}

				@Override
				protected void onBeforeRender() {
					this.setObjectName((new StringResourceModel("MyQuestions.delete.objectName", this, null, "Question").getString()));
					super.onBeforeRender();
				}
			};
			add(dialog);
            // TODO: causes this warning: WARN  org.apache.wicket.Localizer Tried to retrieve a localized string for a component that has not yet been added to the page. This can sometimes lead to an invalid or no localized resource returned. Make sure you are not calling Component#getString() inside your Component's constructor. Offending component: [EditQuestionTitleForm [Component id = editQuestionTitleForm]]
			//dialog.setObjectName((new StringResourceModel("MyQuestions.delete.objectName", this, null, "Question").getString()));
			add(new WebMarkupContainer("delete").add(dialog.getDialogBorder().getClickToOpenBehavior()));
		}

		@Override
		protected void onSubmit() {
			super.onSubmit();
			questionService.updateQuestion(selectedQuestion, getPageName());
		}
	}	

	public void setQuestionsMetadata(ResponseMetadata questionsMetadata) {
		this.questionsMetadata = ISIApplication.get().getResponseMetadata();
	}

	@Override
	public String getPageName() {
		return null;
	}

	@Override
	public String getPageType() {
		return "myquestions";
	}

	@Override
	public String getPageViewDetail() {
		return selectedQuestion != null ? selectedQuestion.getText() : null;
	}
}
