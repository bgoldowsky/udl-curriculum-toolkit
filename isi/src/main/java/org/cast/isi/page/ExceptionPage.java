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
package org.cast.isi.page;


import org.apache.wicket.PageParameters;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.cast.isi.ISIApplication;

public class ExceptionPage extends ISIBasePage {

	public ExceptionPage(final PageParameters param, RuntimeException e) {
		super(param);
		
		setPageTitle("Internal Error");
		
		add(new BookmarkablePageLink<ISIStandardPage>("home", ISIApplication.get().getHomePage()));
		add(new ISIApplication.LogoutLink("logout2"));
		String message = e.getMessage() + "<br /><br />\n";
		StackTraceElement[] trace;
		
		// Find root cause of the exception.
		Throwable cause = e;
		while (cause.getCause() != null)
			cause = cause.getCause();
		message += "<b>" + cause.toString() + "</b>\n<ul>\n";
		trace = cause.getStackTrace();
		for (int i = 0; i < trace.length; i++)
			message += "<li>" + trace[i].toString() + "</li>\n";
		message += "</ul><br />";
		add(new Label("details", message).setEscapeModelStrings(false));
		
		// Redirect to login screen on certain exceptions
		if (cause instanceof PageExpiredException)
			throw new RestartResponseException(Login.class);
	}

	@Override
	public String getPageName() {
		return null;
	}

	@Override
	public String getPageType() {
		return "Error";
	}

	@Override
	public String getPageViewDetail() {
		return null;
	}

}
