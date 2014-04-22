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

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.datetime.PatternDateConverter;
import org.apache.wicket.datetime.markup.html.basic.DateLabel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.ComponentFeedbackPanel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.markup.repeater.util.ModelIteratorAdapter;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.cwm.data.component.AjaxDeletePersistedObjectDialog;
import org.cast.cwm.service.ICwmService;
import org.cast.cwm.service.IEventService;
import org.cast.isi.ISISession;
import org.cast.isi.component.IDisplayFeedbackStatus;
import org.cast.isi.data.FeedbackMessage;
import org.cast.isi.page.ISIStandardPage;
import org.cast.isi.service.IISIResponseService;
import org.cast.isi.service.ISIResponseService;
import org.cast.isi.service.ISectionService;

import com.google.inject.Inject;

public class ResponseFeedbackPanel extends ISIPanel {

	private static final long serialVersionUID = 1L;

	private static String targetName = "#responseFeedbackPanel"; // Div id in Markup with # symbol
	private Component callingButton;  // used to determine vertical position
	private IModel<Prompt> mPrompt;
	private static final String DEFAULT_DATE_PATTERN =  "MMMM d, yyyy hh:mm aaa";
	
	@Inject
	private ICwmService cwmService;

	@Inject
	private ISectionService sectionService;

	@Inject
	private IEventService eventService;
	
	@Inject
	private IISIResponseService responseService;

	public ResponseFeedbackPanel(String id) {
		super(id);
		setOutputMarkupPlaceholderTag(true);		
	}
	
	@Override
	protected void onInitialize() {
		SidebarDialog sidebarDialog = new SidebarDialog("sidebarDialog", "Feedback", null);
		sidebarDialog.setMoveContainer(this);
		add(sidebarDialog);
			
		FeedbackMessageForm form = new FeedbackMessageForm("feedbackMessageForm", getNewFeedbackMessageModel());
		add(form);
		sidebarDialog.getBodyContainer().add(form);
		
		WebMarkupContainer responseContainer = new WebMarkupContainer("responseContainer");
		responseContainer.setOutputMarkupPlaceholderTag(true);
		sidebarDialog.getBodyContainer().add(responseContainer);
		RefreshingFeedbackView responses = new RefreshingFeedbackView("responses");
		responseContainer.add(responses);

		super.onInitialize();
	}

	
	private IModel<FeedbackMessage> getNewFeedbackMessageModel() {
		return new CompoundPropertyModel<FeedbackMessage>(new FeedbackMessage());
	}

	private User getUser() {
		return ISISession.get().getUser();
	}			

	private IModel<User> getTargetUserModel() {
		return ISISession.get().getTargetUserModel();
	}			

	public void clearFeedbackMessageForm() {
		getFeedbackMessageForm().setDefaultModel(new CompoundPropertyModel<FeedbackMessage>(new FeedbackMessage()));		
	}
	
	public static String getDivName() {
		return targetName;
	}
	
	public SidebarDialog getSidebarDialog() {
		return (SidebarDialog) get("sidebarDialog");
	}

	public FeedbackMessageForm getFeedbackMessageForm() {
		return (FeedbackMessageForm) getSidebarDialog().getBodyContainer().get("feedbackMessageForm");
	}

	protected WebMarkupContainer getResponseContainer() {
		return (WebMarkupContainer) getSidebarDialog().getBodyContainer().get("responseContainer");
	}

	public void setPromptModel(IModel<Prompt> promptModel) {
		this.mPrompt = promptModel;
	}
		
	public void setCallingButton(Component callingButton) {
		this.callingButton = callingButton;
		getSidebarDialog().setVerticalReferencePointId(callingButton.getMarkupId());
	}
	
	@Override
	protected void detachModel() {
		if (mPrompt != null)
			mPrompt.detach();
		super.detachModel();
	}


	public class FeedbackMessageForm extends Form<FeedbackMessage> {
		private static final long serialVersionUID = 1L;

		public FeedbackMessageForm(String id, IModel<FeedbackMessage> mFeedbackMessage) {
			super(id, mFeedbackMessage);
			setOutputMarkupPlaceholderTag(true);
		}

		@Override
		public String getValidatorKeyPrefix() {
			return "FeedbackMessageForm";
		}

		@Override
		protected void onInitialize() {
			TextArea<String> messageField = new TextArea<String>("message");
			messageField.setRequired(true);
			add(messageField);
			add(new FormComponentLabel("messageLabel", messageField));

			FeedbackPanel feedbackPanel = new ComponentFeedbackPanel("feedbackPanel", messageField);
			feedbackPanel.setOutputMarkupPlaceholderTag(true);
			add(feedbackPanel);

			AjaxSubmitLink sendLink = new AjaxSubmitLink("send") {
				private static final long serialVersionUID = 1L;

				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					responseService.saveFeedbackMessage(getFeedbackMessageModel(), mPrompt);

					User student = getTargetUserModel().getObject();
					if (getAuthorRole().equals(Role.STUDENT)) {
						sectionService.adjustMessageCount(student, ((ISIStandardPage) getPage()).getLoc(), Role.STUDENT, 1);
					} else {
						sectionService.adjustMessageCount(student, ((ISIStandardPage) getPage()).getLoc(), getAuthorRole(), 1);
					}
					eventService.saveEvent("message:sent", String.valueOf(getFeedbackMessageId()), ((ISIStandardPage) getPage()).getPageName());
					clearFeedbackMessageForm();
					if (target != null) {
						target.add(form);
						target.add(getResponseContainer());
						target.add(callingButton);
					}
					super.onSubmit();
				}
				
				@Override
				protected void onError(AjaxRequestTarget target, Form<?> form) {
					target.add(getFeedbackPanel());
				}
			};
			add(sendLink);
			super.onInitialize();
		}

		@Override
		protected void onConfigure() {
			setVisible(!ISISession.get().getUser().hasRole(Role.RESEARCHER)); //researchers don't view
			super.onConfigure();
		}

		private Component getFeedbackPanel() {
			return get("feedbackPanel");
		}

		@SuppressWarnings("unchecked")
		private IModel<FeedbackMessage> getFeedbackMessageModel() {
			return (IModel<FeedbackMessage>) getDefaultModel();
		}
		
		private Role getAuthorRole() {
			return ((FeedbackMessage)getDefaultModelObject()).getAuthor().getRole();
		}

		private Long getFeedbackMessageId() {
			return ((FeedbackMessage)getDefaultModelObject()).getId();
		}
	}
	
	
	public class RefreshingFeedbackView extends RefreshingView<FeedbackMessage> {

		private static final long serialVersionUID = 1L;

		public RefreshingFeedbackView(String id) {
			super(id);
		}

		@Override
		protected Iterator<IModel<FeedbackMessage>> getItemModels() {
			List<FeedbackMessage> feedbackMessageList = new ArrayList<FeedbackMessage>(); 
			if (mPrompt != null && mPrompt.getObject() != null)
				feedbackMessageList = ISIResponseService.get().getFeedbackMessages(mPrompt, ISISession.get().getTargetUserModel().getObject());
			
			return new ModelIteratorAdapter<FeedbackMessage>(feedbackMessageList.iterator()) {
				@Override
				protected IModel<FeedbackMessage> model(FeedbackMessage object) {
					return new CompoundPropertyModel<FeedbackMessage>(object);
				}
			};
		}

		@Override
		protected void populateItem(Item<FeedbackMessage> item) {
			FeedbackMessage m = (FeedbackMessage) item.getModelObject();
			item.add(new DateLabel("date", new Model<Date>(m.getTimestamp()), new PatternDateConverter(DEFAULT_DATE_PATTERN, true)));
			item.add(new Label("name", m.getAuthor().getFullName()));
			item.add(new Label("message", m.getMessage()));

			// Delete Dialog
			AjaxDeletePersistedObjectDialog<FeedbackMessage> dialog = new AjaxDeletePersistedObjectDialog<FeedbackMessage>("deleteModal", new Model<FeedbackMessage>(m)) {
				private static final long serialVersionUID = 1L;
				
				@SuppressWarnings("unchecked")
				@Override
				protected void deleteObject(AjaxRequestTarget target) {
					FeedbackMessage m = (FeedbackMessage) getDefaultModelObject();
					ISIResponseService.get().deleteFeedbackMessage((IModel<FeedbackMessage>) getDefaultModel());
					if (m.isUnread()) {
						if (m.getAuthor().getRole().equals(Role.STUDENT)) {
							sectionService.adjustMessageCount(m.getStudent(), ((ISIStandardPage) getPage()).getLoc(), Role.STUDENT, -1);
						} else {
							sectionService.adjustMessageCount(m.getStudent(), ((ISIStandardPage) getPage()).getLoc(), m.getAuthor().getRole(), -1);
						}
					}
					if (target != null) {
						target.add(getResponseContainer());				
						target.add(callingButton);				
						target.addChildren(getPage(), IDisplayFeedbackStatus.class);
					}
				}
			};
			dialog.setObjectName("Feedback Message");
			item.add(dialog);
			
			// Delete Link
			item.add(new WebMarkupContainer("delete").add(dialog.getClickToOpenBehavior())
				.setVisible(getUser().equals(m.getAuthor())));

			if (m.getAuthor().getRole().equals(Role.STUDENT)) {
				item.add(new AttributeModifier("class", "respStudent"));
			} else if (m.getAuthor().getRole().equals(Role.TEACHER)){
				item.add(new AttributeModifier("class", "respTeacher"));
			}
		}	
	}	
}