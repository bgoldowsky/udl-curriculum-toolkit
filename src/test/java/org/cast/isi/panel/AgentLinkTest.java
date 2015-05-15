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

import java.util.HashMap;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.tester.WicketTester;
import org.cast.cwm.test.CwmWicketTester;
import org.cast.cwm.test.GuiceInjectedCwmTestApplication;
import org.junit.Before;
import org.junit.Test;

public class AgentLinkTest {
	
	private WicketTester tester;
	private HashMap<Class<? extends Object>, Object> injectionMap;

	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Before
	public void setUp() {
		setupInjectedServices();
		this.tester = new CwmWicketTester(new GuiceInjectedCwmTestApplication(injectionMap));
	}
	
	@Test
	public void canRender() {
		tester.startComponentInPage(new AgentLink("agentLink", "test", "123"));
		tester.assertComponent("agentLink", AgentLink.class);
	}

	@Test
	public void hasLink() {
		tester.startComponentInPage(new AgentLink("agentLink", "test", "123"));
		tester.assertComponent("agentLink:link", WebMarkupContainer.class);
	}
	
	@Test
	public void showsTitle() {
		tester.startComponentInPage(new AgentLink("agentLink", "test", "123"));
		tester.assertComponent("agentLink:link:title", Label.class);
		tester.assertLabel("agentLink:link:title", "test");
	}
	
	private void setupInjectedServices() {
		injectionMap = new HashMap<Class<? extends Object>, Object>();		
	}

}