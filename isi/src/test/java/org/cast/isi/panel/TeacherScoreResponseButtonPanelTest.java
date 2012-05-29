/*
 * Copyright 2012 CAST, Inc.
 *
 * This file is part of the CAST Wicket Modules:
 * see <http://code.google.com/p/cast-wicket-modules>.
 *
 * The CAST Wicket Modules are free software: you can redistribute and/or
 * modify them under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * The CAST Wicket Modules are distributed in the hope that they will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cast.isi.panel;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.ITestPanelSource;
import org.cast.cwm.data.Response;
import org.cast.cwm.service.ICwmService;
import org.cast.cwm.test.CwmWicketTester;
import org.cast.cwm.test.GuiceInjectedTestApplication;
import org.junit.Before;
import org.junit.Test;

public class TeacherScoreResponseButtonPanelTest {

	private Map<Class<? extends Object>,Object> injectionMap;
	private ICwmService cwmService;
	private CwmWicketTester wicketTester;
	private Response response1;
	private Response response2;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Before
	public void setUp() {
		response1 = new Response();
		response2 = new Response();
		setupInjectedServices();
		wicketTester = new CwmWicketTester(new GuiceInjectedTestApplication(injectionMap));
	}
	
	@Test
	public void canRenderPanel() {
		wicketTester.startPanel(new TestPanelSource());
		wicketTester.assertComponent("panel", TeacherScoreResponseButtonPanel.class);
	}
	
	@Test
	public void panelHasGotItButton() {
		wicketTester.startPanel(new TestPanelSource());
		wicketTester.assertComponent("panel:gotItButton", AjaxLink.class);
	}
	
	@Test
	public void gotItButtonHasIcon() {
		wicketTester.startPanel(new TestPanelSource());
		wicketTester.assertComponent("panel:gotItButton:icon", Image.class);
	}
	
	@Test
	public void panelHasNotGotItButton() {
		wicketTester.startPanel(new TestPanelSource());
		wicketTester.assertComponent("panel:notGotItButton", AjaxLink.class);
	}
	
	@Test
	public void notGotItButtonHasIcon() {
		wicketTester.startPanel(new TestPanelSource());
		wicketTester.assertComponent("panel:notGotItButton:icon", Image.class);
	}
	
	@Test
	public void gotItButtonIsHighlightedIfResponsesMarkedCorrect() {
		setResponseScores(1);
		wicketTester.startPanel(new TestPanelSource());
		Component link = wicketTester.getComponentFromLastRenderedPage("panel:gotItButton");
		wicketTester.assertAttribute("Should have 'current' class attribute", "current", link, "class");
	}
	
	@Test
	public void gotItButtonIsNotHighlightedIfResponsesUnmarked() {
		setResponseScores(null);
		wicketTester.startPanel(new TestPanelSource());
		Component link = wicketTester.getComponentFromLastRenderedPage("panel:gotItButton");
		wicketTester.assertNotAttribute("Should not have 'current' class attribute", "current", link, "class");
	}
	
	@Test
	public void notGotItButtonIsHighlightedIfResponsesMarkedIncorrect() {
		setResponseScores(0);
		wicketTester.startPanel(new TestPanelSource());
		Component link = wicketTester.getComponentFromLastRenderedPage("panel:notGotItButton");
		wicketTester.assertAttribute("Should have 'current' class attribute", "current", link, "class");
	}
	
	@Test
	public void notGotItButtonIsNotHighlightedIfResponsesUnmarked() {
		setResponseScores(null);
		wicketTester.startPanel(new TestPanelSource());
		Component link = wicketTester.getComponentFromLastRenderedPage("panel:notGotItButton");
		wicketTester.assertNotAttribute("Should not have 'current' class attribute", "current", link, "class");
	}
	
	@Test
	public void gotItIconHasProperAltTextIfResponsesUnmarked() {
		setResponseScores(null);
		wicketTester.startPanel(new TestPanelSource());
		Component icon = wicketTester.getComponentFromLastRenderedPage("panel:gotItButton:icon");
		String expectedAltText = "Click to score as \"Got it!\"";
		wicketTester.assertAttribute("Should have 'alt' text \"" + expectedAltText + "\"" , expectedAltText, icon, "alt");
	}
	
	@Test
	public void gotItIconHasProperAltTextIfResponsesScoredCorrect() {
		setResponseScores(1);
		wicketTester.startPanel(new TestPanelSource());
		Component icon = wicketTester.getComponentFromLastRenderedPage("panel:gotItButton:icon");
		String expectedAltText = "Click to remove \"Got it!\" scoring.";
		wicketTester.assertAttribute("Should have 'alt' text \"" + expectedAltText + "\"" , expectedAltText, icon, "alt");
	}
	
	@Test
	public void clickingGotItSetsScoresIfNotSet() {
		setResponseScores(null);
		wicketTester.startPanel(new TestPanelSource());
		wicketTester.clickLink("panel:gotItButton");
		assertThat("Response should be scored as correct", response1.getScore(), equalTo(1));
		assertThat("Response should be scored as correct", response2.getScore(), equalTo(1));
	}
	
	@Test
	public void clickingGotItSetsScoresToCorrectIfSetToIncorrect() {
		setResponseScores(0);
		wicketTester.startPanel(new TestPanelSource());
		wicketTester.clickLink("panel:gotItButton");
		assertThat("Response should be scored as correct", response1.getScore(), equalTo(1));
		assertThat("Response should be scored as correct", response2.getScore(), equalTo(1));
	}
	
	@Test
	public void clickingGotItSavesNewlySetScores() {
		setResponseScores(null);
		wicketTester.startPanel(new TestPanelSource());
		wicketTester.clickLink("panel:gotItButton");
		verify(cwmService).flushChanges();
	}
	
	@Test
	public void clickingGotItClearsScoresIfSetCorrect() {
		setResponseScores(1);
		wicketTester.startPanel(new TestPanelSource());
		wicketTester.clickLink("panel:gotItButton");
		assertThat("Response should not be scored", response1.getScore(), nullValue());
		assertThat("Response should not be scored", response2.getScore(), nullValue());
	}
	
	@Test
	public void clickingGotItSavesNewlyClearedScores() {
		setResponseScores(null);
		wicketTester.startPanel(new TestPanelSource());
		wicketTester.clickLink("panel:gotItButton");
		verify(cwmService).flushChanges();
	}
	
	@Test
	public void clickingGotItRefreshesButtonAndSetsAttribute() {
		setResponseScores(null);
		wicketTester.startPanel(new TestPanelSource());
		wicketTester.clickLink("panel:gotItButton");
		Component link = wicketTester.getComponentFromLastRenderedPage("panel:gotItButton");
		wicketTester.assertAttribute("Should have 'current' class attribute", "current", link, "class");
	}
	
	@Test
	public void notGotItIconHasProperAltTextIfResponsesUnmarked() {
		setResponseScores(null);
		wicketTester.startPanel(new TestPanelSource());
		Component icon = wicketTester.getComponentFromLastRenderedPage("panel:notGotItButton:icon");
		String expectedAltText = "Click to score as \"Didn't get it\"";
		wicketTester.assertAttribute("Should have 'alt' text \"" + expectedAltText + "\"" , expectedAltText, icon, "alt");
	}
	
	@Test
	public void notGotItIconHasProperAltTextIfResponsesScoredIncorrect() {
		setResponseScores(0);
		wicketTester.startPanel(new TestPanelSource());
		Component icon = wicketTester.getComponentFromLastRenderedPage("panel:notGotItButton:icon");
		String expectedAltText = "Click to remove \"Didn't get it\" scoring.";
		wicketTester.assertAttribute("Should have 'alt' text \"" + expectedAltText + "\"" , expectedAltText, icon, "alt");
	}
	
	@Test
	public void clickingNotGotItSetsScoresIfNotSet() {
		setResponseScores(null);
		wicketTester.startPanel(new TestPanelSource());
		wicketTester.clickLink("panel:notGotItButton");
		assertThat("Response should be scored as incorrect", response1.getScore(), equalTo(0));
	}
	
	@Test
	public void clickingNotGotItSetsScoresToIncorrectIfSetToCorrect() {
		setResponseScores(1);
		wicketTester.startPanel(new TestPanelSource());
		wicketTester.clickLink("panel:notGotItButton");
		assertThat("Response should be scored as incorrect", response1.getScore(), equalTo(0));
	}
	
	@Test
	public void clickingGotItRefreshesButtonAndClearsAttribute() {
		setResponseScores(1);
		wicketTester.startPanel(new TestPanelSource());
		wicketTester.clickLink("panel:gotItButton");
		Component link = wicketTester.getComponentFromLastRenderedPage("panel:gotItButton");
		wicketTester.assertNotAttribute("Should not have 'current' class attribute", "current", link, "class");
	}

	@Test
	public void clickingNotGotItSavesNewlySetScores() {
		setResponseScores(null);
		wicketTester.startPanel(new TestPanelSource());
		wicketTester.clickLink("panel:notGotItButton");
		verify(cwmService).flushChanges();
	}
	
	@Test
	public void clickingNotGotItRefreshesButtonAndSetsAttribute() {
		setResponseScores(null);
		wicketTester.startPanel(new TestPanelSource());
		wicketTester.clickLink("panel:notGotItButton");
		Component link = wicketTester.getComponentFromLastRenderedPage("panel:notGotItButton");
		wicketTester.assertAttribute("Should have 'current' class attribute", "current", link, "class");
	}
	
	@Test
	public void clickingNotGotItClearsScoresIfSetIncorrect() {
		setResponseScores(0);
		wicketTester.startPanel(new TestPanelSource());
		wicketTester.clickLink("panel:notGotItButton");
		assertThat("Response should not be scored", response1.getScore(), nullValue());
		assertThat("Response should not be scored", response2.getScore(), nullValue());
	}
	
	@Test
	public void clickingNotGotItSavesNewlyClearedScore() {
		setResponseScores(null);
		wicketTester.startPanel(new TestPanelSource());
		wicketTester.clickLink("panel:notGotItButton");
		verify(cwmService).flushChanges();
	}
	
	@Test
	public void clickingNotGotItRefreshesButtonAndClearsAttribute() {
		setResponseScores(0);
		wicketTester.startPanel(new TestPanelSource());
		wicketTester.clickLink("panel:notGotItButton");
		Component link = wicketTester.getComponentFromLastRenderedPage("panel:notGotItButton");
		wicketTester.assertNotAttribute("Should not have 'current' class attribute", "current", link, "class");
	}
	
	private void setupInjectedServices() {
		cwmService = mock(ICwmService.class);
		injectionMap = new HashMap<Class<? extends Object>, Object>();
		injectionMap.put(ICwmService.class, cwmService);
	}

	private class TestPanelSource implements ITestPanelSource {
		private static final long serialVersionUID = 1L;

		public Panel getTestPanel(String panelId) {
			return new TeacherScoreResponseButtonPanel(panelId, makeResponseModelList()); 
		}
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
