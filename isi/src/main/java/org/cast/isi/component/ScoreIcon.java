package org.cast.isi.component;

import org.apache.wicket.Application;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.cast.cwm.data.Response;

/**
 * An icon that is "Thumbs up" for a correct response, or "X" for an incorrect one.
 * @author bgoldowsky
 *
 */
public class ScoreIcon extends Image {

	private static final long serialVersionUID = 1L;

	public ScoreIcon(String id, IModel<Response> mResponse) {
		super(id, mResponse);
	}
	
	@Override
	protected void onBeforeRender() {
		setImageResourceReference(getResourceReference());
		super.onBeforeRender();
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		String alt = getAltText();
		tag.put("alt", alt);
		tag.put("title", alt);
	}
	
	private String getAltText() {
		if (isCorrect())
			return new ResourceModel("ScoreIcon.correct").getObject();
		else
			return new ResourceModel("ScoreIcon.incorrect").getObject();
	}

	private ResourceReference getResourceReference() {
		if (isCorrect())
			return new ResourceReference(Application.class, "/img/icons/response_positive.png");
		else
			return new ResourceReference(Application.class, "/img/icons/response_negative.png");
	}
	
	private boolean isCorrect() {
		Response r = (Response) getDefaultModelObject();
		return (r!=null && r.isCorrect());
	}
	
	
}
