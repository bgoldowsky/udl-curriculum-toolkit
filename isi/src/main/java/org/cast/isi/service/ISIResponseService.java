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
import java.util.Date;
import java.util.List;
import java.util.Set;

import net.databinder.hib.Databinder;
import net.databinder.models.hib.HibernateListModel;
import net.databinder.models.hib.HibernateObjectModel;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.model.IModel;
import org.cast.cwm.CwmApplication;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.Response;
import org.cast.cwm.data.ResponseData;
import org.cast.cwm.data.User;
import org.cast.cwm.data.models.PromptModel;
import org.cast.cwm.data.models.UserModel;
import org.cast.cwm.service.CwmService;
import org.cast.cwm.service.EventService;
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


/**
 * Methods for teacher administrative duties: Flagging students, creating class messages, etc.
 * 
 * TODO: Many of these methods should be swapped to cwm-data standards or removed.
 * @author jbrookover
 *
 */
public class ISIResponseService extends ResponseService {
	
//	private static Map<ThumbNailLookup, byte[]> thumbNails;
//	private static final int MAX_THUMBNAIL_ENTRIES = 1000;
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ISIResponseService.class);
	
	protected ISIResponseService() {/* Protected Constructor - use ISIResponseService.get() */}
	
	public static ISIResponseService get() {
		return (ISIResponseService) ResponseService.get();
	}
	
	/**
	 * Use this Service class.  Called in {@link Application#init()}.
	 */
	public static void useAsServiceInstance() {
		ResponseService.instance = new ISIResponseService();
	}
		
	public IModel<Prompt> getOrCreatePrompt(PromptType type) {
		return genericGetOrCreatePrompt(type, null, null, null, null, null);
	}
	
	public IModel<Prompt> getOrCreatePrompt(PromptType type, String identifier) {
		return genericGetOrCreatePrompt(type, null, null, null, identifier, null);
	}
	public IModel<Prompt> getOrCreatePrompt(PromptType type, ContentLoc loc) {
		return genericGetOrCreatePrompt(type, loc, null, null, null, null);
	}
	
	public IModel<Prompt> getOrCreatePrompt(PromptType type, ContentLoc loc, String xmlId) {
		return genericGetOrCreatePrompt(type, loc, xmlId, null, null, null);
	}
	
	public IModel<Prompt> getOrCreatePrompt(PromptType type, IModel<User> targetUser) {
		return genericGetOrCreatePrompt(type, null, null, targetUser, null, null);
	}

	public IModel<Prompt> getOrCreatePrompt(PromptType type, IModel<User> targetUser, String identifier) {
		return genericGetOrCreatePrompt(type, null, null, targetUser, identifier, null);
	}

	
	public IModel<Prompt> getOrCreatePrompt(PromptType type, ContentLoc loc, String xmlId, String collectionName) {
		return genericGetOrCreatePrompt(type, loc, xmlId, null, null, collectionName);
	}

	public IModel<Prompt> getOrCreateHighlightPrompt(PromptType highlightlabel, ContentLoc loc, String color) {
		return genericGetOrCreatePrompt(highlightlabel, loc, null, null, color, null);
	}

	
	public synchronized IModel<Prompt> genericGetOrCreatePrompt(PromptType type, ContentLoc loc, String xmlId, IModel<User> targetUser, String identifier, String collectionName) {
		
		IModel<ContentElement> ce = getOrCreateContentElement(loc, xmlId);
		
		Criteria c = Databinder.getHibernateSession().createCriteria(ISIPrompt.class)
		.add(Restrictions.eq("type", type))
		.setCacheable(true);
		
		if (ce != null)
			c.add(Restrictions.eq("contentElement", ce.getObject()));
		if (targetUser != null)
			c.add(Restrictions.eq("targetUser", targetUser.getObject()));
		if (identifier != null)
			c.add(Restrictions.eq("identifier", identifier));

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
		
		CwmService.get().flushChanges();
		
		return new PromptModel(p);
	}
	
	/**
	 * Returns a persisted ContentElement object for the given ContentLoc.
	 * @param loc ContentLoc of a page
	 * 
	 * @return model of ContentElement, created if necessary and saved to DB
	 */
	public IModel<ContentElement> getOrCreateContentElement(ContentLoc loc) {
		return getOrCreateContentElement(loc, null);
	}
	
	/**
	 * Return a persisted ContentElement for the given page ContentLoc and (optionally) XML ID within the page.
	 * Returns null if ContentLoc is null.
	 * 
	 * @param loc  ContentLoc of a page
	 * @param xmlId ID of element within the page
	 * @return model of ContentElement, created if necessary and saved to DB
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
		CwmService.get().flushChanges();
		
		return new HibernateObjectModel<ContentElement>(e);
	}

	public IModel<Response> newPageHighlightsLabel(IModel<User> user, IModel<ISIPrompt> prompt) {
		return super.newResponse(user, CwmApplication.get().getResponseType("TEXT"), prompt);
	}

	
	/**
	 * Save the response for a multiple choice response
	 * 
	 * @param mResponse
	 * @param text
	 * @param correct
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

	public IModel<Response> newSingleSelectResponse(IModel<User> user, IModel<Prompt> model) {
		return super.newResponse(user, CwmApplication.get().getResponseType("SINGLE_SELECT"), model);
	}

	/**
	 * A set of methods for saving a response object with the appropriate type for these custom responses.
	 */	
	public void saveHighlighterLabel(IModel<Response> response, String label) {
		super.saveTextResponse(response, label, null);
	}
	
	/**
	 * Set the message/announcement for a given Period.
	 * 
	 * @param p - the period
	 * @param s - the message
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
		CwmService.get().flushChanges();
		EventService.get().saveEvent("classmessage:create", "Period Id: " + p.getId() + " Message Id: " + m.getId(), null);		
	}
	
	/**
	 * Get the message/announcement for a given period that is marked current.
	 * 
	 * @param p - the period
	 * @return
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
	
	
	/**
	 * Marks the current message/announcement for a period as not-current.  It remains in the database, but is not displayed.
	 * 
	 * @param p
	 */
	public void deleteClassMessage(IModel<Period> mPeriod) {
		
		Period p = mPeriod.getObject();
		ClassMessage m = getClassMessage(mPeriod);
		if (m != null) {
			m.setCurrent(false);
			CwmService.get().flushChanges();
			EventService.get().saveEvent("classmessage:delete", "Period Id: " + p.getId() + " Message Id: " + m.getId(), null);
		}
	}

	/**
	 * This is an expensive method as the Event table is a very large table.  Call this only
	 * once per session as part of getting the bookmark.
	 * 
	 * @param person
	 * @param type
	 * @return Most recent event for Person of a particular type
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
		CwmService.get().flushChanges();
		EventService.get().saveEvent("flag", note, null);
	}
	
	public void toggleFlag(User person) {
		toggleFlag(person, null);
	}
	
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
	
	public boolean isFlagged(User person) {
		return isFlagged(person, null);
	}
	
	@SuppressWarnings("unchecked")
	public List<StudentFlag> getAllFlags () {
		Query q = Databinder.getHibernateSession().getNamedQuery("StudentFlag.queryByFlagger");
		q.setParameter("flagger", ISISession.get().getUser());
		q.setCacheable(true);
		return q.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<FeedbackMessage> getFeedbackMessages(IModel<Prompt> promptM, User student) {
		// FIXME needs to query by prompt
		Query q = Databinder.getHibernateSession().getNamedQuery("FeedbackMessage.getAllMessagesByPromptAndStudent");
		q.setParameter("prompt", promptM.getObject());
		q.setParameter("student", student);
		q.setCacheable(true);
		return q.list();
	}
	
	public void deleteFeedbackMessage(IModel<FeedbackMessage> mFeedbackMessage) {
		FeedbackMessage feedbackMessage = mFeedbackMessage.getObject();
		feedbackMessage.setVisible(false);
		Databinder.getHibernateSession().update(feedbackMessage);
		CwmService.get().flushChanges();
		EventService.get().saveEvent("feedback:delete", "Message Id: " + feedbackMessage.getId(), null);
	}

	public void updateFeedbackMessage(IModel<FeedbackMessage> mFeedbackMessage, Page page) {
		FeedbackMessage feedbackMessage = mFeedbackMessage.getObject();
		Databinder.getHibernateSession().update(feedbackMessage);
		CwmService.get().flushChanges();
		String pageName = page instanceof ISIBasePage ? ((ISIBasePage)page).getPageName() : null;
		EventService.get().saveEvent("message:view", "Message Id: " + String.valueOf(feedbackMessage.getId()), pageName);
	}

	/**
	 * @param teacher
	 * @return list of prompts
	 * 
	 * Get all of the active teacher notes written by this teacher.
	 */
	@SuppressWarnings("unchecked")
	public List<ISIPrompt> getTeacherNotes(IModel<User> teacher) {
		Criteria promptCriteria = Databinder.getHibernateSession()
			.createCriteria(ISIPrompt.class)
			.createAlias("responses", "r")
		    .add(Restrictions.eq("type", PromptType.TEACHER_NOTES))
			.add(Restrictions.eq("r.user", teacher.getObject()))
			.add(Restrictions.eq("r.valid", true))
			.setCacheable(true)
			.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		return promptCriteria.list();
	}

	
	@SuppressWarnings("unchecked")
	public List<String> getPagesWithNotes(User student, boolean isUnread) {
		Query q = Databinder.getHibernateSession().getNamedQuery("FeedbackMessage.getPagesWithNotesByStudentAndUnreadStatus");
		q.setParameter("student", student);
		q.setParameter("isUnread", isUnread);
		q.setParameter("role", ISISession.get().getUser().getRole());
		q.setCacheable(true);
		return q.list();
	}

	
	@SuppressWarnings("unchecked")
	public List<String> getPagesWithNotes(User student) {
		Query q = Databinder.getHibernateSession().getNamedQuery("FeedbackMessage.getPagesWithNotesByStudent");
		q.setParameter("student", student);
		q.setCacheable(true);
		return q.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<FeedbackMessage> getNotesForPage(User student, String loc) {
		Query q = Databinder.getHibernateSession().getNamedQuery("FeedbackMessage.getNotesForPage");
		q.setParameter("student", student);
		q.setParameter("loc", loc);
		q.setCacheable(true);
		return q.list();
	}
	
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
	
	public IModel<List<ISIResponse>> getAllNotebookResponsesByStudent (IModel<User> student) {
		ISIResponseCriteriaBuilder builder = new ISIResponseCriteriaBuilder();
		builder.setUserModel(student);
		builder.setInNotebook(true);
		builder.setOrderByNotebookInsert(true);
		// TODO: Extend ResponseListModel?  How do you resolve the return type of Response vs. ISIResponse?
		return new HibernateListModel<ISIResponse>(ISIResponse.class, builder);
	}
	
	@SuppressWarnings("unchecked")
	public List<ISIResponse> getAllWhiteboardResponses() {
		Query q = Databinder.getHibernateSession().getNamedQuery("ISIResponse.getAllWhiteboardResponses");
		q.setCacheable(true);
		return q.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<ISIResponse> getWhiteboardResponsesByPeriod(Period p) {
		Query q = Databinder.getHibernateSession().getNamedQuery("ISIResponse.getPeriodWhiteboardResponses");
		q.setParameter("period", p);
		q.setCacheable(true);
		return q.list();
	}
	
	public void clearWhiteboardForPeriod(Period p) {
		List<ISIResponse> list = getWhiteboardResponsesByPeriod(p);
		for (ISIResponse r : list) {
			r.setInWhiteboard(false);
			r.setWhiteboardInsertTime(null);
		}
		EventService.get().saveEvent("whiteboard:clear", "Period: "+p.getId(), null);
		CwmService.get().flushChanges();
	}

	public void addToWhiteboard(ISIResponse resp, Page page) {
		resp.setInWhiteboard(true);
		resp.setWhiteboardInsertTime(new Date());
		String pageName = page instanceof ISIBasePage ? ((ISIBasePage)page).getPageName() : null;
		EventService.get().saveEvent("whiteboard:add", String.valueOf(resp.getId()), pageName);
		CwmService.get().flushChanges();
	}
	
	public void removeFromWhiteboard(ISIResponse resp, Page page) {
		resp.setInWhiteboard(false);
		resp.setWhiteboardInsertTime(null);
		String pageName = page instanceof ISIBasePage ? ((ISIBasePage)page).getPageName() : null;
		EventService.get().saveEvent("whiteboard:remove", String.valueOf(resp.getId()), pageName);
		CwmService.get().flushChanges();
	}

	public void addToNotebook(ISIResponse resp, Page page) {
		resp.setInNotebook(true);
		resp.setNotebookInsertTime(new Date());
		String pageName = page instanceof ISIBasePage ? ((ISIBasePage)page).getPageName() : null;
		EventService.get().saveEvent("notebook:add", String.valueOf(resp.getId()), pageName);
		CwmService.get().flushChanges();
	}

	public void removeFromNotebook(ISIResponse resp, Page page) {
		resp.setInNotebook(false);
		resp.setNotebookInsertTime(null);
		String pageName = page instanceof ISIBasePage ? ((ISIBasePage)page).getPageName() : null;
		EventService.get().saveEvent("notebook:remove", String.valueOf(resp.getId()), pageName);
		CwmService.get().flushChanges();
	}

	/**
	 * Get the Response Collections
	 */

	@SuppressWarnings("unchecked")
	public List<String> getResponseCollectionNames(UserModel mUser) {
		Query q = Databinder.getHibernateSession().createQuery("select distinct(p.collectionName) " +
				"from ISIPrompt p join p.responses r where p.collectionName is not null and r.user.id=:userId"); 
		q.setLong("userId", mUser.getObject().getId());
		q.setCacheable(true);
		return q.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<ISIPrompt> getResponseCollectionPrompts(UserModel mUser, String collectionName) {
		Criteria promptCriteria = Databinder.getHibernateSession()
			.createCriteria(ISIPrompt.class)
			.createAlias("responses", "r")
		    .add(Restrictions.eq("collectionName", collectionName))
			.add(Restrictions.eq("r.user", mUser.getObject()))
			.add(Restrictions.eq("r.valid", true))
			.setCacheable(true)
			.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		return promptCriteria.list();
	}


	/*
	public byte[] resizeImage(ExtendedResponseDatum er, String type, int maxW, int maxH) {
		
		BufferedImage img = null;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ThumbNailLookup id = new ThumbNailLookup(er.getId(), maxW, maxH);
		if (ISIService.thumbNails.containsKey(id))
			return ISIService.thumbNails.get(id);

		if (type == null || type.equals(""))
			type = er.getMimeType().getSubType();
		try {
			img = ImageIO.read(new ByteArrayInputStream(er.getBinaryData()));
		} catch (IOException e) {
			img = null;
			e.printStackTrace();
		}
		if (img != null) {
			int w = img.getWidth();
			int h = img.getHeight();
			if (w > maxW) {
				float factor = (float) maxW / w;
				w = (int) (w * (factor));
				h = (int) (h * (factor));
			}
			if (h > maxH) {
				float factor = (float) maxH / h;
				w = (int) (w * (factor));
				h = (int) (h * (factor));
			}
			BufferedImage resized = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = resized.createGraphics();
			g.drawImage(img, 0, 0, w, h, null);
			try {
				ImageIO.write(resized, type, out);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		ISIService.thumbNails.put(id, out.toByteArray());
		return out.toByteArray();
	}
	
	private class ThumbNailLookup {
		
		private long id;
		private int width;
		private int height;
		
		public ThumbNailLookup(long i, int w, int h) {
			this.id = i;
			this.width = w;
			this.height = h;
		}
		
		@Override
		public int hashCode() {
			return new HashCodeBuilder(19, 23).
			append(id).
			append(width).
			append(height).
			toHashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == null) { return false; }
			if (obj == this) { return true; }
			if (obj.getClass() != getClass()) {
				return false;
			}
			ThumbNailLookup rhs = (ThumbNailLookup) obj;
			return new EqualsBuilder()
			.append(id, rhs.id)
			.append(width, rhs.width)
			.append(height, rhs.height)
			.isEquals();
		}

	}
*/
}
