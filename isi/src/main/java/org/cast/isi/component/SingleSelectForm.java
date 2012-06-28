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
package org.cast.isi.component;

import lombok.Getter;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.Response;
import org.cast.cwm.data.User;
import org.cast.isi.ISISession;
import org.cast.isi.service.IISIResponseService;

import com.google.inject.Inject;

public abstract class SingleSelectForm extends Form<Prompt> implements ISingleSelectFormListener {

	private static final long serialVersionUID = 1L;

	@Getter
	protected IModel<Response> mResponse;
	
	protected IModel<User> mUser;
	protected IModel<User> mTargetUser;
	
	@Inject
	protected IISIResponseService responseService;

	public SingleSelectForm(String id) {
		super(id);
	}

	public SingleSelectForm(String id, IModel<Prompt> mcPrompt) {
		this(id, mcPrompt, ISISession.get().getUserModel(), ISISession.get().getTargetUserModel());
	}

	public SingleSelectForm(String id, IModel<Prompt> mcPrompt, IModel<User> userModel, IModel<User> targetUserModel) {
		super(id, mcPrompt);
		this.mUser = userModel;
		this.mTargetUser = targetUserModel;
		if (!mTargetUser.getObject().equals(mUser.getObject()))
			setEnabled(false);
		
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		mResponse = responseService.getResponseForPrompt(getModel(), mTargetUser);
	}
	
	protected void refreshListeners(final AjaxRequestTarget target) {
		getPage().visitChildren(ISingleSelectFormListener.class, new IVisitor<Component>() {
			public Object component(Component component) {
				target.addComponent(component);
				return CONTINUE_TRAVERSAL;
			}

		});
	}
	
	/**
	 * Find the currently selected SingleSelectItem
	 * @return the selected item, or null if there is none.
	 */
	protected SingleSelectItem getSelectedItem() {
		return (SingleSelectItem) visitChildren(SingleSelectItem.class, new IVisitor<SingleSelectItem>() {
			public Object component(SingleSelectItem component) {
				if (component.isSelected()) {
					// Halt traversal by returning this component
					return component;
				} else {
					return CONTINUE_TRAVERSAL;
				}
			}
		});
		
	}
	
	@Override
	protected void onDetach() {
		if (mResponse != null)
			mResponse.detach();
		if (mUser != null) {
			mUser.detach();
		}
		if (mTargetUser != null) {
			mTargetUser.detach();
		}
		super.onDetach();
	}
	
	protected User getUser() {
		if (mUser == null)
			return null;
		return mUser.getObject();
	}

}