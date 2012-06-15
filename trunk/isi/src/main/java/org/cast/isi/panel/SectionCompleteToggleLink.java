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
import org.cast.cwm.xml.XmlSection;
import org.cast.isi.ISISession;
import org.cast.isi.ISIXmlSection;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.service.ISectionService;

import com.google.inject.Inject;

public abstract class SectionCompleteToggleLink extends AjaxLink<XmlSection> implements ISectionCompleteToggleListener {

	private static final long serialVersionUID = 1L;

	@Inject
	protected ISectionService sectionService;
	protected IModel<User> targetUserModel;
	
	@Getter
	protected ContentLoc pageContentLocation;

	@Getter
	protected ContentLoc sectionContentLocation;

	/**
	 * Constructor
	 * 
	 * @param id wicket id
	 * @param model a model containing the page whose containing section is to be checked/toggled
	 * @param targetUserModel the user to be marked/unmarked as completing the section
	 */
	public SectionCompleteToggleLink(String id,
			IModel<XmlSection> model, IModel<User> targetUserModel) {
		super(id);
		this.pageContentLocation = getContentLoc(getISIXmlSection(model));
		this.sectionContentLocation = getContentLoc(getSectionAncestor(getISIXmlSection(model)));
		this.targetUserModel = targetUserModel;
		setOutputMarkupId(true);
	}

	/**
	 * Constructor
	 * 
	 * @param id wicket id
	 * @param model a model containing  the page whose containing section is to be checked/toggled
	 */
	public SectionCompleteToggleLink(String id, IModel<XmlSection> model) {
		this(id, model, ISISession.get().getTargetUserModel());
	}

	private ISIXmlSection getSectionAncestor(ISIXmlSection isiXmlSection) {
		return isiXmlSection.getSectionAncestor();
	}

	public static ISIXmlSection getISIXmlSection(IModel<XmlSection> model) {
		return (ISIXmlSection) model.getObject();
	}

	public static ContentLoc getContentLoc(ISIXmlSection xmlSection) {
		return xmlSection.getContentLoc();
	}

	@Override
	public void onClick (final AjaxRequestTarget target) {	
		handleClick();
		notifyListeners(target);
	}

	protected abstract void handleClick();

	protected User getUser() {
		return targetUserModel.getObject();
	}

	public String getLocation() {
		return sectionContentLocation.getLocation();
	}

	protected ISIXmlSection getSection() {
		return pageContentLocation.getSection();
	}

	protected abstract boolean isComplete();

	protected boolean isLastPageInSection() {
		ISIXmlSection section = getSection();
		return (section != null) && (section.isLastPageInSection());
	}

	protected boolean isLockResponse() {
		ISIXmlSection section = getSection();
		return (section != null) && (section.isLockResponse());
	}

	protected ISIXmlSection getSectionAncestorForLocation(String location) {
		ISIXmlSection section = new ContentLoc(location).getSection();
		return section.getSectionAncestor();
	}

	public ISIXmlSection getSectionAncestor() {
		return getSection().getSectionAncestor();
	}

	public void notifyListeners(final AjaxRequestTarget target) {
		final String location = getLocation();
		if ((target != null) && (location != null)) {
			getPage().visitChildren(ISectionCompleteToggleListener.class, new IVisitor<Component>() {
				public Object component(Component component) {
					ISectionCompleteToggleListener listener = (ISectionCompleteToggleListener) component;
					String listenerLocation = listener.getLocation();
					if (listenerLocation.equals(location))
						target.addComponent(component);
					return CONTINUE_TRAVERSAL;
				}

			});
		}
	}

}