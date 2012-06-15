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

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.cast.cwm.CwmApplication;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.Response;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.cwm.data.behavior.AjaxAutoSavingBehavior;
import org.cast.isi.ISISession;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.data.PromptType;
import org.cast.isi.service.ISIResponseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This panel creates a thumbs panel, an affect selection, and a text response area
 *
 */
public class RatePanel extends Panel {
	private static final long serialVersionUID = 1L;
	protected static final Logger log = LoggerFactory.getLogger(RatePanel.class);

	// TODO replace this with something more configurable
	protected static final List<String> FEELINGS = Arrays.asList("happy", "relieved", "uninterested", "worried");

	protected boolean readOnly;
	protected ContentLoc contentLoc;
	protected IModel<User> mUser;
	protected IModel<Response> mResponseAffect;
	protected String xmlId;
	protected TextArea<String> commentText;
	
	/**
	 * @param id wicket component id
	 * @param contentLoc page location
	 * @param xmlId where on the page
	 * @param promptText what to call the thing being rated (as in "Rate this ______")
	 */
	public RatePanel(String id, ContentLoc contentLoc, String xmlId, String promptText) {
		super(id);
		this.contentLoc = contentLoc;
		this.xmlId = xmlId;
		ISISession session = ISISession.get();
		mUser = session.getTargetUserModel();
		
		// If teacher logged in, show ratings of the student they are looking at.
		boolean isTeacher = ISISession.get().getUser().getRole().subsumes(Role.TEACHER);
		if (isTeacher)
			readOnly = true;
		
		add(new ThumbPanel("thumbPanel", contentLoc, xmlId));
		
		// add one affect button for each affect in the array
		for (int i = 0; i<FEELINGS.size(); i++) {
			addAffectButton(FEELINGS.get(i));
		}
		
		// Set up the prompt and response for the affect button
		IModel<Prompt> mPrompt = ISIResponseService.get().getOrCreatePrompt(PromptType.RATING_AFFECT, contentLoc, xmlId);
		mResponseAffect = ISIResponseService.get().getResponseForPrompt(mPrompt, mUser);
		if (mResponseAffect == null || mResponseAffect.getObject() == null && !readOnly)
			// TODO: should probably be one of the other response types
			mResponseAffect = ISIResponseService.get().newResponse(mUser, CwmApplication.get().getResponseType("TEXT"), mPrompt);
		setDefaultModel(mResponseAffect);	
		
		addCommentForm("commentForm");

		Label promptLabel = new Label("promptText", promptText);
		add(promptLabel);
		// the prompt text came in with all of the html it needed, pass it through as html not text
		promptLabel.setEscapeModelStrings(false);
		promptLabel.setRenderBodyOnly(true);

	}


	/**
	 * @param affectText - name of the affect button
	 */
	private void addAffectButton(final String affectText) {
		AjaxLink<Void> link = new AjaxLink<Void>(affectText) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isEnabled() {
				if (readOnly)
					return false;
				// The currently selected affect button is disabled
				return (!affectText.equals(RatePanel.this.getDefaultModelObjectAsString()));
			}

			@SuppressWarnings("unchecked")
			@Override
			public void onClick(AjaxRequestTarget target) {
				ISIResponseService.get().saveTextResponse(((IModel<Response>)RatePanel.this.getDefaultModel()), affectText, contentLoc.getLocation());
				target.addChildren(RatePanel.this, AjaxLink.class);
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				// if this is the selected button then set the class info for display purposes
				String tagAttribute = null;
				if (RatePanel.this.getDefaultModelObject() != null && affectText.equals(((Response)RatePanel.this.getDefaultModelObject()).getText())) {
					tagAttribute = tag.getAttribute("class");
					tag.put("class", tagAttribute + " selected");
				}
				super.onComponentTag(tag);
			}
		};
		add(link);		
		link.setOutputMarkupId(true);
	}	
	
	
	/**
	 * This method adds the form for the text area to comment on this rating.  The comment is auto saved.
	 * @param id - wicket id of this form
	 */
	protected void addCommentForm(String id) {
		final CommentForm commentForm = new CommentForm("commentForm");
		add(commentForm);
		commentForm.add(new AjaxAutoSavingBehavior(commentForm) {
			private static final long serialVersionUID = 1L;
			
			@SuppressWarnings("unchecked")
			@Override
			protected void onAutoSave(AjaxRequestTarget target) {
				super.onAutoSave(target);
				ISIResponseService.get().saveTextResponse(((IModel<Response>)commentForm.getDefaultModel()), commentText.getDefaultModelObjectAsString(), contentLoc.getLocation());
			}
		});
	}
	
	
	protected class CommentForm extends Form<Response> {
		private static final long serialVersionUID = 1L;

		public CommentForm(String id) {
			super(id);
			
			// Set up the prompt and response for the rating comment
			IModel<Prompt> mPrompt = ISIResponseService.get().getOrCreatePrompt(PromptType.RATING_COMMENT, contentLoc, xmlId);
			IModel<Response> mResponseComment = ISIResponseService.get().getResponseForPrompt(mPrompt, mUser);
			if ((mResponseComment == null || mResponseComment.getObject() == null) && !readOnly)
				mResponseComment = ISIResponseService.get().newResponse(mUser, CwmApplication.get().getResponseType("TEXT"), mPrompt);
			setDefaultModel(mResponseComment);
			
			// set the text area to either what was in the db or a new string if nothing has been entered
			IModel<String> textModel = (((mResponseComment != null) && (mResponseComment.getObject() != null)) ? (new Model<String>(mResponseComment.getObject().getText())) : new Model<String>(""));
			commentText = new TextArea<String>("commentText", textModel);
			add(commentText).setEnabled(!readOnly);
			commentText.setOutputMarkupId(true);
		}	
	}		

	
	@Override
	protected void onDetach() {
		super.onDetach();
		if (mUser != null)
			mUser.detach();
		if (mResponseAffect != null)
			mResponseAffect.detach();
	}
}
