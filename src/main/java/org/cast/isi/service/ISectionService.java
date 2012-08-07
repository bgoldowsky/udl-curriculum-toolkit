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
package org.cast.isi.service;

import java.util.List;
import java.util.Map;

import org.cast.cwm.data.Period;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.isi.ISIXmlSection;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.data.SectionStatus;

public interface ISectionService {

	SectionStatus getSectionStatus(User person, String loc);

	SectionStatus getSectionStatus(User person, ISIXmlSection xs);

	SectionStatus getSectionStatus(User user, ContentLoc contentLoc);

	Map<String, Boolean> getSectionStatusMap(User person);

	/**
	 * Get Map of all section statuses for a Period
	 * @return
	 */
	Map<Long, Map<String, SectionStatus>> getSectionStatusMaps(
			List<Period> periodList);

	boolean sectionIsCompleted(User person, String loc);

	boolean sectionIsCompleted(User person, ISIXmlSection xs);

	SectionStatus setCompleted(User user, String location, boolean complete);

	SectionStatus setCompleted(User person, ContentLoc loc, boolean complete);

	void adjustMessageCount(User student, ContentLoc loc, Role role, int amount);

	boolean sectionIsReviewed(User person, String loc);

	boolean sectionIsReviewed(User person, ISIXmlSection xs);

	boolean sectionReviewable(User person, ISIXmlSection xs);

	SectionStatus setReviewed(User person, String location, boolean reviewed);

	SectionStatus setReviewed(User person, ContentLoc loc, boolean reviewed);

	SectionStatus toggleReviewed(User person, ContentLoc loc);

	SectionStatus setLocked(User person, ContentLoc loc, boolean locked);

}