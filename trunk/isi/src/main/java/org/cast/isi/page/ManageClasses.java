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
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import lombok.Setter;
import net.databinder.hib.Databinder;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.StringValidator.MaximumLengthValidator;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.Site;
import org.cast.cwm.data.User;
import org.cast.cwm.data.component.DeletePersistedObjectDialog;
import org.cast.cwm.data.component.PeriodChoice;
import org.cast.cwm.data.validator.UniqueDataFieldValidator;
import org.cast.cwm.data.validator.UniqueUserFieldValidator;
import org.cast.cwm.data.validator.UniqueUserFieldValidator.Field;
import org.cast.cwm.data.validator.UniqueUserInPeriodValidator;
import org.cast.cwm.service.CwmService;
import org.cast.cwm.service.EventService;
import org.cast.cwm.service.UserService;
import org.cast.isi.ISISession;
import org.cast.isi.component.AddPeriodPanel;
import org.cast.isi.data.ClassMessage;
import org.cast.isi.data.StudentFlag;
import org.cast.isi.panel.PeriodStudentSelectPanel;
import org.cast.isi.panel.StudentFlagPanel;
import org.cast.isi.service.IISIResponseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Teacher page for managing student accounts.
 * 
 * @author jbrookover
 */
@AuthorizeInstantiation("TEACHER")
public class ManageClasses extends ISIStandardPage {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ManageClasses.class);
	private final HashMap<Long, Boolean> flagMap;
	private WebMarkupContainer periodTitle;
	private EditStudentForm editStudentForm;
	private EditPeriodForm editPeriodForm; 
	private AddPeriodPanel addPeriodPanel;
	private FeedbackPanel feedback;
	private MoveForm moveForm;
	private ISortableDataProvider<User> studentListProvider;
	
	@Inject
	private IISIResponseService responseService;
	
	
	public ManageClasses(final PageParameters param) {
		super(param);

		setPageTitle(new StringResourceModel("ManageClasses.pageTitle", this, null, "Manage Classes").getString());
		
		// Period Title and Link to edit
		periodTitle = new WebMarkupContainer("period-title"); 
		add(periodTitle);
		periodTitle.setOutputMarkupPlaceholderTag(true);
		periodTitle.add(new Label("name", new PropertyModel<String>(ISISession.get().getCurrentPeriodModel(), "name")));
		periodTitle.add(new AjaxFallbackLink<Object>("edit") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				periodTitle.setVisible(false);
				editPeriodForm.setVisible(true);
				if (target != null) {
					target.addComponent(periodTitle);
					target.addComponent(editPeriodForm);
				}
			}
		});	
		// Edit Period Form
		editPeriodForm = new EditPeriodForm("editPeriodForm", ISISession.get().getCurrentPeriodModel());
		add(editPeriodForm);
		editPeriodForm.setOutputMarkupPlaceholderTag(true);
		editPeriodForm.setVisible(false);
		
		// New Period
		addPeriodPanel = new AddPeriodPanel("addPeriodPanel");
		add(addPeriodPanel);
		addPeriodPanel.setOutputMarkupPlaceholderTag(true);
		addPeriodPanel.setVisible(false);
		AjaxFallbackLink<Object> addPeriodButton = new AjaxFallbackLink<Object>("addPeriodButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				addPeriodPanel.setVisible(true);
				if (target != null) {
					target.addComponent(addPeriodPanel);
				}				
			}			
		};
		add(addPeriodButton);


		// Load flags for this period and this teacher
		// Do just once so we're not querying for every single flag.
		flagMap = new HashMap<Long, Boolean>();
		List<StudentFlag> l = responseService.getAllFlags();
		for (StudentFlag f : l) {
			flagMap.put(f.getFlagee().getId(), true);
		}
				
		// "Add Student" button
		add(new EditLink<Void>("addStudentButton") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				IModel<User> user = new CompoundPropertyModel<User>(UserService.get().newUser());
				FormRowFragment newFragment = new FormRowFragment("newStudent", user);
				editStudentForm.setModel(user);
				editStudentForm.replace(newFragment);
				ManageClasses.this.visitChildren(EditLink.class, EditLink.getVisitor(target, false));
				
				if (target != null) {
					target.addComponent(newFragment);
					target.addComponent(feedback);
				}
			}
		});
		
		// Form for editing student
		editStudentForm = new EditStudentForm("editStudentForm");
		add(editStudentForm);
		
		// A row for creating a new student; hidden by default.
		editStudentForm.add(new WebMarkupContainer("newStudent").setVisible(false).setOutputMarkupPlaceholderTag(true));

		studentListProvider = UserService.get().getUncachedUserListProvider(ISISession.get().getCurrentPeriodModel());

		// A list of students
		editStudentForm.add(new DataView<User>("studentList", studentListProvider) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(Item<User> item) {
				item.add(new DisplayRowFragment("studentFragment", item.getModel()));
			}
		});
		editStudentForm.setOutputMarkupId(true);
		
		// Feedback panel
		feedback = new FeedbackPanel("feedback");
		add(feedback);
		feedback.setOutputMarkupId(true);

		// Container for editing the Class Message
		add(new ClassMessageContainer("classMessage"));
		
		// move student form
		moveForm = new MoveForm("moveForm");
		add(moveForm);
		moveForm.setOutputMarkupId(true);
	}

	
	/**
	 * The form for moving students from one period to another
	 */
	protected class MoveForm extends Form<User> {
		private static final long serialVersionUID = 1L;
		private PeriodChoice periodChoiceMove;
		@Setter private User user = null;

		public MoveForm(String id) {
			super(id);
			
			add(new Label("lastName",  new Model<String>() {
				private static final long serialVersionUID = 1L;

				@Override
				public String getObject() {
					if (getDefaultModelObject() == null)
						return "";
					else {
						return user.getLastName();
					}
				}
			}));
			add(new Label("firstName",  new Model<String>() {
				private static final long serialVersionUID = 1L;

				@Override
				public String getObject() {
					if (getDefaultModelObject() == null)
						return "";
					else {
						return user.getFirstName();
					}
				}
			}));
			add(new Label("currentPeriod", new PropertyModel<String>(ISISession.get().getCurrentPeriodModel(), "name")));
			periodChoiceMove = new PeriodChoice("newPeriod", ISISession.get().getCurrentPeriodModel());
			add(periodChoiceMove);
			FeedbackPanel f = new FeedbackPanel("feedback", new ContainerFeedbackMessageFilter(this));
			add(f);
			
			AjaxSubmitLink moveLink = new AjaxSubmitLink("moveLink"){
				private static final long serialVersionUID = 1L;

				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					ManageClasses.this.visitChildren(EditLink.class, EditLink.getVisitor(target, true));
					target.addChildren(getPage(), PeriodStudentSelectPanel.class);
					target.addComponent(editStudentForm);
					target.addComponent(periodTitle);
					target.appendJavascript("$('#moveModal').hide();");
				}
									
			};
			add(moveLink);
			
		}
		
		@Override
		protected void onSubmit() {
			super.onSubmit();
			User currentUser = getModelObject();
			Period currentPeriod = currentUser.getPeriods().first();
			Period newPeriod = periodChoiceMove.getModelObject();
			
			// Ensure that someone else does not have the same first and last name in the destination period 
			IModel<User> mOtherUser = UserService.get().getByFullnameFromPeriod(currentUser.getFirstName(), currentUser.getLastName(), periodChoiceMove.getModel());
			boolean error = false;
			if (mOtherUser != null && mOtherUser.getObject() != null && !mOtherUser.getObject().getId().equals(currentUser.getId()) ) {
				error("Move Failed: A student with that name already exists in " + newPeriod.getName() + ". \n");
				error = true;
			} else if (newPeriod == currentPeriod) {
				error("Cannot move student to the same period");
				error = true;			
			}

			if (!error) {
				currentUser.getPeriods().clear();
				currentUser.getPeriods().add(newPeriod);
				
				CwmService.get().flushChanges();
				EventService.get().saveEvent("student:periodmove", "Student: " + currentUser.getId() + 
						"; From PeriodId " + currentPeriod.getId() + " to PeriodId " + newPeriod.getId(), getPageName());
			}	

		}		
	}
	

	/**
	 * This fragment is used for each student row in the table to display relevant
	 * student information
	 */
	private class DisplayRowFragment extends Fragment {
		private static final long serialVersionUID = 1L;

		public DisplayRowFragment(String id, final IModel<User> mUser) {
			super(id, "displayRowFragment", ManageClasses.this, mUser);
			setOutputMarkupId(true);
			
			// Labels to display student information when not actively editing
			add(new StudentFlagPanel("studentFlagPanelLabel", mUser.getObject(), flagMap));
			add(new Label("lastName", mUser.getObject().getLastName()));
			add(new Label("firstName", mUser.getObject().getFirstName()));
			add(new Label("email", mUser.getObject().getEmail()));
			add(new Label("username", mUser.getObject().getUsername()));
			add(new Label("password", new Model<String>("******")));
			
			// Link for editing a student (basically an Ajax request to hide student and show the form.
			EditLink<Object> editLink = new EditLink<Object>("editLink") {
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget target) {
					findParent(EditStudentForm.class).setModel(mUser);
					FormRowFragment newFragment = new FormRowFragment("studentFragment", mUser);
					DisplayRowFragment.this.replaceWith(newFragment);
					ManageClasses.this.visitChildren(EditLink.class, EditLink.getVisitor(target, false));
					
					if (target != null) {
						target.addComponent(newFragment);
						target.addComponent(feedback);
					}
				}
			};
			add(editLink);
		}
	}
	

	/**
	 * This fragment is used for each student row in the table to display relevant
	 * student information when you are editing
	 */
	private class FormRowFragment extends Fragment {
		private static final long serialVersionUID = 1L;
		private boolean newStudent = false;

		public FormRowFragment(String id, final IModel<User> mUser) {
			super(id, "editRowFragment", ManageClasses.this, mUser);
			setOutputMarkupId(true);
			newStudent = mUser.getObject().isTransient();
			
			add(new StudentFlagPanel("studentFlagPanel", mUser.getObject(), flagMap).setVisible(!newStudent));

			TextField<String> lastName = new TextField<String>("lastName", new PropertyModel<String>(mUser, "lastName"));
			lastName.setRequired(true);
			lastName.add(new SimpleAttributeModifier("maxlength", "32"));
			add(lastName);
					
			TextField<String> firstName = new TextField<String>("firstName", new PropertyModel<String>(mUser, "firstName"));
			firstName.setRequired(true);
			firstName.add(new SimpleAttributeModifier("maxlength", "32"));
			add(firstName);

			// E-mail Address
			TextField<String> email = new TextField<String>("email", new PropertyModel<String>(mUser, "email"));
			email.add(EmailAddressValidator.getInstance());
			email.add(new UniqueUserFieldValidator(mUser, Field.EMAIL));
			add(email);

			TextField<String> userName = new TextField<String>("username", new PropertyModel<String>(mUser, "username"));
			userName.add(new SimpleAttributeModifier("maxlength", "32"));
			userName.setRequired(true);
			userName.add(new UniqueUserFieldValidator(mUser, Field.USERNAME));
			add(userName);
					
			// RSAPasswordTextField does not seem to work well with Ajax
			// TODO: Fix that
			PasswordTextField password = new PasswordTextField("password", new Model<String>()) {

				private static final long serialVersionUID = 1L;

				@Override
				public void updateModel() {
					if (getConvertedInput() != null) {
						((EditStudentForm) getForm()).getModelObject().setPassword(getConvertedInput());
					}
					setModelObject(null);
				}
			};
			password.add(new SimpleAttributeModifier("maxlength", "32"));
			password.setRequired(mUser.getObject().isTransient());
			add(password);
			
			add(new AjaxSubmitLink("save") {
				private static final long serialVersionUID = 1L;

				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					
					// New Student hides the form editor and refreshes entire Form.
					if (newStudent) {
						target.addComponent(findParent(EditStudentForm.class));
						FormRowFragment.this.replaceWith(new WebMarkupContainer("newStudent").setVisible(false).setOutputMarkupPlaceholderTag(true));
					} else {
						DisplayRowFragment newFragment = new DisplayRowFragment("studentFragment", mUser);
						FormRowFragment.this.replaceWith(newFragment);
						target.addComponent(newFragment);
					}
					ManageClasses.this.visitChildren(EditLink.class, EditLink.getVisitor(target, true));
					target.addComponent(feedback);
				}

				@Override
				protected void onError(AjaxRequestTarget target, Form<?> form) {
					if (target != null) {
						target.addComponent(feedback);
					}
				}
			});
					
			add(new AjaxFallbackLink<Object>("cancel") {
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget target) {
					
					// New Student just hides the form and replaces with placeholder.
					if (newStudent) {
						WebMarkupContainer placeholder = new WebMarkupContainer("newStudent");
						placeholder.setVisible(false).setOutputMarkupPlaceholderTag(true);
						FormRowFragment.this.replaceWith(placeholder);
						target.addComponent(placeholder);
					} else {
						DisplayRowFragment newFragment = new DisplayRowFragment("studentFragment", mUser);
						FormRowFragment.this.replaceWith(newFragment);
						target.addComponent(newFragment);
					}
					
					ManageClasses.this.visitChildren(EditLink.class, EditLink.getVisitor(target, true));
					target.addComponent(feedback);
				}
			});
			
			add(new AjaxFallbackLink<Object>("move") {
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget target) {
					moveForm.setUser(mUser.getObject());
					moveForm.setDefaultModel(mUser);
					
					if (target != null) {
						target.addComponent(FormRowFragment.this);
						target.addComponent(moveForm);
						String moveButtonMarkupId = this.getMarkupId();
						target.appendJavascript("$('#moveModal').show();");
						target.appendJavascript("matchVerticalPosition('" + moveButtonMarkupId + "', \'moveModal');");
					}
				}

				@Override
				public boolean isVisible() {
					return ((!newStudent && ISISession.get().getUser().getPeriods().size() > 1) && !mUser.getObject().isTransient());
				}

			}.setVisible(!newStudent));
		}
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
			CwmService.get().flushChanges();

			// Log event
			String eventType = isNewStudent ? "student:create" : "student:modify";
			EventService.get().saveEvent(eventType, String.valueOf(student.getId()), getPageName()); 
		}
	}
	
	
	/**
	 * A form to edit the name of the period.
	 */
	private class EditPeriodForm extends Form<Period> {
		private static final long serialVersionUID = 1L;

		public EditPeriodForm(String id, IModel<Period> mPeriod) {
			super(id, mPeriod);
			
			TextField<String> periodName = new TextField<String>("name", new PropertyModel<String>(getModel(), "name"));
			add(periodName);
			
			// Ensure that no two periods in the same site have the same name.
			periodName.add(new UniqueDataFieldValidator<String>(getModel(), "name").limitScope("site", new PropertyModel<Site>(getModel(), "site")));
			periodName.add(new SimpleAttributeModifier("maxlength", "32"));
			periodName.setRequired(true);
			
			add(new AjaxFallbackLink<Object>("cancel") {
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget target) {
					periodTitle.setVisible(true);
					editPeriodForm.setVisible(false);
					if (target != null) {
						target.addComponent(periodTitle);
						target.addComponent(editPeriodForm);	
					}
				}
			});
			
			add(new AjaxSubmitLink("save") {
				private static final long serialVersionUID = 1L;

				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					editPeriodForm.setVisible(false);
					periodTitle.setVisible(true);					
					if (target != null) {
						target.addComponent(periodTitle);
						target.addComponent(editPeriodForm);
						target.addChildren(getPage(), PeriodStudentSelectPanel.class);
					}	
				}
				
				@Override
				protected void onError(AjaxRequestTarget target, Form<?> form) {
					target.addComponent(editPeriodForm);
				}
			});
			
			FeedbackPanel f = new FeedbackPanel("feedback", new ContainerFeedbackMessageFilter(this));
			f.setMaxMessages(1);
			add(f);
		}
		
		@Override
		protected void onSubmit() {
			CwmService.get().flushChanges();
			EventService.get().saveEvent("period:namechange", String.valueOf(getModelObject().getId()), getPageName());
		}
	}


	
	
	/**
	 * The container for displaying, editing and deleting the class message
	 */
	public class ClassMessageContainer extends WebMarkupContainer {
		private static final long serialVersionUID = 1L;
		private WebMarkupContainer staticContent;
		private Form<ClassMessage> classMessageForm;
		private FeedbackPanel classMessageFeedback;
		private ClassMessage m;
		
		public ClassMessageContainer(String id) {
			super(id);
			m = responseService.getClassMessage(ISISession.get().getCurrentPeriodModel());
			setOutputMarkupId(true);
			addStatic();
			addForm();
		}
		
		@Override
		protected void onBeforeRender() { 
			if (m == null) {
				m = new ClassMessage();
				m.setMessage((new StringResourceModel("ManageClasses.noClassMessage", this, null, "No Class Message").getString()));
			}	
			setDefaultModel(new CompoundPropertyModel<ClassMessage>(m));
			classMessageForm.setModel(new CompoundPropertyModel<ClassMessage>(m));
			add(new SimpleAttributeModifier("style", "display:block"));
			super.onBeforeRender();
		}
		
		private void addStatic() {
			staticContent = new WebMarkupContainer("static");
			add(staticContent);
			staticContent.setOutputMarkupPlaceholderTag(true);
			staticContent.add(new Label("message"));
			staticContent.add(new AjaxFallbackLink<Object>("edit") {

				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget target) {
					staticContent.setVisible(false);
					classMessageForm.setVisible(true);
					
					if (target != null) {
						target.addComponent(ClassMessageContainer.this);
					}
				} 
			});
						
			DeletePersistedObjectDialog<ClassMessage> dialog = new DeletePersistedObjectDialog<ClassMessage>("deleteMessageModal", new Model<ClassMessage>(m) ) {
				private static final long serialVersionUID = 1L;

				@Override
				protected void deleteObject() {
					responseService.deleteClassMessage(ISISession.get().getCurrentPeriodModel());
					m = null;
				}
			};
			add(dialog);
			dialog.setObjectName((new StringResourceModel("ManageClasses.delete.objectName", this, null, "Class Message").getString()));
			dialog.setObjectName("Class Message");
			staticContent.add(new WebMarkupContainer("delete").add(dialog.getDialogBorder().getClickToOpenBehavior()));
		}
		
		private void addForm() {
			classMessageForm = new Form<ClassMessage>("classMessageForm") {
				private static final long serialVersionUID = 1L;
				
				@Override
				protected void onSubmit() {
					super.onSubmit();
					m = getModelObject();			
					responseService.setClassMessage(ISISession.get().getCurrentPeriodModel(), m.getMessage());
				}
			};
			add(classMessageForm);
			classMessageForm.setOutputMarkupPlaceholderTag(true);
			classMessageForm.setVisible(false);
			classMessageForm.add(new TextArea<String>("message").add(new MaximumLengthValidator(255)).setRequired(true));
			classMessageForm.add(new AjaxSubmitLink("save") {
				private static final long serialVersionUID = 1L;
				
				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					staticContent.setVisible(true);
					classMessageForm.setVisible(false);
					if (target != null) {
						target.addComponent(ClassMessageContainer.this);
					}
				}
				
				@Override
				protected void onError(AjaxRequestTarget target, Form<?> form) {
					if (target != null) {
						target.addComponent(ClassMessageContainer.this);
					}
				}
			});
			classMessageForm.add(new AjaxFallbackLink<Object>("cancel") {
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget target) {
					staticContent.setVisible(true);
					classMessageForm.setVisible(false);
					if (target != null) {
						target.addComponent(ClassMessageContainer.this);
					}
				} 
			});
			
			classMessageForm.add(classMessageFeedback = new FeedbackPanel("feedback", new ContainerFeedbackMessageFilter(classMessageForm)));
			classMessageFeedback.setOutputMarkupPlaceholderTag(true);
		}
	}
	
	/**
	 * A simple link that can be searched for (and disabled).  Also adds
	 * a class attribute if disabled.
	 * 
	 * @author jbrookover
	 *
	 * @param <T>
	 */
	public static abstract class EditLink<T> extends AjaxFallbackLink<T> {
		private static final long serialVersionUID = 1L;
		
		public EditLink(String id) {
			super(id);
		}
		
		@Override
		protected void onComponentTag(ComponentTag tag) {
			super.onComponentTag(tag);
			tag.put("class", "button" + (isEnabled() ? "" : " off"));
		}
		
		/**
		 * Get a visitor that will visit all {@link EditLink} instances and enable/disable them.
		 * 
		 * @param target
		 * @param enable
		 * @return
		 */
		public static IVisitor<EditLink<?>> getVisitor(final AjaxRequestTarget target, final boolean enable) {
			return new IVisitor<EditLink<?>>() {

				public Object component(EditLink<?> link) {
					link.setEnabled(enable);
					target.addComponent(link);
					return IVisitor.CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER;
				}
			};
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