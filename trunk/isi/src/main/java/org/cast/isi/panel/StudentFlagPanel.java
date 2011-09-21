/*
 * Copyright 2011 CAST, Inc.
 *
 * This file is part of the CAST Wicket Modules:
 * see <http://code.google.com/p/cast-wicket-modules>.
 *
 * The CAST Wicket Modules are free software: you can redistribute and/or
 * modify them under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * The CAST Wicket Modules are distributed in the hope that they will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cast.isi.panel;

import java.util.HashMap;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.cast.cwm.components.ClassAttributeModifier;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.isi.ISISession;
import org.cast.isi.service.ISIResponseService;

/**
 * A simple flag icon can be toggled.
 *
 * @author jbrookover
 * 
 */
public class StudentFlagPanel extends Panel {
	
	private static final long serialVersionUID = 1L;
	// TODO: This should be a model!!!
	private User person;
	private String imagePrefix;
	private boolean isFlagged = false;

	/**
	 * Creates a flag panel (use <span> tags) with the given id.
	 * 
	 * @param id - the component id
	 * @param person - the person being flagged
	 * @param period - the period to be flagged
	 * 
	 */
	public StudentFlagPanel(String id, User person, HashMap<Long, Boolean> flagList, String imagePrefix) {
		super(id);
		this.person = person;
		this.imagePrefix = imagePrefix;
		if (flagList == null)
			this.setFlagged(ISIResponseService.get().isFlagged(person));
		else if (flagList.containsKey(person.getId()))
			this.setFlagged(flagList.get(person.getId()));
		else 
			this.setFlagged(false);
		
		setOutputMarkupId(true);
		draw();

	}
	
	public StudentFlagPanel(String id, User person, HashMap<Long, Boolean> list) {
		this(id, person, list, "/img/icons/flag");
	}
	
	@SuppressWarnings("unchecked")
	public void draw() {
		
		AjaxFallbackLink flagLink = new AjaxFallbackLink("flag-link") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {
				if (person != null) {
					ISIResponseService.get().toggleFlag(person);
					if(target != null) {
						getPage().visitChildren(StudentFlagPanel.class, new IVisitor<StudentFlagPanel>() {
							
							public Object component(StudentFlagPanel component) {
								User p = component.getUser();
								if (p != null && p.equals(getUser())) {
									component.toggleFlag();
									target.addComponent(component);
								}
								return CONTINUE_TRAVERSAL;
							}
							
						});
					}
					
				}
			}
		};
    	if (person == null) {
    		flagLink.add(new ClassAttributeModifier("off"));
    	}
		
		
		Image flagImage = new Image("flag-image", new AbstractReadOnlyModel<ResourceReference>() {

			private static final long serialVersionUID = 1L;
			
			@Override
			public ResourceReference getObject() {
				if (isFlagged())
					return new ResourceReference(imagePrefix + "_on.png");
				else
					return new ResourceReference(imagePrefix + "_off.png");
			}
			
		});
		
		flagImage.add(new AttributeModifier("title", true, new AbstractReadOnlyModel<String>() {

			private static final long serialVersionUID = 1L;

			@Override
			public String getObject() {
				if (person == null) {
					return "No Student Selected";
				} else if (isFlagged()) {
					return person.getFullName() + " is flagged";
				} else {
					return person.getFullName() + " is not flagged";
				}
			}
			
		}));
		flagLink.add(flagImage);
		add(flagLink);
	}
	
	@Override
	public boolean isVisible() {
		// Hide flags for researchers
		return ISISession.get().getUser().hasRole(Role.RESEARCHER) ? false : true;
	}
	
	// Cache the flag for each person so related flags do not make repeated database calls
	
	public User getUser() {
		return person;
	}

	public void setUser(User person) {
		this.person = person;
	}

	public void setFlagged(boolean isFlagged) {
		this.isFlagged = isFlagged;
	}

	public boolean isFlagged() {
		return isFlagged;
	}
	
	public void toggleFlag() {
		this.isFlagged = !isFlagged;
	}

}
