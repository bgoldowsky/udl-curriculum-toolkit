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
package org.cast.isi.service;

import org.cast.isi.ISIApplication;

public class FeatureService implements IFeatureService {

	public boolean isNotebookOn() {
		return ISIApplication.get().isNotebookOn();
	}

	public boolean isWhiteboardOn() {
		return ISIApplication.get().isWhiteboardOn();
	}

	public boolean isGlossaryOn() {
		return ISIApplication.get().isGlossaryOn();
	}

	public boolean isMyQuestionsOn() {
		return ISIApplication.get().isMyQuestionsOn();
	}

	public boolean isResponseCollectionsOn() {
		return ISIApplication.get().isResponseCollectionsOn();
	}

	public boolean isTagsOn() {
		return ISIApplication.get().isTagsOn();
	}

	public boolean isPageNotesOn() {
		return ISIApplication.get().isPageNotesOn();
	}

	public boolean isClassMessageOn() {
		return ISIApplication.get().isClassMessageOn();
	}

	public boolean isPageNumbersOn() {
		return ISIApplication.get().isPageNumbersOn();
	}

	public boolean isMathMLOn() {
		return ISIApplication.get().isMathMLOn();
	}

	public boolean isSectionToggleTextLinksOn() {
		return ISIApplication.get().isSectionToggleTextLinksOn();
	}

	public boolean isSectionToggleImageLinksOn() {
		return ISIApplication.get().isSectionToggleImageLinksOn();
	}

	public boolean isTocSectionTogglesOn() {
		return ISIApplication.get().isTocSectionTogglesOn();
	}

	public boolean isTocSectionCompleteIconsOn() {
		return ISIApplication.get().isTocSectionCompleteIconsOn();
	}

	public boolean isTocSectionIncompleteIconsOn() {
		return ISIApplication.get().isTocSectionIncompleteIconsOn();
	}

	public boolean isCollectionsScoreSummaryOn() {
		return ISIApplication.get().isCollectionsScoreSummaryOn();
	}

	public boolean isCompareScoreSummaryOn() {
		return ISIApplication.get().isCompareScoreSummaryOn();
	}
	
	public boolean isSectionToggleImmediateScoreOn() {
		return ISIApplication.get().isSectionToggleImmediateScoreOn();
	}

}
