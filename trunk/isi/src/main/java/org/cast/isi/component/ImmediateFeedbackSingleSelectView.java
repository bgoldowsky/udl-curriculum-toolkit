package org.cast.isi.component;

import org.apache.wicket.model.IModel;
import org.cast.cwm.data.Prompt;

public class ImmediateFeedbackSingleSelectView extends ImmediateFeedbackSingleSelectForm {

	private static final long serialVersionUID = 1L;

	public ImmediateFeedbackSingleSelectView(String wicketId,
			IModel<Prompt> mPrompt) {
		super(wicketId, mPrompt);
		setEnabled(false);
	}

}
