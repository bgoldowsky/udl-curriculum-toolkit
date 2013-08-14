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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.ResponseMetadata;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.isi.ISIXmlSection;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.service.ISectionService;

import com.google.inject.Inject;

public class LockingResponseButtons extends ResponseButtons implements ISectionStatusChangeListener {

	private static final long serialVersionUID = 1L;

	@Inject
	private ISectionService sectionService;

	private IModel<User> mUser;

	public LockingResponseButtons(String id, IModel<Prompt> mPrompt,
			ResponseMetadata metadata, ContentLoc loc, IModel<User> mUser) {
		super(id, mPrompt, metadata, loc);
		this.mUser = mUser;
		setOutputMarkupPlaceholderTag(true);
	}
	
	@Override
	public void onConfigure() {
		super.onConfigure();
		setVisible(!(isTeacher() || isCompleteAndLocked()));
	}

	public void onSectionCompleteChange(AjaxRequestTarget target, String location) {
		if (location.equals(getLocation()))
			target.add(this);
	}
	
	public String getLocation() {
		ISIXmlSection sectionAncestor = loc.getSection().getSectionAncestor();
		return sectionAncestor.getContentLoc().getLocation();
	}

	private User getUser() {
		if (mUser == null)
			return null;
		return mUser.getObject();
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
	
	@Override
	protected void onDetach() {
		if (mUser != null)
			mUser.detach();
		super.onDetach();
	}
}
