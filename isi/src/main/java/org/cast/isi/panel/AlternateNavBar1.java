/*
 * Copyright 2011-2015 CAST, Inc.
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
package org.cast.isi.panel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.cast.cwm.xml.XmlSection;
import org.cast.cwm.xml.XmlSectionModel;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISIXmlSection;
import org.cast.isi.component.QuickFlipForm;
/**
 *
 * A Navigation bar that shows the Level 1 and Level 2 Headers.
 * Each level 3 header is shown with the page numbers representing level 4 sections.
 * No Section Icons are used in this nav bar
 * 
 */
public class AlternateNavBar1 extends AbstractNavBar<XmlSection> implements ISectionStatusChangeListener {

	private static final long serialVersionUID = 1L;

	/**
	 * Construct nav bar based on a certain page that is currently being displayed.
	 * @param id
	 * @param mCurrentPage - model of the XmlSection that is the current page.
	 * @param teacher if true, a version of the nav bar appropriate for the teacher is produced. 
	 */
	public AlternateNavBar1(String id, IModel<XmlSection> mCurrentPage, boolean teacher) {
		super(id, mCurrentPage);
		setOutputMarkupId(true);

		if (mCurrentPage == null) {
			setModel(new XmlSectionModel(ISIApplication.get().getPageNum(1)));
		}

		ISIXmlSection rootSection = ISIXmlSection.getRootSection(getCurrentSection());
	
		// Current Section's Page Repeater with prev/next
		PageNavPanel pageNavPanelTop = new PageNavPanel("pageNavPanelTop", getModel());
		add(pageNavPanelTop);
				
		// Jump to a certain page
		add(new QuickFlipForm("quickFlipForm", true));
		
		// Chapter Title (xml level 1)
		add(new Label("title", rootSection.getTitle()));		
	}

	@Override
	public void onBeforeRender() {
		ISIXmlSection currentSection = getCurrentSection();
		addOrReplace(new Label("superSectionTitle", currentSection.getParent().getTitle()));
		super.onBeforeRender();
		
	}

	public void onSectionCompleteChange(AjaxRequestTarget target, String location) {
		// refresh on section complete change for any section
		target.add(this);
	}
	
	private ISIXmlSection getCurrentSection() {
		ISIXmlSection currentPage =  (ISIXmlSection) getModelObject();
		 // May be the current page or an ancestor
		return currentPage.getSectionAncestor();
	}

}