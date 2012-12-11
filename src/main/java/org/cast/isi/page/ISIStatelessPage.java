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
package org.cast.isi.page;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;

/**
 * Base page for ISI pages that should be stateless.
 * The login page, for example, should not be tied to a session that can expire and cause logins to fail.
 * 
 * @author bgoldowsky
 *
 */
@Slf4j
public class ISIStatelessPage extends ISIPage {

	public ISIStatelessPage(PageParameters param) {
		super(param);
		setStatelessHint(true);
	}

	@Override
	protected void onBeforeRender() {
		super.onBeforeRender();
		this.checkIfPageStateless(this);
	}

	private void checkIfPageStateless(Page p) {
		if (!p.isPageStateless()) {
			// find out why
			final List<Component> statefulComponents = new ArrayList<Component>();
			p.visitChildren(Component.class, new IVisitor<Component>() {
				public Object component(Component component) {
					if (!component.isStateless())
						statefulComponents.add(component);
					return CONTINUE_TRAVERSAL;
				}
			});
 
			String message = "Whoops! this page is no longer stateless";
			if (statefulComponents.size() > 0) {
				message += " - the reason is that it contains the following stateful components: ";
				for (Component c : statefulComponents) {
					message += "\n" + c.getMarkupId();
				}
			}
			log.warn(message);
		}

	}
	
}
