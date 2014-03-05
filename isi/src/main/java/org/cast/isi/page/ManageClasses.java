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

import java.util.Arrays;
import java.util.TreeSet;

import lombok.Getter;
import lombok.Setter;
import net.databinder.hib.Databinder;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.Site;
import org.cast.cwm.data.User;
import org.cast.cwm.data.validator.UniqueDataFieldValidator;
import org.cast.cwm.data.validator.UniqueUserInPeriodValidator;
import org.cast.cwm.service.ICwmService;
import org.cast.cwm.service.IEventService;
import org.cast.cwm.service.UserService;
import org.cast.isi.ISISession;
import org.cast.isi.component.EditDisableLink;
import org.cast.isi.component.MoveStudentPeriodForm;
import org.cast.isi.dialog.AddPeriodDialog;
import org.cast.isi.panel.ClassMessagePanel;
import org.cast.isi.panel.PeriodStudentSelectPanel;
import org.cast.isi.panel.StudentDisplayRowPanel;
import org.cast.isi.panel.StudentEditRowPanel;
import org.cast.isi.service.IISIResponseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Teacher page for managing student accounts.
 * 
 */
@AuthorizeInstantiation("TEACHER")
public class ManageClasses extends ISIStandardPage {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ManageClasses.class);
	
	@Getter @Setter
	private FeedbackPanel feedback;
	
	private ISortableDataProvider<User> studentListProvider;
	
	@Inject
	private IISIResponseService responseService;
	
	@Inject IEventService eventService;
	
	@Inject ICwmService cwmService;

	public ManageClasses(final PageParameters param) {
		super(param);
		setDefaultModel(getCurrentPeriodModel());

		setPageTitle(new StringResourceModel("ManageClasses.pageTitle", this, null, "Manage Classes").getString());
		
		// Period Title and Link to edit
		WebMarkupContainer periodTitle = new WebMarkupContainer("periodTitle"); 
		add(periodTitle);
		periodTitle.setOutputMarkupPlaceholderTag(true);
		periodTitle.add(new Label("periodName", new PropertyModel<String>(getCurrentPeriodModel(), "name")));
		periodTitle.add(new EditDisableLink<Object>("edit") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				ManageClasses.this.visitChildren(EditDisableLink.class, EditDisableLink.getVisitor(target, false));
				getPeriodTitleContainer().setVisible(false);
				getEditPeriodForm().setVisible(true);
				if (target != null) {
					target.add(getPeriodTitleContainer());
					target.add(getEditPeriodForm());
				}
			}
		});	
		// Edit Period Form
		EditPeriodForm editPeriodForm = new EditPeriodForm("editPeriodForm", getCurrentPeriodModel());
		add(editPeriodForm);
		editPeriodForm.setOutputMarkupPlaceholderTag(true);
		editPeriodForm.setVisible(false);
		
		// New Period
		EditDisableLink<Object> addPeriodButton = new EditDisableLink<Object>("addPeriodButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				AddPeriodDialog addPeriodDialog = new AddPeriodDialog();
				ISIStandardPage page = (ISIStandardPage) getPage();
				page.displayDialog(addPeriodDialog, target);
			}			
		};
		add(addPeriodButton);

				
		// "Add Student" button
		add(new EditDisableLink<Void>("addStudentButton") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				IModel<User> mUser = new CompoundPropertyModel<User>(UserService.get().newUser());
				ManageClasses.this.visitChildren(EditDisableLink.class, EditDisableLink.getVisitor(target, false));
				StudentEditRowPanel newPanel = new StudentEditRowPanel("newStudent", mUser);
				getEditStudentForm().setModel(mUser);
				getEditStudentForm().replace(newPanel);

				if (target != null) {
					target.add(newPanel);
					target.add(feedback);
				}
			}
		});
		
		// Form for editing student
		EditStudentForm editStudentForm = new EditStudentForm("editStudentForm");
		add(editStudentForm);
		
		// A row for creating a new student; hidden by default.
		editStudentForm.add(new WebMarkupContainer("newStudent").setVisible(false).setOutputMarkupPlaceholderTag(true));

		studentListProvider = UserService.get().getUncachedStudentListProvider(ISISession.get().getCurrentPeriodModel());

		// A list of students
		editStudentForm.add(new DataView<User>("studentList", studentListProvider) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(Item<User> item) {
				StudentDisplayRowPanel studentDisplayRow = new StudentDisplayRowPanel("studentPanel", item.getModel());
				item.add(studentDisplayRow);
			}
		});
		editStudentForm.setOutputMarkupId(true);
		
		// Feedback panel
		feedback = new FeedbackPanel("feedback");
		add(feedback);
		feedback.setOutputMarkupId(true);

		// Container for editing the Class Message
		add(new ClassMessagePanel("classMessagePanel"));
		
		// move student form
		MoveStudentPeriodForm moveForm = new MoveStudentPeriodForm("moveForm");
		add(moveForm);
		moveForm.setOutputMarkupId(true);
	}

	
	private IModel<Period> getCurrentPeriodModel() {
		return 	ISISession.get().getCurrentPeriodModel();
	}

	private WebMarkupContainer getPeriodTitleContainer() {
		return 	(WebMarkupContainer) get("periodTitle");
	}

	private EditPeriodForm getEditPeriodForm() {
		return 	(EditPeriodForm) get("editPeriodForm");
	}
	
	private EditStudentForm getEditStudentForm() {
		return 	(EditStudentForm) get("editStudentForm");
	}

	
	
	/**
	 * Form for editing or creating a {@link User}.  This is not a Databinder
	 * DataForm because that demanded far too much at instantiation.  Instead, this
	 * form's model is modified by a FormRowFragment child.
	 */
	private class EditStudentForm extends Form<User> {
		private static final long serialVersionUID = 1L;

		public EditStudentForm(String id) {
			super(id);
			
			// A somewhat ugly validator to ensure that no two students in the same period have the same full name.
			add(new UniqueUserInPeriodValidator() {

				private static final long serialVersionUID = 1L;

				@Override
				public FormComponent<String> getFirstNameComponent() {
					return UniqueUserInPeriodValidator.findFieldInForm(EditStudentForm.this, "firstName");
				}

				@Override
				public FormComponent<String> getLastNameComponent() {
					return UniqueUserInPeriodValidator.findFieldInForm(EditStudentForm.this, "lastName");
				}
			});
		}		
				
		@Override
		protected void onSubmit() {
			
			// Current User being saved
			User student = getModelObject();

			// Are we saving a new user?
			boolean isNewStudent = student.isTransient(); 

			// Add additional information if the user is new.
			if (isNewStudent) {
				student.setValid(true);
				student.setPeriods(new TreeSet<Period>(Arrays.asList(ISISession.get().getCurrentPeriodModel().getObject())));
				student.setSubjectId(student.getUsername());
				student.setRole(Role.STUDENT);
				Databinder.getHibernateSession().save(student); // Persist the new user
			}
			
			// Store changes to the database
			cwmService.flushChanges();

			// Log event
			String eventType = isNewStudent ? "student:create" : "student:modify";
			eventService.saveEvent(eventType, String.valueOf(student.getId()), getPageName()); 
		}
	}
	
	
	/**
	 * A form to edit the name of the period.
	 */
	private class EditPeriodForm extends Form<Period> {
		private static final long serialVersionUID = 1L;

		public EditPeriodForm(String id, IModel<Period> mPeriod) {
			super(id, mPeriod);

			FeedbackPanel feedback = new FeedbackPanel("feedback", new ContainerFeedbackMessageFilter(this));
			feedback.setOutputMarkupPlaceholderTag(true);
			add(feedback);

			TextField<String> periodName = new TextField<String>("periodName", new PropertyModel<String>(getModel(), "name"));
			add(periodName);
			
			// Ensure that no two periods in the same site have the same name.
			periodName.add(new UniqueDataFieldValidator<String>(getModel(), "name").limitScope("site", new PropertyModel<Site>(getModel(), "site")));
			periodName.add(new AttributeModifier("maxlength", "32"));
			periodName.setRequired(true);
			
			add(new AjaxFallbackLink<Object>("cancel") {
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget target) {
					getPeriodTitleContainer().setVisible(true);
					EditPeriodForm newEditPeriodForm = new EditPeriodForm("editPeriodForm", getCurrentPeriodModel());
					add(newEditPeriodForm);
					newEditPeriodForm.setOutputMarkupPlaceholderTag(true);
					newEditPeriodForm.setVisible(false);
					if (target != null) {
						target.add(getPeriodTitleContainer());
						target.add(getEditPeriodForm().replaceWith(newEditPeriodForm));	
					}
					ManageClasses.this.visitChildren(EditDisableLink.class, EditDisableLink.getVisitor(target, true));
				}
			});
			
			add(new AjaxSubmitLink("save") {
				private static final long serialVersionUID = 1L;

				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					cwmService.flushChanges();
					eventService.saveEvent("period:namechange", String.valueOf(getModelObject().getId()), getPageName());
					
					getEditPeriodForm().setVisible(false);
					getPeriodTitleContainer().setVisible(true);					
					if (target != null) {
						target.add(getPeriodTitleContainer());
						target.add(getEditPeriodForm());
						target.addChildren(getPage(), PeriodStudentSelectPanel.class);
					}	
					ManageClasses.this.visitChildren(EditDisableLink.class, EditDisableLink.getVisitor(target, true));
				}
				
				@Override
				protected void onError(AjaxRequestTarget target, Form<?> form) {
					target.add(form.get("feedback"));
				}
			});
			
		}		
	}

	
	
	@Override
	public String getPageName() {
		return null;
	}

	@Override
	public String getPageType() {
		return "manageclasses";
	}
}