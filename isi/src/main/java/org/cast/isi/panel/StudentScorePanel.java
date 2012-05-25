package org.cast.isi.panel;

import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.Response;

public class StudentScorePanel extends ScorePanel {

	private static final long serialVersionUID = 1L;
	public StudentScorePanel(String id, ISortableDataProvider<Response> responseProvider) {
		this(id, getResponses(responseProvider));
	}

	public StudentScorePanel(String id, List<IModel<Response>> models) {
		super(id, models);
		add(new GotItButton("gotItButton"));
		add(new NotGotItButton("notGotItButton"));
	}

	private class NotGotItButton extends WebMarkupContainer {
		private static final long serialVersionUID = 1L;

		private NotGotItButton(String id) {
			super(id);
			add(new Icon("icon", "/img/icons/response_negative.png", "Didn't get it"));
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
			add(new Icon("icon", "/img/icons/response_positive.png", "Got it!"));
		}

		@Override
		public boolean isVisible() {
			return isMarkedCorrect();
		}
	}

}
