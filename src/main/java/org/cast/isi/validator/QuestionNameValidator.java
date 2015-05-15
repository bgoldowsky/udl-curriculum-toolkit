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
package org.cast.isi.validator;

import com.google.inject.Inject;
import net.databinder.hib.Databinder;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.cast.isi.ISISession;
import org.cast.isi.data.Question;
import org.cast.isi.service.IQuestionService;

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
        Injector.get().inject(this);
	}

	@Override
	protected void onValidate(IValidatable<String> validatable) {
		String questionText = validatable.getValue();
		Question other = questionService.getByTextAndStudent(questionText, ISISession.get().getUser());
		if (other != null && !other.getId().equals(questionId)) {
			error(validatable);
		} else if(other != null) {
			Databinder.getHibernateSession().evict(other); // Evict "other" person in case they are the same object
		}
	}

}	
