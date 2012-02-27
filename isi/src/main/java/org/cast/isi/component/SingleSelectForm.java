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
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.Response;
import org.cast.cwm.data.User;
import org.cast.cwm.service.ResponseService;
import org.cast.isi.ISISession;
import org.cast.isi.page.ISIBasePage;
import org.cast.isi.panel.SingleSelectScoreIndicator;
import org.cast.isi.service.ISIResponseService;

/**
 * A multiple choice form.  This will have a {@link RadioGroup}&lt;String&gt; child with
 * several {@link SingleSelectItem} children.
 *  
 * @author jbrookover
 *
 */
@Slf4j
public class SingleSelectForm extends Form<Prompt> {

	private static final long serialVersionUID = 1L;
	
	@Getter
	private IModel<Response> mResponse;
	
	@Getter @Setter
	private boolean disabledOnCorrect = false;

	public SingleSelectForm(String id, IModel<Prompt> mcPrompt) {
		super(id, mcPrompt);

		IModel<User> mTargetUser = ISISession.get().getTargetUserModel();
		if (!mTargetUser.getObject().equals(ISISession.get().getUser()))
			setEnabled(false);
		
		mResponse = ResponseService.get().getResponseForPrompt(getModel(), mTargetUser);
		if (mResponse.getObject() == null)
			mResponse = ISIResponseService.get().newSingleSelectResponse(mTargetUser, getModel());
		
		add(new SingleSelectScoreIndicator("mcScore", mResponse));
		
		RadioGroup<String> radioGroup = new RadioGroup<String>("radioGroup", new Model<String>(mResponse.getObject().getText()));
		add(radioGroup);

		AjaxSubmitLink link = new AjaxSubmitLink("submitLink") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				if (target != null) {
					target.addComponent(SingleSelectForm.this);
				}
			}
			
			@Override
			public boolean isVisible() {
				return isEnabledInHierarchy();
			}
		};
		link.setOutputMarkupId(true);
		radioGroup.add(link);

		// Message displayed if no multiple choice item has been chosen
		radioGroup.add(new Label("selectNone", new ResourceModel("isi.noMultChoiceSelected", "Make a selection")).setVisible(false));
	}
	
	
	@Override
	public void onSubmit() {
		SingleSelectItem selectedItem = getSelectedItem();

		if (selectedItem == null) {
			get("radioGroup:selectNone").setVisible(true);
		} else {
			log.debug("Single Select Option Submitted: {}", selectedItem.getDefaultModelObject());
			get("radioGroup:selectNone").setVisible(false);
			// Save Response
			ISIResponseService.get().saveSingleSelectResponse(mResponse, selectedItem.getModel().getObject(), selectedItem.isCorrect(), ((ISIBasePage)getPage()).getPageName());
		}
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
	protected void onBeforeRender() {
		if (disabledOnCorrect && mResponse.getObject().isCorrect())
			setEnabled(false);
		super.onBeforeRender();
	}

	@Override
	protected void onDetach() {
		if (mResponse != null)
			mResponse.detach();
		super.onDetach();
	}

}