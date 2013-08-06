package org.cast.isi.panel;

import java.util.HashMap;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.cast.cwm.data.User;
import org.cast.isi.component.EditDisableLink;
import org.cast.isi.page.ManageClasses;

/**
 * This panel is used for each student row in the table to display relevant
 * student information
 */
public class StudentDisplayRowPanel extends Panel {
	private static final long serialVersionUID = 1L;

	public StudentDisplayRowPanel(String id, IModel<User> mUser, 
			final HashMap<Long, Boolean> flagMap) {
		super(id, mUser);
		setOutputMarkupId(true);
				
		// Labels to display student information when not actively editing
		add(new StudentFlagPanel("studentFlagPanelLabel", mUser.getObject(), flagMap));
		add(new Label("lastName", mUser.getObject().getLastName()));
		add(new Label("firstName", mUser.getObject().getFirstName()));
		add(new Label("email", mUser.getObject().getEmail()));
		add(new Label("username", mUser.getObject().getUsername()));
		add(new Label("password", new Model<String>("******")));
		
		// Link for editing a student (basically an Ajax request to hide student and show the form.
		EditDisableLink<Object> editLink = new EditDisableLink<Object>("editLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				FeedbackPanel feedback = getFeedbackPanel();
				
				findParent(ManageClasses.class).get("editStudentForm").setDefaultModel(getUserModel());
				findParent(ManageClasses.class).visitChildren(EditDisableLink.class, EditDisableLink.getVisitor(target, false));
				StudentEditRowPanel newStudentRow = new StudentEditRowPanel("studentPanel", getUserModel(), flagMap);
				StudentDisplayRowPanel.this.replaceWith(newStudentRow);
				if (target != null) {
					target.add(newStudentRow);
					target.add(feedback);
				}				
			}
		};
		add(editLink);
	}
	
	@SuppressWarnings("unchecked")
	private IModel<User> getUserModel() {
		return 	(IModel<User>) getDefaultModel();
	}

	private FeedbackPanel getFeedbackPanel() {
		return 	(FeedbackPanel) findParent(ManageClasses.class).getFeedback();
	}
}