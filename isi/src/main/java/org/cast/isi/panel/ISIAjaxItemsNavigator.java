package org.cast.isi.panel;

import org.apache.wicket.model.IModel;

import com.aplombee.IQuickView;
import com.aplombee.navigator.AjaxItemsNavigator;

/**
 * Override in order to use ISI-like markup and styling.
 * @author bgoldowsky
 *
 */
public class ISIAjaxItemsNavigator extends AjaxItemsNavigator {

	private static final long serialVersionUID = 1L;

	public ISIAjaxItemsNavigator(String id, IModel model, IQuickView repeater) {
		super(id, model, repeater);
	}

	public ISIAjaxItemsNavigator(String id, IQuickView repeater) {
		super(id, repeater);
	}
	
}
