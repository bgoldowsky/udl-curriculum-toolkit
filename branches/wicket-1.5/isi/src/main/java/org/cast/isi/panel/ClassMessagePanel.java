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

import org.apache.wicket.AttributeModifier;
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
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.validator.StringValidator.MaximumLengthValidator;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.component.DeletePersistedObjectDialog;
import org.cast.isi.data.ClassMessage;
import org.cast.isi.service.IISIResponseService;

import com.google.inject.Inject;


/**
 * The panel for displaying, editing and deleting the class message
 */
public class ClassMessagePanel extends Panel {

	private static final long serialVersionUID = 1L;

	private ClassMessage m;
	private IModel<Period> mPeriod;

	@Inject
	private IISIResponseService responseService;


	public ClassMessagePanel(String id, IModel<Period> mPeriod) {
		super(id);
		this.mPeriod = mPeriod;
		setOutputMarkupId(true);
	}
	
	@SuppressWarnings("unchecked")
	protected Form<ClassMessage> getClassMessageForm() {
		return (Form<ClassMessage>) get("classMessageForm");
	}

	private WebMarkupContainer getStaticContent() {
		return (WebMarkupContainer) get("static");
	}

	@Override
	protected void onInitialize() {
		addStatic();
		addForm();
		m = responseService.getClassMessage(mPeriod);
		if (m == null) {
			m = new ClassMessage();
			m.setMessage((new StringResourceModel("ManageClasses.noClassMessage", this, null, "No Class Message").getString()));
		}	
		setDefaultModel(new CompoundPropertyModel<ClassMessage>(m));
		getClassMessageForm().setModel(new CompoundPropertyModel<ClassMessage>(m));
		add(new AttributeModifier("style", "display:block"));
		super.onInitialize();
	}

	
	private void addStatic() {
		WebMarkupContainer staticContent = new WebMarkupContainer("static");
		add(staticContent);
		staticContent.setOutputMarkupPlaceholderTag(true);
		staticContent.add(new Label("message"));
		staticContent.add(new AjaxFallbackLink<Object>("edit") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				getStaticContent().setVisible(false);
				getClassMessageForm().setVisible(true);
				
				if (target != null) {
					target.add(ClassMessagePanel.this);
				}
			} 
		});
					
		DeletePersistedObjectDialog<ClassMessage> dialog = new DeletePersistedObjectDialog<ClassMessage>("deleteMessageModal", new Model<ClassMessage>(m) ) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void deleteObject() {
				responseService.deleteClassMessage(mPeriod);
				m = null;
			}
		};
		add(dialog);
		dialog.setObjectName((new StringResourceModel("ManageClasses.delete.objectName", this, null, "Class Message").getString()));
		dialog.setObjectName("Class Message");
		staticContent.add(new WebMarkupContainer("delete").add(dialog.getDialogBorder().getClickToOpenBehavior()));
	}
	
	private void addForm() {
		Form<ClassMessage> classMessageForm = new Form<ClassMessage>("classMessageForm");
		add(classMessageForm);
		classMessageForm.setOutputMarkupPlaceholderTag(true);
		classMessageForm.setVisible(false);
		classMessageForm.add(new TextArea<String>("message").add(new MaximumLengthValidator(255)).setRequired(true));
		classMessageForm.add(new AjaxSubmitLink("save") {
			private static final long serialVersionUID = 1L;
			
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				m = (ClassMessage) form.getModelObject();			
				responseService.setClassMessage(mPeriod, m.getMessage());
				getStaticContent().setVisible(true);
				getClassMessageForm().setVisible(false);
				if (target != null) {
					target.add(ClassMessagePanel.this);
				}
			}
			
			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				if (target != null) {
					target.add(ClassMessagePanel.this);
				}
			}
		});
		classMessageForm.add(new AjaxFallbackLink<Object>("cancel") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				getStaticContent().setVisible(true);
				getClassMessageForm().setVisible(false);
				if (target != null) {
					target.add(ClassMessagePanel.this);
				}
			} 
		});

		FeedbackPanel classMessageFeedback = new FeedbackPanel("feedback", new ContainerFeedbackMessageFilter(classMessageForm));
		classMessageForm.add(classMessageFeedback = new FeedbackPanel("feedback", new ContainerFeedbackMessageFilter(classMessageForm)));
		classMessageFeedback.setOutputMarkupPlaceholderTag(true);
	}

	
	@Override
	protected void onDetach() {
		if (mPeriod != null) {
			mPeriod.detach();
		}
		super.onDetach();
	}	
}