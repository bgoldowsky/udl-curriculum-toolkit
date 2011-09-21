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
package org.cast.isi.page;

import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.cast.cwm.components.ClassAttributeModifier;
import org.cast.cwm.data.ResponseMetadata;
import org.cast.cwm.data.ResponseType;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.cwm.data.builders.UserCriteriaBuilder;
import org.cast.cwm.data.models.PromptModel;
import org.cast.cwm.data.models.UserListModel;
import org.cast.cwm.data.models.UserModel;
import org.cast.cwm.service.ResponseService;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISISession;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.data.ISIPrompt;
import org.cast.isi.panel.ResponseList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Given a prompt id and period, list all the user responses
 * 
 * @author lynnmccormack
 *
 */
@AuthorizeInstantiation("TEACHER")
public class PeriodResponsePage extends ISIBasePage implements IHeaderContributor {

	protected static final Logger log = LoggerFactory.getLogger(PeriodResponsePage.class);
	private long promptId;
	protected boolean showNames = false;
	protected String pageTitleEnd = null;
	protected static ResponseMetadata responseMetadata = new ResponseMetadata();
	static {
		responseMetadata.addType(ResponseType.HTML);
		responseMetadata.addType(ResponseType.AUDIO);
		responseMetadata.addType(ResponseType.SVG);
		responseMetadata.addType(ResponseType.UPLOAD);
	}

	public PeriodResponsePage(final PageParameters parameters) {
		super(parameters);

		pageTitleEnd = (new StringResourceModel("Compare.pageTitle", this, null, "Compare").getString());
		setPageTitle(pageTitleEnd);
		add(new Label("pageTitle", new PropertyModel<String>(this, "pageTitle")));
		add(new Label("periodName", ISISession.get().getCurrentPeriodModel().getObject().getName()));

		add(ISIApplication.get().getToolbar("tht", this));
		add(new NameDisplayToggleLink("hideNamesLink").add(new Label("hideNamesText", new AbstractReadOnlyModel<String>() {
			private static final long serialVersionUID = 1L;
			@Override
			public String getObject() {
				if (showNames) {
					return new ResourceModel("Compare.buttonTitle.hideNames").getObject();
				} else {
					return new ResourceModel("Compare.buttonTitle.showNames").getObject();
				}
			}			
		})).setOutputMarkupId(true));

		WebMarkupContainer rootContainer = new WebMarkupContainer("rootContainer");
		rootContainer.setOutputMarkupId(true);
		add(rootContainer);

		// prompt id is sent in via parameters, get the prompt for this id
		if (parameters.containsKey("promptId")) {
			promptId = (parameters.getLong("promptId"));
		} 

		// get the prompt for this id
		ISIPrompt prompt = (ISIPrompt) (ResponseService.get().getPromptById(promptId)).getObject();

		// Add the crumb trail, link and icon link to the page where this response is located
		String crumbTrail = prompt.getContentElement().getContentLocObject().getSection().getCrumbTrailAsString(1, 1);
		rootContainer.add(new Label("crumbTrail", crumbTrail));
		BookmarkablePageLink<ISIStandardPage> link = ISIStandardPage.linkTo("titleLink", prompt.getContentElement().getContentLocObject().getSection());
		link.add(new Label("title", prompt.getContentElement().getContentLocObject().getSection().getTitle()));
		link.add(new ClassAttributeModifier("sectionLink"));
		rootContainer.add(link);
		rootContainer.add(ISIApplication.get().iconFor(prompt.getContentElement().getContentLocObject().getSection().getSectionAncestor(), ""));		

		ContentLoc location = prompt.getContentElement().getContentLocObject();

		// Add the text associated with Prompt
		String question =  prompt.getQuestionHTML();			
		rootContainer.add(new Label("question", question).setEscapeModelStrings(false));

		// get the list of all users in this period
		List<User> studentList = getUserListModel().getObject();

		// iterate over all the users to get the responses for the prompt
		RepeatingView rv1 = new RepeatingView("studentRepeater");
		rootContainer.add(rv1);
		for (User student : studentList) {

			// get the student name and display
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
			ResponseList studentResponseList = new ResponseList("studentResponseList", new PromptModel(prompt), responseMetadata, location, new UserModel(student));
			studentResponseList.setAllowEdit(false);
			studentResponseList.setAllowNotebook(false);
			studentContainer.add(studentResponseList);			
		}		
	}
	
	private class NameDisplayToggleLink extends AjaxFallbackLink<Object> {
		private static final long serialVersionUID = 1L;

		public NameDisplayToggleLink(String id) {
			super(id);
		}

		@Override
		public void onClick(AjaxRequestTarget target) {
			showNames = !showNames;
			target.addComponent(PeriodResponsePage.this.get("rootContainer")); // Root Repeating View
			target.addComponent(PeriodResponsePage.this.get("hideNamesLink"));			
		}				
	}

	protected UserListModel getUserListModel() {
		UserCriteriaBuilder c = new UserCriteriaBuilder();
		c.setGetAllUsers(false);
		c.setRole(Role.STUDENT);
		c.setPeriod(ISISession.get().getCurrentPeriodModel());
		return new UserListModel(c);
	}	

	@Override
	public String getPageName() {
		return null;
	}

	@Override
	public String getPageType() {
		return "compare";
	}

	@Override
	public String getPageViewDetail() {
		return String.valueOf(promptId);
	}

	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.renderCSSReference(new ResourceReference("/css/window.css"));
		response.renderCSSReference(new ResourceReference("/css/window_print.css"), "print");
	}
}