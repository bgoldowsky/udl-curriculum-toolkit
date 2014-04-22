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
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.junit.Test;

public class TeacherSectionCompleteToggleTextLinkTest extends
		SectionCompleteToggleTextLinkTestCase {

	@Test
	public void canRender() {
		sectionStatus.setCompleted(false);
		startWicket();
		wicketTester.assertComponent("panel:component", TeacherSectionCompleteToggleTextLink.class);
	}
	
	@Test
	public void hasCorrectTextWhenSectionIncomplete() {
		sectionStatus.setCompleted(false);
		startWicket();
		Label text = (Label) wicketTester.getComponentFromLastRenderedPage("panel:component:text");
		assertThat((String) text.getDefaultModelObject(), equalTo("Incomplete Student Work"));
	}

	@Test
	public void hasCorrectTextWhenSectionCompleteAndNotReviewed() {
		sectionStatus.setCompleted(true);
		sectionStatus.setReviewed(false);
		startWicket();
		Label text = (Label) wicketTester.getComponentFromLastRenderedPage("panel:component:text");
		assertThat((String) text.getDefaultModelObject(), equalTo("Mark Section Reviewed"));
	}

	@Test
	public void hasCorrectTextWhenSectionCompleteAndReviewed() {
		sectionStatus.setCompleted(true);
		sectionStatus.setReviewed(true);
		startWicket();
		Label text = (Label) wicketTester.getComponentFromLastRenderedPage("panel:component:text");
		assertThat((String) text.getDefaultModelObject(), equalTo("Mark Section Not Reviewed"));
	}

	@Test
	public void clickingComponentMarksUnreviewedSectionAsReviewed() {
		sectionStatus.setCompleted(true);
		sectionStatus.setReviewed(false);
		startWicket();
		wicketTester.clickLink("panel:component");
		verify(sectionService).setReviewed(eq(student), eq(sectionContentLoc), eq(true));
	}

	@Test
	public void clickingComponentMarksReviewedSectionAsUnreviewed() {
		sectionStatus.setCompleted(true);
		sectionStatus.setReviewed(true);
		startWicket();
		wicketTester.clickLink("panel:component");
		verify(sectionService).setReviewed(eq(student), eq(sectionContentLoc), eq(false));
	}

	@Test
	public void clickingComponentMarksUnreviewedSummativeSectionAsLocked() {
		sectionStatus.setCompleted(true);
		sectionStatus.setReviewed(false);
		when(pageXmlSection.isLockResponse()).thenReturn(true);
		startWicket();
		wicketTester.clickLink("panel:component");
		verify(pageXmlSection).isLockResponse();
		verify(sectionService).setLocked(eq(student), eq(sectionContentLoc), eq(true));
	}

	@Test
	public void clickingComponentMarksReviewedSummativeSectionAsUnlocked() {
		sectionStatus.setCompleted(true);
		sectionStatus.setReviewed(true);
		when(pageXmlSection.isLockResponse()).thenReturn(true);
		startWicket();
		wicketTester.clickLink("panel:component");
		verify(pageXmlSection).isLockResponse();
		verify(sectionService).setLocked(eq(student), eq(sectionContentLoc), eq(false));
	}

	@Test
	public void clickingComponentDoesNotNotAffectLockOnUnreviewedNonSummativeSection() {
		sectionStatus.setCompleted(true);
		sectionStatus.setReviewed(false);
		when(pageXmlSection.isLockResponse()).thenReturn(false);
		startWicket();
		wicketTester.clickLink("panel:component");
		verify(sectionService).setReviewed(eq(student), eq(sectionContentLoc), eq(true));
		verify(pageXmlSection).isLockResponse();
		verify(sectionService, never()).setLocked(eq(student), eq(sectionContentLoc), anyBoolean());
	}

	@Test
	public void clickingComponentDoesNotAffectLockOnReviewedNonSummativeSection() {
		sectionStatus.setCompleted(true);
		sectionStatus.setReviewed(true);
		when(pageXmlSection.isLockResponse()).thenReturn(false);
		startWicket();
		wicketTester.clickLink("panel:component");
		verify(sectionService).setReviewed(eq(student), eq(sectionContentLoc), eq(false));
		verify(pageXmlSection).isLockResponse();
		verify(sectionService, never()).setLocked(eq(student), eq(sectionContentLoc), anyBoolean());
	}

	@Test
	public void componentIsDisabledWhenSectionIncomplete() {
		sectionStatus.setCompleted(false);
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
		return new TeacherSectionCompleteToggleTextLink("component", pageSectionXmlModel, studentModel);
	}


}
