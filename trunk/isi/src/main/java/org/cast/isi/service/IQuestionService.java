package org.cast.isi.service;

import java.util.List;

import net.databinder.models.hib.HibernateObjectModel;

import org.apache.wicket.model.IModel;
import org.cast.cwm.data.User;
import org.cast.cwm.data.models.UserModel;
import org.cast.isi.data.Question;

public interface IQuestionService {

	/**
	 * Get all of the questions for this user.
	 * Questions could be owned by the user or have a null owner
	 * indicating they are used by all users.	 *
	 */
	public abstract List<Question> getQuestionsByUser(long userId);

	/**
	 * Get question model by id
	 */
	public abstract HibernateObjectModel<Question> getQuestionModelById(long id);

	/**
	 * Create the new question and associated prompt
	 */
	public abstract void createQuestion(UserModel mOwner, String text,
			String pageName);

	/**
	 * Return a single Question with the specified text value and author.  Useful for checking to see
	 * if a user has already created a question with that text.
	 * 
	 * @param text
	 * @param user
	 * @return
	 */
	public abstract Question getByTextAndStudent(String text, User user);

	public abstract void deleteQuestion(IModel<Question> mQuestion,
			String pageName);

	public abstract void updateQuestion(Question question, String pageName);

}