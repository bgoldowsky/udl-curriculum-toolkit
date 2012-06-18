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
package org.cast.isi.component;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.ITestPanelSource;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.Response;
import org.cast.cwm.data.User;
import org.cast.cwm.test.CwmWicketTester;
import org.cast.cwm.test.GuiceInjectedTestApplication;
import org.cast.isi.data.ISIResponse;
import org.cast.isi.service.IISIResponseService;
import org.cast.isi.service.ISectionService;
import org.junit.Before;
import org.junit.Test;

public class ImmediateFeedbackSingleSelectFormTest {

	private Map<Class<? extends Object>,Object> injectionMap;
	private ISectionService sectionService;
	private CwmWicketTester wicketTester;
	private IModel<User> userModel;
	private IModel<User> targetUserModel;
	private IISIResponseService responseService;
	private IModel<Prompt> promptModel;
	private IModel<Response> responseModel;
	private ISIResponse response;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Before
	public void setUp() {

		promptModel = mock(IModel.class);
		response = mock(ISIResponse.class);
		responseModel = new Model(response);
		userModel = new Model(new User());
		targetUserModel = new Model(new User());
		setupInjectedServices();
		stub(responseService.getResponseForPrompt(promptModel, targetUserModel)).toReturn(responseModel);
		wicketTester = new CwmWicketTester(new GuiceInjectedTestApplication(injectionMap));
	}
	
	@Test
	public void canRender() {
		wicketTester.startPanel(new TestPanelSource(new ImmediateFeedbackSingleSelectForm("component", promptModel, userModel, targetUserModel)));
		wicketTester.assertComponent("panel:component", ImmediateFeedbackSingleSelectForm.class);
	}
	
	private void setupInjectedServices() {
		sectionService = mock(ISectionService.class);
		responseService = mock(IISIResponseService.class);
		injectionMap = new HashMap<Class<? extends Object>, Object>();
		injectionMap.put(ISectionService.class, sectionService);
		injectionMap.put(IISIResponseService.class, responseService);
	}

	private class TestPanelSource implements ITestPanelSource {

		private static final long serialVersionUID = 1L;
		private Component component;

		public TestPanelSource(Component component) {
			super();
			this.component = component;
		}

		public Panel getTestPanel(String panelId) {
			return new ComponentTestPanel(panelId, component);
		}
	}

	public class ComponentTestPanel extends Panel {

		private static final long serialVersionUID = 1L;

		public ComponentTestPanel(String id, Component component) {
			super(id);
			add(component);
		}

	}

}
