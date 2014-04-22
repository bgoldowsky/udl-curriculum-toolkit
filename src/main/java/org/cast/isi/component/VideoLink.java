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
package org.cast.isi.component;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.cast.cwm.xml.XmlSectionModel;
import org.cast.isi.dialog.VideoDialog;
import org.cast.isi.page.ISIStandardPage;

/**
 * An AjaxLink which when clicked creates a {@link VideoDialog} Video in a popup modal
 */
public class VideoLink extends AjaxLink<Void> {
	private static final long serialVersionUID = 1L;
	
	private String videoId;	
	private XmlSectionModel mSection;

	public VideoLink(String id, String _videoId, XmlSectionModel _mSection) {
		super(id);
		this.videoId = _videoId;
		this.mSection = _mSection;
	}

	@Override
	public void onClick(AjaxRequestTarget target) {		
		VideoDialog videoDialog = new VideoDialog(videoId, mSection);		
		ISIStandardPage page = (ISIStandardPage) getPage();
		page.displayDialog(videoDialog, target);
	}
}