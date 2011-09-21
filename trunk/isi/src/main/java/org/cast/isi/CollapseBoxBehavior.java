/*
 * Copyright 2011 CAST, Inc.
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
package org.cast.isi;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.cast.cwm.service.EventService;

/**
 * A behavior that can be attached to any wicket component that is inside a css styled 'collapseBox'.  
 * When the ajax event is triggered, this behavior will create a database Event representing 
 * the open/close action.
 * 
 * Note:  Requires the javascript function collapseBoxStatus() in functions.js and jQuery.
 * 
 * @author jbrookover
 *
 */
public class CollapseBoxBehavior extends AjaxEventBehavior {

	private static final long serialVersionUID = 1L;
	private String type;
	private String pageName;

	/**
	 * A constructor.  The event is the Ajax Event (e.g. 'onclick') and the type
	 * is the name of the component that will be stored in the database (e.g. 'pagenotes').
	 * 
	 * The stored data will be "detail:toggleCollapseBox" and "open" or "close." 
	 * 
	 * @param event ajax behavior
	 * @param type component logging name
	 * @param pageName the name of the content page
	 */
	public CollapseBoxBehavior(String event, String type, String pageName) {
		super(event);
		this.type = type;
		this.pageName = pageName;
	}
	
	@Override
	protected CharSequence generateCallbackScript(CharSequence partialCall) {
		getComponent().setOutputMarkupId(true);
		String callback = "var url = '" + getCallbackUrl(false) + "'; "; // the base url for the callback
		callback += " var parameters = 'action=' + collapseBoxStatus('" + getComponent().getMarkupId() + "'); "; // the parameters
		callback += "wicketAjaxGet(url + '&' + parameters);";
		return callback;
	}
	

	@Override
	protected void onEvent(AjaxRequestTarget target) {
		String action = RequestCycle.get().getRequest().getParameter("action");
		EventService.get().saveEvent("toggle:" + type, action, pageName);
	}
	
}