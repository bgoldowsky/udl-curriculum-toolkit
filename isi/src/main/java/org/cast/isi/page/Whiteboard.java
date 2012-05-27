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

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import net.databinder.hib.Databinder;
import net.databinder.models.hib.HibernateObjectModel;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.cast.cwm.components.ClassAttributeModifier;
import org.cast.cwm.data.Response;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.models.ResponseModel;
import org.cast.cwm.data.models.UserModel;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISISession;
import org.cast.isi.ISIXmlSection;
import org.cast.isi.data.ISIPrompt;
import org.cast.isi.data.ISIResponse;
import org.cast.isi.panel.RemoveDialog;
import org.cast.isi.panel.ResponseViewer;
import org.cast.isi.service.ISIResponseService;
import org.hibernate.LockOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a general purpose whiteboard shared by all users within a period.
 * Items that can be added to the notebook are both response areas and page notes.
 *
 */
@AuthorizeInstantiation("STUDENT")
public class Whiteboard extends ISIBasePage implements IHeaderContributor {

	protected static final Logger log = LoggerFactory.getLogger(Whiteboard.class);

	protected boolean showNames = false;
	protected boolean enableNames = false;
	protected boolean isTeacher = false;
	protected String pageTitleEnd = null;
	private UserModel mUser;

	public Whiteboard(PageParameters parameters) {
		super(parameters);

		// set teacher flag and target user
		isTeacher = ISISession.get().getUser().getRole().subsumes(Role.TEACHER);
		mUser = new UserModel(ISISession.get().getUser());			

		pageTitleEnd = (new StringResourceModel("Whiteboard.pageTitle", this, null, "Whiteboard").getString());
		setPageTitle(pageTitleEnd);
		add(new Label("pageTitle", new PropertyModel<String>(this, "pageTitle")));
		add(ISIApplication.get().getToolbar("tht", this));

		addContent();
		addLinks();	
	}

	protected void addLinks() {
		add(new Label("heading", isTeacher ?  pageTitleEnd + " - " + ISISession.get().getCurrentPeriodModel().getObject().getName() : pageTitleEnd));
		add(new NameDisplayToggleLink("hideNamesLink").add(new Label("hideNamesText", new AbstractReadOnlyModel<String>() {
			private static final long serialVersionUID = 1L;
			@Override
			public String getObject() {
				if (showNames) {
					return new ResourceModel("Whiteboard.buttonTitle.hideNames").getObject();
				} else {
					return new ResourceModel("Whiteboard.buttonTitle.showNames").getObject();
				}
			}			
		})).setOutputMarkupId(true).setVisible(!isTeacher).setEnabled(enableNames));

		WhiteboardClearDialog clearWhiteboardModal = new WhiteboardClearDialog("clearWhiteboardModal");
		add(clearWhiteboardModal);

		add(new WebMarkupContainer("clearWhiteboardLink1")
			.setVisible(isTeacher && !mUser.getObject().hasRole(Role.RESEARCHER))
			.add(clearWhiteboardModal.getClickToOpenBehavior()));
	}

	private class NameDisplayToggleLink extends AjaxFallbackLink<Object> {
		private static final long serialVersionUID = 1L;

		public NameDisplayToggleLink(String id) {
			super(id);
		}

		@Override
		public void onClick(AjaxRequestTarget target) {
			showNames = !showNames;
			target.addComponent(Whiteboard.this.get("hideNamesLink"));	
			target.addComponent(Whiteboard.this.get("rootContainer")); // Root Repeating View			
		}

		@Override
		public boolean isVisible() {
			return isTeacher;
		}		
	}

	protected void addContent() {
		WebMarkupContainer rootContainer = new WebMarkupContainer("rootContainer");
		rootContainer.setOutputMarkupId(true);
		add(rootContainer);
		WebMarkupContainer noData = new WebMarkupContainer("noData");
		rootContainer.add(noData);

		TreeMap<ISIXmlSection, SortedMap<ISIPrompt, List<ISIResponse>>> responseMap = getResponseMap();
		if (responseMap.size() == 0) {
			rootContainer.add(new WebMarkupContainer("pageListing").setVisible(false));	
		} else {
			enableNames = true;
			noData.setVisible(false);
			RepeatingView pageListing = new RepeatingView("pageListing");
			rootContainer.add(pageListing);			

			for (ISIXmlSection sec : responseMap.keySet()) {
				WebMarkupContainer pageItem = new WebMarkupContainer(pageListing.newChildId());
				pageItem.setOutputMarkupId(true);
				pageListing.add(pageItem);

				RepeatingView questionListing = new RepeatingView("questionListing");
				pageItem.add(questionListing);

				for (ISIPrompt isiprompt : responseMap.get(sec).keySet()) {
					WebMarkupContainer questionItem = new WebMarkupContainer(questionListing.newChildId());
					questionItem.setOutputMarkupId(true);
					questionListing.add(questionItem);

					BookmarkablePageLink<ISIStandardPage> titleLink = new SectionLinkFactory().linkToPage("titleLink", sec);
					titleLink.add(ISIApplication.get().iconFor(sec.getSectionAncestor(), ""));
					questionItem.add(titleLink);

					String question = isiprompt.getQuestionHTML();
					questionItem.add(new Label("questionText", question).setEscapeModelStrings(false));

					RepeatingView responses = new RepeatingView("responseListing");
					questionItem.add(responses);

					for (ISIResponse response : responseMap.get(sec).get(isiprompt)) {
						WebMarkupContainer responseItem = new WebMarkupContainer(responses.newChildId());
						responseItem.setOutputMarkupId(true);
						responses.add(responseItem);
						responseItem.add(new WebMarkupContainer("responseAnchor")
								.add(new SimpleAttributeModifier("name", String.valueOf(response.getId()))));
						ResponseViewer rv = new ResponseViewer("response",
								new HibernateObjectModel<Response>(Response.class, response.getId()), 
								500, 500);
						rv.setShowDateTime(false);
						responseItem.add(rv);

						// Remove from Whiteboard link
						WhiteboardRemoveDialog removeModal = new WhiteboardRemoveDialog("removeModal", new ResponseModel(response));
						responseItem.add(removeModal);
						responseItem.add(new Label("authorName", response.getUser().getFullName()) {
							private static final long serialVersionUID = 1L;
							@Override
							public boolean isVisible() {
								return showNames;
							}
						});

						responseItem.add(new WebMarkupContainer("teacherRemoveLink")
						.setVisible(mUser.getObject().getRole().equals(Role.TEACHER))
						.add(removeModal.getClickToOpenBehavior()));

						responseItem.add(new WebMarkupContainer("removeLink")
						.setVisible(mUser.getObject().getRole().equals(Role.STUDENT) && mUser.getObject().equals(response.getUser()))
						.add(removeModal.getClickToOpenBehavior()));

						if(mUser.getObject().equals(response.getUser()))
							responseItem.add(new ClassAttributeModifier("responseRemoveable"));
					}
				}
			}
		}
	}


	/**
	 *  Query for responses, and creates a multi-level map to sort them out for display.
	 *  
	 *  Structure of map:
	 *    - XMLSection of the page on which the response occurs - in document order
	 *      - ISIPrompt - PAGE_NOTES after RESPONSEAREA
	 *        - List of responses to that prompt - reverse date sorted from the database
	 */
	protected TreeMap<ISIXmlSection, SortedMap<ISIPrompt, List<ISIResponse>>> getResponseMap() {
		List<ISIResponse> responseList = ISIResponseService.get().getWhiteboardResponsesByPeriod(ISISession.get().getCurrentPeriodModel().getObject());
		TreeMap<ISIXmlSection, SortedMap<ISIPrompt, List<ISIResponse>>> responseMap = new TreeMap<ISIXmlSection, SortedMap<ISIPrompt, List<ISIResponse>>>();

		// Fit Responses into Map
		for (ISIResponse r : responseList) {

			ISIPrompt prompt = ((ISIPrompt)r.getPrompt());
			ISIXmlSection section = prompt.getContentElement().getContentLocObject().getSection();
			if (!responseMap.containsKey(section))
				responseMap.put(section, new TreeMap<ISIPrompt, List<ISIResponse>>());
			if (!responseMap.get(section).containsKey(prompt))
				responseMap.get(section).put(prompt, new ArrayList<ISIResponse>());
			responseMap.get(section).get(prompt).add(r);
		}
		if (log.isDebugEnabled()) {
			for (ISIXmlSection loc : responseMap.keySet()) {
				log.debug("   Section Title {}", loc.getTitle());
				for (ISIPrompt prompt : responseMap.get(loc).keySet()) {
					log.debug("      Prompt {}", prompt);
					for (ISIResponse r : responseMap.get(loc).get(prompt)) {
						log.debug("         Response {}", r.getText());
					}
				}
			}
		}
		return responseMap;
	}

	@Override
	public String getPageName() {
		return null;
	}

	@Override
	public String getPageType() {
		return "whiteboard";
	}

	@Override
	public String getPageViewDetail() {
		return null;
	}

	public void renderHead(IHeaderResponse response) {
		response.renderCSSReference(new ResourceReference("/css/window.css"));
		response.renderCSSReference(new ResourceReference("/css/window_print.css"), "print");
		super.renderHead(response);

		response.renderOnLoadJavascript("bindSectionOpenerLinks()");
	}


	protected class WhiteboardRemoveDialog extends RemoveDialog {

		private static final long serialVersionUID = 1L;

		public WhiteboardRemoveDialog(String id, IModel<?> model) {
			super(id, model);
		}

		protected void removeObject() {
			ISIResponse resp = (ISIResponse) getDefaultModelObject();
			// Lock to current session - necessary since objects are stored detached in responseMap
			Databinder.getHibernateSession().buildLockRequest(LockOptions.UPGRADE).lock(resp);
			ISIResponseService.get().removeFromWhiteboard(resp, Whiteboard.this);
			setResponsePage(ISIApplication.get().getWhiteboardPageClass(), getPageParameters());
		}

		@Override
		protected String getDialogTitle() {
			return new ResourceModel("Whiteboard.removeDialogTitle", "Remove from Whiteboard?").getObject();
		}

		@Override
		protected String getDialogText() {
			return new ResourceModel("Whiteboard.removeDialogText", "Are you sure you want to remove this response from the Whiteboard?").getObject();
		}
	}


	protected class WhiteboardClearDialog extends RemoveDialog {

		private static final long serialVersionUID = 1L;

		public WhiteboardClearDialog(String id) {
			super(id, null);
		}

		protected void removeObject() {
			ISIResponseService.get().clearWhiteboardForPeriod (ISISession.get().getCurrentPeriodModel().getObject());
			setResponsePage (ISIApplication.get().getWhiteboardPageClass(), getPageParameters());
		}

		@Override
		protected String getDialogTitle() {
			return new ResourceModel("Whiteboard.clearDialogTitle", "Clear Whiteboard?").getObject();
		}

		@Override
		protected String getDialogText() {
			return new ResourceModel("Whiteboard.clearDialogText", "Are you sure you want to remove ALL items from the whiteboard?").getObject();
		}
	}
}