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
package org.cast.isi.data;

import com.google.inject.Inject;
import lombok.Getter;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.User;
import org.cast.isi.ISIXmlSection;
import org.cast.isi.service.ISectionService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A simple data holder for score counts used in summary pages
 * 
 * @author droby
 *
 */
@Getter
public class ScoreCounts implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final String CONTEXT_STUDENTS = "students";
	private static final String CONTEXT_QUESTIONS = "questions";

	private String context;
	private int correct;
	private int incorrect;
	private int unscored;
	private int total;

	public ScoreCounts(String context, int correct, int incorrect, int unscored, int total) {
		this.context = context;
		this.correct = correct;
		this.incorrect = incorrect;
		this.unscored = unscored;
		this.total = total;
	}
	
	public String formatSummary() {
		return String.format(
			"Total: %d %s - %d correct, %d incorrect, %d unscored", 
			getTotal(),
			getContext(),
			getCorrect(), 
			getIncorrect(),
			getUnscored());
	}
	
	public int getPercentCorrect() {
		int totalTried = correct + incorrect;
		if (totalTried == 0)
			return 100;
		return (int) (Math.round((100.0 * correct) / totalTried));
	}
	
	public static ScoreCounts forResponses(String context, IModel<List<ISIResponse>> responses) {
		if (CONTEXT_QUESTIONS.equals(context))
			return new CollectionScoreCounter(responses).count();
		else if (CONTEXT_STUDENTS.equals(context))
			return new StudentListScoreCounter(responses).count();
		else throw new IllegalArgumentException("Unsupported context:" + context);
	}
	
	private static abstract class ScoreCounter {

		@Inject
		private ISectionService sectionService;
		
		public ScoreCounter() {
            Injector.get().inject(this);
		}

		public abstract ScoreCounts count();

		protected Integer scoreForPromptAndUser(ISIPrompt prompt,
				ISIResponse firstResponse, User user) {
			if (prompt.isDelayFeedback() && needsReview(prompt, user))
				return null;
			if (PromptType.SINGLE_SELECT == prompt.getType()) 
				return firstResponse.getResponseData().getScore();
			if (PromptType.RESPONSEAREA == prompt.getType())
				return firstResponse.getScore();
			return null;
		}
		
		private boolean needsReview(ISIPrompt prompt, User user) {
			ISIXmlSection section = prompt.getSection();
			SectionStatus sectionStatus = sectionService.getSectionStatus(user, section);
			return (sectionStatus == null) || !sectionStatus.getReviewed();
		}

	}
	
	private static class CollectionScoreCounter extends ScoreCounter {

		private Map<ISIPrompt, List<ISIResponse>> responseMap;
		
		public CollectionScoreCounter(IModel<List<ISIResponse>> responses) {
			super();
			responseMap = mapResponses(responses.getObject());
			verifySameUser();
		}

		public ScoreCounts count() {
			int correctCount = 0;
			int incorrectCount = 0;
			int unscoredCount = 0;
			for (Entry<ISIPrompt, List<ISIResponse>> entry: responseMap.entrySet()) {
				Integer score = score(entry);
				if (score == null)
					unscoredCount++;
				else if (score == 0)
					incorrectCount++;
				else
					correctCount++;
			}
			return new ScoreCounts(CONTEXT_QUESTIONS, correctCount, incorrectCount, unscoredCount, responseMap.size());
		}

		private Integer score(Entry<ISIPrompt, List<ISIResponse>> entry) {
			ISIPrompt prompt = entry.getKey();
			List<ISIResponse> responses = entry.getValue();
			if (responses.isEmpty())
				return null;
			ISIResponse firstResponse = responses.get(0);
			User user = firstResponse.getUser();
			return scoreForPromptAndUser(prompt, firstResponse, user);
		}

		private Map<ISIPrompt, List<ISIResponse>> mapResponses(
				List<ISIResponse> responses) {
			Map<ISIPrompt, List<ISIResponse>> result = new HashMap<ISIPrompt, List<ISIResponse>>();
			for (ISIResponse response: responses) {
				ISIPrompt prompt = (ISIPrompt) response.getPrompt();
				if (!(result.containsKey(prompt))) {
					result.put(prompt, new ArrayList<ISIResponse>());
				}
				result.get(prompt).add(response);
			}
			return result;
		}
		
		private void verifySameUser() {
			Set<User> users = new HashSet<User>();
			for (List<ISIResponse> list: responseMap.values()) {
				for (ISIResponse response: list) {
					users.add(response.getUser());
				}
			}
			if (users.size() > 1)
				throw new IllegalArgumentException("Responses counted in a collection must all belong to the same user");
		}
		
	}

	private static class StudentListScoreCounter extends ScoreCounter {

		private Map<User, List<ISIResponse>> responseMap;

		public StudentListScoreCounter(IModel<List<ISIResponse>> responses) {
			super();
			responseMap = mapResponses(responses.getObject());
			verifySamePrompt();
		}

		public ScoreCounts count() {
			int correctCount = 0;
			int incorrectCount = 0;
			int unscoredCount = 0;
			for (Entry<User, List<ISIResponse>> entry: responseMap.entrySet()) {
				Integer score = score(entry);
				if (score == null)
					unscoredCount++;
				else if (score == 0)
					incorrectCount++;
				else
					correctCount++;
			}
			return new ScoreCounts(CONTEXT_STUDENTS, correctCount, incorrectCount, unscoredCount, responseMap.size());
		}

		private Integer score(Entry<User, List<ISIResponse>> entry) {
			User user = entry.getKey();
			List<ISIResponse> responses = entry.getValue();
			if (responses.isEmpty())
				return null;
			ISIResponse firstResponse = responses.get(0);
			ISIPrompt prompt = (ISIPrompt) firstResponse.getPrompt();
			return scoreForPromptAndUser(prompt, firstResponse, user);
		}
		
		private Map<User, List<ISIResponse>> mapResponses(List<ISIResponse> responses) {
			Map<User, List<ISIResponse>> result = new HashMap<User, List<ISIResponse>>();
			for (ISIResponse response: responses) {
				User user = response.getUser();
				if (!(result.containsKey(user))) {
					result.put(user, new ArrayList<ISIResponse>());
				}
				result.get(user).add(response);
			}
			return result;
		}

		private void verifySamePrompt() {
			Set<Prompt> prompts = new HashSet<Prompt>();
			for (List<ISIResponse> list: responseMap.values()) {
				for (ISIResponse response: list) {
					prompts.add(response.getPrompt());
				}
			}
			if (prompts.size() > 1)
				throw new IllegalArgumentException("Responses counted for a list of students must all be for the same prompt");
		}
		
	}

}
