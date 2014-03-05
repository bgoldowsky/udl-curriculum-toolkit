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

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.cast.isi.ISIApplication;

/**
 * A single text field that redirects to the entered page number.
 * 
 * @author jbrookover
 *
 */
public class QuickFlipForm extends Form<Object> {

	private static final long serialVersionUID = 1L;
	
	public QuickFlipForm(String id, boolean addLabel) {
		this(id, addLabel, null);
	}
	
	public QuickFlipForm(String id, boolean addLabel, Integer currentPage) {
		super(id);
		Model<String> numberModel;
		if (currentPage != null)
			numberModel = new Model<String>(currentPage.toString());
		else
			numberModel = new Model<String>("");
		TextField<String> numberField = new TextField<String>("numberField", numberModel);
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
		if (ISIApplication.get().getPageNum(pageNum) == null)
			pageNum = -1;
		
		if (pageNum != -1) {
			PageParameters param = new PageParameters();
			param.add("pageNum", String.valueOf(pageNum));
			setResponsePage(ISIApplication.get().getReadingPageClass(), param);
		}
	}	
}