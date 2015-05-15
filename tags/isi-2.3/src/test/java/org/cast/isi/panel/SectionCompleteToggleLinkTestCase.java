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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import org.cast.cwm.xml.XmlDocument;
import org.cast.cwm.xml.XmlSection;
import org.cast.cwm.xml.service.IXmlService;
import org.cast.isi.ISIXmlSection;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.data.SectionStatus;
import org.cast.isi.service.IFeatureService;
import org.cast.isi.service.ISectionService;
import org.junit.Before;
import org.junit.Test;

public abstract class SectionCompleteToggleLinkTestCase {

	private Map<Class<? extends Object>,Object> injectionMap;
	protected ISectionService sectionService;
	protected CwmWicketTester wicketTester;
	protected IModel<User> studentModel;
	protected SectionStatus sectionStatus;
	protected User student;
	protected String pageLocationId = "test.xml_page";
	protected ContentLoc pageContentLoc;
	protected ISIXmlSection pageXmlSection;
	protected IModel<XmlSection> pageSectionXmlModel;
	protected XmlDocument pageDoc;
	protected String sectionLocationId = "test.xml_section";
	protected ContentLoc sectionContentLoc;
	protected ISIXmlSection sectionXmlSection;
	protected IModel<XmlSection> sectionSectionXmlModel;
	protected XmlDocument sectionDoc;
	protected IXmlService xmlService;
	protected IFeatureService featureService;

	@Test
	public void componentKnowsPageLocation() {
		startWicket();
		SectionCompleteToggleLink component = (SectionCompleteToggleLink) wicketTester.getComponentFromLastRenderedPage("panel:component");
		assertThat(component.getPageContentLocation(), equalTo(pageContentLoc));
	}

	@Test
	public void componentKnowsSectionLocation() {
		startWicket();
		SectionCompleteToggleLink component = (SectionCompleteToggleLink) wicketTester.getComponentFromLastRenderedPage("panel:component");
		assertThat(component.getSectionContentLocation(), equalTo(sectionContentLoc));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Before
	public void setUp() {
		student = new User();
		student.setFirstName("Mickey");
		student.setLastName("Mouse");
		studentModel = new Model(student);

		pageContentLoc = mock(ContentLoc.class);
		pageDoc = mock(XmlDocument.class);
		pageXmlSection = mock(ISIXmlSection.class);
		when(pageContentLoc.getSection()).thenReturn(pageXmlSection);
		when(pageContentLoc.getLocation()).thenReturn(pageLocationId);
		when(pageXmlSection.getXmlDocument()).thenReturn(pageDoc);
		when(pageXmlSection.isLastPageInSection()).thenReturn(true);
		when(pageXmlSection.getContentLoc()).thenReturn(pageContentLoc);
		pageSectionXmlModel = new Model(pageXmlSection);

		sectionContentLoc = mock(ContentLoc.class);
		sectionDoc = mock(XmlDocument.class);
		sectionXmlSection = mock(ISIXmlSection.class);
		when(sectionContentLoc.getSection()).thenReturn(sectionXmlSection);
		when(sectionContentLoc.getLocation()).thenReturn(sectionLocationId);
		when(sectionXmlSection.getXmlDocument()).thenReturn(sectionDoc);
		when(sectionXmlSection.getContentLoc()).thenReturn(sectionContentLoc);
		sectionSectionXmlModel = new Model(sectionXmlSection);

		when(pageXmlSection.getSectionAncestor()).thenReturn(sectionXmlSection);
		
		sectionStatus = new SectionStatus();
		sectionStatus.setCompleted(false);
		sectionStatus.setReviewed(false);
		sectionStatus.setLocked(false);
		setupInjectedServices();

		when(sectionService.getSectionStatus(eq(student), eq(pageLocationId))).thenReturn(sectionStatus);
		when(sectionService.getSectionStatus(eq(student), eq(pageContentLoc))).thenReturn(sectionStatus);

		when(sectionService.getSectionStatus(eq(student), eq(sectionLocationId))).thenReturn(sectionStatus);
		when(sectionService.getSectionStatus(eq(student), eq(sectionContentLoc))).thenReturn(sectionStatus);

		wicketTester = new CwmWicketTester(new GuiceInjectedTestApplication(injectionMap));
	}

	protected void startWicket() {
		wicketTester.startPanel(new TestPanelSource(newTestComponent()));
	}

	protected abstract Component newTestComponent();

	protected abstract Panel newComponentTestPanel(String panelId, Component component);

	private void setupInjectedServices() {
		sectionService = mock(ISectionService.class);
		xmlService = mock(IXmlService.class);
		featureService = mock(IFeatureService.class);
		when(featureService.isSectionToggleImageLinksOn()).thenReturn(true);
		when(featureService.isSectionToggleTextLinksOn()).thenReturn(true);
		injectionMap = new HashMap<Class<? extends Object>, Object>();
		injectionMap.put(ISectionService.class, sectionService);
		injectionMap.put(IXmlService.class, xmlService);
		injectionMap.put(IFeatureService.class, featureService);
	}

	protected class TestPanelSource implements ITestPanelSource {

		private static final long serialVersionUID = 1L;
		private Component component;

		public TestPanelSource(Component component) {
			super();
			this.component = component;
		}

		public Panel getTestPanel(String panelId) {
			return newComponentTestPanel(panelId, component);
		}
	}

}