package org.cast.isi.panel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.databinder.models.hib.HibernateObjectModel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.cast.cwm.data.Response;
import org.cast.cwm.service.ICwmService;

import com.google.inject.Inject;

public class TeacherScoreResponseButtonPanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	@Inject
	private ICwmService cwmService;
	
	private List<IModel<Response>> responseModels;

	public TeacherScoreResponseButtonPanel(String id, ISortableDataProvider<Response> responseProvider) {
		this(id, getResponses(responseProvider));
	}

	public TeacherScoreResponseButtonPanel(String id, List<IModel<Response>> models) {
		super(id);
		this.responseModels = models;
		setOutputMarkupId(true);
	}
	
	public boolean isVisible() {
		return !(getResponses().isEmpty());
	}

	private static List< IModel<Response>> getResponses(
			ISortableDataProvider<Response> responseProvider) {
		List< IModel<Response>> responses = new ArrayList< IModel<Response>>();
		for (Iterator<? extends Response> it = responseProvider.iterator(0, Integer.MAX_VALUE); it.hasNext(); ) {
			responses.add(new HibernateObjectModel<Response>(it.next()));
		}
		return responses;
	}

	@Override
	public void onBeforeRender() {
		addOrReplace(new GotItButton("gotItButton"));
		addOrReplace(new NotGotItButton("notGotItButton"));
		super.onBeforeRender();
	}

	private boolean isMarkedCorrect() {
		Integer score = getScore();
		return (score != null) && (score > 0);
	}

	private boolean isMarkedIncorrect() {
		Integer score = getScore();
		return (score != null) && (score < 1);
	}

	private boolean isUnmarked() {
		Integer score = getScore();
		return (score == null);
	}

	private Integer getScore() {
		List<Response> responses = getResponses();
		return responses.isEmpty() ? null : responses.get(0).getScore();
	}

	private List<Response> getResponses() {
		List<Response> result = new ArrayList<Response>();
		for (IModel<Response> model:  responseModels) {
			result.add(model.getObject());
		}
		return result;
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
			if (isMarkedCorrect()) {
				add(new AttributeAppender("class", new Model<String>("current"), " "));
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
			if (isMarkedIncorrect()) {
				add(new AttributeAppender("class", new Model<String>("current"), " "));
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
