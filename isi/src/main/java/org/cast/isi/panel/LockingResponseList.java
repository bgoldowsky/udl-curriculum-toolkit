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

import org.apache.wicket.model.IModel;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.ResponseMetadata;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.isi.ISIXmlSection;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.service.ISectionService;

import com.google.inject.Inject;

public class LockingResponseList extends ResponseList implements ISectionCompleteToggleListener {

	private static final long serialVersionUID = 1L;

	@Inject
	private ISectionService sectionService;

	public LockingResponseList(String wicketId, IModel<Prompt> mPrompt,
			ResponseMetadata metadata, ContentLoc loc, IModel<User> mUser) {
		super(wicketId, mPrompt, metadata, loc, mUser);
	}
	
	public LockingResponseList(String wicketId, IModel<Prompt> mPrompt,
			ResponseMetadata metadata, ContentLoc loc) {
		super(wicketId, mPrompt, metadata, loc);
	}

	@Override
	public void onBeforeRender() {
		setAllowEdit(!isTeacher() && !isCompleteAndLocked());
		super.onBeforeRender();
	}

	public String getLocation() {
		ISIXmlSection sectionAncestor = loc.getSection().getSectionAncestor();
		return sectionAncestor.getContentLoc().getLocation();
	}

	private User getUser() {
		if (mTargetUser == null)
			return null;
		return mTargetUser.getObject();
	}

	private boolean isTeacher() {
		User user = getUser();
		return (user != null) && user.getRole().subsumes(Role.TEACHER);
	}

	private boolean isCompleteAndLocked() {
		ISIXmlSection section = loc.getSection();
		String location = getLocation();
		return section.isLockResponse() && isComplete(location, getUser());
	}
	
	private boolean isComplete(String location, User user) {
		return nullSafeBoolean(sectionService.sectionIsCompleted(user, location));
	}

	private boolean nullSafeBoolean(Boolean b) {
		return (b != null) && b;
	}
	

}
