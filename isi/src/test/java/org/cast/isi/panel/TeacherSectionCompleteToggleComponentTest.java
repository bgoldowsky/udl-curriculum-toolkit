package org.cast.isi.panel;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.image.Image;
import org.junit.Test;

public class TeacherSectionCompleteToggleComponentTest extends
		SectionCompleteToggleComponentTestCase {

	@Test
	public void canRender() {
		startWicket();
		wicketTester.assertComponent("panel:component", TeacherSectionCompleteToggleComponent.class);
	}
	
	@Test
	public void hasCorrectImageWhenUnreviewed() {
		sectionStatus.setCompleted(true);
		sectionStatus.setReviewed(false);
		startWicket();
		Image image = (Image) wicketTester.getComponentFromLastRenderedPage("panel:component:doneImg");
		assertThat((String) image.getDefaultModelObject(), equalTo("/img/icons/check_notdone.png"));
	}

	@Test
	public void imageHasCorrectAltTagWhenUnreviewed() {
		sectionStatus.setCompleted(true);
		sectionStatus.setReviewed(false);
		startWicket();
		Image image = (Image) wicketTester.getComponentFromLastRenderedPage("panel:component:doneImg");
		wicketTester.assertAttribute("Should have alt=\"Not Finished\"", "Not Finished", image, "alt");
	}

	@Test
	public void imageHasCorrectTitleTagWhenUnreviewed() {
		sectionStatus.setCompleted(true);
		sectionStatus.setReviewed(false);
		startWicket();
		Image image = (Image) wicketTester.getComponentFromLastRenderedPage("panel:component:doneImg");
		wicketTester.assertAttribute("Should have title=\"Not Finished\"", "Not Finished", image, "title");
	}

	@Test
	public void hasCorrectImageWhenReviewed() {
		sectionStatus.setCompleted(true);
		sectionStatus.setReviewed(true);
		startWicket();
		Image image = (Image) wicketTester.getComponentFromLastRenderedPage("panel:component:doneImg");
		assertThat((String) image.getDefaultModelObject(), equalTo("/img/icons/check_done.png"));
	}

	@Test
	public void imageHasCorrectAltTagWhenReviewed() {
		sectionStatus.setCompleted(true);
		sectionStatus.setReviewed(true);
		startWicket();
		Image image = (Image) wicketTester.getComponentFromLastRenderedPage("panel:component:doneImg");
		wicketTester.assertAttribute("Should have alt=\"Finished\"", "Finished", image, "alt");
	}

	@Test
	public void imageHasCorrectTitleTagWhenReviewed() {
		sectionStatus.setCompleted(true);
		sectionStatus.setReviewed(true);
		startWicket();
		Image image = (Image) wicketTester.getComponentFromLastRenderedPage("panel:component:doneImg");
		wicketTester.assertAttribute("Should have title=\"Finished\"", "Finished", image, "title");
	}

	@Test
	public void clickingComponentMarksUnreviewedSectionAsReviewed() {
		sectionStatus.setCompleted(true);
		sectionStatus.setReviewed(false);
		startWicket();
		wicketTester.clickLink("panel:component");
		verify(sectionService).setReviewed(eq(student), eq(contentLoc), eq(true));
	}

	@Test
	public void clickingComponentMarksReviewedSectionAsUnreviewed() {
		sectionStatus.setCompleted(true);
		sectionStatus.setReviewed(true);
		startWicket();
		wicketTester.clickLink("panel:component");
		verify(sectionService).setReviewed(eq(student), eq(contentLoc), eq(false));
	}

	@Test
	public void clickingComponentMarksUnreviewedSummativeSectionAsLocked() {
		sectionStatus.setCompleted(true);
		sectionStatus.setReviewed(false);
		when(section.isLockResponse()).thenReturn(true);
		startWicket();
		wicketTester.clickLink("panel:component");
		verify(section).isLockResponse();
		verify(sectionService).setLocked(eq(student), eq(contentLoc), eq(true));
	}

	@Test
	public void clickingComponentMarksReviewedSummativeSectionAsUnlocked() {
		sectionStatus.setCompleted(true);
		sectionStatus.setReviewed(true);
		when(section.isLockResponse()).thenReturn(true);
		startWicket();
		wicketTester.clickLink("panel:component");
		verify(section).isLockResponse();
		verify(sectionService).setLocked(eq(student), eq(contentLoc), eq(false));
	}

	@Test
	public void clickingComponentDoesNotNotAffectLockOnUnreviewedNonSummativeSection() {
		sectionStatus.setCompleted(true);
		sectionStatus.setReviewed(false);
		when(section.isLockResponse()).thenReturn(false);
		startWicket();
		wicketTester.clickLink("panel:component");
		verify(sectionService).setReviewed(eq(student), eq(contentLoc), eq(true));
		verify(section).isLockResponse();
		verify(sectionService, never()).setLocked(eq(student), eq(contentLoc), anyBoolean());
	}

	@Test
	public void clickingComponentDoesNotAffectLockOnReviewedNonSummativeSection() {
		sectionStatus.setCompleted(true);
		sectionStatus.setReviewed(true);
		when(section.isLockResponse()).thenReturn(false);
		startWicket();
		wicketTester.clickLink("panel:component");
		verify(sectionService).setReviewed(eq(student), eq(contentLoc), eq(false));
		verify(section).isLockResponse();
		verify(sectionService, never()).setLocked(eq(student), eq(contentLoc), anyBoolean());
	}

	@Override
	protected Component newTestComponent() {
		return new TeacherSectionCompleteToggleComponent("component", contentLoc, studentModel);
	}

}
