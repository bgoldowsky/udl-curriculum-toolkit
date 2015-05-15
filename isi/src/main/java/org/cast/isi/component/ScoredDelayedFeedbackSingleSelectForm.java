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

import java.util.Arrays;

import org.apache.wicket.model.IModel;
import org.cast.cwm.components.ShyContainer;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.xml.XmlSection;
import org.cast.isi.panel.StudentScorePanel;

/**
 * A DelayedFeedbackSingleSelectForm with an attached StudentScorePanel.
 * 
 * TODO: This might more appropriately be a border and be applied to 
 * arbitrary response forms.
 *    
 * @author Don Roby
 *
 */
public class ScoredDelayedFeedbackSingleSelectForm extends DelayedFeedbackSingleSelectForm {

	private static final long serialVersionUID = 1L;

	public ScoredDelayedFeedbackSingleSelectForm(String id, IModel<Prompt> mcPrompt, IModel<XmlSection> currentSectionModel) {
		super(id, mcPrompt, currentSectionModel);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onInitialize() {
		super.onInitialize();
		StudentScorePanel studentScorePanel = new StudentScorePanel("mcScore", Arrays.asList(mResponse)){

			private static final long serialVersionUID = 1L;
			
			@Override
			public boolean isVisible() {
				return isReviewed();
			}
		};
		ShyContainer container = new ShyContainer("shy");		
		container.add(studentScorePanel);
		add(container);
	}
	
	
}