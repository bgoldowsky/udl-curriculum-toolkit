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

import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.cast.cwm.components.Icon;
import org.cast.cwm.data.Response;
import org.cast.cwm.data.User;
import org.cast.isi.ISISession;
import org.cast.isi.data.ISIPrompt;
import org.cast.isi.data.SectionStatus;
import org.cast.isi.service.ISectionService;

import com.google.inject.Inject;

public class StudentScorePanel extends ScorePanel {

	private static final long serialVersionUID = 1L;
	
	@Inject
	private ISectionService sectionService;

	private IModel<User> targetUserModel;
	
	public StudentScorePanel(String id, ISortableDataProvider<Response> responseProvider) {
		this(id, getResponses(responseProvider));
	}

	public StudentScorePanel(String id, List<IModel<Response>> models) {
		this(id, models, ISISession.get().getTargetUserModel());
	}

	public StudentScorePanel(String id, List<IModel<Response>> models, IModel<User> targetUserModel) {
		super(id, models);
		this.targetUserModel = targetUserModel;
		add(new GotItButton("gotItButton"));
		add(new NotGotItButton("notGotItButton"));
	}

	@Override
	public boolean isVisible() {
		List<Response> responses = getResponses();
		if (responses.isEmpty())
			return false;
		ISIPrompt prompt = (ISIPrompt) responses.get(0).getPrompt();
		User user = targetUserModel.getObject();
		return !(prompt.isDelayFeedback() && needsReview(prompt, user));
	}
	
	private boolean needsReview(ISIPrompt prompt, User user) {
		SectionStatus sectionStatus = sectionService.getSectionStatus(user, prompt.getSection());
		return (sectionStatus == null) || !sectionStatus.getReviewed();
	}

	private class NotGotItButton extends WebMarkupContainer {
		private static final long serialVersionUID = 1L;

		private NotGotItButton(String id) {
			super(id);
			add(new Icon("icon", "img/icons/response_negative.png", "Didn't get it"));
		}

		@Override
		public boolean isVisible() {
			return isMarkedIncorrect();
		}
	}
	
	private class GotItButton extends WebMarkupContainer {
		private static final long serialVersionUID = 1L;

		private GotItButton(String id) {
			super(id);
			add(new Icon("icon", "img/icons/response_positive.png", "Got it!"));
		}

		@Override
		public boolean isVisible() {
			return isMarkedCorrect();
		}
	}

}
