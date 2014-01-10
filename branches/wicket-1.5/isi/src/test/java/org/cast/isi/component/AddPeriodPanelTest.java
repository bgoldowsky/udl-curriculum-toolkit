package org.cast.isi.component;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.cast.cwm.data.Period;
import org.cast.cwm.service.ICwmSessionService;
import org.cast.cwm.service.ISiteService;
import org.cast.cwm.test.CwmWicketTester;
import org.cast.cwm.test.GuiceInjectedCwmTestApplication;
import org.cwm.db.service.IModelProvider;
import org.cwm.db.service.SimpleModelProvider;
import org.junit.Before;
import org.junit.Test;

public class AddPeriodPanelTest {
	
	private CwmWicketTester tester;
	private HashMap<Class<? extends Object>, Object> injectionMap;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Before
	public void setUp() {
		setupInjectedServices();
		tester = new CwmWicketTester(new GuiceInjectedCwmTestApplication(injectionMap));
	}
	
	private void setupInjectedServices() {
		injectionMap = new HashMap<Class<? extends Object>, Object>();
		ISiteService siteServiceMock = mock(ISiteService.class);
		when(siteServiceMock.newPeriod()).thenReturn(new Period());
		injectionMap.put(ISiteService.class, siteServiceMock);
		
		injectionMap.put(IModelProvider.class, new SimpleModelProvider());
		
		injectionMap.put(ICwmSessionService.class,  mock(ICwmSessionService.class));
	}

	@Test
	public void canRender() {
		tester.startComponentInPage(new AddPeriodPanel("panel"));
		tester.assertComponent("panel", AddPeriodPanel.class);
	}

}
