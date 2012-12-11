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

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.cast.cwm.components.Icon;
import org.cast.cwm.data.Response;

/**
 * An icon that is "Thumbs up" for a correct response, or "Thumbs Down" for an incorrect one.
 * @author bgoldowsky
 *
 */
public class ScoreIcon extends Icon {
	
	IModel<Response> mResponse;

	private static final long serialVersionUID = 1L;

	public ScoreIcon(String id, IModel<Response> mResponse) {
		super(id);
		this.setmImagePath(new PropertyModel<String>(this, "image"));
		this.mResponse = mResponse;
	}
	
	@Override
	protected void onBeforeRender() {
		this.setmAltText(new ResourceModel(isCorrect() ? "ScoreIcon.correct" : "ScoreIcon.incorrect"));
		super.onBeforeRender();
	}

	public String getImage() {
		if (isCorrect())
			return "img/icons/response_positive.png";
		else
			return "img/icons/response_negative.png";
	}
	
	private boolean isCorrect() {
		Response r = mResponse.getObject();
		return (r!=null && r.isCorrect());
	}
	
	
}
