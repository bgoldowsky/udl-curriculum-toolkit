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

import java.util.Iterator;
import java.util.LinkedList;

import org.apache.wicket.PageParameters;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.markup.repeater.util.ModelIteratorAdapter;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.PersistedObject;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.cwm.tag.TagService;
import org.cast.cwm.tag.component.TagLabel;
import org.cast.cwm.tag.component.TaggingsListPanel;
import org.cast.cwm.tag.model.Tag;
import org.cast.cwm.tag.model.Tagging;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISISession;
import org.cast.isi.ISITagLinkBuilder;
import org.cast.isi.ISIXmlSection;
import org.cast.isi.data.ContentElement;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.panel.TagCloudAlternateView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This page creates a clickable listing of tags on the left hand side with 
 * associated links to pages tagged on the right side.
 */
@AuthorizeInstantiation("STUDENT")
public class Tags extends ISIStandardPage {
	
	protected Tag selected;
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(Tags.class);
	private User targetUser;
	
	public Tags(final PageParameters parameters) {
		super(parameters);

		// set teacher flag and target user
		boolean isTeacher = ISISession.get().getUser().getRole().subsumes(Role.TEACHER);
		if (isTeacher) {
			targetUser = ISISession.get().getStudent();
		} else {
			targetUser = ISISession.get().getUser();
		}
				
		String tagName = parameters.getString("tag");
		if (tagName != null) {
			selected = TagService.get().findTag(targetUser, tagName);
		}
		
		final ISITagLinkBuilder linkBuilder = ISIApplication.get().getTagLinkBuilder();

		add(new TagLabel("titletag", selected).setVisible(selected!=null));

		// this was used when the tags were a popup window
		//add(new Label("heading", isTeacher ? "Tags " + " (" + targetUser.getFullName() + ")" : "Tags"));

		add(new WebMarkupContainer("noTagSelected").setVisible((selected == null) && (targetUser != null)));
		add(new WebMarkupContainer("noStudentSelected").setVisible(targetUser == null));   	
		
		TagCloudAlternateView tcav = new TagCloudAlternateView("tagCloud", selected);
		tcav.setTargetUser(targetUser);
		tcav.setVisible(targetUser != null);
		add(tcav);
		
		add(new RefreshingView<Tagging>("items") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(Item<Tagging> item) {
				Tagging ting = item.getModelObject();
				PersistedObject target = TagService.get().getTarget(ting);
				if (target instanceof ContentElement) {
					ContentElement elt = (ContentElement) target;
					ContentLoc loc = elt.getContentLocObject();
					ISIXmlSection sec = (ISIXmlSection) loc.getSection();

					String crumbTrail = sec.getCrumbTrailAsString(1, 1);
					item.add(new Label("crumbTrail", crumbTrail));
					
					BookmarkablePageLink<ISIStandardPage> link = ISIStandardPage.linkTo("titleLink", sec);
					link.add(new Label("title", sec.getTitle()));
					item.add(link);

					BookmarkablePageLink<ISIStandardPage> iconLink = ISIStandardPage.linkTo("iconLink", sec);
					iconLink.add(ISIApplication.get().iconFor(sec.getSectionAncestor(),""));
					item.add(iconLink);
				
				} 
				TaggingsListPanel tlp = new TaggingsListPanel("tags", target, linkBuilder, targetUser);
				tlp.setShowRemoveLinks(false);
				item.add(tlp);
			}

			@Override
			@SuppressWarnings("unchecked")
			protected Iterator getItemModels() {
				if (selected != null && selected.getTaggings() != null) {
					return new ModelIteratorAdapter(selected.getTaggings().iterator()) {
						@Override
						protected IModel model(Object object) {
							return new CompoundPropertyModel((Tagging) object);
						}
					};
				} else {
					// Return empty iterator
					return new LinkedList<IModel>().iterator();
				}
			}			
		});
	}
	
	@Override
	public String getPageName() {
		return null;
	}

	@Override
	public String getPageType() {
		return "tags";
	}
	
	@Override
	public String getPageViewDetail() {
		return selected != null ? selected.getName() : null;
	}	
}