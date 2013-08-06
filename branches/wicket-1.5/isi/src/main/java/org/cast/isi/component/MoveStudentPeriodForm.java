package org.cast.isi.component;

import lombok.Setter;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.User;
import org.cast.cwm.data.component.PeriodChoice;
import org.cast.cwm.service.ICwmService;
import org.cast.cwm.service.IEventService;
import org.cast.cwm.service.UserService;
import org.cast.isi.ISISession;
import org.cast.isi.page.ManageClasses;
import org.cast.isi.panel.PeriodStudentSelectPanel;

import com.google.inject.Inject;

/**
 * The form for moving students from one period to another
 */
public class MoveStudentPeriodForm extends Form<User> {
	private static final long serialVersionUID = 1L;

	private PeriodChoice periodChoiceMove;
	@Setter private User user = null;

	@Inject IEventService eventService;
	
	@Inject ICwmService cwmService;

	public MoveStudentPeriodForm(String id) {
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
				findParent(ManageClasses.class).visitChildren(EditDisableLink.class, EditDisableLink.getVisitor(target, true));
				target.addChildren(getPage(), PeriodStudentSelectPanel.class);
				target.add(findParent(ManageClasses.class).get("editStudentForm"));
//				target.add(MoveStudentPeriodForm.this.manageClasses.getEditStudentForm());
				target.add(getPeriodTitleContainer());
				target.appendJavaScript("$('#moveModal').hide();");
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
			
			cwmService.flushChanges();
			eventService.saveEvent("student:periodmove", "Student: " + currentUser.getId() + 
					"; From PeriodId " + currentPeriod.getId() + " to PeriodId " + newPeriod.getId(), null);
		}	

	}
	
	WebMarkupContainer getPeriodTitleContainer() {
		return 	(WebMarkupContainer) getPage().get("periodTitle");
	}


}