package org.cast.isi.panel;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.cast.cwm.data.Response;
import org.cast.isi.component.ScoreIcon;

/**
 * Display an icon and text indicating the user's success on a single-select multiple choice response.
 * @author bgoldowsky
 *
 */
public class SingleSelectScoreIndicator extends Panel {

	private static final long serialVersionUID = 1L;

	public SingleSelectScoreIndicator(String id, IModel<Response> model) {
		super(id, model);
		add(new ScoreIcon("icon", model));
		add(new Label("text", new PropertyModel<String>(model, "triesOrdinal")));
	}
	
	@Override
	protected void onBeforeRender() {
		// Score indicator is visible only if the response has been scored.
		setVisible(getModel().getObject() != null && getModel().getObject().getScore() != null);
		super.onBeforeRender();
	}

	@SuppressWarnings("unchecked")
	IModel<Response> getModel() {
		return (IModel<Response>) getDefaultModel();
	}

	@Override
	// Required in order to set visibility in onBeforeRender
	protected boolean callOnBeforeRenderIfNotVisible() {
	   return true;
	}
}
