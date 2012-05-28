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
package org.cast.example.panel;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.cast.cwm.components.ClassAttributeModifier;
import org.cast.cwm.data.User;
import org.cast.cwm.xml.XmlSection;
import org.cast.cwm.xml.XmlSectionModel;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISISession;
import org.cast.isi.ISIXmlSection;
import org.cast.isi.data.SectionStatus;
import org.cast.isi.page.ISIStandardPage;
import org.cast.isi.page.SectionLinkFactory;
import org.cast.isi.panel.AbstractNavBar;
import org.cast.isi.panel.PageNavPanel;
import org.cast.isi.panel.QuickFlipForm;
import org.cast.isi.service.SectionService;

/**
 * A Navigation bar that shows the sequence of sections within a chapter.
 * Each section is represented by an icon which may be determined by the its class attribute.
 * Structure above the section level may also be shown.  Allows navigation to any section
 * by clicking on its icon.
 */
public class ExampleNavBar extends AbstractNavBar<XmlSection> {
	private static final long serialVersionUID = 1L;

	/**
	 * Construct nav bar based on a certain page that is currently being displayed.
	 * @param id
	 * @param mCurrentPage - model of the XmlSection that is the current page.
	 * @param teacher if true, a version of the nav bar appropriate for the teacher is produced. 
	 */
	public ExampleNavBar(String id, IModel<XmlSection> mCurrentPage, boolean teacher) {
		super(id, mCurrentPage);
		setOutputMarkupId(true);

		if (mCurrentPage == null) {
			mCurrentPage = new XmlSectionModel(ISIApplication.get().getPageNum(1));
		}
		ISIXmlSection currentPage =  (ISIXmlSection) mCurrentPage.getObject();
		ISIXmlSection currentSection = currentPage.getSectionAncestor(); // May be the current page or an ancestor
		
		ISIXmlSection rootSection = ISIXmlSection.getRootSection(currentSection);

		/**************************
		 * Super Section Repeater *
		 **************************/
		RepeatingView superSectionRepeater = new RepeatingView("superSectionRepeater");
		add(superSectionRepeater);
		
		// Determine if there is a super-section level, or if we just have a list of regular sections 
		if (!((ISIXmlSection)rootSection.getChild(0)).isSuperSection()) {
			
			// Simple case - no supersection.  Create a single container and put sections into it.
			WebMarkupContainer container = new WebMarkupContainer(superSectionRepeater.newChildId());
			superSectionRepeater.add(container);
			container.add(new EmptyPanel("superSectionTitle"));
			container.add(new SectionRepeater("sectionRepeater", rootSection.getChildren(), currentSection, teacher));
		
		} else {
			// We do have supersections
			for (XmlSection ss: rootSection.getChildren()) {
				ISIXmlSection superSection = (ISIXmlSection) ss;
				WebMarkupContainer superSectionContainer = new WebMarkupContainer(superSectionRepeater.newChildId());
				superSectionRepeater.add(superSectionContainer);

				superSectionContainer.add(new Label("superSectionTitle", superSection.getTitle()));

				List<XmlSection> sections;
				// Use Supersection children, or supersection itself if there are no children.
				if (!superSection.hasChildren()) {
					sections = new ArrayList<XmlSection>();
					sections.add(superSection);
				} else {
					sections = superSection.getChildren();
				}

				superSectionContainer.add (new SectionRepeater("sectionRepeater", sections, currentSection, teacher));
			}
		}
	
		// Current Section's Page Repeater with prev/next
		PageNavPanel pageNavPanelTop = new PageNavPanel("pageNavPanelTop", mCurrentPage);
		add(pageNavPanelTop);
		
		
		/*********
		 * Other *
		 *********/
		// Jump to a certain page
		add(new QuickFlipForm("quickFlipForm", true));
		
		// Chapter Title (xml level 1)
		add(new Label("title", rootSection.getTitle()));		
	}
	
	public void renderHead(final IHeaderResponse response) {
		// Nav Bar Tool Tips
		// NOTE: Based on unstable, 2.0 version.  If replacing, take note of CSS style changes as well!
		// response.renderJavascriptReference(new ResourceReference("js/jquery/jquery.qtip-2.0-rev411.min.js"));
		response.renderJavascriptReference(new ResourceReference("js/jquery/jquery.qtip-1.0.min.js"));
		response.renderJavascript("$(window).ready(function() { navBarToolTips(); });", "Nav Bar Tool Tip Init");
	}
	
	/**
	 * Render an icon for each item in the given list of sections.
	 *
	 */
	protected class SectionRepeater extends RepeatingView {

		private static final long serialVersionUID = 1L;

		public SectionRepeater(String id, Iterable<XmlSection> sections, XmlSection currentSection, boolean teacher) {
			super(id);

			for(XmlSection s : sections) {
				ISIXmlSection section = (ISIXmlSection) s;
				WebMarkupContainer sectionContainer = new WebMarkupContainer(newChildId());
				add(sectionContainer);
				boolean current = section.equals(currentSection);

				BookmarkablePageLink<ISIStandardPage> link = new SectionLinkFactory().linkToPage("sectionLink", section);
				if (current) {
					link.setEnabled(false);
					link.add(new ClassAttributeModifier("current"));
				}

				// if this is a teacher then display the section status
				if (teacher) {
					IModel<User> mTargetUser = ISISession.get().getTargetUserModel();
					SectionStatus stat = SectionService.get().getSectionStatus(mTargetUser.getObject(), section);
					link.add(ISIApplication.statusIconFor(stat));					
				} else {
					link.add(ISIApplication.get().iconFor(section, ""));
				}
					
				sectionContainer.add(link);
				sectionContainer.add(new WebMarkupContainer("current").setVisible(current));
			}
			
		}
		
	}
	
	
}