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

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.cwm.xml.XmlSection;
import org.cast.isi.ISISession;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.service.ISectionService;

import com.google.inject.Inject;

/**
 * A container for a message tied to a DelayedFeedbackSingleSelectItem.
 * It is visible when the associated response has been reviewed or when the user is a teacher.
 * 
 * @author droby
 *
 */
public class SingleSelectDelayMessage extends WebMarkupContainer {

	private static final long serialVersionUID = 1L;

	@Inject
	private ISectionService sectionService;

	protected IModel<User> mUser;
	
	private String location;

	/**
	 * Construct with the model of the current section and the specified user;
	 * @param id
	 * @param currentSectionModel
	 * @param mUser 
	 */
	public SingleSelectDelayMessage(String id, IModel<XmlSection> currentSectionModel, IModel<User> mUser) {
		super(id);
		location = new ContentLoc(currentSectionModel.getObject()).getLocation();
		this.mUser = mUser;
	}
	
	/**
	 * Construct with the model of the current section and the currently logged-in user;
	 * @param id
	 * @param currentSectionModel
	 */
	public SingleSelectDelayMessage(String id, IModel<XmlSection> currentSectionModel) {
		this(id, currentSectionModel, ISISession.get().getUserModel());
	}

	@Override
	public boolean isVisible() {
		return isTeacher() || isReviewed();
	}

	@Override
	protected void onDetach() {
		if (mUser != null) {
			mUser.detach();
		}
		super.onDetach();
	}
	
	private boolean isTeacher() {
		return getUser().getRole().subsumes(Role.TEACHER);
	}

	private boolean isReviewed() {
		return nullSafeBoolean(sectionService.sectionIsReviewed(getUser(), location));			
	}

	private User getUser() {
		if (mUser == null)
			return null;
		return mUser.getObject();
	}	

	private boolean nullSafeBoolean(Boolean b) {
		return (b != null) && b;
	}
}
