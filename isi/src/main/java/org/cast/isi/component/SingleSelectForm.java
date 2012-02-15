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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.Response;
import org.cast.cwm.service.ResponseService;
import org.cast.isi.ISISession;
import org.cast.isi.service.ISIResponseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A multiple choice form.  This will have a {@link RadioGroup}&lt;String&gt; child with
 * several {@link SingleSelectItem} children.
 *  
 * @author jbrookover
 *
 */
public class SingleSelectForm extends Form<Prompt> {

	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(SingleSelectForm.class);
	
	@Getter
	private IModel<Response> mResponse;
	
	@Getter @Setter
	private boolean disabledOnCorrect = false;

	private SingleSelectItem selectedItem = null;


	@SuppressWarnings("deprecation")
	public SingleSelectForm(String id, IModel<Prompt> mcPrompt) {
		super(id, mcPrompt);

		mResponse = ResponseService.get().getResponseForPrompt(getModel(), ISISession.get().getTargetUserModel());
		
		if (mResponse.getObject() == null)
			mResponse = ISIResponseService.get().newSingleSelectResponse(ISISession.get().getTargetUserModel(), getModel());

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
		add(link);

		add(new WebMarkupContainer("selectNone").setVisible(false));
		add(new Label("message", "").setOutputMarkupPlaceholderTag(true).setVisible(false));
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public void onSubmit() {

		final RadioGroup<String> rg = (RadioGroup<String>) get("radioGroup");
		
		visitChildren(SingleSelectItem.class, new IVisitor<SingleSelectItem>() {
			public Object component(SingleSelectItem component) {
				String messageId = component.getId().replaceFirst("selectItem", "selectMessage");
				get(messageId).setVisible(false);
				if (component.getModelObject() != null && !component.getModelObject().equals("") && component.getModelObject().equals(rg.getModelObject()))
					selectedItem = component;
				return CONTINUE_TRAVERSAL;
			}
		});

		if (selectedItem == null) {
			get("selectNone").setVisible(true);
			return;
		} else {
			get("selectNone").setVisible(false);
			// Display the component's corresponding "selectMessage" with appropriate style
			String className = selectedItem.isCorrect() ? "stResult correct" : "stResult incorrect";
			String messageId = selectedItem.getId().replaceFirst("selectItem", "selectMessage");
			get(messageId).setVisible(true).add(new SimpleAttributeModifier("class", className));
			log.debug("Single Select Option Submitted: {}", selectedItem.getModelObject());
		}

		// Save Response
		ISIResponseService.get().saveSingleSelectResponse(mResponse, rg.getModelObject(), selectedItem.isCorrect());
	}
	
	@Override
	protected void onDetach() {
		if (mResponse != null)
			mResponse.detach();
		super.onDetach();
	}
	
	@Override
	public boolean isEnabled() {
		// TODO - not sure if this is being implemented for toolkit?? - ldm
		if (disabledOnCorrect)
			return mResponse.getObject().getScore() < mResponse.getObject().getTotal();
		return true;
	}
}