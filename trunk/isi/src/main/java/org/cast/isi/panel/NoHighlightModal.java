package org.cast.isi.panel;

import org.apache.wicket.markup.html.panel.Panel;

/**
 * Simple modal dialog, pre-loaded onto the page, which is displayed by 
 * javascript when the user attempts to "compare highlights" without having
 * made any highlights of their own.
 * 
 * @see org.cast.isi.panel.HighlightControlPanel
 * @author bgoldowsky
 *
 */
public class NoHighlightModal extends Panel {

	private static final long serialVersionUID = 1L;

	public NoHighlightModal(String id) {
		super(id);
	}

}
