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
import static org.mockito.Mockito.when;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.tester.TagTester;
import org.cast.cwm.components.Icon;
import org.junit.Test;

public abstract class SectionCompleteToggleImageLinkTestCase extends SectionCompleteToggleLinkTestCase {

	@Test
	public void hasImage() {
		startWicket();
		wicketTester.assertComponent("panel:component:doneImg", Icon.class);
	}
	
	@Test
	public void hasCorrectImageWhenIncomplete() {
		sectionStatus.setCompleted(false);
		startWicket();
		TagTester imageTag = wicketTester.getTagByWicketId("doneImg");
		assertThat(imageTag.getAttribute("src"), equalTo("../img/icons/check_notdone.png"));
	}

	@Test
	public void imageHasCorrectAltTagWhenIncomplete() {
		sectionStatus.setCompleted(false);
		startWicket();
		TagTester imageTag = wicketTester.getTagByWicketId("doneImg");
		assertThat(imageTag.getAttribute("alt"), equalTo("Not Finished"));
	}

	@Test
	public void imageHasCorrectTitleTagWhenIncomplete() {
		sectionStatus.setCompleted(false);
		startWicket();
		TagTester imageTag = wicketTester.getTagByWicketId("doneImg");
		assertThat(imageTag.getAttribute("title"), equalTo("Not Finished"));
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