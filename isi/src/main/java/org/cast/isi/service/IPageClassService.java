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
package org.cast.isi.service;

import org.apache.wicket.markup.html.WebPage;
import org.cast.cwm.data.Role;
import org.cast.isi.page.ISIStandardPage;

public interface IPageClassService {

	Class<? extends WebPage> getWhiteboardPageClass();

	Class<? extends WebPage> getPasswordPageClass();

	Class<? extends WebPage> getForgotPasswordPageClass();

	Class<? extends WebPage> getRegisterPageClass();

	Class<? extends ISIStandardPage> getManageClassesPageClass();

	Class<? extends ISIStandardPage> getTeacherReadingPageClass();

	Class<? extends ISIStandardPage> getStudentReadingPageClass();

	Class<? extends WebPage> getTeacherNotesPageClass();

	Class<? extends WebPage> getPeriodResponsePageClass();

	Class<? extends WebPage> getQuestionPopupPageClass();

	Class<? extends WebPage> getMyQuestionsPageClass();

	Class<? extends WebPage> getTagsPageClass();

	Class<? extends WebPage> getResponseCollectionsPageClass();

	Class<? extends WebPage> getNotebookPageClass();

	Class<? extends WebPage> getGlossaryPageClass();

	Class<? extends WebPage> getAdminHomePageClass();

	Class<? extends WebPage> getTeacherTOCPageClass();

	Class<? extends WebPage> getStudentTOCPageClass();

	Class<? extends ISIStandardPage> getReadingPageClass();

	Class<? extends WebPage> getTocPageClass(Role role);

}
