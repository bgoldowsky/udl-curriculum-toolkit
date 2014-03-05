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
package org.cast.isi.page;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.cast.cwm.CwmSession;
import org.cast.cwm.data.behavior.JsEventLoggingBehavior;
import org.cast.cwm.service.EventService;
import org.cast.cwm.service.IEventService;

import com.google.inject.Inject;

/**
 * Superclass for logged-in, stateful ISI pages.
 * Subclasses of this page will automatically log an event via the
 * {@link EventService#saveEvent(String, String, String)} method; 
 * the methods that determine the values of the event are abstract and 
 * must be implemented by the actual pages.
 * 
 * @author jacobbrookover
 *
 */
public abstract class ISIBasePage extends ISIPage {
	
	private static final long serialVersionUID = 1L;

	@Inject
	private IEventService eventService;

	public ISIBasePage(final PageParameters param) {
		super(param);
		
		JsEventLoggingBehavior jsEventLoggingBehavior = new JsEventLoggingBehavior() {
			private static final long serialVersionUID = 1L;

			@Override
			protected String getEventPage() {
				return getPageName();
			}
		};
		add (jsEventLoggingBehavior);
	}
	
	@Override
	protected void onBeforeRender() {
		super.onBeforeRender();
		if (CwmSession.get().getUser() != null) {
			eventService.saveEvent("pageview:" + getPageType(), getPageViewDetail(), getPageName());
		}		
	}
	
	/**
	 * Return a type value that will be used in the event log for views of this page.
	 * The returned value will have "pageview:" prepended.
	 * @return the type name
	 */
	public abstract String getPageType();

	/**
	 * Return the name of the ContentPage that is associated with the current page view.
	 * This information is saved in the worklog.
	 * @return the ContentPage name, or null if none.
	 */
	public abstract String getPageName();
	
	/**
	 * Return string that will be logged in the Event detail field of the database
	 * when this page is viewed.
	 * @return event details, or null
	 */
	public abstract String getPageViewDetail();

}
