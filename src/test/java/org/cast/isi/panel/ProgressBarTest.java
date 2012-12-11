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

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.tester.ITestPanelSource;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;

public class ProgressBarTest {
	
	private WicketTester wicketTester;

	@Before
	public void setUp() {
		wicketTester = new WicketTester();
	}
	
	@Test
	public void canRender() {
		wicketTester.startPanel(new TestPanelSource(0));
		wicketTester.assertComponent("panel", ProgressBar.class);
	}

	@Test
	public void progressBarHasContainer() {
		wicketTester.startPanel(new TestPanelSource(0));
		wicketTester.assertComponent("panel:container", WebMarkupContainer.class);
	}
	
	@Test
	public void containerHasStyledWidth() {
		wicketTester.startPanel(new TestPanelSource(83));
		wicketTester.assertContains("style=\"width: 83%\"");

		wicketTester.startPanel(new TestPanelSource(67));
		wicketTester.assertContains("style=\"width: 67%\"");
	}
	
	@Test
	public void progressBarShowsPercentage() {
		wicketTester.startPanel(new TestPanelSource(67));
		wicketTester.assertComponent("panel:container:percent", Label.class);
		wicketTester.assertLabel("panel:container:percent", "67%");
	}
	
	private class TestPanelSource implements ITestPanelSource {

		private static final long serialVersionUID = 1L;
		
		private int percent;

		public TestPanelSource(int percent) {
			this.percent = percent;
		}
		public Panel getTestPanel(String panelId) {
			return new ProgressBar(panelId, percent);
		}
	}

}
