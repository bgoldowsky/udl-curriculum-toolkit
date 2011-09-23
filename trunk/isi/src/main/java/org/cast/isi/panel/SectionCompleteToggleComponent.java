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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.User;
import org.cast.cwm.indira.IndiraImage;
import org.cast.cwm.indira.IndiraImageComponent;
import org.cast.cwm.xml.XmlSection;
import org.cast.isi.ISISession;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.service.SectionService;

public class SectionCompleteToggleComponent extends AjaxLink<XmlSection> {
	
	@Getter @Setter protected String location;
	@Getter @Setter private User targetPerson;
	
//	private static String BUSY_ICON = "/img/icons/busy.gif";
//	private static String busyUrl;
//	private static int busyHeight, busyWidth;
	
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param id wicket id
	 * @param location the xmlSection to be checked/toggled
	 */
	public SectionCompleteToggleComponent(String id, IModel<XmlSection> model) {
		super(id);
		setOutputMarkupId(true);
		this.location = new ContentLoc(model.getObject()).getLocation();
		
//		if (busyUrl == null) {
//			busyUrl = FileResourceManager.get().getUrl(BUSY_ICON);
//			busyWidth = IndiraImage.get(BUSY_ICON).getWidth();
//			busyHeight = IndiraImage.get(BUSY_ICON).getHeight();
//		}
		
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
//		ISIApplication.get().getSectionService().setCompleted(getUser(), new ContentLoc(location), !isComplete());
		SectionService.get().setCompleted(getUser(), new ContentLoc(location), !isComplete());
		if (target != null) {
			getPage().visitChildren(SectionCompleteToggleComponent.class, new IVisitor<SectionCompleteToggleComponent>() {
				public Object component(SectionCompleteToggleComponent component) {
					if (getLocation().equals(component.getLocation()))
						target.addComponent(component);
					return CONTINUE_TRAVERSAL;
				}
			});
		}
	}

//	@Override
//	/**
//	 * Call Decorator that switches the icon to the animated busy icon, and disables the ajax link.
//	 * Similar in function to BlockingAjaxCallDecorator, but the jQuery blockUI plugin seems to
//	 * have trouble with positioning itself properly in inline contexts like this, and anyway
//	 * is probably overkill in this simple situation of changing one link with one icon.
//	 */
//	protected IAjaxCallDecorator getAjaxCallDecorator() {
//		return new IAjaxCallDecorator() {
//
//			private static final long serialVersionUID = 1L;
//
//			public CharSequence decorateOnFailureScript(CharSequence script) {
//				return "alert(\"Update failed\");";
//			}
//
//			public CharSequence decorateOnSuccessScript(CharSequence script) {
//				return null;
//			}
//
//			public CharSequence decorateScript(CharSequence script) {
//				String id = get("img").getMarkupId();
//				return String.format("$(\"#%s\").attr(\"src\", \"%s\").attr(\"height\",\"%d\").attr(\"width\",\"%d\");"
//									+ "$(this).attr(\"onclick\",\"return false;\");%s", 
//									id, busyUrl, busyHeight, busyWidth, script);
//			}
//			
//		};
//	}
//	
	public boolean isComplete() {
//		Boolean isComplete = ISIApplication.get().getSectionService().getSectionStatusMap(getUser()).get(location);			
		Boolean isComplete = SectionService.get().getSectionStatusMap(getUser()).get(location);			
		if (isComplete == null)
			isComplete = false;
		return isComplete;
	}
	
	public User getUser() {
			return ISISession.get().getTargetUserModel().getObject();
	}
}
