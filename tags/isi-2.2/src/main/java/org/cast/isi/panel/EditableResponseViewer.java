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
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.Response;
import org.cast.cwm.data.ResponseMetadata;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.data.ISIResponse;
import org.cast.isi.service.IISIResponseService;

import com.google.inject.Inject;

public class EditableResponseViewer extends Panel {
	
	@Getter @Setter
	private boolean editing = false;
	
	private WebMarkupContainer viewContainer;
	private WebMarkupContainer editContainer;
	private ResponseViewer viewer;
	private ResponseEditor editor;
	private AjaxLink<Void> editLink;

	@Getter @Setter
	protected String context = "default";
	
	@Getter @Setter
	protected boolean showAuthor = false;

	@Getter @Setter
	protected boolean allowEdit = false;

	@Getter @Setter
	protected boolean allowNotebook = false;
	
	@Getter @Setter
	protected boolean allowWhiteboard = false;

	protected ResponseMetadata metadata;
	protected ContentLoc loc;
	
	private static final long serialVersionUID = 1L;

	@Inject
	protected IISIResponseService responseService;
	
	public EditableResponseViewer (String wicketId, IModel<Response> model, ResponseMetadata metadata, ContentLoc loc) {
		super(wicketId, model);
		this.metadata = metadata;
		this.loc = loc;
		setOutputMarkupId(true);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		add (viewContainer = new WebMarkupContainer("viewContainer"));
		add (editContainer = new WebMarkupContainer("editContainer"));
		
		viewer = new ResponseViewer("viewer", getModel());
		viewer.setShowDateTime(true);
		viewer.setShowAuthor(showAuthor);
		viewContainer.add(viewer);
		
		WebMarkupContainer responseActions = new WebMarkupContainer("responseActions");
		responseActions.setOutputMarkupId(true);
		viewContainer.add(responseActions);
		addResponseViewActions(responseActions);

		editor = new ResponseEditor("editor", getModel(), metadata, loc) {
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
		ResponseViewActionsPanel responseViewActionsPanel = new ResponseViewActionsPanel("viewActions", getModel());
		responseViewActionsPanel.setAllowNotebook(allowNotebook);
		responseViewActionsPanel.setAllowWhiteboard(allowWhiteboard);
		container.add(responseViewActionsPanel);
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

}