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
import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.string.UrlUtils;
import org.cast.cwm.components.service.JavascriptService;
import org.cast.isi.ISIApplication;

/**
 * Superclass of all ISI Pages.
 * Loads standard CSS and JS; displays title.
 * 
 * @author bgoldowsky
 *
 */

public class ISIPage extends WebPage implements IHeaderContributor {

	@Getter protected String pageTitle = ISIApplication.get().getPageTitleBase() + " :: Default Page Title";

	public ISIPage(final PageParameters params) {
		super(params);
	}

	public void renderHead(final IHeaderResponse response) {
		JavascriptService.get().includeJQuery(response);
		renderThemeJS(response, "js/lang/en.js");
		renderThemeJS(response, "js/jquery/jquery.form.js");
		renderThemeJS(response, "js/functions.js");
		renderThemeJS(response, "js/jquery/jquery-ui-1.8.16.custom.min.js");
	
		renderThemeCSS(response, "css/toolbar.css");
		renderThemeCSS(response, "css/buttons.css");
		renderThemeCSS(response, "css/boxes.css");
		renderThemeCSS(response, "css/modal.css");

		// custom application-wide css/js can be loaded by overriding this method
		ISIApplication.get().getCustomRenderHead(response);

		// Per page additions usually go here.
		renderAdditionalHeaderResources (response);
		
		// Theme CSS is loaded last, since it has overrides for any of the above CSS.
		renderThemeCSS(response, "css/theme.css");

		// if MathML is configured on, then link out to the MathJax site
		if (ISIApplication.get().isMathMLOn()) {
			response.renderString("<script type=\"text/javascript\" " +
					"src=\"https://d3eoax9i5htok0.cloudfront.net/mathjax/latest/MathJax.js?config=TeX-AMS-MML_HTMLorMML\">" +
					"</script>\n");
		}
		
	
	}

	/**
	 * Pages may override this to render any CSS or JS needed that should go after the
	 * standard CSS and JS files, but before theme.css .
	 * @param response
	 */
	public void renderAdditionalHeaderResources (final IHeaderResponse response) {
		// no op by default.
	}
	
	public void setPageTitle(String pageTitle) {
		this.pageTitle = ISIApplication.get().getPageTitleBase() + " :: " + pageTitle;
	}

	public boolean hasMiniGlossary() {
		return false;
	}

	public static void renderThemeCSS(IHeaderResponse response, String fileName) {
		response.renderCSSReference(UrlUtils.rewriteToContextRelative(fileName, RequestCycle.get().getRequest()));
	}

	public static void renderThemeCSS(IHeaderResponse response, String fileName, String media) {
		response.renderCSSReference(UrlUtils.rewriteToContextRelative(fileName, RequestCycle.get().getRequest()), media);
	}

	public static void renderThemeJS(IHeaderResponse response, String fileName) {
		response.renderJavascriptReference(UrlUtils.rewriteToContextRelative(fileName, RequestCycle.get().getRequest()));
	}


}
