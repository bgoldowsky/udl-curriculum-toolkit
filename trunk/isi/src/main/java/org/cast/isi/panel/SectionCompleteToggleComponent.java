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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.User;
import org.cast.cwm.indira.IndiraImage;
import org.cast.cwm.indira.IndiraImageComponent;
import org.cast.cwm.xml.XmlSection;
import org.cast.isi.ISISession;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.service.ISectionService;

import com.google.inject.Inject;

public class SectionCompleteToggleComponent extends AjaxLink<XmlSection> implements ISectionCompleteToggleListener {
	
	private static final long serialVersionUID = 1L;

	@Inject
	protected ISectionService sectionService;

	@Getter protected String location;
	
	protected IModel<User> targetUserModel;

	/**
	 * Constructor
	 * 
	 * @param id wicket id
	 * @param model the xmlSection to be checked/toggled
	 */
	public SectionCompleteToggleComponent(String id, IModel<XmlSection> model) {
		this(id, model, ISISession.get().getTargetUserModel());
	}
	
	/**
	 * Constructor
	 * 
	 * @param id wicket id
	 * @param model the xmlSection to be checked/toggled
	 * @param targetUserModel the user to be marked/unmarked as completing the section
	 */
	public SectionCompleteToggleComponent(String id, IModel<XmlSection> model, IModel<User> targetUserModel) {
		this(id, new ContentLoc(model.getObject()).getLocation(), targetUserModel);
	}
	
	/**
	 * Constructor
	 * 
	 * @param id wicket id
	 * @param location a string representing the section to be checked/toggled
	 * @param targetUserModel the user to be marked/unmarked as completing the section
	 */
	public SectionCompleteToggleComponent(String id, String location, IModel<User> targetUserModel) {
		super(id);
		setOutputMarkupId(true);
		this.location = location;
		this.targetUserModel = targetUserModel;
		
		add(new IndiraImageComponent("doneImg") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onBeforeRender() {	
				if (isComplete()) {
					setDefaultModelObject(IndiraImage.get("/img/icons/check_done.png"));
					setTitleText("Finished");
					setAltText("Finished");
				} else {
					setDefaultModelObject(IndiraImage.get("/img/icons/check_notdone.png"));
					setTitleText("Not Finished");
					setAltText("Not Finished");
				}			
				super.onBeforeRender();
			}
		});
	}
	
	@Override
	public void onClick (final AjaxRequestTarget target) {	
		sectionService.setCompleted(getUser(), new ContentLoc(location), !isComplete());
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

	public boolean isComplete() {
		Boolean isComplete = sectionService.getSectionStatusMap(getUser()).get(location);			
		if (isComplete == null)
			isComplete = false;
		return isComplete;
	}
	
	public User getUser() {
			return targetUserModel.getObject();
	}
}
