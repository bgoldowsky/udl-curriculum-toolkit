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
package org.cast.isi.panel;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.cast.cwm.data.User;
import org.cast.cwm.xml.XmlSection;
import org.cast.isi.service.IFeatureService;

import com.google.inject.Inject;

public abstract class SectionCompleteToggleTextLink extends	SectionCompleteToggleLink {

	private static final long serialVersionUID = 1L;

	@Inject
	protected IFeatureService featureService;
	
	public SectionCompleteToggleTextLink(String id, IModel<XmlSection> model,
			IModel<User> targetUserModel) {
		super(id, model, targetUserModel);
		add(new AttributeModifier("class", true, new Model<String>(){

				private static final long serialVersionUID = 1L;
				
				@Override
				public String getObject() {
					if (isEnabled())
						return "button big";
					else return "button off";
				}

			})
		);
	}

	public SectionCompleteToggleTextLink(String id, IModel<XmlSection> model) {
		super(id, model);
	}
	
	@Override
	public boolean isVisible() {
		return isConfiguredOn() && isLastPageInSection();
	}

	private boolean isConfiguredOn() {
		return featureService.isSectionToggleTextLinksOn();
	}

	@Override
	public void onBeforeRender() {
		addOrReplace(new Label("text", getLabelText()));
		super.onBeforeRender();
	}

	protected abstract String getLabelText();

	protected String getStringResource(String key, String defaultValue) {
		return new ResourceModel(key, defaultValue).getObject();
	} 

}