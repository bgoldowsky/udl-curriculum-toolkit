/*
 * Copyright 2011 CAST, Inc.
 *
 * This file is part of the CAST Wicket Modules:
 * see <http://code.google.com/p/cast-wicket-modules>.
 *
 * The CAST Wicket Modules are free software: you can redistribute and/or
 * modify them under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * The CAST Wicket Modules are distributed in the hope that they will be
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
import org.cast.cwm.service.EventService;
import org.cast.isi.data.ISIPrompt;
import org.cast.isi.data.PromptType;
import org.cast.isi.data.Question;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuestionService {
	private static QuestionService instance = new QuestionService();
	private static final Logger log = LoggerFactory.getLogger(QuestionService.class);
	
	public static QuestionService get() {
		return instance;
	}
	
	/**
	 * Get all of the questions for this user.
	 * Questions could be owned by the user or have a null owner
	 * indicating they are used by all users.	 *
	 */
	public List<Question> getQuestionsByUser (long userId) {
		Query q = Databinder.getHibernateSession()
			.createQuery("from Question q where (q.owner=:userId or q.owner is null) and active=true order by q.owner desc")
			.setLong("userId", userId)
			.setCacheable(true);
		List<Question> questions = q.list();
		Set<Question> set = new LinkedHashSet<Question>(questions);
		questions.clear();
		questions.addAll(set); 
		return questions;		
	}
	
	/**
	 * Get question by id
	 */
//	public Question getQuestionById (long id) {
//		return (Question) Databinder.getHibernateSession().get(Question.class, id);
//	}

	/**
	 * Get question model by id
	 */
	public HibernateObjectModel<Question> getQuestionModelById (long id) {
		return new HibernateObjectModel<Question>(Question.class, id);
	}
	
	/**
	 * Create the new question and associated prompt
	 */
	public void createQuestion(UserModel mOwner, String text, String pageName) {
		Session session = Databinder.getHibernateSession();
		Question question = new Question(text, mOwner.getObject(), null);
		ISIPrompt prompt = new ISIPrompt(PromptType.MY_QUESTIONS);
		if (mOwner != null) { 
			prompt.setTargetUser(mOwner.getObject());
			EventService.get().saveEvent("question:create", text, pageName);
		}
		session.save(prompt);	
		question.setPrompt(prompt);
		session.save(question);
		CwmService.get().flushChanges();
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
	
	/**
	 * Return a single Question with the specified text value and author.  Useful for checking to see
	 * if a user has already created a question with that text.
	 * 
	 * @param text
	 * @param user
	 * @return
	 */
	public Question getByTextAndStudent(String text, User user) {
		Query q = Databinder.getHibernateSession().createQuery("From Question q where q.owner=:user and q.text=:text and active=true");
		q.setParameter("user", user);
		q.setParameter("text", text);
		return (Question) q.uniqueResult();
	}
	
	public void deleteQuestion (IModel<Question> mQuestion, String pageName) {
		Question q = mQuestion.getObject();
		q.setActive(false);
		Session session = Databinder.getHibernateSession();
		Databinder.getHibernateSession().update(q);
		CwmService.get().flushChanges();			
		EventService.get().saveEvent("question:delete", q.getText() 
				+ " (" + q.getId() + ")", pageName);	
	}

	public void updateQuestion (Question question, String pageName) {
		Question q = question;
		Session session = Databinder.getHibernateSession();
		Databinder.getHibernateSession().update(q);
		CwmService.get().flushChanges();			
		EventService.get().saveEvent("question:namechange", question.getText() 
				+ " (" + question.getId() + ")", pageName);	
		}
}