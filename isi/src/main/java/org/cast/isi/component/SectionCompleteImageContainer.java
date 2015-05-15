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

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeAction;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeActions;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.cast.cwm.components.Icon;
import org.cast.cwm.data.User;
import org.cast.cwm.xml.XmlSection;
import org.cast.isi.ISISession;
import org.cast.isi.ISIXmlSection;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.data.SectionStatus;
import org.cast.isi.service.IFeatureService;
import org.cast.isi.service.ISectionService;

import com.google.inject.Inject;

@AuthorizeActions(actions = { @AuthorizeAction(action="RENDER", roles={"STUDENT"})})
public class SectionCompleteImageContainer extends WebMarkupContainer {

	private static final long serialVersionUID = 1L;

	@Inject
	protected ISectionService sectionService;

	@Inject
	protected IFeatureService featureService;

	private IModel<User> targetUserModel;

	private ContentLoc sectionContentLocation;

	public SectionCompleteImageContainer(String id,
			IModel<XmlSection> model) {
		super(id, model);
		this.sectionContentLocation = getContentLoc(getSectionAncestor(getISIXmlSection(model)));
		this.targetUserModel = ISISession.get().getTargetUserModel();
	}

	@Override
	public void onBeforeRender() {
		addOrReplace(getImage());
		super.onBeforeRender();
	}

	protected Icon getImage() {
		Icon image;
		if (isComplete()) {
			image = new DoneImage("doneImg");
			image.setVisible(featureService.isTocSectionCompleteIconsOn());
		}
		else {
			image = new NotDoneImage("doneImg");
			image.setVisible(featureService.isTocSectionIncompleteIconsOn());
		}
		return image;
	}

	protected boolean isComplete() {
		SectionStatus status = sectionService.getSectionStatus(getUser(), sectionContentLocation);			
		if (status == null)
			return false;
		return status.getCompleted();
	}
	
	protected User getUser() {
		return targetUserModel.getObject();
	}

	private static ISIXmlSection getSectionAncestor(ISIXmlSection isiXmlSection) {
		return isiXmlSection.getSectionAncestor();
	}

	private static ISIXmlSection getISIXmlSection(IModel<XmlSection> model) {
		return (ISIXmlSection) model.getObject();
	}

	private static ContentLoc getContentLoc(ISIXmlSection xmlSection) {
		return xmlSection.getContentLoc();
	}


}
