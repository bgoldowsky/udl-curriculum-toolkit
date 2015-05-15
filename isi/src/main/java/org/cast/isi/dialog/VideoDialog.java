/*
 * Copyright 2011-2015 CAST, Inc.
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

import org.apache.wicket.model.StringResourceModel;
import org.cast.cwm.xml.XmlSectionModel;
import org.cast.cwm.xml.transform.FilterElements;
import org.cast.isi.ISIXmlComponent;
import org.cast.isi.page.ISIBasePage;

/**
 * Dialog to play a single video.
 */
public class VideoDialog extends AbstractISIAjaxDialog<Void> {
	
	private static final long serialVersionUID = 1L;

	private String videoId;	
	private XmlSectionModel mSection;

	public VideoDialog(String _videoId, XmlSectionModel _mSection) {
		super(null);
		this.videoId = _videoId;
		this.mSection = _mSection;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setTitle((new StringResourceModel("mediaModal.title", this, null, "Video").getString()));		
		dialogBorder.setEmptyOnClose(true);
		if (getPage() instanceof ISIBasePage) {
			dialogBorder.setPageName(((ISIBasePage) getPage()).getPageName());
			dialogBorder.setEventDetail("video popup");
		} else {
			// Not on a base page
			dialogBorder.setLogEvents(false);
		}		
		
		// add the video component with a new transformation - set the xpath param
		// add-video-thumb-link to false - to only show the video portion and not the link
		ISIXmlComponent videoComponent = new ISIXmlComponent("video", mSection, "student");
		String objectValue = ".//dtb:object[@id='" + videoId + "']";
		videoComponent.setTransformParameter("add-video-thumb-link", false);
		videoComponent.setTransformParameter(FilterElements.XPATH, objectValue);		
		dialogBorder.getBodyContainer().add(videoComponent);
	}	
}