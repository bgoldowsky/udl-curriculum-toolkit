/*
 * Copyright 2012 CAST, Inc.
 *
 * This file is part of the CAST Wicket Modules:
 * see <http://code.google.com/p/cast-wicket-modules>.
 *
 * The CAST Wicket Modules are free software: you can redistribute and/or
 * modify them under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * The CAST Wicket Modules are distributed in the hope that they will be
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
import org.cast.cwm.data.Response;
import org.cast.cwm.service.ICwmService;

import com.google.inject.Inject;

public class TeacherScoreResponseButtonPanel extends ScorePanel {

	private static final long serialVersionUID = 1L;
	
	@Inject
	private ICwmService cwmService;
	
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

	private void updateResponseScore(AjaxRequestTarget target, Integer score) {
		for (Response response: getResponses()) {
			response.setScore(score);
		}
		cwmService.flushChanges();
		target.addComponent(TeacherScoreResponseButtonPanel.this);
	}

	private class GotItButton extends AjaxLink<Void> {

		private static final long serialVersionUID = 1L;

		private GotItButton(String id) {
			super(id);
			add(new Icon("icon", "/img/icons/response_positive.png", getAltText()));
			if (isMarkedCorrect()) {
				add(new AttributeAppender("class", new Model<String>("current"), " "));
			}
		}
		
		private String getAltText() {
			if (isMarkedCorrect())
				return "Click to remove \"Got it!\" scoring.";
			else {
				return "Click to score as \"Got it!\"";
			}
		}

		@Override
		public void onClick(AjaxRequestTarget target) {
			if (isUnmarked()) {
				updateResponseScore(target, 1);
			}
			else if (isMarkedCorrect()) {
				updateResponseScore(target, null);
			}
		}

	}

	private class NotGotItButton extends AjaxLink<Void> {

		private static final long serialVersionUID = 1L;

		private NotGotItButton(String id) {
			super(id);
			add(new Icon("icon", "/img/icons/response_negative.png", getAltText()));
			if (isMarkedIncorrect()) {
				add(new AttributeAppender("class", new Model<String>("current"), " "));
			}
		}

		private String getAltText() {
			if (isMarkedIncorrect())
				return "Click to remove \"Didn't get it\" scoring.";
			else {
				return "Click to score as \"Didn't get it\"";
			}
		}

		@Override
		public void onClick(AjaxRequestTarget target) {
			if (isUnmarked()) {
				updateResponseScore(target, 0);
			}
			else if (isMarkedIncorrect()) {
				updateResponseScore(target, null);
			}
		}
	}

}
