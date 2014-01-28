package org.cast.isi.panel;

import java.util.HashMap;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.tester.WicketTester;
import org.cast.cwm.test.CwmWicketTester;
import org.cast.cwm.test.GuiceInjectedCwmTestApplication;
import org.junit.Before;
import org.junit.Test;

public class AgentLinkTest {
	
	private WicketTester tester;
	private HashMap<Class<? extends Object>, Object> injectionMap;

	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Before
	public void setUp() {
		setupInjectedServices();
		this.tester = new CwmWicketTester(new GuiceInjectedCwmTestApplication(injectionMap));
	}
	
	@Test
	public void canRender() {
		tester.startComponentInPage(new AgentLink("agentLink", "test", "123"));
		tester.assertComponent("agentLink", AgentLink.class);
	}

	@Test
	public void hasLink() {
		tester.startComponentInPage(new AgentLink("agentLink", "test", "123"));
		tester.assertComponent("agentLink:link", WebMarkupContainer.class);
	}
	
	@Test
	public void showsTitle() {
		tester.startComponentInPage(new AgentLink("agentLink", "test", "123"));
		tester.assertComponent("agentLink:link:title", Label.class);
		tester.assertLabel("agentLink:link:title", "test");
	}
	
	private void setupInjectedServices() {
		injectionMap = new HashMap<Class<? extends Object>, Object>();		
	}

}