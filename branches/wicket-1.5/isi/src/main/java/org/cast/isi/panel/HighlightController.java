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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.Response;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.cwm.service.HighlightService.HighlightType;
import org.cast.cwm.xml.XmlSectionModel;
import org.cast.cwm.xml.transform.FilterElements;
import org.cast.isi.ISISession;
import org.cast.isi.ISIXmlComponent;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.data.ISIPrompt;
import org.cast.isi.data.PromptType;
import org.cast.isi.service.IISIResponseService;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.inject.Inject;


/**
 * Markup for a single highlighter control.
 * Includes display of whether the highlighter is turned on, the button, the label, the label-editing form,
 * and (optionally) compare/hint functionality.
 * 
 * @author bgoldowsky
 *
 */
public class HighlightController extends Panel {

	@Inject
	protected IISIResponseService responseService;
	
	// Target user to display (current user or student that teacher is viewing)
	private IModel<User> targetUser;
	
	private boolean isTeacher;

	private HighlightType type;

	private ContentLoc loc;
	
	// The name for this highlighter set by the user, if any
	private boolean editing = false; // Are we editing the label for the editable highlighter?
	@Getter private String editedName;
	
	private boolean hasHint = false;

	private static final long serialVersionUID = 1L;

	public HighlightController(String id, HighlightType type, ContentLoc loc, XmlSectionModel mSection) {
		super(id);
		this.type = type;
		this.loc = loc;
		setOutputMarkupId(true);
		if (!type.isOn())
			setVisible(false);
		
		targetUser = ISISession.get().getTargetUserModel();
		isTeacher = ISISession.get().getUser().getRole().subsumes(Role.TEACHER);
		
		String color = type.getColor().toString().toLowerCase();
		
		WebMarkupContainer labelContainer = new WebMarkupContainer("labelContainer");
		labelContainer.setOutputMarkupId(true);
		add(labelContainer);
		
		WebMarkupContainer link = new WebMarkupContainer("highlightLink");
		link.add(new SimpleAttributeModifier("onclick", String.format("$().CAST_Highlighter('modify', '%c');return false;", type.getColor())));
		labelContainer.add(link);

		// The label may be editable, or may be retrieved from the properties file.
		IModel<String> labelModel;
		if (type.isEditable()) {
			labelModel = new PropertyModel<String>(this, "editedName");
		} else {
			// The e.g. yellow highlighter name in the properties file is highlightsPanel.yHighlighterName
			labelModel = new StringResourceModel(String.format("highlightsPanel.%sHighlighterName", color),
				this, null, "Highlighter");
		}
		link.add(new Label("name", labelModel) {
			private static final long serialVersionUID = 1L;
			public boolean isVisible() { 
				return !editing;
			}
		});
		
		// Form for editing the label, if requested
		makeEditForm(type, labelContainer);
		
		// the yellow highlighter may have both authored highlights and hints associated with it
		WebMarkupContainer hintContainer = new WebMarkupContainer("hintContainer");
		add(hintContainer);
		if (type.getColor()=='Y')
			makeHint(hintContainer, mSection);
		else
			hintContainer.setVisibilityAllowed(false);
	}


	/**
	 * The highlighter has either an editable or non-editable label.  The non-editable label is found in the properties file.
	 * The editability of this label is set at the application level in the configuration file.
	 *
	 * @param type which highlighter the label will be for
	 */
	private void makeEditForm (HighlightType type, final WebMarkupContainer container) {

		if (type.isEditable()) {
			String color = type.getColor().toString().toLowerCase();
	
			IModel<Prompt> mPrompt = responseService.getOrCreateHighlightPrompt(PromptType.HIGHLIGHTLABEL, loc, color);
	
			EditHighlightLabelForm editHighlightLabelForm = new EditHighlightLabelForm("editHighlightNameForm", 
					responseService.getResponseForPrompt(mPrompt, targetUser), 
					mPrompt, container);
			container.add(editHighlightLabelForm);
			editHighlightLabelForm.setEnabled(!isTeacher);
		} else {
			container.add (new WebMarkupContainer("editHighlightNameForm").setVisibilityAllowed(false));
		}

		// Button to show the edit form
		AjaxLink<Void> editButton = new AjaxLink<Void>("edit") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() { 
				return !editing && !isTeacher; 
			}
			
			public void onClick(AjaxRequestTarget target) {
				editing = true;
				target.addComponent(container);
			}
		};
		editButton.setVisibilityAllowed(type.isEditable());
		container.add (editButton);
	}

	protected void makeHint (WebMarkupContainer hintContainer, XmlSectionModel mSection) {
		// Is there any authored hint text?
		ISIXmlComponent highlighterComponent = new ISIXmlComponent("highlightHint", mSection, "student");
		highlighterComponent.setTransformParameter(FilterElements.XPATH, ".//dtb:annotation[@class='highlight']/dtb:p");
		hintContainer.add(highlighterComponent);
		if (!highlighterComponent.isEmpty())
			hasHint = true;
		

		// Search to see if there are any authored highlights for "compare" functionality
		boolean hasKeySpans = false;
		NodeList elements = mSection.getObject().getElement().getElementsByTagName("span");
		for (int i = 0; i < elements.getLength(); i++) {
			if (((Element) elements.item(i)).getAttributeNS(null,"class").equals("key")) {
				hasKeySpans = true;
				break;
			}
		}
		if (hasKeySpans)
			hasHint = true;
		
		// if the author added highlights then allow the hint box to appear
		WebMarkupContainer hintCompareBox = new WebMarkupContainer("hintCompareBox");
		hintContainer.add(hintCompareBox);
		hintCompareBox.setVisible(hasKeySpans);
		hintCompareBox.add(new Label("hintText", new StringResourceModel("highlightsPanel.hintText", null)));
		hintCompareBox.add(new Label("compareText", new StringResourceModel("highlightsPanel.compareText", null)));

		// if there aren't any hints then hide the hintcontainer
		hintContainer.setVisible(hasHint);
	}
	
	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		// Add color-determining class attribute
		tag.put("class", "hlRow control"+type.getColor() + (hasHint ? " helper" : ""));
	}


	/**
	 * A form that includes controls for highlighting.  This is a Form because it also allows
	 * the user to specify and save a highlighter label. 
	 * 
	 * @author jbrookover
	 *
	 */
	private class EditHighlightLabelForm extends Form<Response> {
	
		private static final long serialVersionUID = 1L;
	
		@SuppressWarnings("unchecked")
		public EditHighlightLabelForm(String id, final IModel<Response> model, IModel<? extends Prompt > mHighlightPrompt, final WebMarkupContainer container) {
			super(id, model);
			setOutputMarkupId(true);
			
			// If there is no existing editable label, create a response and swap to label editing mode
			if (getModelObject() == null) {
				setModel(responseService.newPageHighlightsLabel(targetUser, (IModel<ISIPrompt>) mHighlightPrompt));
				editing = true;
			}
			
			// Put current value into the control's label
			HighlightController.this.editedName = getModelObject().getText();
			
			// Add editable color container
			WebMarkupContainer editContainer = new WebMarkupContainer("editableColor");

			editContainer.add(new TextField<String>("labelEdit", new Model<String>(getModelObject().getText())) {
				private static final long serialVersionUID = 1L;

				@Override
				public boolean isVisible() {
					return editing;
				}				
			}.setRequired(true).add(new SimpleAttributeModifier("maxlength", "32")).setOutputMarkupPlaceholderTag(true));
			
			editContainer.add (new AjaxButton("save") {
				private static final long serialVersionUID = 1L;

				@Override
				public boolean isVisible() { 
					return editing && !isTeacher; 
				}

				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					editing = false;
					if (target != null) {
						target.addComponent(container);
						target.appendJavaScript("showIndicators();");
					}
				}
				
//				@Override
//				protected void onError(AjaxRequestTarget target, Form<?> form) {
//					if (target != null)
//						target.addComponent(form.get("feedback"));
//				}
			});			
			add(editContainer);
// Would be nice to have some feedback on errors, but there's no room for it in the design.
//			add(new FeedbackPanel("feedback", new ContainerFeedbackMessageFilter(HighlightController.this)).setOutputMarkupId(true));		
		}
		
		@Override
		public void onSubmit() {
			editing = false; // In case submitted in a non-ajax manner			
			String label = get("editableColor" + Component.PATH_SEPARATOR + "labelEdit").getDefaultModelObjectAsString();
			responseService.saveHighlighterLabel(getModel(), label);
			HighlightController.this.editedName = label;
		}
	}


}
