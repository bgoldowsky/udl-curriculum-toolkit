package org.cast.isi.panel;

import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.ITestPanelSource;
import org.cast.cwm.data.User;
import org.cast.cwm.test.CwmWicketTester;
import org.cast.cwm.test.GuiceInjectedTestApplication;
import org.cast.isi.service.ISectionService;
import org.junit.Before;
import org.junit.Test;

public class SectionCompleteToggleComponentTest {

	private Map<Class<? extends Object>,Object> injectionMap;
	private ISectionService sectionService;
	private CwmWicketTester wicketTester;
	private IModel<User> userModel;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Before
	public void setUp() {
		userModel = new Model(new User());
		setupInjectedServices();
		wicketTester = new CwmWicketTester(new GuiceInjectedTestApplication(injectionMap));
	}
	
	@Test
	public void canRender() {
		wicketTester.startPanel(new TestPanelSource(new SectionCompleteToggleComponent("component", "testloc", userModel)));
		wicketTester.assertComponent("panel:component", SectionCompleteToggleComponent.class);
	}
	
	private void setupInjectedServices() {
		sectionService = mock(ISectionService.class);
		injectionMap = new HashMap<Class<? extends Object>, Object>();
		injectionMap.put(ISectionService.class, sectionService);
	}

	private class TestPanelSource implements ITestPanelSource {

		private static final long serialVersionUID = 1L;
		private Component component;

		public TestPanelSource(Component component) {
			super();
			this.component = component;
		}

		public Panel getTestPanel(String panelId) {
			component = new SectionCompleteToggleComponent("component", "testloc", userModel);
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
