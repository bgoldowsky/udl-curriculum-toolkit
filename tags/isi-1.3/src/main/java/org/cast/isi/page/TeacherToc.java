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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.databinder.models.hib.HibernateObjectModel;

import org.apache.wicket.PageParameters;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.cast.cwm.components.ClassAttributeModifier;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.cwm.data.builders.UserCriteriaBuilder;
import org.cast.cwm.data.models.UserListModel;
import org.cast.cwm.xml.XmlDocument;
import org.cast.cwm.xml.XmlSection;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISISession;
import org.cast.isi.ISIXmlSection;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.data.ISIPrompt;
import org.cast.isi.data.SectionStatus;
import org.cast.isi.data.StudentFlag;
import org.cast.isi.panel.StudentFlagPanel;
import org.cast.isi.service.IISIResponseService;
import org.cast.isi.service.ISectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Teacher Table of Contents
 * 
 * Listing of each chapter and then each student's status within that chapter.
 * Only one period of students are displayed at a time.
 * 
 * @author bgoldowsky
 * @author jbrookover
 *
 */
@AuthorizeInstantiation("TEACHER")
public class TeacherToc extends ISIStandardPage {


	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(TeacherToc.class);
	protected LoadableDetachableModel<List<User>> mStudentWithNotes;
	protected LoadableDetachableModel<Map<Long, Map<String, SectionStatus>>> mStatusMap;
	protected LoadableDetachableModel<HashMap<Long, Boolean>> mFlagMap;

	@Inject
	protected ISectionService sectionService;

	@Inject
	protected IISIResponseService responseService;
	
	public TeacherToc(PageParameters parameters) {
		super(parameters);

		pageTitle = (new StringResourceModel("TeacherTOC.pageTitle", this, null, "Home").getString());
		setPageTitle(pageTitle);

		loadStudentsWithNotes();
		loadStatusMap();
		loadFlagMap();

		addChapterTables(getCurrentSection());
	}


	public void addChapterTables(ISIXmlSection currentRootSection) {

		RepeatingView chapterRepeater = new RepeatingView("chapterRepeater");
		add(chapterRepeater);

		for (XmlDocument doc : ISIApplication.get().getStudentContent()) { // For each XML Document
			for (XmlSection rs : doc.getTocSection().getChildren()) { // For each chapter in the document; tends to be only one.

				final ISIXmlSection rootSection = (ISIXmlSection) rs;
				WebMarkupContainer rootSectionContainer = new WebMarkupContainer(chapterRepeater.newChildId());
				chapterRepeater.add(rootSectionContainer);

				rootSectionContainer.add(new Label("title", rs.getTitle()));
				rootSectionContainer.add(new Label("subtitle", rootSection.getSubTitle()));

				if (rootSection.equals(currentRootSection))
					rootSectionContainer.add(new ClassAttributeModifier("open"));

				WebMarkupContainer table = new WebMarkupContainer("periodTable");
				rootSectionContainer.add(table);

				table.add(new Label("periodName", new PropertyModel<String>(ISISession.get().getCurrentPeriodModel(), "name")));

				// Add non-section children as Super Section Labels
				// TODO: Lame hack for differences between IQWST/FS
				// TODO: Have TeacherToc create a custom repeater that can be extended by subclasses?
				RepeatingView superSectionRepeater = new RepeatingView("superSectionRepeater");
				for (XmlSection rc : rootSection.getChildren()) {
					ISIXmlSection rootChild = (ISIXmlSection) rc;
					WebMarkupContainer superSectionContainer = new WebMarkupContainer(superSectionRepeater.newChildId());
					superSectionRepeater.add(superSectionContainer);
					if (rootChild.isSuperSection()) {
						superSectionContainer.add(new Label("superSectionTitle", rootChild.getTitle()));
						if (rootChild.getChildren().size() > 1) {
							superSectionContainer.add(new SimpleAttributeModifier("colspan", String.valueOf(rootChild.getChildren().size())));
						}
					} else {
						superSectionContainer.add(new EmptyPanel("superSectionTitle"));
					}
				}
				table.add(superSectionRepeater);
				
				// List Sections across the top of the table.
				table.add(new ListView<ISIXmlSection>("sectionList", rootSection.getSectionChildren()) {
					private static final long serialVersionUID = 1L;
					@Override
					protected void populateItem(ListItem<ISIXmlSection> item) {
						ISIXmlSection sec = item.getModelObject();
						BookmarkablePageLink<ISIStandardPage> link = new SectionLinkFactory().linkToPage("sectionLink", sec);
						item.add(link);
						link.add(ISIApplication.get().iconFor(sec));
					}
				});

				// List students and their appropriate status messages
				// get the list of students in this period
				UserCriteriaBuilder c = new UserCriteriaBuilder();
				c.setRole(Role.STUDENT);
				c.setPeriod(ISISession.get().getCurrentPeriodModel());

				// add the students to the table
				table.add(new ListView<User>("student", new UserListModel(c)) {
					private static final long serialVersionUID = 1L;

					// for each student add the following fields
					@Override
					protected void populateItem(ListItem<User> userItem) {
						final User student = userItem.getModelObject();
						userItem.add(new StudentFlagPanel("flagPanel", student, mFlagMap.getObject()));
						userItem.add(new Label("studentName", student.getSortName()));

						// The cells in the table are status indications
						userItem.add(new ListView<ISIXmlSection>("statusCell", rootSection.getSectionChildren()) {
							private static final long serialVersionUID = 1L;

							@Override
							protected void populateItem(ListItem<ISIXmlSection> sectionItem) {
								final ISIXmlSection sec = sectionItem.getModelObject();

								Link<ISIXmlSection> link = new Link<ISIXmlSection>("link") {
									private static final long serialVersionUID = 1L;

									@Override
									public void onClick() {
										SectionStatus stat = getUserSectionStatus(student, sec);
										ISIXmlSection targetSection = new SectionLinkFactory().sectionLinkDest(sec);
										Class<? extends ISIStandardPage> pageType = ISIApplication.get().getReadingPageClass();
										ISISession.get().setStudentModel(new HibernateObjectModel<User>(student));
										PageParameters param = new PageParameters();

										// TODO: This first statement is a hack.  For some reason, getUnreadStudentMessages()
										// is able to be out of sync with FeedbackMessage.isUnread() flag and is displaying
										// the wrong icon on the page.  See ISIApplication.statusIconFor() as well.
										String s = null;
										if (stat != null && stat.getUnreadStudentMessages() > 0 && (s = responseService.locationOfFirstUnreadMessage(student, sec)) != null) {
											param.put("loc", s);
										} else if (stat != null && stat.getCompleted() && !stat.getReviewed()){
											ISIXmlSection section = targetSection.getSectionAncestor().firstPageWithResponseGroup();
											if (section != null)
												param.put("loc", (new ContentLoc(section).getLocation()));
											else 
												throw new IllegalStateException("Section without response areas marked Ready For Review - should automatically be reviewed.");
										} else {
											param.put("loc", (new ContentLoc(targetSection)).getLocation());
										}
										setResponsePage(pageType, param);										
									}
								};

								link.add(ISIApplication.statusIconFor(getUserSectionStatus(student, sec)));
								sectionItem.add(link);
							}
						});

						Link<Object> teacherNotesLink = new Link<Object>("teacherNotesLink") {
							private static final long serialVersionUID = 1L;

							@Override
							public void onClick() {
								ISISession.get().setStudentModel(new HibernateObjectModel<User>(student));
								setResponsePage(ISIApplication.get().getTeacherNotesPageClass());							
							}							
						};
						userItem.add(teacherNotesLink);
						teacherNotesLink.setPopupSettings(ISIApplication.teacherNotesPopupSettings);
						teacherNotesLink.setVisible(mStudentWithNotes.getObject().contains(student));
					}
				});
			}
		}
	}

	@Override
	public String getPageName() {
		return null;
	}

	@Override
	public String getPageType() {
		return "teachertoc";
	}

	/**
	 * Figure out which section is the current section to open. loc may be previously set for the page.
	 * Otherwise, go check if there is a bookmark set.  If there is still no current section, just set it to 
	 * the first section.
	 * 
	 * @return
	 */
	protected ISIXmlSection getCurrentSection() {
		if (loc == null)
			loc = ISIApplication.get().getBookmarkLoc();
		ISIXmlSection currentRootSection = null;
		ISIXmlSection currentPage = (loc == null ? null : loc.getSection());
		if (currentPage != null)
			currentRootSection = (ISIXmlSection) currentPage.getXmlDocument().getTocSection().getChild(0); // "level1"
		return currentRootSection;
	}


	/**
	 * Return a map of all of the students in this period and if they have a flag or not
	 */
	protected void loadFlagMap() {
		mFlagMap = new LoadableDetachableModel<HashMap<Long, Boolean>>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected HashMap<Long, Boolean> load() {
				// Load Student flags for this period and this teacher
				List<StudentFlag> l = responseService.getAllFlags();
				HashMap<Long, Boolean> flagMap = new HashMap<Long, Boolean>();
				for (StudentFlag f : l) {
					flagMap.put(f.getFlagee().getId(), true);
				}
				return flagMap;
			}			
		};
	}
	

	/**
	 * Return students with associated section status for this period
	 */
	protected void loadStatusMap() {
		mStatusMap = new LoadableDetachableModel<Map<Long, Map<String, SectionStatus>>>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected Map<Long, Map<String, SectionStatus>> load() {
				Map<Long, Map<String, SectionStatus>> statusMap;
				List<Period> periods = new ArrayList<Period>(1);
				periods.add(ISISession.get().getCurrentPeriodModel().getObject());		
				statusMap = sectionService.getSectionStatusMaps(periods);
				return statusMap;
			}			
		};
	}

	protected SectionStatus getUserSectionStatus(User student, ISIXmlSection section) {
		SectionStatus status = mStatusMap.getObject().get(student.getId())!=null ? mStatusMap.getObject().get(student.getId()).get(new ContentLoc(section).getLocation()) : null;
		return status;
	}

	/**
	 * Finds all notes for this teacher and creates a list of the students
	 * with notes (prompt).
	 */
	protected void loadStudentsWithNotes() {
		mStudentWithNotes = new LoadableDetachableModel<List<User>>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected List<User> load() {
				List<ISIPrompt> mTeacherNotes = responseService.getTeacherNotes(ISISession.get().getUserModel());
				List<User> studentsWithNotes = new ArrayList<User>();
				// go through each teacher note and pull off the students (target users)
				for (ISIPrompt prompt : mTeacherNotes) {
					studentsWithNotes.add(prompt.getTargetUser());				
				}
				return studentsWithNotes;
			}			
		};
	}

	@Override
	protected void onDetach() {
		if (mStudentWithNotes != null)
			mStudentWithNotes.detach();
		if (mStatusMap != null)
			mStatusMap.detach();
		if (mFlagMap != null)
			mFlagMap.detach();
		super.onDetach();
	}
	
}