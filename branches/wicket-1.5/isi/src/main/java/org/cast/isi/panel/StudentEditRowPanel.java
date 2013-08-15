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

import java.util.HashMap;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.cast.cwm.data.User;
import org.cast.cwm.data.validator.UniqueUserFieldValidator;
import org.cast.cwm.data.validator.UniqueUserFieldValidator.Field;
import org.cast.isi.ISISession;
import org.cast.isi.component.EditDisableLink;
import org.cast.isi.component.MoveStudentPeriodForm;
import org.cast.isi.page.ManageClasses;

/**
 * This fragment is used for each student row in the table to display relevant
 * student information when you are editing
 */
public class StudentEditRowPanel extends Panel {
	private static final long serialVersionUID = 1L;


	public StudentEditRowPanel(String id, IModel<User> mUser, final HashMap<Long, Boolean> flagMap) {
		super(id, mUser);
		setOutputMarkupId(true);
		
//		add(new StudentFlagPanel("studentFlagPanel", mUser.getObject(), flagMap).setVisible(!newStudent));

		TextField<String> lastName = new TextField<String>("lastName", new PropertyModel<String>(mUser, "lastName"));
		lastName.setRequired(true);
		lastName.add(new AttributeModifier("maxlength", "32"));
		add(lastName);
				
		TextField<String> firstName = new TextField<String>("firstName", new PropertyModel<String>(mUser, "firstName"));
		firstName.setRequired(true);
		firstName.add(new AttributeModifier("maxlength", "32"));
		add(firstName);

		// E-mail Address
		TextField<String> email = new TextField<String>("email", new PropertyModel<String>(mUser, "email"));
		email.add(EmailAddressValidator.getInstance());
		email.add(new UniqueUserFieldValidator(mUser, Field.EMAIL));
		add(email);

		TextField<String> userName = new TextField<String>("username", new PropertyModel<String>(mUser, "username"));
		userName.add(new AttributeModifier("maxlength", "32"));
		userName.setRequired(true);
		userName.add(new UniqueUserFieldValidator(mUser, Field.USERNAME));
		add(userName);
				
		// RSAPasswordTextField does not seem to work well with Ajax
		PasswordTextField password = new PasswordTextField("password", new Model<String>()) {

			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			@Override
			public void updateModel() {
				if (getConvertedInput() != null) {
					Form<User> editForm = (Form<User>)findParent(ManageClasses.class).get("editStudentForm");
					editForm.getModelObject().setPassword(getConvertedInput());
				}
			}
		};
		password.add(new AttributeModifier("maxlength", "32"));
		password.setRequired(isNewStudent());
		add(password);
		
		add(new AjaxSubmitLink("save") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				FeedbackPanel feedback = getFeedbackPanel();
				findParent(ManageClasses.class).visitChildren(EditDisableLink.class, EditDisableLink.getVisitor(target, true));
				
				// New Student hides the form editor and refreshes entire Form.
				if (isNewStudent()) {
					target.add(findParent(ManageClasses.class).get("editStudentForm"));
					StudentEditRowPanel.this.replaceWith(new WebMarkupContainer("newStudent").setVisible(false).setOutputMarkupPlaceholderTag(true));
				} else {
					StudentDisplayRowPanel displayStudentRowPanel = new StudentDisplayRowPanel("studentPanel", getUserModel(), flagMap);
					StudentEditRowPanel.this.replaceWith(displayStudentRowPanel);
					target.add(displayStudentRowPanel);
				}
				target.add(feedback);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				if (target != null) {
					target.add(getFeedbackPanel());
				}
			}
		});
				
		add(new AjaxFallbackLink<Object>("cancel") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				findParent(ManageClasses.class).visitChildren(EditDisableLink.class, EditDisableLink.getVisitor(target, true));

				FeedbackPanel feedback = getFeedbackPanel();
				
				// New Student just hides the form and replaces with placeholder.
				if (isNewStudent()) {
					WebMarkupContainer placeholder = new WebMarkupContainer("newStudent");
					placeholder.setVisible(false).setOutputMarkupPlaceholderTag(true);
					StudentEditRowPanel.this.replaceWith(placeholder);
					target.add(placeholder);
				} else {
					StudentDisplayRowPanel displayStudentRowPanel = new StudentDisplayRowPanel("studentPanel", getUserModel(), flagMap);
					StudentEditRowPanel.this.replaceWith(displayStudentRowPanel);
					target.add(displayStudentRowPanel);
				}
				
				target.add(feedback);
			}
		});
		
		add(new AjaxFallbackLink<Object>("move") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				MoveStudentPeriodForm moveForm = getMoveForm();
				moveForm.setUser(getUserModel().getObject());
				moveForm.setDefaultModel(getUserModel());
				
				if (target != null) {
					target.add(StudentEditRowPanel.this);
					target.add(getMoveForm());
					String moveButtonMarkupId = this.getMarkupId();
					target.appendJavaScript("$('#moveModal').show();");
					target.appendJavaScript("matchVerticalPosition('" + moveButtonMarkupId + "', \'moveModal');");
				}
			}

			@Override
			public boolean isVisible() {
				return ((!isNewStudent() && ISISession.get().getUser().getPeriods().size() > 1) && !getUserModel().getObject().isTransient());
			}

		}.setVisible(!isNewStudent()));
	}
	
	@SuppressWarnings("unchecked")
	private IModel<User> getUserModel() {
		return 	(IModel<User>) getDefaultModel();
	}
	
	private MoveStudentPeriodForm getMoveForm() {
		return	(MoveStudentPeriodForm) getPage().get("moveForm");
	}
	
	private FeedbackPanel getFeedbackPanel() {
		return 	(FeedbackPanel) findParent(ManageClasses.class).getFeedback();
	}
	
	private boolean isNewStudent() {
		IModel<User> mUser = getUserModel();
		if (mUser == null || mUser.getObject() == null || mUser.getObject().isTransient()) {
			return true;
		}
		return false;
	}
}