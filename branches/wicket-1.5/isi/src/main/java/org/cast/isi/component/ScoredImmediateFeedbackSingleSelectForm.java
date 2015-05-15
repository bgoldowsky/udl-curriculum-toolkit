/*
 * Copyright 2011-2015 CAST, Inc.
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
import org.cast.cwm.components.ShyContainer;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.User;
import org.cast.isi.panel.SingleSelectScoreIndicator;

/**
 * An ImmediateFeedbackSingleSelectForm with an attached SingleSelectScoreIndicator.
 * 
 * TODO: This might more appropriately be a border and be applied to 
 * arbitrary response forms.
 *    
 * @author Don Roby
 *
 */
public class ScoredImmediateFeedbackSingleSelectForm extends ImmediateFeedbackSingleSelectForm {

	private static final long serialVersionUID = 1L;

	public ScoredImmediateFeedbackSingleSelectForm(String id, IModel<Prompt> mcPrompt) {
		super(id, mcPrompt);
	}
	
	public ScoredImmediateFeedbackSingleSelectForm(String id,
			IModel<Prompt> mcPrompt, IModel<User> userModel,
			IModel<User> targetUserModel) {
		super(id, mcPrompt, userModel, targetUserModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		SingleSelectScoreIndicator scoreIndicator = new SingleSelectScoreIndicator("mcScore", mResponse);
		ShyContainer container = new ShyContainer("shy");
		container.add(scoreIndicator);
		add(container);
	}
	
	
}