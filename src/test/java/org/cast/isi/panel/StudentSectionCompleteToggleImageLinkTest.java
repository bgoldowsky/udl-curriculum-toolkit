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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import org.apache.wicket.Component;
import org.apache.wicket.util.tester.TagTester;
import org.junit.Test;

public class StudentSectionCompleteToggleImageLinkTest extends SectionCompleteToggleImageLinkTestCase {

	@Test
	public void canRender() {
		startWicket();
		wicketTester.assertComponent("panel:component", StudentSectionCompleteToggleImageLink.class);
	}
	
	@Test
	public void hasCorrectImageWhenComplete() {
		sectionStatus.setCompleted(true);
		startWicket();
		TagTester image = wicketTester.getTagByWicketId("doneImg");
		assertThat(image.getAttribute("src"), equalTo("../img/icons/check_done.png"));
	}

	@Test
	public void imageHasCorrectAltTagWhenComplete() {
		sectionStatus.setCompleted(true);
		startWicket();
		TagTester image = wicketTester.getTagByWicketId("doneImg");
		assertTrue("Should have alt=\"Finished\"", image.getAttributeIs("alt", "Finished"));
	}

	@Test
	public void imageHasCorrectTitleTagWhenComplete() {
		sectionStatus.setCompleted(true);
		startWicket();
		TagTester image = wicketTester.getTagByWicketId("doneImg");
		assertTrue("Should have title=\"Finished\"", image.getAttributeIs("title", "Finished"));
	}

	@Test
	public void clickingComponentMarksIncompleteSectionAsComplete() {
		sectionStatus.setCompleted(false);
		startWicket();
		wicketTester.clickLink("panel:component");
		verify(sectionService).setCompleted(eq(student), eq(sectionContentLoc), eq(true));
	}

	@Test
	public void clickingComponentMarksCompleteSectionAsIncomplete() {
		sectionStatus.setCompleted(true);
		startWicket();
		wicketTester.clickLink("panel:component");
		verify(sectionService).setCompleted(eq(student), eq(sectionContentLoc), eq(false));
	}
	
	@Test
	public void componentIsEnabledWhenSectionIsNotLocked() {
		sectionStatus.setLocked(false);
		startWicket();
		Component component = wicketTester.getComponentFromLastRenderedPage("panel:component");
		assertThat(component.isEnabled(), equalTo(true));
	}

	@Test
	public void componentIsDisabledWhenSectionIsLocked() {
		sectionStatus.setLocked(true);
		startWicket();
		Component component = wicketTester.getComponentFromLastRenderedPage("panel:component");
		assertThat(component.isEnabled(), equalTo(false));
	}

	protected Component newTestComponent() {
		return new StudentSectionCompleteToggleImageLink("component", pageSectionXmlModel, studentModel);
	}
	
}
