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

import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.injection.web.InjectorHolder;
import org.cast.cwm.service.IEventService;

import com.google.inject.Inject;

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
	private String detail;

	@Inject
	private IEventService eventService;
	
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
		this(event);
		this.type = type;
		this.pageName = pageName;
	}

	public CollapseBoxBehavior(String event, String type, String pageName, String detail) {
		this(event);
		this.type = type;
		this.pageName = pageName;
		this.detail = detail;
	}	

	/** Public constructors call this to do essential setup. */
	private CollapseBoxBehavior(final String event) {
		super(event);
		InjectorHolder.getInjector().inject(this);
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
		action = "state=" + action;
		if (!(detail == null || detail.isEmpty())) {
			action += "," + detail;
		}
		eventService.saveEvent("toggle:" + type, action, pageName);
	}
	
}