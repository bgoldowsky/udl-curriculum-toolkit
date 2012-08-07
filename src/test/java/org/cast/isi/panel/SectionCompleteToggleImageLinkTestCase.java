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
import static org.mockito.Mockito.when;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.junit.Test;

public abstract class SectionCompleteToggleImageLinkTestCase extends SectionCompleteToggleLinkTestCase {

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
	
	@Test
	public void componentIsHiddenWhenConfiguredOff() {
		when(featureService.isSectionToggleImageLinksOn()).thenReturn(false);
		startWicket();
		wicketTester.assertInvisible("panel:component");
	}

	@Override
	protected Panel newComponentTestPanel(String panelId, Component component) {
		return new ComponentTestPanel(panelId, component);
	}
	
	public class ComponentTestPanel extends Panel {

		private static final long serialVersionUID = 1L;

		public ComponentTestPanel(String id, Component component) {
			super(id);
			add(component);
		}

	}

}