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
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.Strings;
import org.cast.cwm.components.ClassAttributeModifier;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.ResponseMetadata;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.cwm.data.component.highlight.HighlightDisplayPanel;
import org.cast.cwm.data.models.UserModel;
import org.cast.cwm.service.IUserPreferenceService;
import org.cast.cwm.tag.component.TagPanel;
import org.cast.cwm.xml.XmlSection;
import org.cast.cwm.xml.XmlSectionModel;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISISession;
import org.cast.isi.ISIXmlComponent;
import org.cast.isi.ISIXmlSection;
import org.cast.isi.component.StateSavingCollapseBoxBehavior;
import org.cast.isi.component.StateSavingCollapseBoxBorder;
import org.cast.isi.data.ContentElement;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.data.PromptType;
import org.cast.isi.panel.HighlightControlPanel;
import org.cast.isi.panel.MiniGlossaryModal;
import org.cast.isi.panel.NoHighlightModal;
import org.cast.isi.panel.ResponseButtons;
import org.cast.isi.panel.ResponseFeedbackPanel;
import org.cast.isi.panel.ResponseList;
import org.cast.isi.panel.StudentSectionCompleteTogglePanel;
import org.cast.isi.service.IFeatureService;
import org.cast.isi.service.IISIResponseService;
import org.cast.isi.service.IQuestionService;
import org.cast.isi.validator.QuestionNameValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

@AuthorizeInstantiation("STUDENT")
public class Reading extends ISIStandardPage implements IHeaderContributor {
	
	private static final long serialVersionUID = 1L;

	protected final boolean showXmlContent;

	protected static final Logger log = LoggerFactory.getLogger(Reading.class);

	protected QuestionListView questionList;
	protected WebMarkupContainer questionContainer;
	protected IModel<User> mTargetUser;
	protected XmlSectionModel mSection;
	protected IModel<Prompt> mNotesPrompt;
	protected ResponseMetadata pageNotesMetadata = new ResponseMetadata();
	protected boolean showSectionToggleTextLink;

	@Inject
	protected IQuestionService questionService;

	@Inject
	protected IISIResponseService responseService;

	@Inject
	protected IFeatureService featureService;

	@Inject
	IUserPreferenceService userPreferenceService;
	

	public Reading (PageParameters parameters) {
		this(parameters, true);
	}
	
	/**
	 * Construct page, allowing for a reading page that doesn't show the 
	 * actual main page content (the XML)... this happens for instance when
	 * a teacher has not selected a student to work with. 
	 * @param parameters
	 * @param showXmlContent
	 */
	public Reading (PageParameters parameters, boolean showXmlContent) {
		super(parameters);
		this.showXmlContent = showXmlContent;
		showSectionToggleTextLink = featureService.isSectionToggleTextLinksOn();
		pageTitle = (new StringResourceModel("Reading.pageTitle", this, null, "Reading").getString());
		setPageTitle(pageTitle);

		mTargetUser = ISISession.get().getTargetUserModel();
		boolean teacher = ISISession.get().getUser().getRole().equals(Role.TEACHER);

    	setLoc(parameters);
    	
		addXmlComponent((ISIXmlSection) mSection.getObject());
		addSectionCompleteToggle((ISIXmlSection) mSection.getObject());
    	addNotesPanel();		
		addHighlightPanel();
		addTaggingPanel();
		addQuestionsPanel();
		addTopNavigation(mSection, teacher);
		addBottomNavigation(mSection, teacher);		
	}

	@Override
	public boolean hasMiniGlossary() {
		return true;
	}
	
	protected void setLoc(PageParameters parameters) {
		// setup the loc of this reading page, check the parameters, then
		// the bookmark and then finally the first page		
		if (parameters.getNamedKeys().contains("loc")) {
			loc = new ContentLoc(parameters.get("loc").toString());
		} else if (parameters.getNamedKeys().contains("pageNum")) {
			loc = new ContentLoc(ISIApplication.get().getPageNum(parameters.get("pageNum").toInt()));
		} else {
			loc = new ContentLoc(ISIApplication.get().getBookmarkLoc().getLocation());
		}
		if (loc == null || loc.getSection() == null) {
			loc = new ContentLoc(ISIApplication.get().getPageNum(1));
		}
				
    	ISIXmlSection section = loc.getSection();
    	if (section != null) {
    		ISISession.get().setBookmark(loc);    		
    	}
    	mSection = new XmlSectionModel(section);
	}
	
	protected void addTopNavigation(IModel<XmlSection> mSection, boolean teacher) {
		add(ISIApplication.get().getTopNavigation("navbar", mSection, teacher));
	}
	
	protected void addBottomNavigation(IModel<XmlSection> mSection, boolean teacher) {
		add (ISIApplication.get().getBottomNavigation("pageNavPanelBottom", mSection, teacher));
	}

	protected void addSectionCompleteToggle(ISIXmlSection section) {
		add(new StudentSectionCompleteTogglePanel("toggleCompletePanel", mSection, mTargetUser));
	}

	protected void addXmlComponent (ISIXmlSection section) {
		MiniGlossaryModal miniGlossaryModal = new MiniGlossaryModal("miniGlossaryModal", getPageName());
		add(miniGlossaryModal);

		ResponseFeedbackPanel responseFeedbackPanel = new ResponseFeedbackPanel("responseFeedbackPanel");
		add(responseFeedbackPanel);
		
		if (showXmlContent) {
			ISIXmlComponent xmlComponent = makeXmlComponent("xmlContent",  mSection);
			xmlComponent.setContentPage(getPageName());
			xmlComponent.setMiniGlossaryModal(miniGlossaryModal);
			xmlComponent.setResponseFeedbackPanel(responseFeedbackPanel);
			add(xmlComponent);
		} else {
			add (new WebMarkupContainer("xmlContent").setVisible(false));
		}
	}
	
	protected ISIXmlComponent makeXmlComponent (String wicketId, XmlSectionModel model) {
		ISIXmlComponent component = new ISIXmlComponent(wicketId, model, "student");
		component.setTransformParameter("lock-response", isLockResponse(model));
		component.setTransformParameter("delay-feedback", isDelayFeedback(model));
		if (userIsStudent())
			component.setTransformParameter("strip-class", "teacheronly");
		return component;
	}

	protected boolean userIsStudent() {
		return true;
	}

	private boolean isDelayFeedback(XmlSectionModel model) {
		ISIXmlSection section = (ISIXmlSection) model.getObject();
		return (section != null) && section.isDelayFeedback();
	}

	private boolean isLockResponse(XmlSectionModel model) {
		ISIXmlSection section = (ISIXmlSection) model.getObject();
		return (section != null) && section.isLockResponse();
	}

	protected void addNotesPanel () {
		setPageNotesMetadata();
		mNotesPrompt = responseService.getOrCreatePrompt(PromptType.PAGE_NOTES, loc);
		StateSavingCollapseBoxBorder noteBox = new StateSavingCollapseBoxBorder("noteBox", "noteToggle", null, getPageName());
		add(noteBox);
		ResponseList responseList = new ResponseList ("responseList", mNotesPrompt, pageNotesMetadata, loc, mTargetUser);
		responseList.setContext("pagenote");
		responseList.setAllowNotebook(ISIApplication.get().isNotebookOn());
		responseList.setAllowWhiteboard(ISIApplication.get().isWhiteboardOn());
		noteBox.add(responseList);
		ResponseButtons responseButtons = new ResponseButtons("responseButtons", mNotesPrompt, pageNotesMetadata, loc);
		responseButtons.setContext("pagenote");
		noteBox.add(responseButtons);
		noteBox.setVisible(ISIApplication.get().isPageNotesOn());
	}

	
	protected void setPageNotesMetadata() {
		pageNotesMetadata.addType("TEXT");
		pageNotesMetadata.addType("AUDIO");
	}

	public void addHighlightPanel() {	
		boolean highlightsPanelOn = ISIApplication.get().isHighlightsPanelOn();

		StateSavingCollapseBoxBorder highlightBox = new StateSavingCollapseBoxBorder("highlightBox", "highlightToggle", "globalHighlight", getPageName());
		add(highlightBox);
		highlightBox.setVisible(highlightsPanelOn);
		highlightBox.add(new HighlightControlPanel("highlightControlPanel", loc, mSection));		
		HighlightDisplayPanel highlightDisplayPanel = new HighlightDisplayPanel("highlightDisplayPanel", 
					responseService.getOrCreatePrompt(PromptType.PAGEHIGHLIGHT, loc), 
					ISISession.get().getTargetUserModel());
		highlightDisplayPanel.setVisible(highlightsPanelOn);
		add(highlightDisplayPanel);
		add(new NoHighlightModal("noHighlightModal"));
	}
	
	protected void addTaggingPanel () {
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
		tagBox.add(new TagPanel("tagPanel", ce, ISIApplication.get().getTagLinkBuilder()).setRenderBodyOnly(true));
	}
	
	protected void addQuestionsPanel () {
		StateSavingCollapseBoxBorder questionBox = new StateSavingCollapseBoxBorder("questionBox", "questionToggle", null, getPageName());
		add(questionBox);
		questionBox.setVisible(ISIApplication.get().isMyQuestionsOn());
    	questionContainer = new WebMarkupContainer("questionContainer");
    	questionBox.add(questionContainer);
		questionContainer.setOutputMarkupId(true);
		PopupSettings questionPopupSettings = ISIApplication.questionPopupSettings;
    	questionList = new QuestionListView("question", ISIApplication.get().getQuestionPopupPageClass(), questionPopupSettings, null, null);
		questionContainer.add(questionList);
		questionContainer.add(new WebMarkupContainer("qButtonVisible"));
    	add(new NewQuestionForm("newQuestion"));
	}
	
	protected class NewQuestionForm extends Form<Object> {
		private static final long serialVersionUID = 1L;
		Model<String> textModel = new Model<String>("");
		private FeedbackPanel feedback;

		public NewQuestionForm(String id) {
			super(id);
			add(new TextArea<String>("text", textModel)
					.add(new QuestionNameValidator(null))
					.setRequired(true)
					.add(new AttributeModifier("maxlength", "250")));
			add(new AjaxButton("submit") {
				private static final long serialVersionUID = 1L;

				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					super.onSubmit();
					String qstr = ((String) textModel.getObject());
					textModel.setObject("");
					if (qstr != null)
						qstr = qstr.trim();
					if (!Strings.isEmpty(qstr)) {
						if (qstr.length() > 250)
							qstr = qstr.substring(0, 250);
						questionService.createQuestion(new UserModel(mTargetUser.getObject()), qstr, getPageName());
						questionList.doQuery();
						target.add(questionContainer);
						target.add(NewQuestionForm.this);
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

	
	public void renderHead(IHeaderResponse response) {
		renderThemeCSS(response, "css/highlight.css");
		super.renderHead(response);
	}
	
	@Override
	public String getPageType() {
		return "reading";
	}
	
	@Override
	public String getPageName() {
		return loc.getLocation();
	}
	@Override
	protected void onDetach() {
		if (mTargetUser != null)
			mTargetUser.detach();
		if (mNotesPrompt != null)
			mNotesPrompt.detach();
		super.onDetach();
	}
	
}