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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.databinder.hib.Databinder;
import net.databinder.models.hib.HibernateObjectModel;

import org.apache.wicket.model.IModel;
import org.cast.cwm.data.User;
import org.cast.cwm.data.models.UserModel;
import org.cast.cwm.service.CwmService;
import org.cast.cwm.service.IEventService;
import org.cast.isi.data.ISIPrompt;
import org.cast.isi.data.PromptType;
import org.cast.isi.data.Question;
import org.hibernate.Query;
import org.hibernate.classic.Session;

import com.google.inject.Inject;

public class QuestionService implements IQuestionService {

	@Inject
	private IEventService eventService;

	@Inject
	private CwmService cwmService;

	/* (non-Javadoc)
	 * @see org.cast.isi.service.IQuestionService#getQuestionsByUser(long)
	 */
	public List<Question> getQuestionsByUser (long userId) {
		Query q = Databinder.getHibernateSession()
			.createQuery("from Question q where (q.owner=:userId or q.owner is null) and active=true order by q.owner desc")
			.setLong("userId", userId)
			.setCacheable(true);
		@SuppressWarnings("unchecked")
		List<Question> questions = q.list();
		Set<Question> set = new LinkedHashSet<Question>(questions);
		questions.clear();
		questions.addAll(set); 
		return questions;		
	}
	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.IQuestionService#getQuestionModelById(long)
	 */
	public HibernateObjectModel<Question> getQuestionModelById (long id) {
		return new HibernateObjectModel<Question>(Question.class, id);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.IQuestionService#createQuestion(org.cast.cwm.data.models.UserModel, java.lang.String, java.lang.String)
	 */
	public void createQuestion(UserModel mOwner, String text, String pageName) {
		Session session = Databinder.getHibernateSession();
		Question question = new Question(text, mOwner.getObject(), null);
		ISIPrompt prompt = new ISIPrompt(PromptType.MY_QUESTIONS);
		if (mOwner != null) { 
			prompt.setTargetUser(mOwner.getObject());
			eventService.saveEvent("question:create", text, pageName);
		}
		session.save(prompt);	
		question.setPrompt(prompt);
		session.save(question);
		cwmService.flushChanges();
	}
		
	/**
	 * Returns the number of questions this user has created.  If the user is null, returns the number
	 * of questions without owners.  This count includes deleted (inactive) questions.
	 * 
	 * @param p
	 * @return
	 */
//	public Integer getNumQuestions(User p) {
//		Criteria c = Databinder.getHibernateSession().createCriteria(Question.class);
//		if (p != null)
//			c.add(Restrictions.eq("owner", p));
//		else
//			c.add(Restrictions.isNull("owner"));
//		c.setProjection(Projections.rowCount());
//		return (Integer) c.uniqueResult();		
//	}
	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.IQuestionService#getByTextAndStudent(java.lang.String, org.cast.cwm.data.User)
	 */
	public Question getByTextAndStudent(String text, User user) {
		Query q = Databinder.getHibernateSession().createQuery("From Question q where q.owner=:user and q.text=:text and active=true");
		q.setParameter("user", user);
		q.setParameter("text", text);
		return (Question) q.uniqueResult();
	}
	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.IQuestionService#deleteQuestion(org.apache.wicket.model.IModel, java.lang.String)
	 */
	public void deleteQuestion (IModel<Question> mQuestion, String pageName) {
		Question q = mQuestion.getObject();
		q.setActive(false);
		Databinder.getHibernateSession().update(q);
		cwmService.flushChanges();			
		eventService.saveEvent("question:delete", q.getText() 
				+ " (" + q.getId() + ")", pageName);	
	}

	/* (non-Javadoc)
	 * @see org.cast.isi.service.IQuestionService#updateQuestion(org.cast.isi.data.Question, java.lang.String)
	 */
	public void updateQuestion (Question question, String pageName) {
		Question q = question;
		Databinder.getHibernateSession().update(q);
		cwmService.flushChanges();			
		eventService.saveEvent("question:namechange", question.getText() 
				+ " (" + question.getId() + ")", pageName);	
	}

}