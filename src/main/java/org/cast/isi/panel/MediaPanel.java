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
package org.cast.isi.panel;

import lombok.Getter;

import org.apache.wicket.model.StringResourceModel;
import org.cast.cwm.mediaplayer.MediaPlayerPanel;
/**
 * Modal popup window for displaying videos
 * 
 * @author lynnmccormack
 *
 */
public class MediaPanel extends SidebarDialog {

	private static final long serialVersionUID = 1L;
	protected String videoId;
	protected String videoCloseString;

	public MediaPanel(String id, StringResourceModel title, String videoId) {
		super(id, title, "media_"+ videoId);
		this.videoId = videoId;
		contentContainer.setMarkupId("mediaDetail_" + videoId);
	}

	@Override
	public String getCloseString() {

		String closeString = super.getCloseString();
		StringBuffer videoCloseStringBuffer = new StringBuffer("");
		MediaVisitor visitor = new MediaVisitor(videoCloseStringBuffer);
		visitChildren(MediaPlayerPanel.class, visitor);
		videoCloseString = visitor.stopMediaString.toString();
		if (videoCloseString != null && !videoCloseString.isEmpty()) {
			closeString += videoCloseString;
		} else { // using a youtube video
			// TODO: close a youtube video or put youtube videos inside jwplayer
			// closeString += "player.stopVideo()";
		}
		return closeString;
	}

	
	/**
	 * Visits video children and create a stop command for each
	 */
	public class MediaVisitor implements IVisitor<MediaPlayerPanel> {
		public StringBuffer stopMediaString;
		@Getter private int count = 0;
		
		public MediaVisitor(StringBuffer stopMediaDetails) {
			this.stopMediaString = stopMediaDetails;
		}

		public Object component(MediaPlayerPanel component) {			
			stopMediaString.append(component.getStopString());
			return CONTINUE_TRAVERSAL;
		} 		
	}
}