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

public interface IFeatureService {

	boolean isNotebookOn();
	boolean isWhiteboardOn();
	boolean isGlossaryOn();
	boolean isMyQuestionsOn();
	boolean isResponseCollectionsOn();
	boolean isTagsOn();
	boolean isPageNotesOn();
	boolean isClassMessageOn();
	boolean isPageNumbersOn();
	boolean isMathMLOn();
	boolean isSectionToggleTextLinksOn();
	boolean isSectionToggleImageLinksOn();
	boolean isTocSectionTogglesOn();
	boolean isTocSectionCompleteIconsOn();
	boolean isTocSectionIncompleteIconsOn();
	boolean isCollectionsScoreSummaryOn();
	boolean isCompareScoreSummaryOn();
	boolean isSectionToggleImmediateScoreOn();

}
