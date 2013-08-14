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

import com.google.inject.Inject;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.ResponseMetadata;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISISession;
import org.cast.isi.data.PromptType;
import org.cast.isi.panel.ResponseButtons;
import org.cast.isi.panel.ResponseList;
import org.cast.isi.service.IISIResponseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This page is a notebook for a teacher.  It contains notes
 * about the specific student a teacher is viewing.
 * 
 * @author lynnmccormack
 *
 */
@AuthorizeInstantiation("TEACHER")
public class TeacherNotesPopup extends ISIBasePage implements IHeaderContributor {
	
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(TeacherNotesPopup.class);
	private IModel<User> mStudent = ISISession.get().getStudentModel();
	private IModel<Period> mPeriod = ISISession.get().getCurrentPeriodModel();
	protected ResponseMetadata notebookMetadata;
	
	@Inject
	protected IISIResponseService responseService;
	
	public TeacherNotesPopup (PageParameters param) {
		super(param);

		setNotebookMetadata(notebookMetadata);

		if (mStudent == null && mStudent.getObject() == null)
			throw new IllegalStateException("Must specify a student.\n"); 

		// set the heading for this page - modify the properties file to change this
		String pageTitleEnd = (new StringResourceModel("TeacherNotes.pageTitle", this, null, "Teacher Notes").getString());
		setPageTitle(pageTitleEnd);
		add(new Label("pageTitle", new PropertyModel<String>(this, "pageTitle")));
		String heading = pageTitleEnd;
		
		if (ISISession.get().getUser().hasRole(Role.TEACHER))
			heading +=  " for - " + mPeriod.getObject().getName() + " > " + mStudent.getObject().getFullName();
		add(new Label("heading", heading));
		
		// FIXME? Notes are currently not location specific - LDM
//		ContentLoc location = null;
//		if (param.containsKey("loc") && param.get("loc") != null) {
//			location = new ContentLoc(param.getString("loc"));
//		}
		
		IModel<Prompt> mPrompt = responseService.getOrCreatePrompt(PromptType.TEACHER_NOTES, mStudent);
		ResponseList responseList = new ResponseList("nbResponseList", mPrompt, notebookMetadata, null, ISISession.get().getUserModel());
		add(responseList);
		responseList.setContext("TeacherNotes");
		responseList.setAllowNotebook(false);
		responseList.setAllowWhiteboard(false);
		ResponseButtons responseButtons = new ResponseButtons("responseButtons", mPrompt, notebookMetadata, null);
		responseButtons.setContext("TeacherNotes");
		add(responseButtons);
		add(ISIApplication.get().getToolbar("tht", this));
	}

	public void setNotebookMetadata(ResponseMetadata notebookMetadata) {
		this.notebookMetadata = ISIApplication.get().getResponseMetadata();
	}
	
	@Override
	public String getPageName() {
		return "teachernotes";
	}

	@Override
	public String getPageType() {
		return "teachernotes";
	}
	
	@Override
	public String getPageViewDetail() {
		return null;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		renderThemeCSS(response, "css/window.css");
		renderThemeCSS(response, "css/window_print.css", "print");
		response.renderOnLoadJavaScript("bindSectionOpenerLinks()");
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (mStudent != null) {
			mStudent.detach();
		}
		if (mPeriod != null) {
			mPeriod.detach();
		}
	}
}