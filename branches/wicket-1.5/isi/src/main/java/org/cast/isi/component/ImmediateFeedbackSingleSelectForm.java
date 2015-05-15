/*
 * Copyright 2011-2015 CAST, Inc.
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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.User;
import org.cast.isi.page.ISIBasePage;


/**
 * A multiple choice form.  This will have a {@link RadioGroup}&lt;String&gt; child with
 * several {@link SingleSelectItem} children.
 *  
 * @author jbrookover
 *
 */
@Slf4j
public class ImmediateFeedbackSingleSelectForm extends SingleSelectForm {

	private static final long serialVersionUID = 1L;
	
	@Getter @Setter
	protected boolean disabledOnCorrect = false;

	public ImmediateFeedbackSingleSelectForm(String id, IModel<Prompt> mcPrompt, IModel<User> userModel, IModel<User> targetUserModel) {
		super(id, mcPrompt, userModel, targetUserModel);
	}
	
	public ImmediateFeedbackSingleSelectForm(String id, IModel<Prompt> mcPrompt) {
		super(id, mcPrompt);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		if (mResponse.getObject() == null)
			mResponse = responseService.newSingleSelectResponse(mTargetUser, getModel());
				
		RadioGroup<String> radioGroup = new RadioGroup<String>("radioGroup", new Model<String>(mResponse.getObject().getText()));
		add(radioGroup);

		AjaxSubmitLink link = new AjaxSubmitLink("submitLink") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				if (target != null) {
					refreshListeners(target);
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
	protected void onBeforeRender() {
		if (disabledOnCorrect && mResponse.getObject().isCorrect())
			setEnabled(false);
		super.onBeforeRender();
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
			responseService.saveSingleSelectResponse(mResponse, selectedItem.getModel().getObject(), selectedItem.isCorrect(), ((ISIBasePage)getPage()).getPageName());
		}
	}

}