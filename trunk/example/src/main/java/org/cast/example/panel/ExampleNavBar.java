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
import org.cast.isi.panel.AbstractNavBar;
import org.cast.isi.panel.PageNavPanel;
import org.cast.isi.panel.QuickFlipForm;
import org.cast.isi.service.SectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleNavBar extends AbstractNavBar<XmlSection> {
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ExampleNavBar.class);
	private IModel<User> mTargetUser;

	public ExampleNavBar(String id, IModel<XmlSection> mCurrentSection, boolean teacher) {
		super(id, mCurrentSection);
		setOutputMarkupId(true);
		
		mTargetUser = ISISession.get().getTargetUserModel();

		if (mCurrentSection == null) {
			mCurrentSection = new XmlSectionModel(ISIApplication.get().getPageNum(1));
		}
		ISIXmlSection currentPage =  (ISIXmlSection) mCurrentSection.getObject();
		ISIXmlSection currentSection = currentPage.getSectionAncestor(); // May be itself
		
		ISIXmlSection rootSection = ISIXmlSection.getRootSection(currentSection);
		
		/**************************
		 * Super Section Repeater *
		 **************************/
		RepeatingView superSectionRepeater = new RepeatingView("superSectionRepeater");
		
		for (XmlSection ss: rootSection.getChildren()) {
			ISIXmlSection superSection = (ISIXmlSection) ss;
			WebMarkupContainer superSectionContainer = new WebMarkupContainer(superSectionRepeater.newChildId());
			superSectionRepeater.add(superSectionContainer);
			superSectionContainer.add(new Label("superSectionTitle", superSection.getTitle()));
			
			/********************************
			 * Regular Section sub-Repeater *
			 ********************************/
			RepeatingView sectionRepeater = new RepeatingView("sectionRepeater");
			List<XmlSection> sections;
			
			// Use Supersection children, or supersection itself if there are no children.
			if (!superSection.hasChildren()) {
				sections = new ArrayList<XmlSection>();
				sections.add(superSection);
			} else {
				sections = superSection.getChildren();
			}
			
			for(XmlSection s : sections) {
				ISIXmlSection section = (ISIXmlSection) s;
				WebMarkupContainer sectionContainer = new WebMarkupContainer(sectionRepeater.newChildId());
				sectionRepeater.add(sectionContainer);
				boolean current = section.equals(currentSection);

				BookmarkablePageLink<ISIStandardPage> link = ISIStandardPage.linkTo("sectionLink", section);
				if (current) {
					link.setEnabled(false);
					link.add(new ClassAttributeModifier("current"));
				}

				// if this is a teacher then display the section status
				if (teacher) {
					SectionStatus stat = SectionService.get().getSectionStatus(mTargetUser.getObject(), section);
					link.add(ISIApplication.statusIconFor(stat));					
				} else {
					link.add(ISIApplication.get().iconFor(section, ""));
				}
					
				sectionContainer.add(link);
				sectionContainer.add(new WebMarkupContainer("current").setVisible(current));

//				There is no longer an underline for hasResponse status icons are used for each section				
//				// Teacher "underline" indicator for Response Areas
//				WebMarkupContainer hasResponse = new WebMarkupContainer("hasResponse");
//				hasResponse.setVisible(section.hasResponseGroup() && teacher);
//				sectionContainer.add(hasResponse);
			}
			
			superSectionContainer.add(sectionRepeater);
		}
		add(superSectionRepeater);

		// Current Section's Page Repeater with prev/next
		PageNavPanel pageNavPanelTop = new PageNavPanel("pageNavPanelTop", mCurrentSection);
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
}