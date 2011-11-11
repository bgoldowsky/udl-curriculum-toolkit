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
