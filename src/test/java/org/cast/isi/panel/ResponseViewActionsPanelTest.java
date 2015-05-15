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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.ITestPanelSource;
import org.cast.cwm.data.Response;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.cwm.service.ICwmService;
import org.cast.cwm.test.CwmWicketTester;
import org.cast.cwm.test.GuiceInjectedTestApplication;
import org.cast.isi.data.ISIResponse;
import org.cast.isi.page.Notebook;
import org.cast.isi.page.Whiteboard;
import org.cast.isi.service.IFeatureService;
import org.cast.isi.service.IISIResponseService;
import org.cast.isi.service.ILinkPropertiesService;
import org.cast.isi.service.IPageClassService;
import org.junit.Before;
import org.junit.Test;

public class ResponseViewActionsPanelTest {
	
	private ISIResponse response;
	private ICwmService cwmService;
	private IPageClassService pageClassService;
	private HashMap<Class<? extends Object>, Object> injectionMap;
	private CwmWicketTester wicketTester;
	private IFeatureService featureService;
	private ILinkPropertiesService linkPropertiesService;
	private IISIResponseService responseService;
	private User user;
	private IModel<User> userModel;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Before
	public void setUp() {
		response = new ISIResponse();
		user = new User();
		user.setFirstName("Mickey");
		user.setLastName("Mouse");
		user.setRole(Role.STUDENT);
		response.setUser(user);
		userModel = new Model(user);
		setupInjectedServices();
		wicketTester = new CwmWicketTester(new GuiceInjectedTestApplication(injectionMap));
	}

//	@Test
//	public void canRender() {
//		wicketTester.startPanel(new TestPanelSource());
//		wicketTester.assertComponent("panel", ResponseViewActionsPanel.class);
//	}
//	
//	@Test
//	public void hasVisibleViewOnWhiteboardLinkWhenEnabledAndOnWhiteboard() {
//		when(featureService.isWhiteboardOn()).thenReturn(true);
//		response.setInWhiteboard(true);
//		wicketTester.startPanel(new TestPanelSource());
//		wicketTester.assertComponent("panel:whiteboardLink", BookmarkablePageLink.class);
//		wicketTester.assertVisible("panel:whiteboardLink");
//	}
//	
//	@Test
//	public void viewOnWhiteboardLinkIsHiddenWhenResponseNotOnWhiteboard() {
//		when(featureService.isWhiteboardOn()).thenReturn(true);
//		response.setInWhiteboard(false);
//		wicketTester.startPanel(new TestPanelSource());
//		wicketTester.assertInvisible("panel:whiteboardLink");
//	}
//	
//	@Test
//	public void viewOnWhiteboardLinkIsHiddenWhenWhiteboardIsDisabled() {
//		when(featureService.isWhiteboardOn()).thenReturn(false);
//		response.setInWhiteboard(true);
//		wicketTester.startPanel(new TestPanelSource());
//		wicketTester.assertInvisible("panel:whiteboardLink");
//	}
//	
//	@Test
//	public void hasVisibleAddToWhiteboardLinkWhenEnabledAndNotOnWhiteboard() {
//		when(featureService.isWhiteboardOn()).thenReturn(true);
//		response.setInWhiteboard(false);
//		wicketTester.startPanel(new TestPanelSource());
//		wicketTester.assertComponent("panel:addToWhiteboardLink", AjaxLink.class);
//		wicketTester.assertVisible("panel:addToWhiteboardLink");
//	}
//	
//	@Test
//	public void addToWhiteboardLinkIsHiddenWhenResponseAlreadyOnWhiteboard() {
//		when(featureService.isWhiteboardOn()).thenReturn(true);
//		response.setInWhiteboard(true);
//		wicketTester.startPanel(new TestPanelSource());
//		wicketTester.assertInvisible("panel:addToWhiteboardLink");
//	}
//	
//	@Test
//	public void addToWhiteboardLinkIsHiddenWhenWhiteboardIsDisabled() {
//		when(featureService.isWhiteboardOn()).thenReturn(false);
//		response.setInWhiteboard(false);
//		wicketTester.startPanel(new TestPanelSource());
//		wicketTester.assertInvisible("panel:whiteboardLink");
//	}
//	
//	@Test
//	public void hasVisibleViewOnNotebookLinkWhenEnabledAndOnNotebook() {
//		when(featureService.isNotebookOn()).thenReturn(true);
//		response.setInNotebook(true);
//		wicketTester.startPanel(new TestPanelSource());
//		wicketTester.assertComponent("panel:notebookLink", BookmarkablePageLink.class);
//		wicketTester.assertVisible("panel:notebookLink");
//	}
//	
//	@Test
//	public void viewOnNotebookLinkIsHiddenWhenResponseNotOnNotebook() {
//		when(featureService.isNotebookOn()).thenReturn(true);
//		response.setInNotebook(false);
//		wicketTester.startPanel(new TestPanelSource());
//		wicketTester.assertInvisible("panel:notebookLink");
//	}
//	
//	@Test
//	public void viewOnNotebookLinkIsHiddenWhenNotebookIsDisabled() {
//		when(featureService.isNotebookOn()).thenReturn(false);
//		response.setInNotebook(true);
//		wicketTester.startPanel(new TestPanelSource());
//		wicketTester.assertInvisible("panel:notebookLink");
//	}
//	
//	@Test
//	public void hasVisibleAddToNotebookLinkWhenEnabledAndNotOnNotebook() {
//		when(featureService.isNotebookOn()).thenReturn(true);
//		response.setInNotebook(false);
//		wicketTester.startPanel(new TestPanelSource());
//		wicketTester.assertComponent("panel:addToNotebookLink", AjaxLink.class);
//		wicketTester.assertVisible("panel:addToNotebookLink");
//	}
//	
//	@Test
//	public void addToNotebookLinkIsHiddenWhenResponseAlreadyOnNotebook() {
//		when(featureService.isNotebookOn()).thenReturn(true);
//		response.setInNotebook(true);
//		wicketTester.startPanel(new TestPanelSource());
//		wicketTester.assertInvisible("panel:addToNotebookLink");
//	}
//	
//	@Test
//	public void addToNotebookLinkIsHiddenWhenNotebookIsDisabled() {
//		when(featureService.isNotebookOn()).thenReturn(false);
//		response.setInNotebook(false);
//		wicketTester.startPanel(new TestPanelSource());
//		wicketTester.assertInvisible("panel:notebookLink");
//	}
//	
//	@Test
//	public void clickingAddToWhiteboardLinkAddsToWhiteboard() {
//		when(featureService.isWhiteboardOn()).thenReturn(true);
//		response.setInWhiteboard(false);
//		wicketTester.startPanel(new TestPanelSource());
//		wicketTester.clickLink("panel:addToWhiteboardLink");
//		verify(responseService).addToWhiteboard(eq(response), any(Page.class));
//	}
//	
//	@Test
//	public void clickingAddToNotebookLinkAddsToNotebook() {
//		when(featureService.isNotebookOn()).thenReturn(true);
//		response.setInNotebook(false);
//		wicketTester.startPanel(new TestPanelSource());
//		wicketTester.clickLink("panel:addToNotebookLink");
//		verify(responseService).addToNotebook(eq(response), any(Page.class));
//	}
	
	private void setupInjectedServices() {
		cwmService = mock(ICwmService.class);
		responseService = mock(IISIResponseService.class);
		pageClassService = mock(IPageClassService.class);
		doReturn(Whiteboard.class).when(pageClassService).getWhiteboardPageClass();
		doReturn(Notebook.class).when(pageClassService).getNotebookPageClass();
		featureService = mock(IFeatureService.class);
		when(featureService.isWhiteboardOn()).thenReturn(true);
		linkPropertiesService = mock(ILinkPropertiesService.class);
		injectionMap = new HashMap<Class<? extends Object>, Object>();
		injectionMap.put(ICwmService.class, cwmService);
		injectionMap.put(IISIResponseService.class, responseService);
		injectionMap.put(IPageClassService.class, pageClassService);
		injectionMap.put(IFeatureService.class, featureService);
		injectionMap.put(ILinkPropertiesService.class, linkPropertiesService);
	}

	private class TestPanelSource implements ITestPanelSource {
		private static final long serialVersionUID = 1L;

		public Panel getTestPanel(String panelId) {
			ResponseViewActionsPanel panel = new ResponseViewActionsPanel(panelId, new Model<Response>(response), userModel, userModel);
//			panel.setAllowNotebook(true);
//			panel.setAllowWhiteboard(true);
			return panel;
		}
	}

}
