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

import lombok.Getter;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.cast.cwm.CwmSession;
import org.cast.cwm.components.service.JavascriptService;
import org.cast.cwm.service.EventService;
import org.cast.isi.ISIApplication;

/**
 * Subclasses of this page will automatically log an event via the
 * {@link EventService#saveEvent(String, String, String)} method. They
 * will also load the base js and css files.
 * 
 * @author jacobbrookover
 *
 */
public abstract class ISIBasePage extends WebPage implements IHeaderContributor {
	
//	public ISIBasePage() {
//		super();
//	}
	@Getter protected String pageTitle = ISIApplication.get().getPageTitleBase() + " :: Default Page Title";
	
	public ISIBasePage(final PageParameters param) {
		super(param);
	}
	
	@Override
	protected void onBeforeRender() {
		super.onBeforeRender();
		if (CwmSession.get().getUser() != null) {
			EventService.get().saveEvent("pageview:" + getPageType(), getPageViewDetail(), getPageName());
		}
	}
	
	public void renderHead(final IHeaderResponse response) {
		JavascriptService.get().includeJQuery(response);
		response.renderJavascriptReference(new ResourceReference("/js/lang/en.js"));
		response.renderJavascriptReference(new ResourceReference("/js/jquery/jquery.form.js"));
		response.renderJavascriptReference(new ResourceReference("/js/functions.js"));
		response.renderJavascriptReference(new ResourceReference("/js/jquery/jquery-ui-1.8.16.custom.min.js"));
	
		response.renderCSSReference(new ResourceReference("/css/toolbar.css"));
		response.renderCSSReference(new ResourceReference("/css/buttons.css"));
		response.renderCSSReference(new ResourceReference("/css/boxes.css"));
		response.renderCSSReference(new ResourceReference("/css/modal.css"));
		response.renderCSSReference(new ResourceReference("/css/theme.css"));

		// if MathML is configured on, then link out to the MathJax site
		if (ISIApplication.get().isMathMLOn()) {
			response.renderString("<script type=\"text/javascript\" " +
					"src=\"https://d3eoax9i5htok0.cloudfront.net/mathjax/latest/MathJax.js?config=TeX-AMS-MML_HTMLorMML\">" +
					"</script>\n");
		}
		
		// custom css/js can be loaded by overriding this method
		ISIApplication.get().getCustomRenderHead(response);
	
	}

	public void setPageTitle(String pageTitle) {
		this.pageTitle = ISIApplication.get().getPageTitleBase() + " :: " + pageTitle;
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
