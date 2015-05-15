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

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.Strings;
import org.cast.cwm.components.ClassAttributeModifier;
import org.cast.cwm.service.IEventService;
import org.cast.cwm.xml.XmlDocument;
import org.cast.cwm.xml.XmlSection;
import org.cast.cwm.xml.XmlSectionModel;
import org.cast.cwm.xml.component.NavListView;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISIXmlSection;
import org.cast.isi.component.PageNumberLink;
import org.cast.isi.component.QuickFlipWithTotal;
import org.cast.isi.page.ISIBasePage;
import org.cast.isi.page.SectionLinkFactory;
import org.cast.isi.service.ISectionService;

import com.google.inject.Inject;

/**
 * A Navigation bar that displays the entire book hierarchy as a nested set of drop-down menus.
 */
public class DropDownNavBar extends AbstractNavBar<XmlSection> implements IHeaderContributor {
	private static final long serialVersionUID = 1L;

	@Inject
	private ISectionService sectionService;
	
	@Inject
	private IEventService eventService;
	
	private IModel<XmlSection> mCurrentPage;

	/**
	 * Construct nav bar based on a certain page that is currently being displayed.
	 * @param id
	 * @param mCurrentPage - model of the XmlSection that is the current page.
	 * @param teacher if true, a version of the nav bar appropriate for the teacher is produced. 
	 */
	public DropDownNavBar(String id, IModel<XmlSection> mCurrentPage, boolean teacher) {
		super(id, mCurrentPage);
		setOutputMarkupId(true);

		if (mCurrentPage == null) {
			mCurrentPage = new XmlSectionModel(ISIApplication.get().getPageNum(1));
		}
		this.mCurrentPage = mCurrentPage;
		
		ISIXmlSection currentPage =  (ISIXmlSection) mCurrentPage.getObject();
		ISIXmlSection currentSection = currentPage.getSectionAncestor(); // May be the current page or an ancestor
		ISIXmlSection rootSection = ISIXmlSection.getRootSection(currentSection);
		
		add(new Label("chapterTitle", getMenuTitle(rootSection)));		
		add(new Label("sectionTitle", getMenuTitle(currentSection)));

		add(new ChapterRepeater("chapter", rootSection, currentSection));
		
		// Page by page navigation
		int currentPageNum = ISIApplication.get().getStudentContent().getLabelIndex(ISIXmlSection.SectionType.PAGE, currentPage);
		add(new QuickFlipWithTotal("quickFlipForm", true, currentPageNum));
		add(new PageNumberLink("prevLink", currentPageNum-1));
		add(new PageNumberLink("nextLink", currentPageNum+1));
	}
	
	public void renderHead(final IHeaderResponse response) {
		ISIBasePage.renderThemeCSS(response, "css/dropdown.css");
		ISIBasePage.renderThemeJS(response, "js/dropdown.js");
		response.renderOnDomReadyJavaScript("$('.navMenu').CAST_Dropdown_Menu();");
		super.renderHead(response);
	}
	
	/**
	 * Render an icon for each item in the given list of sections.
	 *
	 */
	protected class ChapterRepeater extends RepeatingView {

		private static final long serialVersionUID = 1L;

		public ChapterRepeater(String id, XmlSection currentChapter, XmlSection currentSection) {
			super(id);

		   	for (XmlDocument doc : ISIApplication.get().getStudentContent()) { // For each XML document
		   		for (XmlSection rs : doc.getTocSection().getChildren()) { // For each chapter in the document
		   			ISIXmlSection chapter = (ISIXmlSection) rs;
		   			WebMarkupContainer chapterContainer = new WebMarkupContainer(this.newChildId());
		   			this.add(chapterContainer);

		   			chapterContainer.add(new Label("title", getMenuTitle(chapter)));
		   			
		   			if (currentChapter.equals(rs))
		   				chapterContainer.add(new ClassAttributeModifier("current"));

		   			NavListView navlist = new NavListView("section", new XmlSectionModel(chapter), new XmlSectionModel(currentSection)) {

		   				private static final long serialVersionUID = 1L;

		   				@Override
		   				protected void populateItem(ListItem<XmlSection> item) {
		   					super.populateItem(item);
		   					item.add(new PageLinkPanel("pageLinks", item.getModel(), mCurrentPage));
		   				}
		   				
		   				@Override
		   				protected WebMarkupContainer makeLink(XmlSection section) {
		   					return new SectionLinkFactory().linkToPage("link", section);
		   				}
		   				
		   				@Override
						protected Component makeLabel (XmlSection sec) {
		   					return new Label("title", getMenuTitle(sec));
		   				}

		// TODO: add section status
//					IModel<User> mTargetUser = ISISession.get().getTargetUserModel();
//					SectionStatus stat = sectionService.getSectionStatus(mTargetUser.getObject(), section);
//					if (teacher) {
//						link.add(ISIApplication.get().teacherStatusIconFor(section, stat));
//					} else {
//						link.add(ISIApplication.get().studentStatusIconFor(section, stat));
//					}

		   			};
		   			chapterContainer.add(navlist);

		   		}
		   	}
			
		}
		
	}

	/**
	 * Return the title of an XmlSection that should be displayed in the menu.
	 * Subtitle (which comes from XML &lt;covertitle&gt;) will be used if it exists,
	 * otherwise &lt;title&gt;.
	 * @param sec
	 * @return
	 */
	protected String getMenuTitle (XmlSection sec) {
		if (!Strings.isEmpty(sec.getSubTitle()))
			return sec.getSubTitle();
		return sec.getTitle();
	}

}