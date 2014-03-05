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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.cast.cwm.components.Icon;
import org.cast.cwm.data.Response;
import org.cast.cwm.data.Role;
import org.cast.cwm.service.ICwmService;
import org.cast.cwm.service.ICwmSessionService;

import com.google.inject.Inject;

public class TeacherScoreResponseButtonPanel extends ScorePanel {

	private static final long serialVersionUID = 1L;
	
	@Inject
	private ICwmService cwmService;

	@Inject 
	protected ICwmSessionService sessionService;


	public TeacherScoreResponseButtonPanel(String id, ISortableDataProvider<Response> responseProvider) {
		this(id, getResponses(responseProvider));
	}

	public TeacherScoreResponseButtonPanel(String id, List<IModel<Response>> models) {
		super(id, models);
		setOutputMarkupId(true);
	}
	
	@Override
	public void onBeforeRender() {
		addOrReplace(new GotItButton("gotItButton"));
		addOrReplace(new NotGotItButton("notGotItButton"));
		super.onBeforeRender();
	}

	private void scoreAsCorrect(AjaxRequestTarget target) {
		updateResponseScore(target, 1);
	}

	private void scoreAsIncorrect(AjaxRequestTarget target) {
		updateResponseScore(target, 0);
	}

	private void removeScore(AjaxRequestTarget target) {
		updateResponseScore(target, null);
	}

	private void updateResponseScore(AjaxRequestTarget target, Integer score) {
		for (Response response: getResponses()) {
			response.setScore(score);
		}
		cwmService.flushChanges();
		target.add(TeacherScoreResponseButtonPanel.this);
	}

	private boolean isResearcher() {
		return sessionService.getUser().hasRole(Role.RESEARCHER);
	}

	private class GotItButton extends AjaxLink<Void> {

		private static final long serialVersionUID = 1L;

		private GotItButton(String id) {
			super(id);
			add(new Icon("icon", "img/icons/response_positive.png", getAltText()));
			if (isMarkedCorrect()) 
				add(new AttributeAppender("class", new Model<String>("current"), " "));
			if (isResearcher())
				setEnabled(false);
		}
		
		private String getAltText() {
			if (isMarkedCorrect())
				return "Click to remove \"Got it!\" scoring.";
			else 
				return "Click to score as \"Got it!\"";
		}

		@Override
		public void onClick(AjaxRequestTarget target) {
			if (isMarkedCorrect()) 
				removeScore(target);
			else
				scoreAsCorrect(target);
		}

	}

	private class NotGotItButton extends AjaxLink<Void> {

		private static final long serialVersionUID = 1L;

		private NotGotItButton(String id) {
			super(id);
			add(new Icon("icon", "img/icons/response_negative.png", getAltText()));
			if (isMarkedIncorrect()) 
				add(new AttributeAppender("class", new Model<String>("current"), " "));
			if (isResearcher())
				setEnabled(false);
		}

		private String getAltText() {
			if (isMarkedIncorrect())
				return "Click to remove \"Didn't get it\" scoring.";
			else 
				return "Click to score as \"Didn't get it\"";
		}

		@Override
		public void onClick(AjaxRequestTarget target) {
			if (isMarkedIncorrect())
				removeScore(target);
			else
				scoreAsIncorrect(target);
		}

	}

}
