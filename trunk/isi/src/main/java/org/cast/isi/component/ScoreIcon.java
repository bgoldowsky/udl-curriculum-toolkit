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
