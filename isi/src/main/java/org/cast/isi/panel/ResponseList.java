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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.Response;
import org.cast.cwm.data.ResponseMetadata;
import org.cast.cwm.data.User;
import org.cast.cwm.service.IResponseService;
import org.cast.isi.ISIApplication;
import org.cast.isi.data.ContentLoc;

import com.google.inject.Inject;

/**
 * Container for a list of responses and some auxiliary components.
 * Includes a message if the list is empty, and a placeholder where an additional, unsaved response can be edited.
 * 
 * @author bgoldowsky
 *
 */
public class ResponseList extends Panel {

	protected ISortableDataProvider<Response> dataProvider;
	
	@Getter
	protected IModel<Prompt> promptModel;
	
	/**
	 * This is used to construct the resource key for the directions.
	 */
	@Getter @Setter
	protected String context = "default";
	
	@Getter @Setter
	protected IModel<User> mTargetUser;

	@Getter @Setter
	protected boolean allowEdit = true;

	@Getter @Setter
	protected boolean allowNotebook = true;
	
	@Getter @Setter
	protected boolean allowWhiteboard = true;
	
	protected ResponseMetadata metadata;
	protected ContentLoc loc;

	@Inject
	IResponseService responseService;
	
	private static final long serialVersionUID = 1L;

	/**
	 * Create a ResponseList for a particular response area and user.
	 * @param wicketId
	 * @param mPrompt
	 * @param metadata
	 * @param loc
	 * @param mUser
	 */
	public ResponseList (String wicketId, IModel<Prompt> mPrompt, final ResponseMetadata metadata, final ContentLoc loc, IModel<User> mUser) {
		super(wicketId);
		setOutputMarkupId(true);
		this.promptModel = mPrompt;
		this.metadata = metadata;
		this.loc = loc;
		this.mTargetUser = mUser;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		dataProvider = getResponseProvider();
		
		DataView<Response> dataView = new DataView<Response>("dataView", dataProvider) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(Item<Response> item) {
				item.add(getEditableResponseViewer("response", item.getModel(), metadata, loc));
			}
		};
		add(dataView);
		add (new EmptyPanel("placeholder").setOutputMarkupId(true));
		add (new Directions("directions"));
	}

	@Override
	public void onBeforeRender() {
		getDirectionsComponent().setVisibility();
		super.onBeforeRender();
	}
	
	protected ISortableDataProvider<Response> getResponseProvider() {
		 ISortableDataProvider<Response> provider = responseService.getResponseProviderForPrompt(promptModel, mTargetUser);
		// response list sort order is set by application configuration
		ISIApplication app = ISIApplication.get();
		provider.getSortState().setPropertySortOrder(app.getResponseSortField(), app.getResponseSortState());
		return provider;
	}
	
	protected EditableResponseViewer getEditableResponseViewer(String wicketId, IModel<Response> mResponse, ResponseMetadata metadata, ContentLoc loc) {
		EditableResponseViewer viewer = new EditableResponseViewer(wicketId, mResponse, metadata, loc) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onDelete(AjaxRequestTarget target) {
				super.onDelete(target);
				target.add(ResponseList.this);
				getDirectionsComponent().setVisibility();
				target.add(getDirectionsComponent());
			}
		};
		viewer.setAllowEdit(allowEdit);
		viewer.setAllowNotebook(allowNotebook);
		viewer.setAllowWhiteboard(allowWhiteboard);
		viewer.setContext(context); // pass through the context
		return viewer;
	}				

	public String getPlaceholderId() {
		return "placeholder";
	}
	
	/**
	 * Return the placeholder component that is used for new response editors.
	 * Often replaced via ajax, so don't cache the returned component. 
	 * @return
	 */
	public Component getPlaceholderComponent() {
		return get(getPlaceholderId());
	}
	
	public void clearPlaceholderComponent(AjaxRequestTarget target) {
		Component panel = new EmptyPanel(getPlaceholderId()).setOutputMarkupId(true);
		getPlaceholderComponent().replaceWith(panel);
		getDirectionsComponent().setVisibility();
		target.add(getDirectionsComponent());
	}
	
	public void putPlaceholderComponent (Component component, AjaxRequestTarget target) {
		getPlaceholderComponent().replaceWith(component);
		target.add(component);
		getDirectionsComponent().setVisibility();
		target.add(getDirectionsComponent()); 
	}
	
	/**
	 * Return the directions component, so that other components can refresh it.
	 */
	public Directions getDirectionsComponent() {
		return (Directions) get("directions");
	}
	
	/**
	 * The directions component shows a helpful message if there are no current responses.
	 * @author borisgoldowsky
	 *
	 */
	private class Directions extends WebMarkupContainer {
		private static final long serialVersionUID = 1L;
		
		private Directions (String id) {
			super(id);
			setOutputMarkupPlaceholderTag(true);
			add(new Label("text", new StringResourceModel("isi.${context}.directions", ResponseList.this, Model.of(ResponseList.this), "No responses")));
		}
		
		@Override
		public void onConfigure() {
			super.onConfigure();
			setVisibility();
		}
		
		public void setVisibility() {
			// Directions hidden when any response is shown or in progress or not allowed. 
			setVisible(dataProvider.size()==0 && (getPlaceholderComponent() instanceof EmptyPanel));
		}
	}	
	
	@Override
	protected void onDetach() {		
		if (mTargetUser != null)
			mTargetUser.detach();
		if (promptModel != null)
			promptModel.detach();
		super.onDetach();
	}
}