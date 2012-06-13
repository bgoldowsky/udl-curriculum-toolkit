package org.cast.isi.panel;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.ITestPanelSource;
import org.cast.cwm.data.User;
import org.cast.cwm.test.CwmWicketTester;
import org.cast.cwm.test.GuiceInjectedTestApplication;
import org.cast.cwm.xml.XmlDocument;
import org.cast.isi.ISIXmlSection;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.data.SectionStatus;
import org.cast.isi.service.ISectionService;
import org.junit.Before;
import org.junit.Test;

public abstract class SectionCompleteToggleComponentTestCase {

	private Map<Class<? extends Object>,Object> injectionMap;
	protected ISectionService sectionService;
	protected CwmWicketTester wicketTester;
	protected IModel<User> studentModel;
	protected SectionStatus sectionStatus;
	protected User student;
	protected String loc = "test.xml_l1";
	protected ContentLoc contentLoc;
	protected ISIXmlSection section;
	protected IModel<ISIXmlSection> sectionModel;
	protected XmlDocument doc;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Before
	public void setUp() {
		student = new User();
		student.setFirstName("Mickey");
		student.setLastName("Mouse");
		studentModel = new Model(student);
		contentLoc = mock(ContentLoc.class);
		doc = mock(XmlDocument.class);
		section = mock(ISIXmlSection.class);
		when(contentLoc.getSection()).thenReturn(section);
		when(contentLoc.getLocation()).thenReturn(loc);
		when(section.getXmlDocument()).thenReturn(doc);
		sectionModel = new Model(section);
		sectionStatus = new SectionStatus();
		sectionStatus.setCompleted(false);
		sectionStatus.setReviewed(false);
		sectionStatus.setLocked(false);
		setupInjectedServices();
		when(sectionService.getSectionStatus(eq(student), eq(loc))).thenReturn(sectionStatus);
		when(sectionService.getSectionStatus(eq(student), eq(contentLoc))).thenReturn(sectionStatus);
		wicketTester = new CwmWicketTester(new GuiceInjectedTestApplication(injectionMap));
	}
	
	@Test
	public void hasImage() {
		startWicket();
		wicketTester.assertComponent("panel:component:doneImg", Image.class);
	}
	
	@Test
	public void hasCorrectImageWhenIncomplete() {
		sectionStatus.setCompleted(false);
		startWicket();
		Image image = (Image) wicketTester.getComponentFromLastRenderedPage("panel:component:doneImg");
		assertThat((String) image.getDefaultModelObject(), equalTo("/img/icons/check_notdone.png"));
	}

	@Test
	public void imageHasCorrectAltTagWhenIncomplete() {
		sectionStatus.setCompleted(false);
		startWicket();
		Image image = (Image) wicketTester.getComponentFromLastRenderedPage("panel:component:doneImg");
		wicketTester.assertAttribute("Should have alt=\"Not Finished\"", "Not Finished", image, "alt");
	}

	@Test
	public void imageHasCorrectTitleTagWhenIncomplete() {
		sectionStatus.setCompleted(false);
		startWicket();
		Image image = (Image) wicketTester.getComponentFromLastRenderedPage("panel:component:doneImg");
		wicketTester.assertAttribute("Should have title=\"Not Finished\"", "Not Finished", image, "title");
	}

	protected void startWicket() {
		wicketTester.startPanel(new TestPanelSource(newTestComponent()));
	}


	protected abstract Component newTestComponent();

	private void setupInjectedServices() {
		sectionService = mock(ISectionService.class);
		injectionMap = new HashMap<Class<? extends Object>, Object>();
		injectionMap.put(ISectionService.class, sectionService);
	}

	protected class TestPanelSource implements ITestPanelSource {

		private static final long serialVersionUID = 1L;
		private Component component;

		public TestPanelSource(Component component) {
			super();
			this.component = component;
		}

		public Panel getTestPanel(String panelId) {
			return new ComponentTestPanel(panelId, component);
		}
	}

	public class ComponentTestPanel extends Panel {

		private static final long serialVersionUID = 1L;

		public ComponentTestPanel(String id, Component component) {
			super(id);
			add(component);
		}

	}

}