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

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.tester.ITestPanelSource;
import org.apache.wicket.util.tester.WicketTester;
import org.cast.isi.data.ScoreCounts;
import org.junit.Before;
import org.junit.Test;

public class ResponseCollectionSummaryTest {
	
	private static final int TOTAL_COUNT = 6;
	private static final int UNSCORED_COUNT = 3;
	private static final int INCORRECT_COUNT = 1;
	private static final int CORRECT_COUNT = 2;
	private WicketTester wicketTester;
	private ScoreCounts counts;

	@Before
	public void setUp() {
		wicketTester = new WicketTester();
		counts = new ScoreCounts("questions", CORRECT_COUNT, INCORRECT_COUNT, UNSCORED_COUNT, TOTAL_COUNT);
		wicketTester.startPanel(new TestPanelSource());
	}
	
//	@Test
//	public void canRender() {
//		wicketTester.assertComponent("panel", ResponseCollectionSummary.class);
//	}
//	
//	@Test
//	public void showsCorrectCount() {
//		wicketTester.assertComponent("panel:correctCount", Label.class);
//		wicketTester.assertLabel("panel:correctCount", Integer.toString(CORRECT_COUNT));
//	}
//	
//	@Test
//	public void showsIncorrectCount() {
//		wicketTester.assertComponent("panel:incorrectCount", Label.class);
//		wicketTester.assertLabel("panel:incorrectCount", Integer.toString(INCORRECT_COUNT));
//	}
//	
//	@Test
//	public void showsSummaryLine() {
//		wicketTester.assertComponent("panel:summary", Label.class);
//		wicketTester.assertLabel("panel:summary", counts.formatSummary());
//	}
//	
//	@Test
//	public void showsProgressBar() {
//		wicketTester.assertComponent("panel:bar", ProgressBar.class);
//	}
	
	private class TestPanelSource implements ITestPanelSource {
		private static final long serialVersionUID = 1L;

		public Panel getTestPanel(String panelId) {
			return new ResponseCollectionSummary(panelId, counts);
		}
	}

}
