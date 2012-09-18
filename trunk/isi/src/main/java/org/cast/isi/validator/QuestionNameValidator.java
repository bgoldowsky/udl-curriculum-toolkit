package org.cast.isi.validator;

import net.databinder.hib.Databinder;

import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.cast.isi.ISISession;
import org.cast.isi.data.Question;
import org.cast.isi.service.IQuestionService;

import com.google.inject.Inject;

public class QuestionNameValidator extends AbstractValidator<String> {
	
	private Long questionId;
	
	@Inject
	private IQuestionService questionService;

	private static final long serialVersionUID = 1L;
	
	public QuestionNameValidator(Question question) {
		super();
		if (question != null)
			this.questionId = question.getId();
		else
			questionId = 0L;
		InjectorHolder.getInjector().inject(this);
	}

	@Override
	protected void onValidate(IValidatable<String> validatable) {
		String questionText = validatable.getValue();
		Question other = questionService.getByTextAndStudent(questionText, ISISession.get().getUser());
		if (other != null && !other.getId().equals(questionId)) {
			error(validatable);
		} else {
			Databinder.getHibernateSession().evict(other); // Evict "other" person in case they are the same object
		}
	}

}	
