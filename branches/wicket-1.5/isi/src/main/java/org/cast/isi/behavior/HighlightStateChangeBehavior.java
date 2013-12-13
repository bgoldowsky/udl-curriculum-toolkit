/*
 * Copyright 2011-2013 CAST, Inc.
 *
 * This file is part of the CAST Wicket Modules:
 * see <http://code.google.com/p/cast-wicket-modules>.
 *
 * The CAST Wicket Modules are free software: you can redistribute and/or
 * modify them under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * The CAST Wicket Modules are distributed in the hope that they will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cast.isi.behavior;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.cast.cwm.data.User;
import org.cast.cwm.service.IUserPreferenceService;
import org.cast.isi.ISISession;

import com.google.inject.Inject;

/**
 * A behavior that captures the changed state of the highlighter 
 */
public class HighlightStateChangeBehavior extends AbstractDefaultAjaxBehavior {
	private static final long serialVersionUID = 1L;

	@Inject
	protected IUserPreferenceService preferenceService;
	

	public HighlightStateChangeBehavior() {
		Injector.get().inject(this);
	}

	@Override
	protected void respond(AjaxRequestTarget target) {
		String highlightColor = RequestCycle.get().getRequest().getRequestParameters().getParameterValue("highlightColor").toString();
		String highlightOn = RequestCycle.get().getRequest().getRequestParameters().getParameterValue("highlightOn").toString();
		saveHighlightState(highlightOn, highlightColor);		
	}
	
	protected void saveHighlightState(String stringHighlightOn, String highlightColor) {
		IModel<User> mUser = ISISession.get().getUserModel();		

		Boolean highlightOn = false;
		if (stringHighlightOn.contentEquals("true")) {
			highlightOn = true;
		}
		preferenceService.setUserPreferenceBoolean(mUser, "highlightOn", highlightOn);
		preferenceService.setUserPreferenceString(mUser, "highlightColor", highlightColor);
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		// setup a global js variable with the callback URL
		response.renderJavaScript("var highlightStateChangeCallbackUrl = '" + this.getCallbackUrl() + "';", "highlightStateChangeCallbackUrl");
		super.renderHead(component, response);
	}		
}