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

import com.google.inject.Inject;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.cycle.RequestCycle;
import org.cast.cwm.service.IUserPreferenceService;
import org.cast.isi.CollapseBoxBehavior;
import org.cast.isi.ISISession;

/**
 * Event logging and state saving behavior for a collapse box.  See @CollapseBoxBehavior@ 
 * for more details.
 * 
 * @author lynnmccormack
 *
 */
public final class StateSavingCollapseBoxBehavior extends CollapseBoxBehavior {
	
	private static final long serialVersionUID = 1L;

	private final String userPreferenceName;
	
	@Inject
	IUserPreferenceService userPreferenceService;

	/**
	 * State Saving Behavior added to the onclick event
	 * 
	 * @param type - event type
	 * @param pageName - usually supplied by the page - can be null
	 * @param userPreferenceName - unique name for a user preference
	 */
	public StateSavingCollapseBoxBehavior(String type, String pageName, String userPreferenceName) {
		super("onclick", type, pageName);
		this.userPreferenceName = userPreferenceName;
	}

	@Override
	protected void onEvent(AjaxRequestTarget target) {
		String action = RequestCycle.get().getRequest().getRequestParameters().getParameterValue("action").toString();
		boolean toggleState;
		if (action.equals("open")) {
			toggleState = true;
		} else {
			toggleState = false;
		}
		userPreferenceService.setUserPreferenceBoolean(ISISession.get().getUserModel(), userPreferenceName, toggleState);
		super.onEvent(target);
	}
}