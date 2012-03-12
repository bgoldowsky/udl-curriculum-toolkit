package org.cast.isi.panel;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.ResponseMetadata;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.cwm.data.builders.UserCriteriaBuilder;
import org.cast.cwm.data.models.UserListModel;
import org.cast.cwm.data.models.UserModel;
import org.cast.isi.ISISession;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.data.ISIPrompt;

public class PeriodResponseListPanel extends Panel {
	
	@Getter @Setter
	protected boolean showNames;
	
	protected static ResponseMetadata responseMetadata = new ResponseMetadata();
	static {
		responseMetadata.addType("HTML");
		responseMetadata.addType("AUDIO");
		responseMetadata.addType("SVG");
		responseMetadata.addType("UPLOAD");
	}
	
	private static final long serialVersionUID = 1L;

	public PeriodResponseListPanel(String wicketId, IModel<Prompt> mPrompt) {
		super(wicketId);
		setOutputMarkupId(true);
		
		ContentLoc location = ((ISIPrompt)mPrompt.getObject()).getContentElement().getContentLocObject();

		List<User> studentList = getUserListModel().getObject();

		// iterate over all the users to get the responses for the prompt
		RepeatingView rv1 = new RepeatingView("studentRepeater");
		add(rv1);
		
		for (User student : studentList) {
			WebMarkupContainer studentContainer = new WebMarkupContainer(rv1.newChildId());
			studentContainer.add(new Label("studentName", student.getFullName()) {
				private static final long serialVersionUID = 1L;
				@Override
				public boolean isVisible() {
					return showNames;
				}
			});
			rv1.add(studentContainer);
			
			// list all of the responses for this student
			ResponseList studentResponseList = new ResponseList("studentResponseList", mPrompt, responseMetadata, location, new UserModel(student));
			studentResponseList.setAllowEdit(false);
			studentResponseList.setAllowNotebook(false);
			studentContainer.add(studentResponseList);			
		}		

	}
	
	protected UserListModel getUserListModel() {
		UserCriteriaBuilder c = new UserCriteriaBuilder();
		c.setGetAllUsers(false);
		c.setRole(Role.STUDENT);
		c.setPeriod(ISISession.get().getCurrentPeriodModel());
		return new UserListModel(c);
	}	



}
