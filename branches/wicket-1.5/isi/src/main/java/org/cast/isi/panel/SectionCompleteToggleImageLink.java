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

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeAction;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeActions;
import org.apache.wicket.model.IModel;
import org.cast.cwm.components.Icon;
import org.cast.cwm.data.User;
import org.cast.cwm.xml.XmlSection;
import org.cast.isi.component.DoneImage;
import org.cast.isi.component.NotDoneImage;
import org.cast.isi.service.IFeatureService;

import com.google.inject.Inject;

@AuthorizeActions(actions = { @AuthorizeAction(action="RENDER", roles={"STUDENT"})})
public abstract class SectionCompleteToggleImageLink extends SectionCompleteToggleLink {

	private static final long serialVersionUID = 1L;

	@Inject
	protected IFeatureService featureService;
	
	/**
	 * Constructor
	 * 
	 * @param id wicket id
	 * @param model a model containing the section to be checked/toggled
	 * @param targetUserModel the user to be marked/unmarked as completing the section
	 */
	public SectionCompleteToggleImageLink(String id,
			IModel<XmlSection> model, IModel<User> targetUserModel) {
		super(id, model, targetUserModel);
	}

	/**
	 * Constructor
	 * 
	 * @param id wicket id
	 * @param model a model containing the section to be checked/toggled
	 */
	public SectionCompleteToggleImageLink(String id, IModel<XmlSection> model) {
		super(id, model);
	}
	
	@Override
	public boolean isVisible() {
		return featureService.isSectionToggleImageLinksOn();
	}

	@Override
	public void onBeforeRender() {
		addOrReplace(getImage());
		super.onBeforeRender();
	}

	protected Icon getImage() {
		if (isComplete())
			return new DoneImage("doneImg");
		else return new NotDoneImage("doneImg");
	}
}