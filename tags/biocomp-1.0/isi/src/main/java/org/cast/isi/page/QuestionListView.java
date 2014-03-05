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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.cast.cwm.components.ClassAttributeModifier;
import org.cast.cwm.data.User;
import org.cast.cwm.service.ICwmSessionService;
import org.cast.isi.ISISession;
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
	
	@Inject
	protected IQuestionService questionService;
	
	@Inject
	protected ICwmSessionService cwmSessionService;

	private static final long serialVersionUID = 1L;

	public QuestionListView (String id, Class<? extends WebPage>linkPage, PopupSettings popupSettings, Question current) {
		super(id);
		this.current = current;
		this.linkPage = linkPage;
		this.popupSettings = popupSettings;
		doQuery();
	}

	void doQuery() {
		// TODO: This won't get detached when used in this manner.
		User user = ISISession.get().getTargetUserModel().getObject();
		if (!user.isGuest())
			setModel(new ListModel<Question>(questionService.getQuestionsByUser(user.getId())));
	}

	@Override
	protected void populateItem(ListItem<Question> item) {
		Question q = item.getModelObject();
		boolean isCurrent = q.equals(current);
		
		PageParameters pp = new PageParameters();
		pp.set("question", q.getId());
		pp.set("callingPageName", ((ISIStandardPage) getPage()).getPageName());
		BookmarkablePageLink<WebPage> link = new BookmarkablePageLink<WebPage>("link", linkPage, pp);
		link.setPopupSettings(popupSettings);
		item.add(link);
		if (isCurrent)
			link.setEnabled(false);

		link.add(new Label("text", q.getText()));
		
		if (q.getOwner() != null) {
			if (isCurrent)
				link.add(new AttributeModifier("class", "selected"));
			else
				item.add(new AttributeModifier("class", "questionP"));
		} else {
			if (isCurrent)
				link.add(new ClassAttributeModifier("selected"));
		}
	}

}