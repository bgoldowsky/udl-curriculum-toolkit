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

import net.databinder.models.hib.HibernateObjectModel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.cast.cwm.components.ClassAttributeModifier;
import org.cast.cwm.components.Icon;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.isi.ISISession;
import org.cast.isi.service.ISIResponseService;

import java.util.HashMap;

/**
 * A simple flag icon can be toggled.
 *
 * @author jbrookover
 * 
 */
public class StudentFlagPanel extends Panel {
	
	private static final long serialVersionUID = 1L;
	private IModel<User> mUser;
	private String imagePrefix;
	private boolean isFlagged = false;

    /**
     * Creates a flag panel (use <span> tags) with the given id.
     *
     * @param id
     * @param person
     * @param flagList
     * @param imagePrefix
     */
	public StudentFlagPanel(String id, User person, HashMap<Long, Boolean> flagList, String imagePrefix) {
		super(id);
		this.mUser = new HibernateObjectModel<User>(person);
		this.imagePrefix = imagePrefix;
		if (flagList == null)
			this.setFlagged(ISIResponseService.get().isFlagged(person));
		else if (flagList.containsKey(person.getId()))
			this.setFlagged(flagList.get(person.getId()));
		else 
			this.setFlagged(false);
		
		setOutputMarkupPlaceholderTag(true);
		draw();

	}

	/**
	 * Creates a flag panel with the standard image.
	 * @param id
	 * @param person
	 * @param list
	 */
	public StudentFlagPanel(String id, User person, HashMap<Long, Boolean> list) {
		this(id, person, list, "img/icons/flag");
	}
	
	public void draw() {
		
		AjaxFallbackLink<Void> flagLink = new AjaxFallbackLink<Void>("flag-link") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {
				if (mUser != null) {
					ISIResponseService.get().toggleFlag(mUser.getObject());
					if(target != null) {
						getPage().visitChildren(StudentFlagPanel.class, new IVisitor<StudentFlagPanel, Void>() {
                            public void component(StudentFlagPanel component, IVisit<Void> visit) {
                                IModel<User> mOtherUser = component.getmUser();
                                if (mOtherUser!=null && mOtherUser.getObject()!=null && mOtherUser.getObject().equals(mUser.getObject())) {
                                    component.toggleFlag();
                                    target.add(component);
                                }
                            }
                        });
					}
				}
			}
		};
    	if (mUser == null) {
    		flagLink.add(new ClassAttributeModifier("off"));
    	}
		
    	Icon flagImage = new Icon("flag-image", 
    			new AbstractReadOnlyModel<String>() {
    		private static final long serialVersionUID = 1L;
    		@Override
    		public String getObject() {
    			if (isFlagged())
    				return (imagePrefix + "_on.png");
    			else
    				return (imagePrefix + "_off.png");
    		}
    	}, 

    	new AbstractReadOnlyModel<String>() {
    		private static final long serialVersionUID = 1L;
    		@Override
    		public String getObject() {
    			if (mUser == null || mUser.getObject() == null) {
    				return "No Student Selected";
    			} else if (isFlagged()) {
    				return mUser.getObject().getFullName() + " is flagged";
    			} else {
    				return mUser.getObject().getFullName() + " is not flagged";
    			}
    		}

    	}, null);
		
		flagLink.add(flagImage);
		add(flagLink);
	}
	
	@Override
	protected void onBeforeRender() {
		if (ISISession.get().getUser().hasRole(Role.RESEARCHER)) {
			setVisible(false);
		} 
		super.onBeforeRender();
	}

	// Cache the flag for each person so related flags do not make repeated database calls
	public void setFlagged(boolean isFlagged) {
		this.isFlagged = isFlagged;
	}

	public boolean isFlagged() {
		return isFlagged;
	}
	
	public void toggleFlag() {
		this.isFlagged = !isFlagged;
	}
	
	public IModel<User> getmUser() {
		return mUser;
	}
	
	public void setmUser(IModel<User> mUser) {
		this.mUser = mUser;
	}

}
