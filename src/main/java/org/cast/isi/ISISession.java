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

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import org.apache.wicket.Request;
import org.apache.wicket.Session;
import org.apache.wicket.model.IModel;
import org.cast.cwm.CwmSession;
import org.cast.cwm.data.User;
import org.cast.isi.data.ContentLoc;

/** Customized Session holds application-specific data */

public class ISISession extends CwmSession {
	
	@Getter @Setter
	protected Map<Character, String> highlighterLabels = null;
	
	@Getter @Setter
	protected ContentLoc bookmark = null;
	
	@Getter @Setter

	// Teachers can have a student whose data they are looking at; this persists across requests
	private IModel<User> studentModel;
	
	private static final long serialVersionUID = 1L;

	public ISISession(Request request) {
		super(request);
	}

	public static ISISession get() {
		return (ISISession) Session.get();
	}
	
	/** Get the current student that a teacher is looking at; null if none */
	public User getStudent() {
		if (studentModel == null)
			return null;
		return studentModel.getObject();
	}
	
	/** Get the Model of the User whose data should be shown.
	 *  This is getStudentModel() (the student being examined by a teacher) if there is one, 
	 *  otherwise it is just the currently logged in user. 
	 * @return
	 */
	public IModel<User> getTargetUserModel() {
		if (studentModel == null || studentModel.getObject() == null)
			return this.getUserModel();
		else
			return this.getStudentModel();
	}
	
	@Override
	public void signOut() {
		super.signOut();
		bookmark = null;
		studentModel = null;
		highlighterLabels = null;
	}
	
	@Override
	protected void detach() {
		if (studentModel != null)
			studentModel.detach();
		super.detach();
	}

}
