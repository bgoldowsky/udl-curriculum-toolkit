/*
 * Copyright 2011-2015 CAST, Inc.
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

import lombok.Getter;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.Site;
import org.cast.cwm.data.User;
import org.cast.cwm.data.builders.UserCriteriaBuilder;
import org.cast.cwm.data.component.PeriodChoice;
import org.cast.cwm.data.component.UserChoice;
import org.cast.cwm.data.models.UserListModel;
import org.cast.cwm.data.models.UserModel;
import org.cast.isi.ISISession;

/**
 * A panel that displays Period and Student Drop Down Choice menus for the logged in teacher.  Changing the 
 * period or student changes those values in the session.
 * 
 * @author jbrookover
 *
 */
@SuppressWarnings("serial")
public abstract class PeriodStudentSelectPanel extends ISIPanel {

	@Getter private PeriodChoice periodChoice;
	@Getter private UserChoice studentChoice;
	private FeedbackPanel feedback;
	private Form<Void> periodStudentSelectForm;
	//private static final Logger log = LoggerFactory.getLogger(PeriodStudentSelectPanel.class);
	
	public PeriodStudentSelectPanel(String id, final boolean showStudents) {
		super(id);
		setOutputMarkupId(true);
		periodStudentSelectForm = new Form<Void>("periodStudentSelectForm") {

			@Override
			protected void onSubmit() {
				PeriodStudentSelectPanel.this.onFormSubmit();  // Subclass defined actions
				super.onSubmit();
			}
		};
    	add(periodStudentSelectForm);
    	periodStudentSelectForm.setOutputMarkupId(true);
    	
    	// Period Chooser
    	periodChoice = new PeriodChoice("periodChoice", ISISession.get().getCurrentPeriodModel()); // Set default period from session
    	periodChoice.add(new AttributeModifier("autocomplete", "off"));
		periodChoice.setOutputMarkupId(true);
		
		// Set Period onChange Behavior
		periodChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				ISISession.get().setStudentModel(null); // Reset session's selected student since period changed
				ISISession.get().setCurrentPeriodModel(periodChoice.getModel()); // Save period change in session
				// Save current site information
				Site currentSite = ISISession.get().getCurrentPeriodModel().getObject().getSite();
				IModel<Site> mCurrentSite = new Model<Site>(currentSite);
				ISISession.get().setCurrentSiteModel(mCurrentSite);
				if (showStudents) {
					studentChoice.setChoices(getUserListModel()); // Alert student drop-down of the change					
					studentChoice.setModel(new UserModel());
					target.add(studentChoice);
				}
				target.add(feedback); // Update Feedback Panel
				target.add(periodChoice);
				PeriodStudentSelectPanel.this.onPeriodUpdate(target);  // Subclass defined actions
			}	
		});
		// make sure the label is linked to the period dropdown		
		FormComponentLabel periodChoiceLabel = (new FormComponentLabel("periodChoiceLabel", periodChoice));
		periodStudentSelectForm.add(periodChoiceLabel);
		periodStudentSelectForm.add(periodChoice);
		
		// Student Chooser
		IModel<User> studentModel = ISISession.get().getStudentModel();
		if (studentModel == null) {
			studentModel = new UserModel();
		}
		studentChoice = new UserChoice("studentChoice", studentModel, getUserListModel());
    	periodStudentSelectForm.add(studentChoice);
		studentChoice.setNullValid(true);
		if (studentChoice.getModelObject() == null) { // If session did not have a student or student was not in the session's period, reset session's student
			ISISession.get().setStudentModel(null);
		}
		studentChoice.setOutputMarkupPlaceholderTag(true);
		
		// Set Student onChange Behavior
		studentChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				ISISession.get().setStudentModel(studentChoice.getModel()); // Save student change in session
				target.add(feedback);
				target.add(studentChoice);
				PeriodStudentSelectPanel.this.onStudentUpdate(target);  // Subclass defined actions
			}			
		});		
		if(!showStudents) {
			studentChoice.setVisible(false);
		}
		// make sure the label is linked to the student dropdown		
		FormComponentLabel studentChoiceLabel = (new FormComponentLabel("studentChoiceLabel", studentChoice));
		periodStudentSelectForm.add(studentChoiceLabel);
  	
    	feedback = new FeedbackPanel("feedback", new ContainerFeedbackMessageFilter(periodStudentSelectForm));
    	feedback.setOutputMarkupId(true);
    	feedback.setMaxMessages(1);
    	periodStudentSelectForm.add(feedback);
		
	}
	
	protected UserListModel getUserListModel() {
		UserCriteriaBuilder c = new UserCriteriaBuilder(); // see below - can we avoid this repetition?
		c.setGetAllUsers(false);
		c.setRole(Role.STUDENT);
		c.setPeriod(periodChoice.getModel());
		return new UserListModel(c);
	}
	
	@Override
	protected void onBeforeRender() {
		studentChoice.setModelObject(ISISession.get().getStudent());
		super.onBeforeRender();
	}

	/**
	 * Method that runs when the Period Drop Down is changed.
	 */
	protected abstract void onPeriodUpdate(AjaxRequestTarget target);
	
	/**
	 * Method that runs when the Student Drop Down is changed.
	 */
	protected abstract void onStudentUpdate(AjaxRequestTarget target);
	
	/**
	 * Method that runs when the form is submitted.
	 */
	protected abstract void onFormSubmit();
}