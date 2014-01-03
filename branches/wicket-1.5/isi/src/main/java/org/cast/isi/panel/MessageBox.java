package org.cast.isi.panel;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;

/**
 * Displays an advisory message with appropriate HTML formatting.
 * Various messages can be defined in the properties files, for different contexts.
 * 
 * @author bgoldowsky
 *
 */
public class MessageBox extends Panel {

	private static final long serialVersionUID = 1L;
	
	protected Label text;

	public MessageBox(String id, String context) {
		super(id);
		
		add(text = new Label("text", new ResourceModel("message." + context)));
		
	}

}
