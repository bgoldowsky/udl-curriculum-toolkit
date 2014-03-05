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

import static org.cast.cwm.test.CwmMatchers.modelOf;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.apache.wicket.model.Model;
import org.cast.cwm.data.Period;
import org.cast.cwm.service.ICwmSessionService;
import org.cast.cwm.test.CwmWicketTester;
import org.cast.cwm.test.GuiceInjectedCwmTestApplication;
import org.cast.isi.data.ClassMessage;
import org.cast.isi.panel.ClassMessagePanel.MessageViewer;
import org.cast.isi.service.IISIResponseService;
import org.junit.Before;
import org.junit.Test;

public class ClassMessagePanelTest {
	
	private CwmWicketTester tester;
	private HashMap<Class<? extends Object>, Object> injectionMap;
	protected ICwmSessionService sessionService;
	protected IISIResponseService responseService;
	protected Period period;
	protected ClassMessage classMessage;


	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Before
	public void setUp() {
		setupInjectedServices();
		setUpPeriod();
		setUpClassMessage();
		tester = new CwmWicketTester(new GuiceInjectedCwmTestApplication(injectionMap));
	}

	public void setUpPeriod() {
		period = new Period();
		period.setName("Test Period");
		stubPeriod(period);
	}

	public void setUpClassMessage() {
		classMessage = new ClassMessage();
		classMessage.setMessage("This is the Class Message");
		stubClassMessage(period);
	}

	private void setupInjectedServices() {
		injectionMap = new HashMap<Class<? extends Object>, Object>();

		sessionService = mock(ICwmSessionService.class);
		injectionMap.put(ICwmSessionService.class,  sessionService);
		
		responseService = mock(IISIResponseService.class);
		injectionMap.put(IISIResponseService.class,  responseService);
	}

	@Test
	public void canRender() {
		tester.startComponentInPage(new ClassMessagePanel("component"));
		tester.assertComponent("component", ClassMessagePanel.class);
	}

	@Test
	public void canRenderMessageViewer() {
		tester.startComponentInPage(new ClassMessagePanel("component"));
		tester.assertComponent("component:messageViewer", MessageViewer.class);
	}

	@Test
	public void canRenderMessageForm() {
		tester.startComponentInPage(new ClassMessagePanel("component"));
		tester.assertInvisible("component:messageForm");
	}

	protected void stubPeriod(Period period) {
		when(sessionService.getCurrentPeriodModel()).thenReturn(Model.of(period));
	}
	
	protected void stubClassMessage(Period period) {
		when(responseService.getClassMessage(argThat(modelOf(period)))).thenReturn(classMessage);
	}
}