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

import lombok.Getter;
import lombok.Setter;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.cast.cwm.CwmSession;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.Response;
import org.cast.cwm.data.ResponseMetadata;
import org.cast.cwm.data.ResponseType;
import org.cast.cwm.service.ResponseService;
import org.cast.isi.data.ContentLoc;

public class ResponseButtons extends Panel {

	@Getter @Setter
	protected String context = "default";
	private IModel<Prompt> mPrompt;
	private ResponseMetadata metadata;
	private ContentLoc loc;
	private ResponseList listComponent;
	
	private ResponseType openEditor = null;

	private static final long serialVersionUID = 1L;

	public ResponseButtons(String id, IModel<Prompt> mPrompt, ResponseMetadata metadata, ContentLoc loc) {
		super(id);
		this.mPrompt = mPrompt;
		this.metadata = metadata;
		this.loc = loc;
		setOutputMarkupId(true);

		add (new ButtonLink("write", ResponseType.HTML)); // write for wysiwyg text
		add (new ButtonLink("text", ResponseType.TEXT));  // plain text box
		add (new ButtonLink("draw", ResponseType.SVG));	
		add (new ButtonLink("record", ResponseType.AUDIO));
		add (new ButtonLink("upload", ResponseType.UPLOAD));
	}

	/**
	 * Add a single button which calls up an editor for the given type of response.
	 * If the type is not mentioned in the ResponseMetadata, then this button will be invisible.
	 */
	private class ButtonLink extends AjaxLink<Void> {
		
		private static final long serialVersionUID = 1L;
		private ResponseType type;

		private ButtonLink (String wicketId, final ResponseType type) {
			super(wicketId);
			this.type = type;
			WebMarkupContainer preferredImage = new WebMarkupContainer("preferredImage") {
				private static final long serialVersionUID = 1L;

				@Override
				protected void onConfigure() {
					setVisible(metadata.getType(type).isPreferred());
					super.onConfigure();
				}
			};
			add (preferredImage);
		}
		
		@Override
		public void onConfigure() {
			super.onConfigure();
			setVisible(metadata.getType(type) != null);
		}
		
		@Override
		protected void onComponentTag(ComponentTag tag) {
			super.onComponentTag(tag);
			if (openEditor == type)
				tag.append("class", "selected", " ");
			else if (openEditor != null)
				tag.append("class", "off", " ");
			if (metadata.getType(type).isPreferred()) 
				tag.append("class", "preferred", " ");
			
		}

		@Override
		public void onClick(AjaxRequestTarget target) {
			// Make sure only one editor is open at a time.
			if (openEditor != null)
				return;
			openEditor = type;

			IModel<Response> mResponse = ResponseService.get().newResponse(CwmSession.get().getUserModel(), type, mPrompt);
			ResponseEditor editor = new ResponseEditor(getPlaceholderId(), mResponse, metadata, loc) {
				private static final long serialVersionUID = 1L;
				@Override
				public void onSave (final AjaxRequestTarget target) {
					super.onSave(target);
					removeFromPage(target);
				}
				public void onCancel (AjaxRequestTarget target) {
					super.onCancel(target);
					removeFromPage(target);
				}
				private void removeFromPage (AjaxRequestTarget target) {
					openEditor = null;
					getListComponent().clearPlaceholderComponent(target);
					target.addComponent(ResponseButtons.this);					
					target.addComponent(getListComponent());
				}
			};
			editor.setContext(getContext());
			editor.setNewResponse(true);
			getListComponent().putPlaceholderComponent(editor, target);
			target.addComponent(ResponseButtons.this);
		}

	}
	
	/**
	 * Returns a ResponseList component on this page that refers to the same Prompt.
	 * This ResponseList will be used to insert new responses, and refreshed when appropriate.
	 * @return
	 */
	private ResponseList getListComponent() {
		if (listComponent != null)
			return listComponent;
		getPage().visitChildren(ResponseList.class, new IVisitor<ResponseList>() {

			public Object component(ResponseList component) {
				if (component.getPromptModel().equals(mPrompt)) {
					listComponent = component; // found!
					return STOP_TRAVERSAL;
				}
				return CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER;
			}
		});
		return listComponent;
	}
	
	private String getPlaceholderId() {
		ResponseList rl = getListComponent();
		return rl.getPlaceholderComponent().getId();
	}
}
