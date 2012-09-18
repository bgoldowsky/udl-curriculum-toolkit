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

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.util.ListModel;
import org.cast.cwm.CwmSession;
import org.cast.cwm.components.ClassAttributeModifier;
import org.cast.isi.data.Question;
import org.cast.isi.service.IQuestionService;

import com.google.inject.Inject;

/**
 * Show the user's list of Question objects, with various markup to show
 * ownership, etc.
 * @author bgoldowsky
 *
 */
class QuestionListView extends ListView<Question> {
	
	Question current;
	Class<? extends WebPage> linkPage;
	PopupSettings popupSettings;
	Long userId;
	
	@Inject
	protected IQuestionService questionService;

	private static final long serialVersionUID = 1L;

	public QuestionListView (String id, Class<? extends WebPage>linkPage, PopupSettings popupSettings, Question current, Long userId) {
		super(id);
		this.current = current;
		this.linkPage = linkPage;
		this.popupSettings = popupSettings;
		if (userId == null) {
			this.userId = CwmSession.get().getUser().getId();
		} else {
			this.userId = userId;
		}
		doQuery();
	}

	void doQuery() {
		// TODO: This won't get detached when used in this manner.
		setModel(new ListModel<Question>(questionService.getQuestionsByUser(userId)));		
	}

	@Override
	protected void populateItem(ListItem<Question> item) {
		Question q = (Question)item.getModelObject();
		boolean isCurrent = q.equals(current);
		
		BookmarkablePageLink<WebPage> link = new BookmarkablePageLink<WebPage>("link", linkPage);
		item.add(link);
		link.setParameter("question", q.getId());
		link.setParameter("callingPageName", ((ISIStandardPage) getPage()).getPageName());
		link.add(new Label("text", q.getText()));
		link.setPopupSettings(popupSettings);
		if (isCurrent)
			link.setEnabled(false);
		
		if (q.getOwner() != null) {
			if (isCurrent) {
				link.add(new SimpleAttributeModifier("class", "selected"));
			}
			else
				item.add(new SimpleAttributeModifier("class", "questionP"));
		} else {
			if (isCurrent)
				link.add(new ClassAttributeModifier("selected"));

		}
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}
}