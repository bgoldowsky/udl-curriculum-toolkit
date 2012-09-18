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

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import net.databinder.hib.Databinder;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.ComponentFeedbackPanel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
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
import org.cast.isi.data.FeedbackMessage;
import org.cast.isi.page.ISIStandardPage;
import org.cast.isi.service.ISIResponseService;
import org.cast.isi.service.ISectionService;

import com.google.inject.Inject;

public class ResponseFeedbackPanel extends ISIPanel {

	private static final long serialVersionUID = 1L;
	private static String targetName = "#responseFeedbackPanel"; // Div id in Markup with # symbol
	private Form<FeedbackMessage> form;
	private ListView<FeedbackMessage> responses;
	private List<FeedbackMessage> messageList;
	private WebMarkupContainer responseContainer;
	private Component callingButton;
	private IModel<Prompt> promptM;
	
	@Getter private SidebarDialog sidebarDialog;

	@Inject
	private ICwmService cwmService;

	@Inject
	private ISectionService sectionService;

	@Inject
	private IEventService eventService;

	public ResponseFeedbackPanel(String id) {
		super(id);
		setOutputMarkupPlaceholderTag(true);
		
		sidebarDialog = new SidebarDialog("sidebarDialog", "Feedback", null);
		sidebarDialog.setMoveContainer(this);
		add(sidebarDialog);
		
		form = new Form<FeedbackMessage>("form") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void onSubmit() {
				super.onSubmit();
				FeedbackMessage m = getModelObject();
				User student;

				m.setAuthor(ISISession.get().getUser());
				m.setTimestamp(new Date());
				if (m.getAuthor().getRole().equals(Role.STUDENT)) {
					student = ISISession.get().getUser();
					sectionService.adjustMessageCount(student, ((ISIStandardPage) getPage()).getLoc(), Role.STUDENT, 1);
				} else {
					student = ISISession.get().getStudent();
					sectionService.adjustMessageCount(student, ((ISIStandardPage) getPage()).getLoc(), m.getAuthor().getRole(), 1);
				}
				m.setStudent(student);
				m.setPrompt(promptM.getObject());
				m.setLocation(((ISIStandardPage) getPage()).getLoc().getLocation());
				m.setUnread(true);
				m.setVisible(true);
				Databinder.getHibernateSession().save(m);
				cwmService.flushChanges();
				eventService.saveEvent("message:sent", String.valueOf(m.getId()), ((ISIStandardPage) getPage()).getPageName());
			}
			
			@Override
			public boolean isVisible() {
				return !ISISession.get().getUser().hasRole(Role.RESEARCHER); // Researchers cannot send messages
			}
		};
		form.setOutputMarkupPlaceholderTag(true);
		TextArea<String> messageField = new TextArea<String>("message");
		messageField.setRequired(true);
		form.add(messageField);
		form.add(new FormComponentLabel("messageLabel", messageField));
		final FeedbackPanel feedbackPanel = new ComponentFeedbackPanel("feedbackPanel", form);
		feedbackPanel.setOutputMarkupPlaceholderTag(true);
		form.add(feedbackPanel);
		AjaxSubmitLink sendLink = new AjaxSubmitLink("send") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				messageList.add(0, (FeedbackMessage) form.getModelObject());
				form.setDefaultModel(new CompoundPropertyModel<FeedbackMessage>(new FeedbackMessage()));
				if (target != null) {
					target.addComponent(form);
					target.addComponent(responseContainer);
					target.addComponent(callingButton);
				}
			}
			
			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				form.error("Please type a message.");
				if (target != null)
					target.addComponent(feedbackPanel);
			}		
		};
		form.add(sendLink);
		sidebarDialog.getBodyContainer().add(form);
		
		responses = new ListView<FeedbackMessage>("responses", new FeedbackListModel()) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<FeedbackMessage> item) {
				final FeedbackMessage m = (FeedbackMessage) item.getModelObject();
				item.add(new Label("date", DateFormat.getDateInstance(DateFormat.MEDIUM).format(m.getTimestamp())));
				item.add(new Label("time", DateFormat.getTimeInstance(DateFormat.MEDIUM).format(m.getTimestamp())));
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
						messageList.remove(m);
						if (m.isUnread()) {
							if (m.getAuthor().getRole().equals(Role.STUDENT)) {
								sectionService.adjustMessageCount(m.getStudent(), ((ISIStandardPage) getPage()).getLoc(), Role.STUDENT, -1);
							} else {
								sectionService.adjustMessageCount(m.getStudent(), ((ISIStandardPage) getPage()).getLoc(), m.getAuthor().getRole(), -1);
							}
						}
						target.addComponent(responseContainer);				
						target.addComponent(callingButton);				
					}
				};
				dialog.setObjectName("Feedback Message");
				item.add(dialog);
				
				// Delete Link
				item.add(new WebMarkupContainer("delete").add(dialog.getClickToOpenBehavior())
					.setVisible(ISISession.get().getUser().equals(m.getAuthor())));

				if (m.getAuthor().getRole().equals(Role.STUDENT)) {
					item.add(new SimpleAttributeModifier("class", "respStudent"));
				} else if (m.getAuthor().getRole().equals(Role.TEACHER)){
					item.add(new SimpleAttributeModifier("class", "respTeacher"));
				}
			}			
		};
		responseContainer = new WebMarkupContainer("responseContainer");
		responseContainer.setOutputMarkupPlaceholderTag(true);
		responseContainer.add(responses);
		sidebarDialog.getBodyContainer().add(responseContainer);
	}
	
	
	public void clearFeedbackMessageForm() {
		form.setDefaultModel(new CompoundPropertyModel<FeedbackMessage>(new FeedbackMessage()));		
	}

	
	private class FeedbackListModel extends AbstractReadOnlyModel<List<FeedbackMessage>> {
		private static final long serialVersionUID = 1L;

		@Override
		public List<FeedbackMessage> getObject() {
			return messageList;
		}		
	}

	
	public static String getDivName() {
		return targetName;
	}

	
	public void setPromptModel(IModel<Prompt> promptModel) {
		this.promptM = promptModel;
	}
	
	
	public void setMessageList (List<FeedbackMessage> l) {
		messageList = l;
	}

	
	public void setCallingButton(Component callingButton) {
		this.callingButton = callingButton;
		sidebarDialog.setVerticalReferencePointId(callingButton.getMarkupId());
	}

}