package org.cast.isi.panel;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.image.Image;
import org.junit.Test;

public class StudentSectionCompleteToggleComponentTest extends SectionCompleteToggleComponentTestCase {

	@Test
	public void canRender() {
		startWicket();
		wicketTester.assertComponent("panel:component", StudentSectionCompleteToggleComponent.class);
	}

	@Test
	public void hasCorrectImageWhenComplete() {
		sectionStatus.setCompleted(true);
		startWicket();
		Image image = (Image) wicketTester.getComponentFromLastRenderedPage("panel:component:doneImg");
		assertThat((String) image.getDefaultModelObject(), equalTo("/img/icons/check_done.png"));
	}

	@Test
	public void imageHasCorrectAltTagWhenComplete() {
		sectionStatus.setCompleted(true);
		startWicket();
		Image image = (Image) wicketTester.getComponentFromLastRenderedPage("panel:component:doneImg");
		wicketTester.assertAttribute("Should have alt=\"Finished\"", "Finished", image, "alt");
	}

	@Test
	public void imageHasCorrectTitleTagWhenComplete() {
		sectionStatus.setCompleted(true);
		startWicket();
		Image image = (Image) wicketTester.getComponentFromLastRenderedPage("panel:component:doneImg");
		wicketTester.assertAttribute("Should have title=\"Finished\"", "Finished", image, "title");
	}

	@Test
	public void clickingComponentMarksIncompleteSectionAsComplete() {
		sectionStatus.setCompleted(false);
		startWicket();
		wicketTester.clickLink("panel:component");
		verify(sectionService).setCompleted(eq(student), eq(contentLoc), eq(true));
	}

	@Test
	public void clickingComponentMarksCompleteSectionAsIncomplete() {
		sectionStatus.setCompleted(true);
		startWicket();
		wicketTester.clickLink("panel:component");
		verify(sectionService).setCompleted(eq(student), eq(contentLoc), eq(false));
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
		return new StudentSectionCompleteToggleComponent("component", contentLoc, studentModel);
	}
	
}
