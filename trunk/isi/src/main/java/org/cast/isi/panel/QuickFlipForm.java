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
package org.cast.isi.panel;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.cast.isi.ISIApplication;

/**
 * A single text field that redirects to the entered page number.
 * 
 * @author jbrookover
 *
 */
public class QuickFlipForm extends Form<Object> {

	private static final long serialVersionUID = 1L;

	public QuickFlipForm(String id, Boolean addLabel) {
		super(id);
		TextField<String> numberField = new TextField<String>("numberField", new Model<String>(""));
		add(numberField);
		if (addLabel) {
			FormComponentLabel numberFieldLabel =  new FormComponentLabel("numberFieldLabel", numberField);
			add(numberFieldLabel);
		}
		add(new SubmitLink("goLink"));
	}

	@Override
	public void onSubmit() {
		super.onSubmit();
		String page = get("numberField").getDefaultModelObjectAsString();
		int pageNum = -1;
		try {
			pageNum = Integer.parseInt(page);
		} catch (Exception ex) { /* Do Nothing */}
		
		// check if the page number is valid
		if (ISIApplication.get().getPageNum(pageNum-1) == null)
			pageNum = -1;
		
		if (pageNum != -1) {
			PageParameters param = new PageParameters();
			param.add("pageNum", String.valueOf(pageNum));
			setResponsePage(ISIApplication.get().getReadingPageClass(), param);
		}
	}	
}