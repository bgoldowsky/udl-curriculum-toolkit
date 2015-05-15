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

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.cast.cwm.components.ClassAttributeModifier;
import org.cast.cwm.data.Role;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISISession;
import org.cast.isi.page.ISIStandardPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TeacherSubHeaderPanel includes the Period/Student pull down as well as
 * teacher notes and student flags.  It is a useful sub-header
 * for non reading teacher pages.
 * 
 * @author lynnmccormack
 *
 */
public class TeacherSubHeaderPanel extends ISIPanel {
	private static final long serialVersionUID = 1L;
	protected StudentFlagPanel studentFlagPanel;
	protected BookmarkablePageLink<Page> teacherNotesLink;
	protected PeriodStudentSelectPanel periodStudentSelectPanel;
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(TeacherSubHeaderPanel.class);

	boolean showStudents;

	public TeacherSubHeaderPanel (String id, final PageParameters parameters) {
		super(id);

		addContent(parameters);
	}
	
	protected void addContent(final PageParameters parameters) {

		 periodStudentSelectPanel = new PeriodStudentSelectPanel("periodStudentSelectPanel", true) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onPeriodUpdate(AjaxRequestTarget target) {
				teacherNotesLink.setEnabled(false);
				studentFlagPanel.setmUser(null); // Set Flag to no student
				studentFlagPanel.setEnabled(false);
				if (target != null) {
					target.add(studentFlagPanel);
					target.add(teacherNotesLink);
				}
			}

			@Override
			protected void onStudentUpdate(AjaxRequestTarget target) {
				studentFlagPanel.setEnabled(false);
				target.add(studentFlagPanel);
			}

			@Override
			protected void onFormSubmit() {
				ISIStandardPage currentPage = (ISIStandardPage) getPage();
				currentPage.reloadForPeriodStudentChange(parameters);								
			}

		};    	
		add(periodStudentSelectPanel);
		periodStudentSelectPanel.setOutputMarkupId(true);

		// Teacher Notes Pop-up for the current student
		teacherNotesLink = new BookmarkablePageLink<Page>("teacherNotesLink", ISIApplication.get().getTeacherNotesPageClass());
		ISIApplication.get().setLinkProperties(teacherNotesLink);
		teacherNotesLink.setOutputMarkupId(true);
		if (ISISession.get().getStudentModel() == null) {
			teacherNotesLink.add(new ClassAttributeModifier("off"));
		}
		teacherNotesLink.setEnabled(ISISession.get().getStudentModel() != null);
		teacherNotesLink.setVisible(!ISISession.get().getUser().hasRole(Role.RESEARCHER) || showStudents); // Researchers do not use Teacher Notes
		teacherNotesLink.setOutputMarkupPlaceholderTag(true);
		add(teacherNotesLink);

		// Student Flag Panel for the current student
		studentFlagPanel = new StudentFlagPanel("studentFlagPanel", ISISession.get().getStudent(), null) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onBeforeRender() {
				if (ISIApplication.get().getHomePage().isAssignableFrom(getPage().getClass())
						|| ISIApplication.get().getManageClassesPageClass().isAssignableFrom(getPage().getClass())
						|| ISISession.get().getStudentModel() == null) {
					setVisible(false);
				} else {
					setVisible(true);
				}
				super.onBeforeRender();
			}			
			
		};
		add(studentFlagPanel);	
	}
	

	@Override
	protected void onConfigure() {
		// don't show the student related objects on home or manage classes
		super.onConfigure();
		studentFlagPanel.setVisible(ISISession.get().getUser().hasRole(Role.RESEARCHER) ? false : true);
		if (ISIApplication.get().getHomePage().isAssignableFrom(getPage().getClass()) ||
		ISIApplication.get().getManageClassesPageClass().isAssignableFrom(getPage().getClass())) {
			teacherNotesLink.setVisible(false);
			periodStudentSelectPanel.getStudentChoice().setVisible(false);
		}
	}
}