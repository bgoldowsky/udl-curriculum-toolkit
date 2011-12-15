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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.model.Model;
import org.cast.cwm.indira.IndiraImage;
import org.cast.cwm.indira.IndiraImageComponent;
import org.cast.cwm.service.EventService;
import org.cast.isi.page.ISIBasePage;

public class ImageDetailButtonPanel extends ISIPanel{

	private static final long serialVersionUID = 1L;

	public ImageDetailButtonPanel(String panelId, final String imageId, final boolean expand) {
		super(panelId);
		
		AjaxFallbackLink<Void> link = new AjaxFallbackLink<Void>("link") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				EventService.get().saveEvent("image:" + (expand ? "zoom:detail" : "detail"), imageId, ((ISIBasePage) getPage()).getPageName());
			}
			
			@Override
			public IAjaxCallDecorator getAjaxCallDecorator() {
				return new IAjaxCallDecorator() {

					private static final long serialVersionUID = 1L;

					public CharSequence decorateOnFailureScript(CharSequence script) {
						return script;
					}

					public CharSequence decorateOnSuccessScript(CharSequence script) {
						return script;
					}

					public CharSequence decorateScript(CharSequence script) {
						return "showImageDetail('" + imageId + "', true); " + script;
					}
				};
			}
		};
		
		if (expand)
			link.add(new IndiraImageComponent("image", new Model<IndiraImage>(IndiraImage.get("/img/icons/plus.png"))));
		else
			link.add(new IndiraImageComponent("image", new Model<IndiraImage>(IndiraImage.get("/img/icons/info.png"))));
		add(link);
	}

}
