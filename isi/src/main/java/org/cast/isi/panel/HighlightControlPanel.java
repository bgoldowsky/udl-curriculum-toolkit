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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.cast.cwm.components.ClassAttributeModifier;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.Response;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.cwm.data.component.highlight.ActivateHighlighterBehavior;
import org.cast.cwm.data.component.highlight.HighlightDisplayPanel;
import org.cast.cwm.service.HighlightService;
import org.cast.cwm.service.HighlightService.HighlightType;
import org.cast.cwm.xml.XmlSectionModel;
import org.cast.cwm.xml.transform.FilterElements;
import org.cast.isi.ISISession;
import org.cast.isi.ISIXmlComponent;
import org.cast.isi.data.ISIPrompt;
import org.cast.isi.service.ISIResponseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A page-level control panel for highlights.  The basic controls are handled
 * via javascript.  The bulk of this panel is actually support content and
 * the ability to edit an application wide "Highlight Label."
 * @author jbrookover
 *
 */
public class HighlightControlPanel extends Panel {
	
	protected static final Logger log = LoggerFactory.getLogger(HighlightControlPanel.class);
	private static final long serialVersionUID = 1L;
	protected boolean isTeacher;
	
	// Target user to display (current user or student that teacher is viewing)
	private IModel<User> targetUser = ISISession.get().getTargetUserModel();

	public HighlightControlPanel(String id, IModel<? extends Prompt > mHighlightPrompt, XmlSectionModel mSection) {
		super(id, mHighlightPrompt);

		isTeacher = ISISession.get().getUser().getRole().subsumes(Role.TEACHER);

		for (HighlightType type : HighlightService.get().getHighlighters()) {
			WebMarkupContainer controlContainer = new WebMarkupContainer("control" + type.getColor());
			controlContainer.add(new ActivateHighlighterBehavior(type));
			add(controlContainer);
			
			if (type.getColor().equals('Y')) {
				WebMarkupContainer hintContainer = new WebMarkupContainer("hintContainer");
				controlContainer.add(hintContainer);
				
				// get the authored highlight hint text
				ISIXmlComponent highlighterComponent = new ISIXmlComponent("highlightHint", mSection, "student");
				highlighterComponent.setTransformParameter(FilterElements.XPATH, ".//dtb:annotation[@class='highlight']/dtb:p");
				hintContainer.add(highlighterComponent);
				
				// find authored highlights, look for a <span="key"> 
				boolean hintFound = false;
				NodeList elements = mSection.getObject().getElement().getElementsByTagName("span");
				for (int i = 0; i < elements.getLength(); i++) {
					if (((Element) elements.item(i)).getAttribute("class").equals("key")) {
						hintFound = true;
						break;
					}
				}

				// if the author added a hint or highlights then allow the yellow highlight box to open
				if (!highlighterComponent.isEmpty() || hintFound) {
					controlContainer.add(new ClassAttributeModifier("helper"));
				}				
				
				// if the author added highlights then allow the hint box to appear
				WebMarkupContainer hintCompareBox = new WebMarkupContainer("hintCompareBox");
				hintContainer.add(hintCompareBox);
				hintCompareBox.setVisible(hintFound);
				hintCompareBox.add(new Label("hintText", new StringResourceModel("highlightsPanel.hintText", null)));
				hintCompareBox.add(new Label("compareText", new StringResourceModel("highlightsPanel.compareText", null)));
				
				// if there aren't any hints then hide the hintcontainer
				hintContainer.setVisible(!highlighterComponent.isEmpty() || hintFound);
		
			} else if (type.getColor().equals('G')) {
				controlContainer.add(new EditHighlightLabelForm("editHighlightLabelForm", 
						ISIResponseService.get().getResponseForPrompt(mHighlightPrompt, targetUser), 
						mHighlightPrompt)).setEnabled(!isTeacher);
			}
		}
		
		setOutputMarkupId(true);
		setMarkupId(HighlightService.GLOBAL_CONTROL_ID);
	}
	
	@Override
	protected void onBeforeRender() {
		
		Object result = getPage().visitChildren(HighlightDisplayPanel.class, new IVisitor<HighlightDisplayPanel>() {

			public Object component(HighlightDisplayPanel component) {
				return IVisitor.STOP_TRAVERSAL;
			}
		});
		
		if (result == null)
			throw new IllegalStateException("HighlightControlPanel must be on the same page as a HighlightDisplayPanel.");
		
		super.onBeforeRender();
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
		private boolean editing = false; // Are we editing the label for the editable highlighter?
	
		@SuppressWarnings("unchecked")
		public EditHighlightLabelForm(String id, final IModel<Response> model, IModel<? extends Prompt > mHighlightPrompt) {
			super(id, model);
			setOutputMarkupId(true);
			
			// If there is no existing editable label, create a response and swap to label editing mode
			if (getModelObject() == null) {
				setModel(ISIResponseService.get().newPageHighlightsLabel(targetUser, (IModel<ISIPrompt>) mHighlightPrompt));
				editing = true;
			}
			
			// Add editable color container
			WebMarkupContainer editContainer = new WebMarkupContainer("editableColor");

			editContainer.add(new Label("label", new PropertyModel<String>(getModel(), "text")) {
				private static final long serialVersionUID = 1L;

				@Override
				public boolean isVisible() {
					return !editing;
				}				
			}.setOutputMarkupPlaceholderTag(true));
			
			editContainer.add(new TextField<String>("labelEdit", new Model<String>(getModelObject().getText())) {
				private static final long serialVersionUID = 1L;

				@Override
				public boolean isVisible() {
					return editing;
				}				
			}.setRequired(true).add(new SimpleAttributeModifier("maxlength", "32")).setOutputMarkupPlaceholderTag(true));
			
			editContainer.add (new AjaxLink<Void>("edit") {
				private static final long serialVersionUID = 1L;

				@Override
				public boolean isVisible() { 
					return !editing && !isTeacher; 
				}
				
				public void onClick(AjaxRequestTarget target) {
					editing = true;
					target.addComponent(EditHighlightLabelForm.this);
				}

			});

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
						target.addComponent(EditHighlightLabelForm.this);
						target.appendJavascript("showIndicators();");
					}
				}
				
				@Override
				protected void onError(AjaxRequestTarget target, Form<?> form) {
					if (target != null)
						target.addComponent(form.get("feedback"));
				}
			});			
			add(editContainer);			
			add(new FeedbackPanel("feedback", new ContainerFeedbackMessageFilter(HighlightControlPanel.this)).setOutputMarkupId(true));		
		}
		
		@Override
		public void onSubmit() {
			editing = false; // In case submitted in a non-ajax manner			
			String label = get("editableColor" + Component.PATH_SEPARATOR + "labelEdit").getDefaultModelObjectAsString();
			ISIResponseService.get().saveHighlighterLabel(getModel(), label);
		}
	}

}