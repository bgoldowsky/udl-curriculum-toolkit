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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.databinder.hib.Databinder;

import org.cast.cwm.data.Period;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.cwm.service.EventService;
import org.cast.cwm.service.ICwmService;
import org.cast.isi.ISIXmlSection;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.data.SectionStatus;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Restrictions;

import com.google.inject.Inject;

/**
 * Database service methods for Section, SectionStatus, etc.
 * @author boris
 *
 */
public class SectionService implements ISectionService {

	@Inject
	private ICwmService cwmService;

	/* (non-Javadoc)
	 * @see org.cast.isi.service.ISectionService#getSectionStatus(org.cast.cwm.data.User, java.lang.String)
	 */
	public SectionStatus getSectionStatus(User person, String loc) {
		return (SectionStatus) Databinder.getHibernateSession().createCriteria(SectionStatus.class)
				.add(Restrictions.eq("user", person))
				.add(Restrictions.eq("loc", loc))
				.setCacheable(true)
				.uniqueResult();
	}

	/* (non-Javadoc)
	 * @see org.cast.isi.service.ISectionService#getSectionStatus(org.cast.cwm.data.User, org.cast.isi.data.ContentLoc)
	 */
	public SectionStatus getSectionStatus(User person, ContentLoc contentLoc) {
		return getSectionStatus(person, contentLoc.getLocation());
	}
	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.ISectionService#getSectionStatus(org.cast.cwm.data.User, org.cast.isi.ISIXmlSection)
	 */
	public SectionStatus getSectionStatus(User person, ISIXmlSection xs) {
		xs = xs.getSectionAncestor();
		if (xs == null)
			return null;
		return getSectionStatus(person, new ContentLoc(xs));
	}
	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.ISectionService#getSectionStatusMap(org.cast.cwm.data.User)
	 */
	@SuppressWarnings("unchecked")
	public Map<String,Boolean> getSectionStatusMap (User person) {
		List<SectionStatus> list = 
			Databinder.getHibernateSession().createCriteria(SectionStatus.class)
			.add(Restrictions.eq("user", person))
			.setCacheable(true)
			.list();
		Map<String,Boolean> m = new HashMap<String,Boolean>();
		for (SectionStatus stat : list) {
			m.put(stat.getLoc(), stat.getCompleted());
		}
		return m;
	}
	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.ISectionService#getSectionStatusMaps(java.util.List)
	 */
	@SuppressWarnings("unchecked")
	public Map<Long,Map<String,SectionStatus>> getSectionStatusMaps (List<Period> periodList) {
		Map<Long,Map<String,SectionStatus>> m = new HashMap<Long,Map<String,SectionStatus>>();
		for (Period p : periodList) {
			List<SectionStatus> list = 
				Databinder.getHibernateSession().createQuery(
						"select ss from SectionStatus ss "
						+ " join ss.user user "
						+ " join user.periods period "
						+ " where period=:p ")
						.setParameter("p", p)
						.setCacheable(true)
						.list();
			for (SectionStatus stat : list) {
				if (!m.containsKey(stat.getUser().getId()))
					m.put(stat.getUser().getId(), new HashMap<String,SectionStatus>());
				m.get(stat.getUser().getId()).put(stat.getLoc(), stat);
			}
		}
		return m;
	}

	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.ISectionService#sectionIsCompleted(org.cast.cwm.data.User, java.lang.String)
	 */
	public boolean sectionIsCompleted (User person, String loc) {
		SectionStatus stat = getSectionStatus(person, loc);
		if (stat==null || !stat.getCompleted())
			return false;
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.ISectionService#sectionIsCompleted(org.cast.cwm.data.User, org.cast.isi.ISIXmlSection)
	 */
	public boolean sectionIsCompleted (User person, ISIXmlSection xs) {
		SectionStatus stat = getSectionStatus(person, xs);
		if (stat==null || !stat.getCompleted())
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.cast.isi.service.ISectionService#setCompleted(org.cast.cwm.data.User, java.lang.String, boolean)
	 */
	public SectionStatus setCompleted(User user, String location, boolean complete) {
		return setCompleted(user, new ContentLoc(location), complete);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.ISectionService#setCompleted(org.cast.cwm.data.User, org.cast.isi.data.ContentLoc, boolean)
	 */
	public SectionStatus setCompleted (User person, ContentLoc loc, boolean complete) {
		ISIXmlSection sec = loc.getSection().getSectionAncestor();
		if (sec == null)
			return null;
		Session s = Databinder.getHibernateSession();
		SectionStatus stat = getSectionStatus(person, sec);
		if (stat == null) {
			stat = new SectionStatus(person, new ContentLoc(sec).getLocation(), complete);
			s.save(stat);
		} else {
			stat.setCompleted(complete);
		}
		// If there are no response groups, automatically mark as Reviewed
		if (!sec.hasResponseGroup()) {
			stat.setReviewed(complete);
		}
		cwmService.flushChanges();
		EventService.get().saveEvent("section:" + (complete ? "done" : "not done"), null, loc.getLocation());
		return stat;
	}
	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.ISectionService#adjustMessageCount(org.cast.cwm.data.User, org.cast.isi.data.ContentLoc, org.cast.cwm.data.Role, int)
	 */
	public void adjustMessageCount (User student, ContentLoc loc, Role role, int amount) {
		ISIXmlSection sec = loc.getSection().getSectionAncestor();
		if (sec == null) {
			throw new IllegalArgumentException("Content Location not valid");
		}
		Session s = Databinder.getHibernateSession();
		SectionStatus stat = getSectionStatus(student, sec);
		if (stat == null) {
			stat = new SectionStatus(student, new ContentLoc(sec).getLocation(), false);
			s.save(stat);
		}
		if (role.equals(Role.STUDENT))
			stat.setUnreadStudentMessages(stat.getUnreadStudentMessages() + amount);
		else
			stat.setUnreadTeacherMessages(stat.getUnreadTeacherMessages() + amount);
		cwmService.flushChanges();
	}
	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.ISectionService#sectionIsReviewed(org.cast.cwm.data.User, java.lang.String)
	 */
	public boolean sectionIsReviewed (User person, String loc) {
		SectionStatus stat = getSectionStatus(person, loc);
		if (stat == null || !stat.getReviewed()) {
			return false;
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.ISectionService#sectionIsReviewed(org.cast.cwm.data.User, org.cast.isi.ISIXmlSection)
	 */
	public boolean sectionIsReviewed (User person, ISIXmlSection xs) {
		SectionStatus stat = getSectionStatus(person, xs);
		if (stat == null || !stat.getReviewed()) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.cast.isi.service.ISectionService#sectionReviewable(org.cast.cwm.data.User, org.cast.isi.ISIXmlSection)
	 */
	public boolean sectionReviewable (User person, ISIXmlSection xs) {
		SectionStatus stat = getSectionStatus(person, xs);
		// teacher can only review if the sectionstatus exists, the student has completed
		// and there are no outstanding feedback messages for this section
		if (stat == null || stat.getCompleted() == null || stat.getCompleted() == false
				|| stat.getUnreadTeacherMessages() > 0) {
			return false;
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.ISectionService#setCompleted(org.cast.cwm.data.User, java.lang.String, boolean)
	 */
	public SectionStatus setReviewed(User user, String location, boolean reviewed) {
		return setReviewed(user, new ContentLoc(location), reviewed);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.ISectionService#setReviewed(org.cast.cwm.data.User, org.cast.isi.data.ContentLoc, boolean)
	 */
	public SectionStatus setReviewed (User person, ContentLoc loc, boolean reviewed) {
		ISIXmlSection sec = loc.getSection().getSectionAncestor();
		if (sec == null)
			return null;
		SectionStatus stat = getSectionStatus(person, sec);
		if (stat == null || !stat.getCompleted()) {
			throw new IllegalStateException("Should not be able to adjust a review on an uncomplete or non-existant section status");
		} else {
			stat.setReviewed(reviewed);
		}
		cwmService.flushChanges();
		EventService.get().saveEvent("section:" + (reviewed ? "reviewed" : "not reviewed"), null, loc.getLocation());
		return stat;
	}
	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.ISectionService#toggleReviewed(org.cast.cwm.data.User, org.cast.isi.data.ContentLoc)
	 */
	public SectionStatus toggleReviewed (User person, ContentLoc loc) {
		ISIXmlSection sec = loc.getSection().getSectionAncestor();
		if (sec == null)
			return null;
		SectionStatus stat = getSectionStatus(person, sec);
		if (stat == null || !stat.getCompleted()) {
			throw new IllegalStateException("Should not be able to adjust a review on an uncomplete or non-existant section status");
		} else {
			boolean reviewed = stat.getReviewed();
			stat.setReviewed(!reviewed);
		}
		cwmService.flushChanges();
		EventService.get().saveEvent("section:" + (stat.getReviewed() ? "reviewed" : "not reviewed"), null, loc.getLocation());
		return stat;
	}

	public SectionStatus setLocked(User person, ContentLoc loc, boolean locked) {
		ISIXmlSection sec = loc.getSection().getSectionAncestor();
		if (sec == null)
			return null;
		SectionStatus stat = getSectionStatus(person, sec);
		if (stat == null || !stat.getCompleted()) {
			throw new IllegalStateException("Should not be able to adjust a lock on an uncomplete or non-existant section status");
		} else {
			stat.setLocked(locked);
		}
		cwmService.flushChanges();
		return stat;
	}

}
