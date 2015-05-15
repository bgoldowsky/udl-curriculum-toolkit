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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.ITestPanelSource;
import org.cast.cwm.components.Icon;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.Response;
import org.cast.cwm.data.User;
import org.cast.cwm.test.CwmWicketTester;
import org.cast.cwm.test.GuiceInjectedTestApplication;
import org.cast.isi.data.ISIPrompt;
import org.cast.isi.data.PromptType;
import org.junit.Before;
import org.junit.Test;

public class StudentScorePanelTest {

	private CwmWicketTester wicketTester;
	private Response response1;
	private Response response2;
	private ISIPrompt prompt;
	private IModel<User> userModel;
	private Map<Class<? extends Object>, Object> injectionMap;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Before
	public void setUp() {
		prompt = mock(ISIPrompt.class);
		when(prompt.getType()).thenReturn(PromptType.RESPONSEAREA);
		response1 = makeResponse(prompt);
		response2 = makeResponse(prompt);
		userModel = new Model(makeUser() );
		setupInjectedServices();
		wicketTester = new CwmWicketTester(new GuiceInjectedTestApplication(injectionMap));
	}

//	@Test
//	public void canRenderPanel() {
//		wicketTester.startPanel(new TestPanelSource());
//		wicketTester.assertComponent("panel", StudentScorePanel.class);
//	}
//	
//	@Test
//	public void panelHasGotItButtonVisibleIfScoredCorrect() {
//		setResponseScores(1);
//		wicketTester.startPanel(new TestPanelSource());
//		wicketTester.assertComponent("panel:gotItButton", WebMarkupContainer.class);
//		wicketTester.assertVisible("panel:gotItButton");
//	}
//	
//	@Test
//	public void gotItButtonHasIcon() {
//		setResponseScores(1);
//		wicketTester.startPanel(new TestPanelSource());
//		wicketTester.assertComponent("panel:gotItButton:icon", Icon.class);
//	}
//	
//	@Test
//	public void gotItButtonIsHiddenIfScoredIncorrect() {
//		setResponseScores(0);
//		wicketTester.startPanel(new TestPanelSource());
//		wicketTester.assertInvisible("panel:gotItButton");
//	}
//	
//	@Test
//	public void gotItButtonIsHiddenIfUnscored() {
//		setResponseScores(null);
//		wicketTester.startPanel(new TestPanelSource());
//		wicketTester.assertInvisible("panel:gotItButton");
//	}
//	
//	@Test
//	public void panelHasNotGotItButtonVisibleIfScoredIncorrect() {
//		setResponseScores(0);
//		wicketTester.startPanel(new TestPanelSource());
//		wicketTester.assertComponent("panel:notGotItButton", WebMarkupContainer.class);
//		wicketTester.assertVisible("panel:notGotItButton");
//	}
//	
//	@Test
//	public void notGotItButtonHasIcon() {
//		setResponseScores(0);
//		wicketTester.startPanel(new TestPanelSource());
//		wicketTester.assertComponent("panel:notGotItButton:icon", Icon.class);
//	}
//	
//	@Test
//	public void notGotItButtonIsHiddenIfScoredCorrect() {
//		setResponseScores(1);
//		wicketTester.startPanel(new TestPanelSource());
//		wicketTester.assertInvisible("panel:notGotItButton");
//	}
//	
//	@Test
//	public void notGotItButtonIsHiddenIfUnscored() {
//		setResponseScores(null);
//		wicketTester.startPanel(new TestPanelSource());
//		wicketTester.assertInvisible("panel:notGotItButton");
//	}
	
	private void setupInjectedServices() {
		// Only so that injection doesn't blow up in test.
		injectionMap = new HashMap<Class<? extends Object>, Object>();
	}

	private class TestPanelSource implements ITestPanelSource {
		private static final long serialVersionUID = 1L;

		public Panel getTestPanel(String panelId) {
			return new StudentScorePanel(panelId, makeResponseModelList(), userModel); 
		}
	}

	private User makeUser() {
		User user = new User();
		user.setFirstName("Mickey");
		user.setLastName("Mouse");
		return user;
	}
	
	private Response makeResponse(Prompt prompt) {
		Response response = new Response();
		response.setPrompt(prompt);
		return response;
	}
	
	private void setResponseScores(Integer score) {
		response1.setScore(score);
		response2.setScore(score);
	}
	
	private List<IModel<Response>> makeResponseModelList() {
		List<IModel<Response>> list = new ArrayList<IModel<Response>>();
		list.add(new Model<Response>(response1));
		list.add( new Model<Response>(response2));
		return list;
	}

}
