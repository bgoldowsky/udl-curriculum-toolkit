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
package org.cast.isi.service;

import org.apache.wicket.markup.html.WebPage;
import org.cast.cwm.data.Role;
import org.cast.isi.ISIApplication;
import org.cast.isi.page.ISIStandardPage;

public class PageClassService implements IPageClassService {

	public Class<? extends WebPage> getWhiteboardPageClass() {
		return ISIApplication.get().getWhiteboardPageClass();
	}

	public Class<? extends WebPage> getPasswordPageClass() {
		return ISIApplication.get().getPasswordPageClass();
	}

	public Class<? extends WebPage> getForgotPasswordPageClass() {
		return ISIApplication.get().getForgotPasswordPageClass();
	}

	public Class<? extends WebPage> getRegisterPageClass() {
		return ISIApplication.get().getRegisterPageClass();
	}

	public Class<? extends ISIStandardPage> getManageClassesPageClass() {
		return ISIApplication.get().getManageClassesPageClass();
	}

	public Class<? extends ISIStandardPage> getTeacherReadingPageClass() {
		return ISIApplication.get().getTeacherReadingPageClass();
	}

	public Class<? extends ISIStandardPage> getStudentReadingPageClass() {
		return ISIApplication.get().getStudentReadingPageClass();
	}

	public Class<? extends WebPage> getTeacherNotesPageClass() {
		return ISIApplication.get().getTeacherNotesPageClass();
	}

	public Class<? extends WebPage> getPeriodResponsePageClass() {
		return ISIApplication.get().getPeriodResponsePageClass();
	}

	public Class<? extends WebPage> getQuestionPopupPageClass() {
		return ISIApplication.get().getQuestionPopupPageClass();
	}

	public Class<? extends WebPage> getMyQuestionsPageClass() {
		return ISIApplication.get().getMyQuestionsPageClass();
	}

	public Class<? extends WebPage> getTagsPageClass() {
		return ISIApplication.get().getTagsPageClass();
	}

	public Class<? extends WebPage> getResponseCollectionsPageClass() {
		return ISIApplication.get().getResponseCollectionsPageClass();
	}

	public Class<? extends WebPage> getNotebookPageClass() {
		return ISIApplication.get().getNotebookPageClass();
	}

	public Class<? extends WebPage> getGlossaryPageClass() {
		return ISIApplication.get().getGlossaryPageClass();
	}

	public Class<? extends WebPage> getAdminHomePageClass() {
		return ISIApplication.get().getAdminHomePageClass();
	}

	public Class<? extends WebPage> getTeacherTOCPageClass() {
		return ISIApplication.get().getTeacherTOCPageClass();
	}

	public Class<? extends WebPage> getStudentTOCPageClass() {
		return ISIApplication.get().getStudentTOCPageClass();
	}

	public Class<? extends ISIStandardPage> getReadingPageClass() {
		return ISIApplication.get().getReadingPageClass();
	}

	public Class<? extends WebPage> getTocPageClass(Role role) {
		return ISIApplication.get().getTocPageClass(role);
	}

}
