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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import net.databinder.hib.Databinder;
import net.databinder.models.hib.HibernateListModel;
import net.databinder.models.hib.HibernateObjectModel;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.util.ListModel;
import org.cast.cwm.CwmApplication;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.Response;
import org.cast.cwm.data.ResponseData;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.cwm.data.models.PromptModel;
import org.cast.cwm.data.models.UserModel;
import org.cast.cwm.service.IEventService;
import org.cast.cwm.service.ResponseService;
import org.cast.cwm.xml.XmlSection;
import org.cast.isi.ISISession;
import org.cast.isi.ISIXmlSection;
import org.cast.isi.data.ClassMessage;
import org.cast.isi.data.ContentElement;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.data.FeedbackMessage;
import org.cast.isi.data.ISIEvent;
import org.cast.isi.data.ISIPrompt;
import org.cast.isi.data.ISIResponse;
import org.cast.isi.data.PromptType;
import org.cast.isi.data.StudentFlag;
import org.cast.isi.data.builder.ISIResponseCriteriaBuilder;
import org.cast.isi.page.ISIBasePage;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;


/**
 * Methods for teacher administrative duties: Flagging students, creating class messages, etc.
 * 
 * TODO: Many of these methods should be swapped to cwm-data standards or removed.
 * @author jbrookover
 *
 */
public class ISIResponseService extends ResponseService implements IISIResponseService {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ISIResponseService.class);
	
	@Inject
	private IEventService eventService;

	protected ISIResponseService() {/* Protected Constructor - use injection */}
	
	public static ISIResponseService get() {
		return (ISIResponseService) ResponseService.get();
	}
	
	/**
	 * Use this Service class.  Called in {@link Application#init()}.
	 */
	public static void useAsServiceInstance() {
		ResponseService.instance = new ISIResponseService();
	}
		
	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#getOrCreatePrompt(org.cast.isi.data.PromptType)
	 */
	public IModel<Prompt> getOrCreatePrompt(PromptType type) {
		return genericGetOrCreatePrompt(type, null, null, null, null, null);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#getOrCreatePrompt(org.cast.isi.data.PromptType, java.lang.String)
	 */
	public IModel<Prompt> getOrCreatePrompt(PromptType type, String identifier) {
		return genericGetOrCreatePrompt(type, null, null, null, identifier, null);
	}
	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#getOrCreatePrompt(org.cast.isi.data.PromptType, org.cast.isi.data.ContentLoc)
	 */
	public IModel<Prompt> getOrCreatePrompt(PromptType type, ContentLoc loc) {
		return genericGetOrCreatePrompt(type, loc, null, null, null, null);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#getOrCreatePrompt(org.cast.isi.data.PromptType, org.cast.isi.data.ContentLoc, java.lang.String)
	 */
	public IModel<Prompt> getOrCreatePrompt(PromptType type, ContentLoc loc, String xmlId) {
		return genericGetOrCreatePrompt(type, loc, xmlId, null, null, null);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#getOrCreatePrompt(org.cast.isi.data.PromptType, org.apache.wicket.model.IModel)
	 */
	public IModel<Prompt> getOrCreatePrompt(PromptType type, IModel<User> targetUser) {
		return genericGetOrCreatePrompt(type, null, null, targetUser, null, null);
	}

	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#getOrCreatePrompt(org.cast.isi.data.PromptType, org.apache.wicket.model.IModel, java.lang.String)
	 */
	public IModel<Prompt> getOrCreatePrompt(PromptType type, IModel<User> targetUser, String identifier) {
		return genericGetOrCreatePrompt(type, null, null, targetUser, identifier, null);
	}

	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#getOrCreatePrompt(org.cast.isi.data.PromptType, org.cast.isi.data.ContentLoc, java.lang.String, java.lang.String)
	 */
	public IModel<Prompt> getOrCreatePrompt(PromptType type, ContentLoc loc, String xmlId, String collectionName) {
		return genericGetOrCreatePrompt(type, loc, xmlId, null, null, collectionName);
	}

	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#getOrCreateHighlightPrompt(org.cast.isi.data.PromptType, org.cast.isi.data.ContentLoc, java.lang.String)
	 */
	public IModel<Prompt> getOrCreateHighlightPrompt(PromptType highlightlabel, String color) {
		return genericGetOrCreatePrompt(highlightlabel, null, null, null, color, null);
	}

	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#genericGetOrCreatePrompt(org.cast.isi.data.PromptType, org.cast.isi.data.ContentLoc, java.lang.String, org.apache.wicket.model.IModel, java.lang.String, java.lang.String)
	 */
	public synchronized IModel<Prompt> genericGetOrCreatePrompt(PromptType type, ContentLoc loc, String xmlId, IModel<User> targetUser, String identifier, String collectionName) {

		//TODO: Do we need this for other fields?
		if ("".equals(collectionName))
			collectionName = null;

		IModel<ContentElement> ce = getOrCreateContentElement(loc, xmlId);
		
		Criteria c = Databinder.getHibernateSession().createCriteria(ISIPrompt.class)
		.add(Restrictions.eq("type", type))
		.setCacheable(true);
		
		if (ce != null)
			c.add(Restrictions.eq("contentElement", ce.getObject()));
		else
			c.add(Restrictions.isNull("contentElement"));

		if (targetUser != null)
			c.add(Restrictions.eq("targetUser", targetUser.getObject()));
		else
			c.add(Restrictions.isNull("targetUser"));

		if (identifier != null)
			c.add(Restrictions.eq("identifier", identifier));
		else
			c.add(Restrictions.isNull("identifier"));

		if (collectionName != null)
			c.add(Restrictions.eq("collectionName", collectionName));
		else
			c.add(Restrictions.isNull("collectionName"));

		ISIPrompt p = (ISIPrompt) c.uniqueResult();

		// If it doesn't exist, create it
		if (p == null) {
			p = new ISIPrompt(type);
			if (ce != null)
				p.setContentElement(ce.getObject());
			if (targetUser != null)
				p.setTargetUser(targetUser.getObject());
			if (identifier != null)
				p.setIdentifier(identifier);
			if (collectionName != null) {
				p.setCollectionName(collectionName);
			}
			Databinder.getHibernateSession().save(p);	
		}
		
		cwmService.flushChanges();
		
		return new PromptModel(p);
	}

	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#getOrCreateContentElement(org.cast.isi.data.ContentLoc)
	 */
	public IModel<ContentElement> getOrCreateContentElement(ContentLoc loc) {
		return getOrCreateContentElement(loc, null);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#getOrCreateContentElement(org.cast.isi.data.ContentLoc, java.lang.String)
	 */
	public synchronized IModel<ContentElement> getOrCreateContentElement(ContentLoc loc, String xmlId) {
		
		if (loc == null)
			return null;
	
		Criteria c = Databinder.getHibernateSession().createCriteria(ContentElement.class)
		.add(Restrictions.eq("contentLocation", loc.getLocation()))
		.setCacheable(true);
		
		if (xmlId != null) {
			c.add(Restrictions.eq("xmlId", xmlId));
		} else {
			c.add(Restrictions.isNull("xmlId"));
		}
		
		ContentElement e = (ContentElement) c.uniqueResult();

		// If it doesn't exist, create it
		if (e == null) {
			e = new ContentElement(loc);
			if (xmlId != null)
				e.setXmlId(xmlId);
			Databinder.getHibernateSession().save(e);	
		}
		cwmService.flushChanges();
		
		return new HibernateObjectModel<ContentElement>(e);
	}

	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#newPageHighlightsLabel(org.apache.wicket.model.IModel, org.apache.wicket.model.IModel)
	 */
	public IModel<Response> newPageHighlightsLabel(IModel<User> user, IModel<ISIPrompt> prompt) {
		return super.newResponse(user, CwmApplication.get().getResponseType("TEXT"), prompt);
	}

	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#saveSingleSelectResponse(org.apache.wicket.model.IModel, java.lang.String, boolean, java.lang.String)
	 */
	public void saveSingleSelectResponse(IModel<Response> mResponse, String text, boolean correct, String pageName) {
		Response r = mResponse.getObject();
		int score = correct ? 1 : 0;
		// Number of tries counts up until a correct response is recorded, then stops.
		if (!r.isCorrect()) {
			Set<ResponseData> previousResponses = r.getAllResponseData();
			r.setTries(previousResponses==null ? 1 : previousResponses.size()+1);
		}
		// New score is recorded if it is better than previous tries
		if (r.getScore()==null || score > r.getScore())
			r.setScore(score);
		// Multiple choice always has total points = 1
		r.setTotal(1);
		super.genericSaveResponse(mResponse, text, score, 1, 1, null, pageName);
	}

	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#newSingleSelectResponse(org.apache.wicket.model.IModel, org.apache.wicket.model.IModel)
	 */
	public IModel<Response> newSingleSelectResponse(IModel<User> user, IModel<Prompt> model) {
		return super.newResponse(user, CwmApplication.get().getResponseType("SINGLE_SELECT"), model);
	}

	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#saveHighlighterLabel(org.apache.wicket.model.IModel, java.lang.String)
	 */	
	public void saveHighlighterLabel(IModel<Response> response, String label) {
		super.saveTextResponse(response, label, null);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#setClassMessage(org.apache.wicket.model.IModel, java.lang.String)
	 */
	public void setClassMessage(IModel<Period> mPeriod, String s) {
		
		Period p = mPeriod.getObject();
		
		ClassMessage m = new ClassMessage();
		m.setTimestamp(new Date());
		m.setPeriod(p);
		m.setCurrent(true);
		m.setMessage(s);
		m.setAuthor(ISISession.get().getUser());
		
		ClassMessage old = getClassMessage(mPeriod);
		if (old != null) {
			old.setCurrent(false);
		}
		Databinder.getHibernateSession().save(m);
		cwmService.flushChanges();
		eventService.saveEvent("classmessage:create", "Period Id: " + p.getId() + " Message Id: " + m.getId(), null);		
	}
	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#getClassMessage(org.apache.wicket.model.IModel)
	 */
	public ClassMessage getClassMessage(IModel<Period> p) {
		Query q = Databinder.getHibernateSession().getNamedQuery("ClassMessage.queryByPeriod");
		q.setParameter("period", p.getObject());
		q.setCacheable(true);
		ClassMessage m = (ClassMessage) q.uniqueResult();
		// Databinder.getHibernateSession().evict(m);
		// TODO: test without evict
		return m;
	}
	
	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#deleteClassMessage(org.apache.wicket.model.IModel)
	 */
	public void deleteClassMessage(IModel<Period> mPeriod) {
		
		Period p = mPeriod.getObject();
		ClassMessage m = getClassMessage(mPeriod);
		if (m != null) {
			m.setCurrent(false);
			cwmService.flushChanges();
			eventService.saveEvent("classmessage:delete", "Period Id: " + p.getId() + " Message Id: " + m.getId(), null);
		}
	}

	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#findLatestMatchingEvent(org.cast.cwm.data.User, java.lang.String)
	 */
	public ISIEvent findLatestMatchingEvent (User person, String type) {
		Session s = Databinder.getHibernateSession();
		return (ISIEvent) s.createCriteria(ISIEvent.class)
			.add(Restrictions.eq("user", person))
			.add(Restrictions.eq("type", type))
			.addOrder(Order.desc("insertTime"))
			.setMaxResults(1)
			.uniqueResult();
	}

	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#toggleFlag(org.cast.cwm.data.User, org.cast.cwm.data.Period)
	 */
	public void toggleFlag(User person, Period period) {
		Query q;
		String note;
		if (period != null) {
			q = Databinder.getHibernateSession().getNamedQuery("StudentFlag.queryByPeriodAndFlaggerAndFlagee");
			q.setParameter("period", period);
		} else {
			q = Databinder.getHibernateSession().getNamedQuery("StudentFlag.queryByFlaggerAndFlagee");
		}
		q.setParameter("flagee", person);
		q.setParameter("flagger", ISISession.get().getUser());
		StudentFlag f = (StudentFlag) q.uniqueResult();
		// Databinder.getHibernateSession().evict(f);
		Session session = Databinder.getHibernateSession();
		if (f == null) { // Create Flag
			f = new StudentFlag();
			f.setFlagee(person);
			f.setFlagger(ISISession.get().getUser());
			if (period != null)
				f.setPeriod(period);
			f.setTimestamp(new Date());
			session.save(f);
			note = "on";
		} else { // Delete Flag
			session.delete(f);
			note = "off";
		}
		cwmService.flushChanges();
		eventService.saveEvent("flag", note, null);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#toggleFlag(org.cast.cwm.data.User)
	 */
	public void toggleFlag(User person) {
		toggleFlag(person, null);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#isFlagged(org.cast.cwm.data.User, org.cast.cwm.data.Period)
	 */
	public boolean isFlagged(User person, Period period) {
		Query q;
		if (period != null) {
			q = Databinder.getHibernateSession().getNamedQuery("StudentFlag.queryByPeriodAndFlaggerAndFlagee");
			q.setParameter("period", period);
		} else {
			q = Databinder.getHibernateSession().getNamedQuery("StudentFlag.queryByFlaggerAndFlagee");
		}
		q.setParameter("flagee", person);
		q.setParameter("flagger", ISISession.get().getUser());
		q.setCacheable(true);
		StudentFlag f = (StudentFlag) q.uniqueResult();
		// Databinder.getHibernateSession().evict(f);
		if (f == null) {
			return false;
		} else {
			return true;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#isFlagged(org.cast.cwm.data.User)
	 */
	public boolean isFlagged(User person) {
		return isFlagged(person, null);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#getAllFlags()
	 */
	@SuppressWarnings("unchecked")
	public List<StudentFlag> getAllFlags () {
		Query q = Databinder.getHibernateSession().getNamedQuery("StudentFlag.queryByFlagger");
		q.setParameter("flagger", ISISession.get().getUser());
		q.setCacheable(true);
		return q.list();
	}
	
		
	public void saveFeedbackMessage(IModel<FeedbackMessage> mFeedbackMessage, IModel<Prompt> mPrompt) {
		FeedbackMessage feedbackMessage = mFeedbackMessage.getObject();

		feedbackMessage.setUnread(true);
		feedbackMessage.setVisible(true);

		feedbackMessage.setAuthor(ISISession.get().getUser());
		feedbackMessage.setStudent(ISISession.get().getTargetUserModel().getObject());
		feedbackMessage.setTimestamp(new Date());
		
		ISIPrompt prompt = (ISIPrompt) mPrompt.getObject();
		feedbackMessage.setPrompt(prompt);
		String location = prompt.getContentLoc().getLocation();
		feedbackMessage.setLocation(location);

		Databinder.getHibernateSession().save(feedbackMessage);	
		cwmService.flushChanges();
	}
	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#getFeedbackMessages(org.apache.wicket.model.IModel<org.cast.cwm.data.Prompt>, org.cast.cwm.data.User)
	 */
	@SuppressWarnings("unchecked")
	public List<FeedbackMessage> getFeedbackMessages(IModel<Prompt> promptM, User student) {
		Query q = Databinder.getHibernateSession().getNamedQuery("FeedbackMessage.getAllMessagesByPromptAndStudent");
		q.setParameter("prompt", promptM.getObject());
		q.setParameter("student", student);
		q.setCacheable(true);
		return q.list();
	}
	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#deleteFeedbackMessage(org.apache.wicket.model.IModel)
	 */
	public void deleteFeedbackMessage(IModel<FeedbackMessage> mFeedbackMessage) {
		FeedbackMessage feedbackMessage = mFeedbackMessage.getObject();
		feedbackMessage.setVisible(false);
		Databinder.getHibernateSession().update(feedbackMessage);
		cwmService.flushChanges();
		eventService.saveEvent("feedback:delete", "Message Id: " + feedbackMessage.getId(), null);
	}

	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#updateFeedbackMessage(org.apache.wicket.model.IModel, org.apache.wicket.Page)
	 */
	public void updateFeedbackMessage(IModel<FeedbackMessage> mFeedbackMessage, Page page) {
		FeedbackMessage feedbackMessage = mFeedbackMessage.getObject();
		Databinder.getHibernateSession().update(feedbackMessage);
		cwmService.flushChanges();
		String pageName = page instanceof ISIBasePage ? ((ISIBasePage)page).getPageName() : null;
		eventService.saveEvent("message:view", "Message Id: " + String.valueOf(feedbackMessage.getId()), pageName);
	}

	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#getTeacherNotes(org.apache.wicket.model.IModel)
	 */
	@SuppressWarnings("unchecked")
	public List<ISIPrompt> getTeacherNotes(IModel<User> teacher) {
        /*
		Criteria promptCriteria = Databinder.getHibernateSession()
			.createCriteria(ISIPrompt.class)
			.createAlias("responses", "r")
		    .add(Restrictions.eq("type", PromptType.TEACHER_NOTES))
			.add(Restrictions.eq("r.user", teacher.getObject()))
			.add(Restrictions.eq("r.valid", true))
			.setCacheable(true)
			.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		return promptCriteria.list();
        */
        // TODO heikki verify this is a correct replacement for the above
        Query q = Databinder.getHibernateSession().createQuery("select r.prompt from Response r where r.prompt.type=:type and r.valid='true' and r.user.id=:userId");
        q.setString("type", PromptType.TEACHER_NOTES.name());
        q.setLong("userId", teacher.getObject().getId());
        return q.list();
	}

	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#getPagesWithNotes(org.cast.cwm.data.User, boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<String> getPagesWithNotes(User student, boolean isUnread) {
		if (student.getRole().equals(Role.GUEST))
			return Collections.EMPTY_LIST;
		
		Query q = Databinder.getHibernateSession().getNamedQuery("FeedbackMessage.getPagesWithNotesByStudentAndUnreadStatus");
		q.setParameter("student", student);
		q.setParameter("isUnread", isUnread);
		q.setParameter("role", ISISession.get().getUser().getRole());
		q.setCacheable(true);
		return q.list();
	}

	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#getPagesWithNotes(org.cast.cwm.data.User)
	 */
	@SuppressWarnings("unchecked")
	public List<String> getPagesWithNotes(User student) {
		if (student.getRole().equals(Role.GUEST))
			return Collections.EMPTY_LIST;
		
		Query q = Databinder.getHibernateSession().getNamedQuery("FeedbackMessage.getPagesWithNotesByStudent");
		q.setParameter("student", student);
		q.setCacheable(true);
		return q.list();
	}
	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#getNotesForPage(org.cast.cwm.data.User, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<FeedbackMessage> getNotesForPage(User student, String loc) {
		if (student.getRole().equals(Role.GUEST))
			return Collections.EMPTY_LIST;

		Query q = Databinder.getHibernateSession().getNamedQuery("FeedbackMessage.getNotesForPage");
		q.setParameter("student", student);
		q.setParameter("loc", loc);
		q.setCacheable(true);
		return q.list();
	}
	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#locationOfFirstUnreadMessage(org.cast.cwm.data.User, org.cast.isi.ISIXmlSection)
	 */
	public String locationOfFirstUnreadMessage(User student, ISIXmlSection sec) {
		List<String> locations = new ArrayList<String>();
		if (sec.hasChildren()) {
			for (XmlSection sec3 : sec.getChildren()) {
				locations.add((new ContentLoc(sec3)).getLocation());
			}
		} else {
			locations.add(new ContentLoc(sec).getLocation());
		}
		Query q = Databinder.getHibernateSession().getNamedQuery("FeedbackMessage.getFirstUnreadMessageByStudentAndSection");
		q.setParameter("student", student);
		q.setParameterList("locationList", locations);
		q.setMaxResults(1);
		q.setCacheable(true);
		if (q.list().isEmpty())
			return null;
		FeedbackMessage m = (FeedbackMessage) q.list().get(0);
		return m.getLocation();
	}
	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#getAllNotebookResponsesByStudent(org.apache.wicket.model.IModel<org.cast.cwm.data.User>)
	 */
	public IModel<List<ISIResponse>> getAllNotebookResponsesByStudent (IModel<User> student) {
		ISIResponseCriteriaBuilder builder = new ISIResponseCriteriaBuilder();
		builder.setUserModel(student);
		builder.setInNotebook(true);
		builder.setOrderByNotebookInsert(true);
		// TODO: Extend ResponseListModel?  How do you resolve the return type of Response vs. ISIResponse?
		return new HibernateListModel<ISIResponse>(ISIResponse.class, builder);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#getAllWhiteboardResponses()
	 */
	@SuppressWarnings("unchecked")
	public List<ISIResponse> getAllWhiteboardResponses() {
		Query q = Databinder.getHibernateSession().getNamedQuery("ISIResponse.getAllWhiteboardResponses");
		q.setCacheable(true);
		return q.list();
	}
	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#getWhiteboardResponsesByPeriod(org.cast.cwm.data.Period)
	 */
	@SuppressWarnings("unchecked")
	public List<ISIResponse> getWhiteboardResponsesByPeriod(Period p) {
		Query q = Databinder.getHibernateSession().getNamedQuery("ISIResponse.getPeriodWhiteboardResponses");
		q.setParameter("period", p);
		q.setCacheable(true);
		return q.list();
	}
	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#clearWhiteboardForPeriod(org.cast.cwm.data.Period)
	 */
	public void clearWhiteboardForPeriod(Period p) {
		List<ISIResponse> list = getWhiteboardResponsesByPeriod(p);
		for (ISIResponse r : list) {
			r.setInWhiteboard(false);
			r.setWhiteboardInsertTime(null);
		}
		eventService.saveEvent("whiteboard:clear", "Period: "+p.getId(), null);
		cwmService.flushChanges();
	}

	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#addToWhiteboard(org.cast.isi.data.ISIResponse, org.apache.wicket.Page)
	 */
	public void addToWhiteboard(ISIResponse resp, Page page) {
		resp.setInWhiteboard(true);
		resp.setWhiteboardInsertTime(new Date());
		String pageName = page instanceof ISIBasePage ? ((ISIBasePage)page).getPageName() : null;
		eventService.saveEvent("whiteboard:add", String.valueOf(resp.getId()), pageName);
		cwmService.flushChanges();
	}
	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#removeFromWhiteboard(org.cast.isi.data.ISIResponse, org.apache.wicket.Page)
	 */
	public void removeFromWhiteboard(ISIResponse resp, Page page) {
		resp.setInWhiteboard(false);
		resp.setWhiteboardInsertTime(null);
		String pageName = page instanceof ISIBasePage ? ((ISIBasePage)page).getPageName() : null;
		eventService.saveEvent("whiteboard:remove", String.valueOf(resp.getId()), pageName);
		cwmService.flushChanges();
	}

	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#addToNotebook(org.cast.isi.data.ISIResponse, org.apache.wicket.Page)
	 */
	public void addToNotebook(ISIResponse resp, Page page) {
		resp.setInNotebook(true);
		resp.setNotebookInsertTime(new Date());
		String pageName = page instanceof ISIBasePage ? ((ISIBasePage)page).getPageName() : null;
		eventService.saveEvent("notebook:add", String.valueOf(resp.getId()), pageName);
		cwmService.flushChanges();
	}

	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#removeFromNotebook(org.cast.isi.data.ISIResponse, org.apache.wicket.Page)
	 */
	public void removeFromNotebook(ISIResponse resp, Page page) {
		resp.setInNotebook(false);
		resp.setNotebookInsertTime(null);
		String pageName = page instanceof ISIBasePage ? ((ISIBasePage)page).getPageName() : null;
		eventService.saveEvent("notebook:remove", String.valueOf(resp.getId()), pageName);
		cwmService.flushChanges();
	}

	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#getResponseCollectionNames(org.apache.wicket.model.IModel<org.cast.cwm.data.User>)
	 */

	@SuppressWarnings("unchecked")
	public List<String> getResponseCollectionNames(IModel<User> mUser) {
		//Query q = Databinder.getHibernateSession().createQuery("select distinct(p.collectionName) " +
		//		"from ISIPrompt p join p.responses r where p.collectionName is not null and r.valid='true' and r.user.id=:userId");
        // TODO heikki verify query is OK
        Query q = Databinder.getHibernateSession().createQuery("select distinct(r.prompt.collectionName) from Response r where r.prompt.collectionName is not null and r.valid='true' and r.user.id=:userId");

		q.setLong("userId", mUser.getObject().getId());
		q.setCacheable(true);
		return q.list();
	}

	
	/**
	 * Generate the prompts for this user's responseCollections
	 * @param mUser
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<ISIPrompt> getResponseCollectionNamePrompts(IModel<User> mUser) {
	
        Query q = Databinder.getHibernateSession().createQuery("select distinct(r.prompt) from Response r where r.prompt.collectionName is not null and r.prompt.collectionName !='' and r.valid='true' and r.user.id=:userId");

		q.setLong("userId", mUser.getObject().getId());
		q.setCacheable(true);
		return q.list();
	}
	
	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#getResponseCollectionPrompts(org.apache.wicket.model.IModel<org.cast.cwm.data.User>, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<ISIPrompt> getResponseCollectionPrompts(IModel<User> mUser, String collectionName) {
/*
		Criteria promptCriteria = Databinder.getHibernateSession()
			.createCriteria(ISIPrompt.class)
			.createAlias("responses", "r")
		    .add(Restrictions.eq("collectionName", collectionName))
			.add(Restrictions.eq("r.user", mUser.getObject()))
			.add(Restrictions.eq("r.valid", true))
			.setCacheable(true)
			.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return promptCriteria.list();
*/
        // TODO heikki verify this is a correct replacement for the above
        Query q = Databinder.getHibernateSession().createQuery("select r.prompt from Response r where r.prompt.collectionName=:collectionName and r.valid='true' and r.user.id=:userId");
        q.setString("collectionName", collectionName);
        q.setLong("userId", mUser.getObject().getId());
        return q.list();
	}

	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#getAllResponsesForPromptByStudent(org.apache.wicket.model.IModel<org.cast.cwm.data.Prompt>, org.apache.wicket.model.IModel<org.cast.cwm.data.User>)
	 */
	@SuppressWarnings("unchecked")
	public List<ISIResponse> getAllResponsesForPromptByStudent(
			IModel<Prompt> mPrompt, IModel<User> mUser) {
		Criteria criteria = Databinder.getHibernateSession()
				.createCriteria(ISIResponse.class)
				.add(Restrictions.eq("prompt", mPrompt.getObject()))
				.add(Restrictions.eq("user", mUser.getObject()))
				.add(Restrictions.eq("valid", true))
				.setCacheable(true)
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		return criteria.list();
	}

	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#getAllResponsesForCollectionByStudent(String, org.apache.wicket.model.IModel<org.cast.cwm.data.User>)
	 */
	@SuppressWarnings("unchecked")
	public IModel<List<ISIResponse>> getAllResponsesForCollectionByStudent(
			String collectionName, UserModel mUser) {
		Criteria criteria = Databinder.getHibernateSession()
				.createCriteria(ISIResponse.class)
				.createAlias("prompt", "p")
				.add(Restrictions.eq("p.collectionName", collectionName))
				.add(Restrictions.eq("user", mUser.getObject()))
				.add(Restrictions.eq("valid", true))
				.setCacheable(true)
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		return new ListModel<ISIResponse>(criteria.list());
	}

	/* (non-Javadoc)
	 * @see org.cast.isi.service.IISIResponseService#gePeriodResponsesForPrompt(org.apache.wicket.model.IModel<org.cast.isi.data.ISIPrompt>,org.apache.wicket.model.IModel<org.cast.cwm.data.Period>)
	 */
	@SuppressWarnings("unchecked")
	public IModel<List<ISIResponse>> getPeriodResponsesForPrompt(IModel<Prompt> mPrompt, IModel<Period> mPeriod) {
		Criteria criteria = Databinder.getHibernateSession()
				.createCriteria(ISIResponse.class)
				.createAlias("user", "user")
				.createAlias("user.periods", "p" )
				.add(Restrictions.eq("p.id", mPeriod.getObject().getId()))
				.add(Restrictions.eq("prompt", mPrompt.getObject()))
				.add(Restrictions.eq("valid", true))
				.setCacheable(true)
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		return new ListModel<ISIResponse>(criteria.list());
	}

}
