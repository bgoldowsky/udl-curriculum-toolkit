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
package org.cast.isi.panel;

import lombok.Setter;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.Response;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.cwm.data.models.PromptModel;
import org.cast.isi.ISISession;
import org.cast.isi.component.ISingleSelectFormListener;
import org.cast.isi.data.ISIResponse;
import org.cast.isi.page.ISIBasePage;
import org.cast.isi.service.IFeatureService;
import org.cast.isi.service.IISIResponseService;
import org.cast.isi.service.ILinkPropertiesService;
import org.cast.isi.service.IPageClassService;

import com.google.inject.Inject;

public class ResponseViewActionsPanel extends Panel implements ISingleSelectFormListener {

	private static final long serialVersionUID = 1L;

	@Inject
	private IISIResponseService responseService;
	
	@Inject
	private IPageClassService pageClassService;
	
	@Inject 
	private IFeatureService featureService;
	
	@Inject 
	private ILinkPropertiesService linkPropertiesService;

	private IModel<User> mTargetUser;

	private IModel<User> mUser;

	@Setter
	private boolean allowWhiteboard;

	@Setter
	private boolean allowNotebook;
	
	public ResponseViewActionsPanel(String id, IModel<Response> model) {
		this(id, model, ISISession.get().getTargetUserModel(), ISISession.get().getUserModel());
	}

	public ResponseViewActionsPanel(String id, Long promptId) {
		this(id, promptId, ISISession.get().getTargetUserModel(), ISISession.get().getUserModel());
	}

	public ResponseViewActionsPanel(String id, IModel<Response> model, IModel<User> mTargetUser, IModel<User> mUser) {
		super(id, model);
		this.mTargetUser = mTargetUser;
		this.mUser = mUser;
	}

	public ResponseViewActionsPanel(String id, Long promptId, IModel<User> mTargetUser, IModel<User> mUser) {
		super(id);
		this.mTargetUser = mTargetUser;
		this.mUser = mUser;
		setDefaultModel(getResponseModel(promptId));
	}

	private IModel<Response> getResponseModel(
			Long promptId) {
		PromptModel promptModel = new PromptModel(promptId);
		return responseService.getResponseForPrompt(promptModel, mTargetUser);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onInitialize() {
		super.onInitialize();
		add(new WhiteboardAnchorLink("whiteboardLink", getWhiteboardClass()));
		add(new WhiteboardAddLink("addToWhiteboardLink", (IModel<Response>) getDefaultModel()));
		add(new NotebookAnchorLink("notebookLink", getNotebookClass()));
		add(new NotebookAddLink("addToNotebookLink", (IModel<Response>) getDefaultModel()));
		setOutputMarkupId(true);
		setOutputMarkupPlaceholderTag(true);
	}
	
	@Override
	public void onConfigure() {
		super.onConfigure();
		ISIResponse isiResponse = getISIResponse();
		setVisible((isiResponse != null) && belongsToUser(isiResponse));
	}
	
	private boolean belongsToUser(ISIResponse isiResponse) {
		User user = mUser.getObject();
		return (user != null) && Role.STUDENT.equals(user.getRole()) && user.equals(isiResponse.getUser());
	}

	protected boolean whiteboardEnabled() {
		return allowWhiteboard && featureService.isWhiteboardOn();
	}

	protected boolean notebookEnabled() {
		return allowNotebook && featureService.isNotebookOn();
	}

	protected Long getResponseId() {
		ISIResponse response = getISIResponse();
		return response.getId();
	}

	private ISIResponse getISIResponse() {
		return (ISIResponse) getDefaultModelObject();
	}

	protected boolean inWhiteboard() {
		ISIResponse response = getISIResponse();
		return (response != null) && response.isInWhiteboard();
	}

	protected boolean inNotebook() {
		ISIResponse response = getISIResponse();
		return (response != null) && response.isInNotebook();
	}

	private Class<? extends WebPage> getWhiteboardClass() {
		return pageClassService.getWhiteboardPageClass();
	}

	private Class<? extends WebPage> getNotebookClass() {
		return pageClassService.getNotebookPageClass();
	}
	
	@Override
	public void onDetach() {
		if (mUser != null)
			mUser.detach();
		if (mTargetUser != null)
			mTargetUser.detach();
		super.onDetach();
	}

	private class WhiteboardAddLink extends AjaxLink<Response> {
		private static final long serialVersionUID = 1L;

		private WhiteboardAddLink(String id, IModel<Response> model) {
			super(id, model);
			setOutputMarkupId(true);
			setOutputMarkupPlaceholderTag(true);
		}

		@Override
		public void onConfigure() {
			super.onConfigure();
			setVisible(whiteboardEnabled() && !inWhiteboard());
		}

		@Override
		public void onClick(AjaxRequestTarget target) {
			responseService.addToWhiteboard((ISIResponse) getModelObject(), getPage());
			target.addComponent(ResponseViewActionsPanel.this);
		}
	}

	private class NotebookAddLink extends AjaxLink<Response> {
		private static final long serialVersionUID = 1L;

		private NotebookAddLink(String id, IModel<Response> model) {
			super(id, model);
			setOutputMarkupId(true);
			setOutputMarkupPlaceholderTag(true);
		}

		@Override
		public void onConfigure() {
			super.onConfigure();
			setVisible(notebookEnabled() && !inNotebook());
		}

		@Override
		public void onClick(AjaxRequestTarget target) {
			responseService.addToNotebook((ISIResponse) getModelObject(), getPage());
			target.addComponent(ResponseViewActionsPanel.this);
		}
	}

	private class WhiteboardAnchorLink extends AnchoredLink {
		private static final long serialVersionUID = 1L;

		private WhiteboardAnchorLink(String id,
				Class<? extends WebPage> pageClass) {
			super(id, pageClass);
		}

		@Override
		public void onConfigure() {
			super.onConfigure();
			setVisible(whiteboardEnabled() && inWhiteboard());
		}
	}

	private class NotebookAnchorLink extends AnchoredLink {
		private static final long serialVersionUID = 1L;

		private NotebookAnchorLink(String id,
				Class<? extends WebPage> pageClass) {
			super(id, pageClass);
		}

		@Override
		public void onConfigure() {
			super.onConfigure();
			setVisible(notebookEnabled() && inNotebook());
		}
	}

	/**
	 * Link that anchors to the id of the object in the page.  For links to Notebook and Whiteboard.
	 */
	private class AnchoredLink extends BookmarkablePageLink<ISIBasePage> {
		
		private static final long serialVersionUID = 1L;

		public AnchoredLink(String id, Class<? extends WebPage> pageClass) {
			super(id, pageClass);
			linkPropertiesService.setLinkProperties(this);
			setOutputMarkupId(true);
			setOutputMarkupPlaceholderTag(true);
		}

		@Override
		protected CharSequence appendAnchor(ComponentTag tag, CharSequence url) {
			if (url.toString().indexOf('#')>-1)
				return super.appendAnchor(tag, url);
			return url + "#" + getResponseId();
		}

	}


}
