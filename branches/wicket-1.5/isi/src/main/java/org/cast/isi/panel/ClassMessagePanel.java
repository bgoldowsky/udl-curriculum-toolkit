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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.validator.StringValidator.MaximumLengthValidator;
import org.cast.cwm.CwmSession;
import org.cast.cwm.data.Period;
import org.cast.isi.data.ClassMessage;
import org.cast.isi.service.IISIResponseService;

import com.google.inject.Inject;


/**
 * The panel for displaying and editing the class message
 */
public class ClassMessagePanel extends Panel {

	private static final long serialVersionUID = 1L;

	private ClassMessage m;
	private boolean hasClassMessage = true;
	private boolean editing = false;

	@Inject
	private IISIResponseService responseService;


	public ClassMessagePanel(String id) {
		super(id);
		setOutputMarkupId(true);
	}
	
	@Override
	protected void onInitialize() {
		// determine if there is already a class message
		m = responseService.getClassMessage(getCurrentPeriodModel());
		if (m == null) { // no class message
			hasClassMessage = false;
			ClassMessage defaultMessage = new ClassMessage();
			defaultMessage.setMessage(new ResourceModel("classMessage").getObject().toString());
			add(new MessageViewer("messageViewer", new CompoundPropertyModel<ClassMessage>(defaultMessage)));
			add(new MessageForm("messageForm", new CompoundPropertyModel<ClassMessage>(new ClassMessage())));
		} else {  // class message exists
			add(new MessageViewer("messageViewer", new CompoundPropertyModel<ClassMessage>(m)));
			add(new MessageForm("messageForm", new CompoundPropertyModel<ClassMessage>(m)));
		}
		super.onInitialize();
	}
	
	
	private class MessageViewer extends WebMarkupContainer {

		private static final long serialVersionUID = 1L;

		public MessageViewer(String id, CompoundPropertyModel<ClassMessage> compoundPropertyModel) {
			super(id, compoundPropertyModel);
			setOutputMarkupPlaceholderTag(true);
			
			add(new Label("message"));
			Label defaultIndicator = new Label("defaultIndicator", (new StringResourceModel("ManageClasses.noClassMessageDefaultIndicator", this, null, "(Default Message").getString())) {
				private static final long serialVersionUID = 1L;

				@Override
				protected void onConfigure() {
					setVisible(!hasClassMessage);
					super.onConfigure();
				}
			};
			add(defaultIndicator);
			defaultIndicator.setOutputMarkupPlaceholderTag(true);
			
			AjaxFallbackLink<Void> editLink = new AjaxFallbackLink<Void>("edit") {
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget target) {
					editing = true;
					target.add(getMessageForm());
					target.add(getMessageViewer());
				}
			};
			add(editLink);
		}	
		
		@Override
		protected void onConfigure() {
			super.onConfigure();
			setVisible(!editing);
		}		
	}
	
	
	private class MessageForm extends Form<ClassMessage> {

		private static final long serialVersionUID = 1L;

		public MessageForm(String id, IModel<ClassMessage> model) {
			super(id, model);
			setOutputMarkupPlaceholderTag(true);
			
			TextArea<String> message = new TextArea<String>("message");
			add(message);
			message.add(new MaximumLengthValidator(255));
			
			AjaxSubmitLink saveLink = new AjaxSubmitLink("save") {
				private static final long serialVersionUID = 1L;
				
				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					m = (ClassMessage) form.getModelObject();			
					responseService.setClassMessage(getCurrentPeriodModel(), m.getMessage());
					if (!hasClassMessage) {
						hasClassMessage = true;
					}
					getMessageViewer().setDefaultModel(form.getModel());
					editing = false;
					target.add(getMessageForm());
					target.add(getMessageViewer());
				}
				
				@Override
				protected void onError(AjaxRequestTarget target, Form<?> form) {
					target.add(getParent().get("feedback"));
				}
			};
			add(saveLink);

			AjaxFallbackLink<Void> cancelLink = new AjaxFallbackLink<Void>("cancel") {
			private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget target) {
					editing = false;
					target.add(getMessageForm());
					target.add(getMessageViewer());
				} 
			};
			add(cancelLink);
			
			FeedbackPanel classMessageFeedback = new FeedbackPanel("feedback", new ContainerFeedbackMessageFilter(this));
			add(classMessageFeedback);
			classMessageFeedback.setOutputMarkupPlaceholderTag(true);


		}

		@Override
		protected void onConfigure() {
			super.onConfigure();
			setVisible(editing);
		}		
	}


	private IModel<Period> getCurrentPeriodModel() {
		return 	CwmSession.get().getCurrentPeriodModel();
	}

	
	@SuppressWarnings("unchecked")
	private Form<ClassMessage> getMessageForm() {
		return (Form<ClassMessage>) get("messageForm");
	}

	
	private WebMarkupContainer getMessageViewer() {
		return (WebMarkupContainer) get("messageViewer");
	}

}