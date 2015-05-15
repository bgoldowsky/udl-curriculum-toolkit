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
