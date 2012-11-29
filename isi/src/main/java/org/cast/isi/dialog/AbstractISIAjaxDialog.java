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
package org.cast.isi.dialog;

import lombok.Getter;
import lombok.Setter;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.cast.cwm.data.component.DialogBorder;
import org.cast.isi.page.ISIBasePage;
import org.cast.isi.page.ISIStandardPage;
/**
 * This is a styled dialog that is designed to be displayed via AJAX.  It
 * coordinates with {@link ISIBasePage#displayDialog(AbstractISIAjaxDialog, AjaxRequestTarget)}
 * to show itself with a certain wicket:id.  Therefore, no wicket:id is required
 * for these dialogs.
 * 
 * @param <T>
 */
public abstract class AbstractISIAjaxDialog<T> extends Panel implements IHeaderContributor {

	private static final long serialVersionUID = 1L;
	
	@Getter
	protected DialogBorder dialogBorder;
	
	@Getter @Setter 
	private String title = "Default Dialog Title";

	public AbstractISIAjaxDialog() {
		this(null);
	}
	
	public AbstractISIAjaxDialog(IModel<T> model) {
		super(ISIStandardPage.DISPLAY_DIALOG_ID, model);
		setOutputMarkupId(true);		
		
		dialogBorder = newDialogBorder("dialogBorder", new PropertyModel<String>(this, "title"));

		// This allows us to add components directly to the panel, even though they are enclosed in the DialogBorder
		dialogBorder.setTransparentResolver(true); 
		dialogBorder.setMoveContainer(this);		
		dialogBorder.getContentContainer().add(new SimpleAttributeModifier("class", "visuraloverlaycontent modalContainer modalBody"));
		add(dialogBorder);
	}
	
	protected DialogBorder newDialogBorder(String string, IModel<String> mTitle) {
		return new DialogBorder(string, mTitle);
	}
	
	public void renderHead(final IHeaderResponse response) {
		ISIStandardPage.renderThemeCSS(response, "css/modal.css");
		// set up any collapse boxes in the modal
		response.renderOnLoadJavascript("collapseBox();");
	}
	
	/**
	 * Add this behavior to a link and clicking the link will Open the dialog.
	 * @return
	 */
	public IBehavior getClickToOpenBehavior() {
		return getDialogBorder().getClickToOpenBehavior();
	}
	
	/**
	 * Add this behavior to a link and clicking the link will Close the dialog.
	 * @return
	 */
	public IBehavior getClickToCloseBehavior() {
		return getDialogBorder().getClickToCloseBehavior();
	}
	
	/**
	 * Closes the dialog in the current Ajax Request by appending the 
	 * close Javascript to the request target.
	 * 
	 * @param target
	 */
	public void close(AjaxRequestTarget target) {
		getDialogBorder().close(target);
	}
	
	/**
	 * Gets model
	 * 
	 * @return model
	 */
	@SuppressWarnings("unchecked")
	public final IModel<T> getModel()
	{
		return (IModel<T>)getDefaultModel();
	}

	/**
	 * Sets model
	 * 
	 * @param model
	 */
	public final void setModel(IModel<T> model)
	{
		setDefaultModel(model);
	}

	/**
	 * Gets model object
	 * 
	 * @return model object
	 */
	@SuppressWarnings("unchecked")
	public final T getModelObject()
	{
		return (T)getDefaultModelObject();
	}

	/**
	 * Sets model object
	 * 
	 * @param object
	 */
	public final void setModelObject(T object)
	{
		setDefaultModelObject(object);
	}
}