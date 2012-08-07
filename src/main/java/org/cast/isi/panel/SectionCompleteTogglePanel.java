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

import lombok.Getter;
import lombok.Setter;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.cast.cwm.data.User;
import org.cast.cwm.xml.XmlSection;
import org.cast.isi.ISIXmlSection;
import org.cast.isi.component.DoneImage;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.data.SectionStatus;
import org.cast.isi.service.IFeatureService;
import org.cast.isi.service.ISectionService;

import com.google.inject.Inject;

public abstract class SectionCompleteTogglePanel extends Panel implements ISectionStatusChangeListener {

	private static final long serialVersionUID = 1L;

	@Inject
	protected IFeatureService featureService;

	@Inject
	protected ISectionService sectionService;

	protected IModel<XmlSection> mSection;

	protected IModel<User> mTargetUser;

	@Getter @Setter
	private boolean visibilityEnabled = true;
	
	@Getter
	protected ContentLoc sectionContentLocation;
	protected ContentLoc pageContentLocation;

	public SectionCompleteTogglePanel(String id, IModel<XmlSection> mSection, IModel<User> mTargetUser) {
		super(id);
		this.mSection = mSection;
		this.mTargetUser = mTargetUser;
		this.pageContentLocation = getContentLoc(getISIXmlSection());
		this.sectionContentLocation = getContentLoc(getSectionAncestor(getISIXmlSection()));
		setOutputMarkupId(true);
	}
	
	@Override
	public void onConfigure() {
		setVisible(visibilityEnabled && featureService.isSectionToggleTextLinksOn() && isLastPageInSection());
		super.onConfigure();
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		addSectionToggleButton();
	}
	
	@Override
	public void onBeforeRender() {
		addOrReplace(getCompletedImage("doneImage", showCompletedImage()));
		addOrReplace(new Label("instructions", getInstructions()));
		super.onBeforeRender();
	}

	private boolean isLastPageInSection() {
		ISIXmlSection section = getSection();
		return (section != null) && (section.isLastPageInSection());
	}

	private ISIXmlSection getSection() {
		return pageContentLocation.getSection();
	}

	/**
	 * Provides an image indicating whether a section has been completed or not.
	 * 
	 * @param id wicket id
	 * @param completed true if the section has been completed, false otherwise.
	 * @return the appropriate image component for the 'completed' parameter
	 */
	public Component getCompletedImage(String id, boolean isComplete) {
		Component image = new DoneImage(id);
		image.setVisible(isComplete);
		return image;
	}
	
	@Override
	public void onDetach() {
		if (mSection != null)
			mSection.detach();
		if (mTargetUser != null)
			mTargetUser.detach();
		super.onDetach();
	}
	
	public void onSectionCompleteChange(AjaxRequestTarget target, String location) {
		if (location.equals(getLocation()))
			target.addComponent(this);
	}
	
	protected String getLocation() {
		return sectionContentLocation.getLocation();
	}

	protected User getUser() {
		return mTargetUser.getObject();
	}

	protected boolean isComplete() {
		SectionStatus status = sectionService.getSectionStatus(getUser(), sectionContentLocation);			
		if (status == null)
			return false;
		return status.getCompleted();
	}

	protected abstract void addSectionToggleButton();

	protected abstract String getInstructions();

	protected abstract boolean showCompletedImage();
	
	protected String getStringResource(String key, String defaultValue) {
		return new ResourceModel(key, defaultValue).getObject();
	} 

	private static ContentLoc getContentLoc(ISIXmlSection xmlSection) {
		return xmlSection.getContentLoc();
	}

	private ISIXmlSection getISIXmlSection() {
		return (ISIXmlSection) mSection.getObject();
	}
	
	private ISIXmlSection getSectionAncestor(ISIXmlSection isiXmlSection) {
		return isiXmlSection.getSectionAncestor();
	}

}