package org.cast.isi;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import wicket.contrib.tinymce.settings.Button;
import wicket.contrib.tinymce.settings.TinyMCESettings;


/**
 * This custom TinyMCE Settings can be overridden by your application class.  It specifies tinyMCE Settings used
 * in @ResponseEditor@ or other responses if needed.
 * 
 * @author lynnmccormack
 *
 */
public class ISITinyMCESettings extends TinyMCESettings {
	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("deprecation")
	public ISITinyMCESettings (Theme theme) {
		super(theme);
		this.setToolbarLocation(Location.top);
		this.setToolbarAlign(Align.left);
		this.setToolbarButtons(Toolbar.first, 
				Arrays.asList(Button.fontsizeselect, Button.forecolor, Button.bold, Button.italic, Button.separator,
				Button.bullist, Button.numlist, Button.separator, Button.undo, Button.redo));
		List<Button> noButtons = Collections.emptyList();
		this.setToolbarButtons(Toolbar.second, noButtons);
		this.setToolbarButtons(Toolbar.third, noButtons);
		this.setAutoResize(true);
	}

}
