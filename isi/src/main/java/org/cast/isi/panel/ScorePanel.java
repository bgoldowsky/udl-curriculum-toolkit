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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.databinder.models.hib.HibernateObjectModel;

import org.apache.wicket.Application;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.cast.cwm.data.Response;

public abstract class ScorePanel extends Panel {

	private static final long serialVersionUID = 1L;

	private List<IModel<Response>> responseModels;

	public ScorePanel(String id, List<IModel<Response>> models) {
		super(id);
		this.responseModels = models;
		setOutputMarkupId(true);
	}
	
	public boolean isVisible() {
		return !(getResponses().isEmpty());
	}

	protected static List< IModel<Response>> getResponses(ISortableDataProvider<Response> responseProvider) {
		List< IModel<Response>> responses = new ArrayList< IModel<Response>>();
		for (Iterator<? extends Response> it = responseProvider.iterator(0, Integer.MAX_VALUE); it.hasNext(); ) {
			responses.add(new HibernateObjectModel<Response>(it.next()));
		}
		return responses;
	}

	protected boolean isMarkedCorrect() {
		Integer score = getScore();
		return (score != null) && (score > 0);
	}

	protected boolean isMarkedIncorrect() {
		Integer score = getScore();
		return (score != null) && (score < 1);
	}

	protected boolean isUnmarked() {
		Integer score = getScore();
		return (score == null);
	}

	protected Integer getScore() {
		List<Response> responses = getResponses();
		return responses.isEmpty() ? null : responses.get(0).getScore();
	}

	protected List<Response> getResponses() {
		List<Response> result = new ArrayList<Response>();
		for (IModel<Response> model:  responseModels) {
			result.add(model.getObject());
		}
		return result;
	}

	//TODO: Can we use ScoreIcon instead or make a common superclass?
	public class Icon extends Image {

		private static final long serialVersionUID = 1L;

		public Icon(String id, String imageFile, String altText) {
			super(id);
			setImageResourceReference(new ResourceReference(Application.class, imageFile));
			add(new AttributeModifier("alt", true, new Model<String>(altText)));
			add(new AttributeModifier("title", true, new Model<String>(altText)));
		}

	}

}