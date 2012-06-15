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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.junit.Test;

public class StudentSectionCompleteToggleTextLinkTest extends
		SectionCompleteToggleTextLinkTestCase {

	@Test
	public void canRender() {
		sectionStatus.setCompleted(false);
		startWicket();
		wicketTester.assertComponent("panel:component", StudentSectionCompleteToggleTextLink.class);
	}
	
	@Test
	public void hasCorrectTextWhenSectionIncomplete() {
		sectionStatus.setCompleted(false);
		startWicket();
		Label text = (Label) wicketTester.getComponentFromLastRenderedPage("panel:component:text");
		assertThat((String) text.getDefaultModelObject(), equalTo("Mark Section Complete"));
	}

	@Test
	public void hasCorrectTextWhenSectionCompleteAndNotLocked() {
		sectionStatus.setCompleted(true);
		sectionStatus.setLocked(false);
		startWicket();
		Label text = (Label) wicketTester.getComponentFromLastRenderedPage("panel:component:text");
		assertThat((String) text.getDefaultModelObject(), equalTo("Mark Section Incomplete"));
	}

	@Test
	public void hasCorrectTextWhenSectionCompleteAndLocked() {
		sectionStatus.setCompleted(true);
		sectionStatus.setLocked(true);
		startWicket();
		Label text = (Label) wicketTester.getComponentFromLastRenderedPage("panel:component:text");
		assertThat((String) text.getDefaultModelObject(), equalTo("Section is Locked"));
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
	public void componentIsDisabledWhenSectionLocked() {
		sectionStatus.setCompleted(true);
		sectionStatus.setLocked(true);
		startWicket();
		Component component = wicketTester.getComponentFromLastRenderedPage("panel:component");
		assertThat("component should be disabled", component.isEnabled(), equalTo(false));
	}
	
	@Test
	public void componentIsHiddenUnlessOnLastPageInSection() {
		when(pageXmlSection.isLastPageInSection()).thenReturn(false);
		startWicket();
		wicketTester.assertInvisible("panel:component");
	}

	@Test
	public void componentIsNotHiddenWhenOnLastPageInSection() {
		when(pageXmlSection.isLastPageInSection()).thenReturn(true);
		startWicket();
		wicketTester.assertVisible("panel:component");
	}

	@Override
	protected Component newTestComponent() {
		return new StudentSectionCompleteToggleTextLink("component", pageSectionXmlModel, studentModel);
	}

}
