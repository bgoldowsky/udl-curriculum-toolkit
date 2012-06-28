package org.cast.isi.panel;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.cast.cwm.data.Response;

public class CorrectDelayedSingleSelectSummaryPanel extends SingleSelectSummaryPanel {

	private static final long serialVersionUID = 1L;

	public CorrectDelayedSingleSelectSummaryPanel(String id,
			List<Response> responses) {
		super(id);
		setOutputMarkupId(true);
		add(new Icon("score", "/img/icons/response_positive.png", "Got it!"));
		add(new Label("count", String.valueOf(responses.size())));
		add(new StudentList("studentList", responses));
	}
	

}
