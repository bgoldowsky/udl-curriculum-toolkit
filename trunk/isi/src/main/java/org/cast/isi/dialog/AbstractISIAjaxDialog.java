package org.cast.isi.dialog;

import lombok.Getter;
import lombok.Setter;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.cast.cwm.components.ShyLabel;
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
	protected boolean showCloseLink = true;
	
	@Getter @Setter 
	private String title = "Default Dialog Title";

	public AbstractISIAjaxDialog() {
		this(null);
	}
	
	public AbstractISIAjaxDialog(IModel<T> model) {
		super(ISIStandardPage.DISPLAY_DIALOG_ID, model);
		setOutputMarkupId(true);		
		
		dialogBorder = new DialogBorder ("dialogBorder", new PropertyModel<String>(this, "title")) {

			private static final long serialVersionUID = 1L;

			// We customize DialogBorder to have the top-right close button optionally hidden.
			@Override
			protected void addCloseLink(WebMarkupContainer container) {
				WebMarkupContainer link = new WebMarkupContainer("closeWindowLink") {
					private static final long serialVersionUID = 1L;
					@Override
					public boolean isVisible() {
						return showCloseLink;
					}
				};
				container.add(link);
				link.add(getClickToCloseBehavior());
			}

			// Customize the title so that it can be invisible when not used.
			@Override
			protected void addTitle(WebMarkupContainer container) {
				container.add(new ShyLabel("title", getModel()));
			}
		};

		// This allows us to add components directly to the panel, even though they are enclosed in the DialogBorder
		dialogBorder.setTransparentResolver(true); 
		dialogBorder.setMoveContainer(this);		
		dialogBorder.getContentContainer().add(new SimpleAttributeModifier("class", "visuraloverlaycontent modalContainer modalBody"));
		add(dialogBorder);
	}
	
	public void renderHead(final IHeaderResponse response) {
		response.renderCSSReference(new ResourceReference("/css/modal.css"));
		// set up move button and any collapse boxes in the modal
		response.renderOnLoadJavascript("collapseBox();modalMove();");
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