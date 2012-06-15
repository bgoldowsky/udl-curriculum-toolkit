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
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.Response;
import org.cast.cwm.data.ResponseMetadata;
import org.cast.isi.ISIApplication;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.data.ISIResponse;
import org.cast.isi.page.ISIBasePage;
import org.cast.isi.service.ISIResponseService;

public class EditableResponseViewer extends Panel {
	
	@Getter @Setter
	private boolean editing = false;
	
	private WebMarkupContainer viewContainer;
	private WebMarkupContainer editContainer;
	private ResponseViewer viewer;
	private ResponseEditor editor;
	private WebMarkupContainer responseActions;
	private AjaxLink<Void> editLink;

	@Getter @Setter
	protected String context = "default";

	@Getter @Setter
	protected boolean allowEdit = false;

	@Getter @Setter
	protected boolean allowNotebook = false;
	
	@Getter @Setter
	protected boolean allowWhiteboard = false;

	protected IModel<Response> model;
	protected ResponseMetadata metadata;
	protected ContentLoc loc;
	
	private static final long serialVersionUID = 1L;

	private AjaxLink<Response> addToWhiteboard;

	private AjaxLink<Response> addToNotebook;

	public EditableResponseViewer (String wicketId, IModel<Response> model, ResponseMetadata metadata, ContentLoc loc) {
		super(wicketId, model);
		this.model = model;
		this.metadata = metadata;
		this.loc = loc;
		setOutputMarkupId(true);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		add (viewContainer = new WebMarkupContainer("viewContainer"));
		add (editContainer = new WebMarkupContainer("editContainer"));
		
		viewer = new ResponseViewer("viewer", model);
		viewer.setShowDateTime(true);
		viewContainer.add(viewer);
		
		responseActions = new WebMarkupContainer("responseActions");
		responseActions.setOutputMarkupId(true);
		viewContainer.add(responseActions);
		addResponseViewActions(responseActions);

		editor = new ResponseEditor("editor", model, metadata, loc) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onCancel(AjaxRequestTarget target) {
				super.onCancel(target);
				editing = false;
				setVisibility(target);
				EditableResponseViewer.this.onCancel(target);
			}

			@Override
			protected void onDelete(AjaxRequestTarget target) {
				super.onDelete(target);
				EditableResponseViewer.this.onDelete(target);
			}

			@Override
			protected void onSave(AjaxRequestTarget target) {
				super.onSave(target);
				editing = false;
				setVisibility(target);
				EditableResponseViewer.this.onSave(target);
			}
		};
		editor.setOutputMarkupPlaceholderTag(true);
		editor.setContext(context);
		editContainer.add(editor);
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		editContainer.setVisible(editing);
		viewContainer.setVisible(!editing);
		editLink.setVisible(allowEdit);
	}

	private void setVisibility(AjaxRequestTarget target) {
		editContainer.setVisible(editing);
		viewContainer.setVisible(!editing);
		target.addComponent(this);
	}

	protected void addResponseViewActions(WebMarkupContainer container) {
		editLink = new AjaxLink<Void>("editLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {
				editing = true;
				setVisibility(target);
				EditableResponseViewer.this.onEdit(target);
			}
		};
		container.add(editLink);
		
		// Link to the whiteboard when response is already on the whiteboard
		final AnchoredLink whiteboardLink = new AnchoredLink("whiteboardLink", ISIApplication.get().getWhiteboardPageClass()) {	
			private static final long serialVersionUID = 1L;

			@Override
			public void onBeforeRender() {
				setVisible(((ISIResponse)EditableResponseViewer.this.getModelObject()).isInWhiteboard());
				super.onBeforeRender();
			}
		};
		ISIApplication.get().setLinkProperties(whiteboardLink);
		whiteboardLink.setOutputMarkupPlaceholderTag(true);
		container.add(whiteboardLink);

		addToWhiteboard = new AdderButton("addToWhiteboardLink", getModel()) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				ISIResponseService.get().addToWhiteboard((ISIResponse) getModelObject(), (ISIBasePage)getPage());
				target.addComponent(whiteboardLink);
				super.onClick(target);
			}
			
			@Override
			public void onBeforeRender() {
				setVisible(allowWhiteboard && !((ISIResponse)getModelObject()).isInWhiteboard());
				super.onBeforeRender();
			}
		};
		container.add(addToWhiteboard);
		
		// Link to the notebook when response is already on the whiteboard
		final AnchoredLink notebookLink = new AnchoredLink("notebookLink", ISIApplication.get().getNotebookPageClass()) {	
			private static final long serialVersionUID = 1L;

			@Override
			public void onBeforeRender() {
				setVisible(((ISIResponse)EditableResponseViewer.this.getModelObject()).isInNotebook());
				super.onBeforeRender();
			}
		};
		ISIApplication.get().setLinkProperties(notebookLink);
		notebookLink.setOutputMarkupId(true);
		container.add(notebookLink);

		addToNotebook = new AdderButton("addToNotebookLink", getModel()) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				ISIResponseService.get().addToNotebook((ISIResponse) getModelObject(), (ISIBasePage)getPage());
				target.addComponent(notebookLink);
				super.onClick(target);
			}
			
			@Override
			public void onBeforeRender() {
				setVisible(allowNotebook && !((ISIResponse)getModelObject()).isInNotebook());
				super.onBeforeRender();
			}
		};
		container.add(addToNotebook);
	}

	@SuppressWarnings("unchecked")
	protected IModel<Response> getModel() {
		return (IModel<Response>) getDefaultModel();
	}
	
	protected ISIResponse getModelObject() {
		return (ISIResponse) getDefaultModelObject();
	}
	
	/** Called when switching to edit mode
	 * 
	 * @param target
	 */
	protected void onEdit(AjaxRequestTarget target) { }
	
	/** Called when switching to view mode due to cancel.
	 * 
	 * @param target
	 */
	protected void onCancel(AjaxRequestTarget target) { }
	
	/** Called when the response is deleted
	 * 
	 * @param target
	 */
	protected void onDelete(AjaxRequestTarget target) { }
	
	/** Called when the response is saved
	 * 
	 * @param target
	 */
	protected void onSave(AjaxRequestTarget target) { }
	

	/**
	 * Button that adds the response to a list and pops up a window - common code for adding to Notebook and Whiteboard
	 */
	private class AdderButton extends AjaxLink<Response> {

		private static final long serialVersionUID = 1L;

		public AdderButton(String id, IModel<Response> model) {
			super(id, model);
		}

		@Override
		public void onClick(AjaxRequestTarget target) {
			target.addComponent(responseActions);
			target.addComponent(this);
		}
		
		@Override
		protected boolean callOnBeforeRenderIfNotVisible() { return true; }
		
	};

	/**
	 * Link that anchors to the id of the object in the page.  For links to Notebook and Whiteboard.
	 */
	private class AnchoredLink extends BookmarkablePageLink<ISIBasePage> {
		
		private static final long serialVersionUID = 1L;

		public AnchoredLink(String id, Class<? extends WebPage> pageClass) {
			super(id, pageClass);
		}

		@Override
		protected CharSequence appendAnchor(ComponentTag tag, CharSequence url) {
			if (url.toString().indexOf('#')>-1)
				return super.appendAnchor(tag, url);
			return url + "#" + EditableResponseViewer.this.getModelObject().getId();
		}

		@Override
		protected boolean callOnBeforeRenderIfNotVisible() { return true; }
	}

}