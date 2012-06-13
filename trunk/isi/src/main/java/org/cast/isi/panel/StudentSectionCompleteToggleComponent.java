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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.User;
import org.cast.cwm.xml.XmlSection;
import org.cast.isi.ISISession;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.data.SectionStatus;

public class StudentSectionCompleteToggleComponent extends SectionCompleteToggleComponent implements ISectionCompleteToggleListener {
	
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param id wicket id
	 * @param model the xmlSection to be checked/toggled
	 */
	public StudentSectionCompleteToggleComponent(String id, IModel<XmlSection> model) {
		this(id, model, ISISession.get().getTargetUserModel());
	}
	
	/**
	 * Constructor
	 * 
	 * @param id wicket id
	 * @param model the xmlSection to be checked/toggled
	 * @param targetUserModel the user to be marked/unmarked as completing the section
	 */
	public StudentSectionCompleteToggleComponent(String id, IModel<XmlSection> model, IModel<User> targetUserModel) {
		this(id, new ContentLoc(model.getObject()).getLocation(), targetUserModel);
	}
	
	/**
	 * Constructor
	 * 
	 * @param id wicket id
	 * @param location a string representing the section to be checked/toggled
	 * @param targetUserModel the user to be marked/unmarked as completing the section
	 */
	public StudentSectionCompleteToggleComponent(String id, String location, IModel<User> targetUserModel) {
		super(id, location, targetUserModel);
		setOutputMarkupId(true);
	}

	public StudentSectionCompleteToggleComponent(String id,
			ContentLoc contentLoc, IModel<User> targetUserModel) {
		super(id, contentLoc, targetUserModel);
	}

	@Override
	public boolean isEnabled() {
		return !isLocked();
	}
	
	@Override
	public void onClick (final AjaxRequestTarget target) {	
		sectionService.setCompleted(getUser(), contentLoc, !isComplete());
		if (target != null) {
			getPage().visitChildren(ISectionCompleteToggleListener.class, new IVisitor<Component>() {
				public Object component(Component component) {
					ISectionCompleteToggleListener listener = (ISectionCompleteToggleListener) component;
					if (getLocation().equals(listener.getLocation()))
						target.addComponent(component);
					return CONTINUE_TRAVERSAL;
				}
			});
		}
	}

	@Override
	protected boolean isComplete() {
		SectionStatus status = sectionService.getSectionStatus(getUser(), contentLoc);			
		if (status == null)
			return false;
		return status.getCompleted();
	}
	
	protected boolean isLocked() {
		SectionStatus status = sectionService.getSectionStatus(getUser(), contentLoc);			
		if (status == null)
			return false;
		return status.getLocked();
	}
	

}
