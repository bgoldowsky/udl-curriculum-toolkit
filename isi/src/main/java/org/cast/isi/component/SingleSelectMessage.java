package org.cast.isi.component;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;

/**
 * A container for a message tied to a SingleSelectItem.
 * It makes itself visible whenever the associated item is selected.
 * 
 * @author bgoldowsky
 *
 */
public class SingleSelectMessage extends WebMarkupContainer {

	private String itemId;

	private static final long serialVersionUID = 1L;

	/**
	 * Construct with the wicket ID of the SingleSelectItem to be connected.
	 * It is looked for as a sibling of this component.
	 * @param id
	 * @param itemId
	 */
	public SingleSelectMessage(String id, String itemId) {
		super(id);
		this.itemId = itemId;
	}

	@Override
	protected void onBeforeRender() {
		Component item =  getParent().get(itemId);
		if (item == null || !(item instanceof SingleSelectItem))
			throw new IllegalArgumentException("'for' attribute of SingleSelectMessage '" + itemId
					+ "' doesn't correspond to a SingleSelectItem; it is " + item);

		setVisible(((SingleSelectItem)item).isSelected());
		super.onBeforeRender();
	}

	@Override
	// Required in order to set visibility in onBeforeRender
	protected boolean callOnBeforeRenderIfNotVisible() {
	   return true;
	}	
	
}
