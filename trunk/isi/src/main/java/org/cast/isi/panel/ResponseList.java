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
import org.cast.isi.ISISession;
import org.cast.isi.data.ContentLoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	protected IModel<User> mTargetUser = ISISession.get().getTargetUserModel();

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
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ResponseList.class);
	private static final long serialVersionUID = 1L;

	public ResponseList (String wicketId, IModel<Prompt> mPrompt, final ResponseMetadata metadata, final ContentLoc loc, IModel<User> mUser) {
		super(wicketId);
		setOutputMarkupId(true);
		this.promptModel = mPrompt;
		this.metadata = metadata;
		this.loc = loc;
		if (mUser != null) 
			this.mTargetUser = mUser;
		
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		dataProvider = responseService.getResponseProviderForPrompt(promptModel, mTargetUser);
		// response list sort order is set by application configuration
		dataProvider.getSortState().setPropertySortOrder(ISIApplication.get().getResponseSortField(), ISIApplication.get().getResponseSortState());
		
		DataView<Response> dataView = new DataView<Response>("dataView", dataProvider) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(Item<Response> item) {
				EditableResponseViewer viewer = new EditableResponseViewer("response", item.getModel(), metadata, loc) {
					private static final long serialVersionUID = 1L;

					@Override
					protected void onDelete(AjaxRequestTarget target) {
						super.onDelete(target);
						target.addComponent(ResponseList.this);
						getDirectionsComponent().setVisibility();
						target.addComponent(getDirectionsComponent());
					}
				};
				viewer.setAllowEdit(allowEdit);
				viewer.setAllowNotebook(allowNotebook);
				viewer.setAllowWhiteboard(allowWhiteboard);
				viewer.setContext(context); // pass through the context
				item.add(viewer);
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
		target.addComponent(getDirectionsComponent());
	}
	
	public void putPlaceholderComponent (Component component, AjaxRequestTarget target) {
		getPlaceholderComponent().replaceWith(component);
		target.addComponent(component);
		getDirectionsComponent().setVisibility();
		target.addComponent(getDirectionsComponent()); 
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
			setVisible(dataProvider.size()==0 && (getPlaceholderComponent() instanceof EmptyPanel) && isAllowEdit());
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