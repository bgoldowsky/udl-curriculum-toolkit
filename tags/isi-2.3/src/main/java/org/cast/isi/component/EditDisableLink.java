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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

/**
 * A simple link that can be searched for (and disabled).  Also adds
 * a class attribute if disabled.
 */
public abstract class EditDisableLink<T> extends AjaxFallbackLink<T> {

	private static final long serialVersionUID = 1L;

	public EditDisableLink(String id) {
		super(id);
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		tag.put("class", "button" + (isEnabled() ? "" : " off"));
	}
	
	/**
	 * Get a visitor that will visit all {@link EditDisableLink} instances and enable or disable them.
	 * 
	 * @param target
	 * @param enable
	 * @return
	 */
	public static IVisitor<EditDisableLink<?>, Void> getVisitor(final AjaxRequestTarget target, final boolean enable) {

		return new IVisitor<EditDisableLink<?>, Void>() {

            public void component(EditDisableLink<?> link, IVisit<Void> visit) {
                link.setEnabled(enable);
                target.add(link);
                visit.dontGoDeeper();
            }
        };
	}
}