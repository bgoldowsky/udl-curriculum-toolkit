/*
 * Copyright 2011 CAST, Inc.
 *
 * This file is part of the CAST Wicket Modules:
 * see <http://code.google.com/p/cast-wicket-modules>.
 *
 * The CAST Wicket Modules are free software: you can redistribute and/or
 * modify them under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * The CAST Wicket Modules are distributed in the hope that they will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cast.isi.panel;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.cast.cwm.components.ClassAttributeModifier;
import org.cast.cwm.data.Role;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISISession;
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
				studentFlagPanel.setUser(null); // Set Flag to no student
				studentFlagPanel.setEnabled(false);
				if (target != null) {
					target.addComponent(studentFlagPanel);
					target.addComponent(teacherNotesLink);
				}
			}

			@Override
			protected void onStudentUpdate(AjaxRequestTarget target) {
				studentFlagPanel.setEnabled(false);
				target.addComponent(studentFlagPanel);
			}

			@Override
			protected void onFormSubmit() {
				setResponsePage(getPage().getClass(), parameters);								
			}			
		};    	
		add(periodStudentSelectPanel);

		// Teacher Notes Pop-up for the current student
		teacherNotesLink = new BookmarkablePageLink<Page>("teacherNotesLink", ISIApplication.get().getTeacherNotesPageClass());
		ISIApplication.get().setLinkProperties(teacherNotesLink);
		teacherNotesLink.setOutputMarkupId(true);
		if (ISISession.get().getStudentModel() == null) {
			teacherNotesLink.add(new ClassAttributeModifier("off"));
		}
		teacherNotesLink.setEnabled(ISISession.get().getStudentModel() != null);
		teacherNotesLink.setVisible(!ISISession.get().getUser().hasRole(Role.RESEARCHER) || showStudents); // Researchers do not use Teacher Notes
		add(teacherNotesLink);

		// Student Flag Panel for the current student
		studentFlagPanel = new StudentFlagPanel("studentFlagPanel", ISISession.get().getStudent(), null, "/img/icons/flag") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				super.isVisible();
				if (ISIApplication.get().getHomePage().isAssignableFrom(getPage().getClass()) ||
						ISIApplication.get().getManageClassesPageClass().isAssignableFrom(getPage().getClass())) {
					return false;
				}
				return true;
			}			
		};
		studentFlagPanel.setOutputMarkupId(true);
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