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
package org.cast.isi.panel;

import com.google.inject.Inject;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.cast.cwm.components.ClassAttributeModifier;
import org.cast.cwm.components.Icon;
import org.cast.cwm.data.User;
import org.cast.cwm.xml.XmlSection;
import org.cast.cwm.xml.XmlSectionModel;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISISession;
import org.cast.isi.ISIXmlSection;
import org.cast.isi.component.IDisplayFeedbackStatus;
import org.cast.isi.component.QuickFlipForm;
import org.cast.isi.data.SectionStatus;
import org.cast.isi.page.ISIStandardPage;
import org.cast.isi.page.SectionLinkFactory;
import org.cast.isi.service.ISectionService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
/**
 * A Navigation bar that shows the sequence of sections within a chapter.
 * Each section is represented by an icon which may be determined by the its class attribute.
 * Structure above the section level may also be shown.  Allows navigation to any section
 * by clicking on its icon.
 */
public class DefaultNavBar extends AbstractNavBar<XmlSection> implements ISectionStatusChangeListener {

	private static final long serialVersionUID = 1L;
	private SectionIconFactory iconFactory;

	/**
	 * Construct nav bar based on a certain page that is currently being displayed.
	 * @param id
	 * @param mCurrentPage - model of the XmlSection that is the current page.
	 * @param teacher if true, a version of the nav bar appropriate for the teacher is produced. 
	 */
	public DefaultNavBar(String id, IModel<XmlSection> mCurrentPage, boolean teacher) {
		super(id, mCurrentPage);
		setOutputMarkupId(true);
		iconFactory = SectionIconFactory.getIconFactory(teacher);

		if (mCurrentPage == null) {
			setModel(new XmlSectionModel(ISIApplication.get().getPageNum(1)));
		}

		ISIXmlSection rootSection = ISIXmlSection.getRootSection(getCurrentSection());
	
		// Current Section's Page Repeater with prev/next
		PageNavPanel pageNavPanelTop = new PageNavPanel("pageNavPanelTop", getModel());
		add(pageNavPanelTop);
		
		
		/*********
		 * Other *
		 *********/
		// Jump to a certain page
		add(new QuickFlipForm("quickFlipForm", true));
		
		// Chapter Title (xml level 1)
		add(new Label("title", rootSection.getTitle()));		
	}

	@Override
	public void onBeforeRender() {
		ISIXmlSection currentSection = getCurrentSection();
		ISIXmlSection rootSection = ISIXmlSection.getRootSection(currentSection);

		/**************************
		 * Super Section Repeater *
		 **************************/
		RepeatingView superSectionRepeater = new RepeatingView("superSectionRepeater");
		addOrReplace(superSectionRepeater);

		// Determine if there is a super-section level, or if we just have a list of regular sections 
		if (rootSection.hasSuperSections()) {
			addSuperSections(currentSection, rootSection, superSectionRepeater, iconFactory);
		} 
		else {
			addSimpleSections(currentSection, rootSection, superSectionRepeater, iconFactory);
		}
		super.onBeforeRender();
		
	}

	public void onSectionCompleteChange(AjaxRequestTarget target, String location) {
		// refresh on section complete change for any section
		// TODO: We really should only have to refresh the icon and link for the changed section, not the whole navbar.
		target.add(this);
	}
	
	private ISIXmlSection getCurrentSection() {
		ISIXmlSection currentPage =  (ISIXmlSection) getModelObject();
		 // May be the current page or an ancestor
		return currentPage.getSectionAncestor();
	}
	
	private void addSimpleSections(ISIXmlSection currentSection,
			ISIXmlSection rootSection, RepeatingView superSectionRepeater,
			SectionIconFactory iconFactory) {
		// Simple case - no supersection.  Create a single container and put sections into it.
		WebMarkupContainer container = new WebMarkupContainer(superSectionRepeater.newChildId());
		superSectionRepeater.add(container);
		container.add(new EmptyPanel("superSectionTitle"));
		container.add(new SectionRepeater("sectionRepeater", rootSection.getChildren(), currentSection, iconFactory));
	}

	private void addSuperSections(ISIXmlSection currentSection,
			ISIXmlSection rootSection, RepeatingView superSectionRepeater,
			SectionIconFactory iconFactory) {
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

			superSectionContainer.add (new SectionRepeater("sectionRepeater", sections, currentSection, iconFactory));
		}
	}

	public void renderHead(final IHeaderResponse response) {
		// Nav Bar Tool Tips
		// NOTE: Based on unstable, 2.0 version.  If replacing, take note of CSS style changes as well!
		// response.renderJavascriptReference(new ResourceReference("js/jquery/jquery.qtip-2.0-rev411.min.js"));

        // TODO heikki commented this out because the file is not there
		// response.renderJavaScriptReference(new JavaScriptResourceReference(this.getClass(), "js/jquery/jquery.qtip.min.js"));
		// response.renderJavaScript("$(window).ready(function() { navBarToolTips(); });", "Nav Bar Tool Tip Init");
	}
	
	/**
	 * Render an icon for each item in the given list of sections.
	 *
	 */
	protected class SectionRepeater extends RepeatingView  {

		private static final long serialVersionUID = 1L;

		public SectionRepeater(String id, Iterable<XmlSection> sections, XmlSection currentSection, SectionIconFactory iconFactory) {
			super(id);

			for(XmlSection s : sections) {
				ISIXmlSection section = (ISIXmlSection) s;
				boolean current = section.equals(currentSection);

				WebMarkupContainer sectionContainer = new WebMarkupContainer(newChildId());
				add(sectionContainer);

				BookmarkablePageLink<ISIStandardPage> link = new SectionLinkFactory().linkToPage("sectionLink", section);
				sectionContainer.add(link);
				if (current) {
					link.setEnabled(false);
					link.add(new ClassAttributeModifier("current"));
					link.add(new IconContainer("iconContainer", section, iconFactory));
					sectionContainer.add(new WebMarkupContainer("current"));
				} else {
					WebMarkupContainer iconContainer = new WebMarkupContainer("iconContainer");
					link.add(iconContainer);
					iconContainer.add(iconFactory.getIconFor(section));
					sectionContainer.add(new WebMarkupContainer("current").setVisible(false));					
				}
			}
			
		}	
	}
	
	public class IconContainer extends WebMarkupContainer implements IDisplayFeedbackStatus {
		private static final long serialVersionUID = 1L;
		private ISIXmlSection section;
		private SectionIconFactory iconFactory;

		public IconContainer(String id, ISIXmlSection section, SectionIconFactory iconFactory) {
			super(id);
			this.section = section;
			this.iconFactory = iconFactory;
			setOutputMarkupId(true);
		}

		@Override
		protected void onBeforeRender() {
			Component teacherStatusIcon = iconFactory.getIconFor(section);
 			addOrReplace(teacherStatusIcon);
			super.onBeforeRender();
		}
	}
		
	
	
	public static abstract class SectionIconFactory implements Serializable {

		private static final long serialVersionUID = 1L;
		
		private static final String ICON_TYPE_CLASS = "class";
        private static final String ICON_TYPE_STATUS = "status";
        // TODO: this looks like it will be a problem with custom theme mapping - ldm
        private static final String ACTIVITY_ICON_URI_PREFIX = "theme/img/icons/activity_";

		@Inject
		protected ISectionService sectionService;

		public SectionIconFactory() {
            Injector.get().inject(this);
		}
		
		public static SectionIconFactory getIconFactory(boolean teacher) {
			IModel<User> targetUserModel = ISISession.get().getTargetUserModel();
			if (teacher) {
				String type = ISIApplication.get().getNavbarSectionIconsTeacher();
				if (ICON_TYPE_STATUS.equals(type)) {
					return new TeacherStatusSectionIconFactory(targetUserModel);
				}
				else if (ICON_TYPE_CLASS.equals(type)) {
					return new ClassSectionIconFactory();
				}
				else  {
					return new NullSectionIconFactory();
				}
			}
			else {
				String type = ISIApplication.get().getNavbarSectionIconsStudent();
				if (ICON_TYPE_STATUS.equals(type)) {
					return new StudentStatusSectionIconFactory(targetUserModel);
				}
				else if (ICON_TYPE_CLASS.equals(type)) {
					return new ClassSectionIconFactory();
				}
				else {
					return new NullSectionIconFactory();
				}
			}
		}

		public abstract Component getIconFor(ISIXmlSection section);

		public static class NullSectionIconFactory extends SectionIconFactory {

			private static final long serialVersionUID = 1L;

			@Override
			public Component getIconFor(ISIXmlSection section) {
                return new Icon("icon", ACTIVITY_ICON_URI_PREFIX + section.getClassName() + ".png").setVisible(false);
			}

		}

		public static class ClassSectionIconFactory extends SectionIconFactory {

			private static final long serialVersionUID = 1L;

			@Override
			public Component getIconFor(ISIXmlSection section) {
				return ISIApplication.get().iconFor(section);
			}

		}

		public static class StudentStatusSectionIconFactory extends SectionIconFactory {

			private static final long serialVersionUID = 1L;

			protected IModel<User> targetUserModel;

			public StudentStatusSectionIconFactory(IModel<User> targetUserModel) {
				this.targetUserModel = targetUserModel;
			}

			@Override
			public Component getIconFor(ISIXmlSection section) {
				SectionStatus stat = sectionService.getSectionStatus(targetUserModel.getObject(), section);
				return ISIApplication.get().studentStatusIconFor(section, stat);					
			}

		}

		public static class TeacherStatusSectionIconFactory extends SectionIconFactory {

			private static final long serialVersionUID = 1L;
			
			private IModel<User> targetUserModel;

			public TeacherStatusSectionIconFactory(IModel<User> targetUserModel) {
				this.targetUserModel = targetUserModel;
			}

			@Override
			public Component getIconFor(ISIXmlSection section) {
				SectionStatus stat = sectionService.getSectionStatus(targetUserModel.getObject(), section);
				return ISIApplication.get().teacherStatusIconFor(section, stat);					
			}

		}

	}

}