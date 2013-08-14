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
import java.util.List;

import net.databinder.models.hib.HibernateObjectModel;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.cast.cwm.components.Icon;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.Role;
import org.cast.cwm.service.IEventService;
import org.cast.isi.ISISession;
import org.cast.isi.component.IDisplayFeedbackStatus;
import org.cast.isi.data.FeedbackMessage;
import org.cast.isi.page.ISIStandardPage;
import org.cast.isi.service.ISIResponseService;
import org.cast.isi.service.ISectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;


/**
 * This feedback panel is added via the XML.  It indicates whether there are feedback
 * messages and what the status of that message is.
 *
 */
public class ResponseFeedbackButtonPanel extends ISIPanel {

	private static final long serialVersionUID = 1L;
	private ResponseFeedbackPanel responseFeedbackPanel;
	private Role role;
	private Icon button;
	private IModel<Prompt> mPrompt;
	private List<FeedbackMessage> unreadStudentMessages;
	private List<FeedbackMessage> unreadTeacherMessages;
	private List<FeedbackMessage> messageList;

	@Inject
	private IEventService eventService;

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ResponseFeedbackButtonPanel.class);

	@Inject
	private ISectionService sectionService;

	public ResponseFeedbackButtonPanel(String id, final IModel<Prompt> mPrompt, ResponseFeedbackPanel p) {
		super(id);
		this.mPrompt = mPrompt;
		this.responseFeedbackPanel = p;
		this.role = ISISession.get().getUser().getRole();
		setOutputMarkupPlaceholderTag(true);

		messageList = getMessageList();
		unreadStudentMessages = getUnreadStudentMessages();
		unreadTeacherMessages = getUnreadTeacherMessages();
				
		AjaxFallbackLink<Void> link = new AjaxFallbackLink<Void>("link") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {

				responseFeedbackPanel.setPromptModel(mPrompt);
				responseFeedbackPanel.setMessageList(messageList);

				if (role.subsumes(Role.RESEARCHER)) {
					// Don't trigger any database changes if researcher clicks button.

				} else if (role.equals(Role.STUDENT) && !unreadTeacherMessages.isEmpty()) {
					for (FeedbackMessage m : unreadTeacherMessages) {
						m.setUnread(false);
						ISIResponseService.get().updateFeedbackMessage(new HibernateObjectModel<FeedbackMessage>(m), getPage());
					}

					sectionService.adjustMessageCount(ISISession.get().getUser(), ((ISIStandardPage) getPage()).getLoc(), Role.TEACHER, -1 * unreadTeacherMessages.size());
					unreadTeacherMessages.clear();

				} else if (!role.equals(Role.STUDENT) && !unreadStudentMessages.isEmpty()) {
					for (FeedbackMessage m : unreadStudentMessages) {
						m.setUnread(false);
						ISIResponseService.get().updateFeedbackMessage(new HibernateObjectModel<FeedbackMessage>(m), getPage());
					}
					
					sectionService.adjustMessageCount(ISISession.get().getStudent(), ((ISIStandardPage) getPage()).getLoc(), Role.STUDENT, -1 * unreadStudentMessages.size());
					unreadStudentMessages.clear();
				}
				responseFeedbackPanel.clearFeedbackMessageForm();
				responseFeedbackPanel.setCallingButton(ResponseFeedbackButtonPanel.this);
				eventService.saveEvent("messagepanel:view", mPrompt.getObject().toString(), ((ISIStandardPage) getPage()).getPageName());
				if (target != null) {
					target.add(responseFeedbackPanel);
					target.add(this);
					target.appendJavaScript(responseFeedbackPanel.getSidebarDialog().getOpenString());
					target.addChildren(getPage(), IDisplayFeedbackStatus.class);
				}				
			}
			
		};
		link.add(new AttributeModifier("href", ResponseFeedbackPanel.getDivName()));
		
		button = new Icon("button", new AbstractReadOnlyModel<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getObject() {
				if (role.equals(Role.STUDENT)) 
					if (unreadTeacherMessages.isEmpty())
						return "img/icons/teacher_comments.png";
					else 
						return "img/icons/teacher_comments_new.png";
				else
					if (messageList.isEmpty())
						return "img/icons/give_feedback.png";
					else if (!unreadStudentMessages.isEmpty())
						return "img/icons/new_feedback_from_student.png";
					else
						return "img/icons/view_feedback.png";
			}			
		});

		link.add(new AttributeModifier("title", true, new AbstractReadOnlyModel<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getObject() {
				if (role.equals(Role.STUDENT)) 
					if (unreadTeacherMessages.isEmpty())
						return (new ResourceModel("feedback.buttonTitle.viewTComments").getObject());
					else
						return (new ResourceModel("feedback.buttonTitle.newTComments").getObject());
				else
					if (messageList.isEmpty())
						return (new ResourceModel("feedback.buttonTitle.giveFeedback").getObject());
					else if (!unreadStudentMessages.isEmpty())
						return (new ResourceModel("feedback.buttonTitle.viewNewFeedback").getObject());
					else
						return (new ResourceModel("feedback.buttonTitle.viewFeedback").getObject());
			}			
		}));
		button.setOutputMarkupPlaceholderTag(true);
		link.add(button);

		// add the correct text for the button
		link.add(new Label("buttonText", new StringResourceModel("feedback.buttonText.${userRole}.${state}", this, Model.of(this), "default")));
		add(link);
		if (messageList.isEmpty() && role != Role.TEACHER)
			link.setVisible(false);
	}
	
	@Override
	protected void onDetach() {
		if (mPrompt != null) {
			mPrompt.detach();
		}
		super.onDetach();
	}

	public String getUserRole () {
		return role.name().toLowerCase();
	}
	
	public boolean isVisible() {
		return (role.equals(Role.TEACHER) || !messageList.isEmpty());
	}

	
	public List<FeedbackMessage> getUnreadStudentMessages() {
		List<FeedbackMessage> unreadStudentMessages = new ArrayList<FeedbackMessage>();
		for (FeedbackMessage m : messageList) {
			if (m.isUnread())
				if (m.getAuthor().getRole().equals(Role.STUDENT))
					unreadStudentMessages.add(m);
		}
		return unreadStudentMessages;
	}

	public List<FeedbackMessage> getUnreadTeacherMessages() {
		List<FeedbackMessage> unreadTeacherMessages = new ArrayList<FeedbackMessage>();
		for (FeedbackMessage m : messageList) {
			if (m.isUnread())
				if (!m.getAuthor().getRole().equals(Role.STUDENT))
					unreadTeacherMessages.add(m);
		}	
		return unreadTeacherMessages;
	}
	
	public List<FeedbackMessage> getMessageList() {
		if (role.equals(Role.STUDENT)) {
			return ISIResponseService.get().getFeedbackMessages(mPrompt, ISISession.get().getUser());
		} else {
			return ISIResponseService.get().getFeedbackMessages(mPrompt, ISISession.get().getStudent());
		}		
	}
	
	public String getState() {
		if (getMessageList().isEmpty())
			return "empty";
		if (role.equals(Role.STUDENT) && !getUnreadTeacherMessages().isEmpty())
			return "new";
		if (role.equals(Role.TEACHER) && !getUnreadStudentMessages().isEmpty())
			return "new";
		return "old";
	}	
}