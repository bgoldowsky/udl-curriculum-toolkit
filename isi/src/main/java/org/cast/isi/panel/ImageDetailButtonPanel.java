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
package org.cast.isi.panel;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.cast.cwm.components.Icon;
import org.cast.cwm.service.IEventService;
import org.cast.isi.page.ISIBasePage;

import com.google.inject.Inject;

/**
 * Displays a magnifier button when a larger image is present for a thumbnail. Creates js
 * for image detail modal to display.
 * 
 * @author lynnmccormack
 *
 */
public class ImageDetailButtonPanel extends ISIPanel{

	private static final long serialVersionUID = 1L;

	@Inject
	private IEventService eventService;

	public ImageDetailButtonPanel(String panelId, final String imageId) {
		super(panelId);
		
		AjaxFallbackLink<Void> link = new AjaxFallbackLink<Void>("link") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				eventService.saveEvent("image:" + "zoom:detail", "imageId=" + imageId, ((ISIBasePage) getPage()).getPageName());
			}
			
			@Override
			public IAjaxCallDecorator getAjaxCallDecorator() {
				return new IAjaxCallDecorator() {

					private static final long serialVersionUID = 1L;

                    public CharSequence decorateScript(Component component, CharSequence script) {
                        return "showImageDetail('" + imageId + "', true); " + script;
                    }

                    public CharSequence decorateOnSuccessScript(Component component, CharSequence script) {
                        return script;
                    }

                    public CharSequence decorateOnFailureScript(Component component, CharSequence script) {
                        return script;
                    }
                };
			}
		};
		
		link.add(new Icon("image", "img/icons/expand_image.png"));
		add(link);
	}

}
