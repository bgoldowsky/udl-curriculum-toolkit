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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.cast.cwm.CwmApplication;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.Response;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.isi.ISISession;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.data.PromptType;
import org.cast.isi.service.ISIResponseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This panel creates a response for the three thumbs ratings: up, side, down
 *
 */
public class ThumbPanel extends Panel {
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ThumbPanel.class);

	protected boolean readOnly = false;
	protected ContentLoc contentLoc;

	/**
	 * @param id - the wicket id
	 * @param contentLoc - the location in the document - replaces pagename
	 * @param xmlId - the xml id of this element - needed for multi instances per page
	 */
	public ThumbPanel(String id, ContentLoc contentLoc, String xmlId) {
		super(id);
		this.setOutputMarkupId(true);		
		this.contentLoc = contentLoc;

		addButton("up");
		addButton("side");
		addButton("down");

		ISISession session = ISISession.get();
		IModel<User> mUser = session.getTargetUserModel();

		// If teacher logged in, show ratings of the student they are looking at.
		Role role = ISISession.get().getUser().getRole();
		if (role.equals(Role.GUEST) || role.subsumes(Role.TEACHER))
			readOnly = true;

		// Set up the prompt and response for this thumb panel
		IModel<Prompt> mPrompt = ISIResponseService.get().getOrCreatePrompt(PromptType.RATING_THUMB, contentLoc, xmlId);
		IModel<Response> mResponse = null;
		if (!role.equals(Role.GUEST))
			ISIResponseService.get().getResponseForPrompt(mPrompt, mUser);
		if (mResponse == null || mResponse.getObject() == null && !readOnly)
			// TODO: this isn't really the correct ResponseType, but it's what we've used in the past.
			mResponse = ISIResponseService.get().newResponse(mUser, CwmApplication.get().getResponseType("TEXT"), mPrompt);
		setDefaultModel(mResponse);		
	}

	/**
	 * Add one button for each type of thumb name
	 * @param buttonName
	 */
	protected void addButton(final String buttonName) {		
		AjaxLink<Void> link = new AjaxLink<Void>(buttonName) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isEnabled() {
				if (readOnly)
					return false;
				// The currently selected rating is disabled
				return (!buttonName.equals(ThumbPanel.this.getDefaultModelObjectAsString()));
			}

			@SuppressWarnings("unchecked")
			@Override
			public void onClick(AjaxRequestTarget target) {
				ISIResponseService.get().saveTextResponse(((IModel<Response>)ThumbPanel.this.getDefaultModel()), buttonName, contentLoc.getLocation());
				target.addChildren(ThumbPanel.this, AjaxLink.class);
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				// if the current button is selected, set the class info for the background color
				String tagAttribute = null;
				if (ThumbPanel.this.getDefaultModelObject() != null && buttonName.equals(((Response)ThumbPanel.this.getDefaultModel().getObject()).getText())) {
					tagAttribute = tag.getAttribute("class");
					tag.put("class", tagAttribute + " current");
				}
				super.onComponentTag(tag);
			}
		};
		add(link);		
		link.setOutputMarkupId(true);
	}

}