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

import org.apache.wicket.Page;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.Response;
import org.cast.cwm.data.User;
import org.cast.cwm.data.models.UserModel;
import org.cast.cwm.service.IResponseService;
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

public interface IISIResponseService extends IResponseService {

	IModel<Prompt> getOrCreatePrompt(PromptType type);

	IModel<Prompt> getOrCreatePrompt(PromptType type, String identifier);

	IModel<Prompt> getOrCreatePrompt(PromptType type, ContentLoc loc);

	IModel<Prompt> getOrCreatePrompt(PromptType type, ContentLoc loc,
			String xmlId);

	IModel<Prompt> getOrCreatePrompt(PromptType type, IModel<User> targetUser);

	IModel<Prompt> getOrCreatePrompt(PromptType type, IModel<User> targetUser,
			String identifier);

	IModel<Prompt> getOrCreatePrompt(PromptType type, ContentLoc loc,
			String xmlId, String collectionName);

	IModel<Prompt> getOrCreateHighlightPrompt(PromptType highlightlabel,
			String color);

	IModel<Prompt> genericGetOrCreatePrompt(PromptType type, ContentLoc loc,
			String xmlId, IModel<User> targetUser, String identifier,
			String collectionName);

	/**
	 * Returns a persisted ContentElement object for the given ContentLoc.
	 * @param loc ContentLoc of a page
	 * 
	 * @return model of ContentElement, created if necessary and saved to DB
	 */
	IModel<ContentElement> getOrCreateContentElement(ContentLoc loc);

	/**
	 * Return a persisted ContentElement for the given page ContentLoc and (optionally) XML ID within the page.
	 * Returns null if ContentLoc is null.
	 * 
	 * @param loc  ContentLoc of a page
	 * @param xmlId ID of element within the page
	 * @return model of ContentElement, created if necessary and saved to DB
	 */
	IModel<ContentElement> getOrCreateContentElement(ContentLoc loc,
			String xmlId);

	IModel<Response> newPageHighlightsLabel(IModel<User> user,
			IModel<ISIPrompt> prompt);

	/**
	 * Save the response for a multiple choice response
	 * 
	 * @param mResponse
	 * @param text
	 * @param correct
	 */
	void saveSingleSelectResponse(IModel<Response> mResponse, String text,
			boolean correct, String pageName);

	IModel<Response> newSingleSelectResponse(IModel<User> user,
			IModel<Prompt> model);

	/**
	 * A set of methods for saving a response object with the appropriate type for these custom responses.
	 */
	void saveHighlighterLabel(IModel<Response> response, String label);

	/**
	 * Set the message/announcement for a given Period.
	 * 
	 * @param p - the period
	 * @param s - the message
	 */
	void setClassMessage(IModel<Period> mPeriod, String s);

	/**
	 * Get the message/announcement for a given period that is marked current.
	 * 
	 * @param p - the period
	 * @return
	 */
	ClassMessage getClassMessage(IModel<Period> p);

	/**
	 * Marks the current message/announcement for a period as not-current.  It remains in the database, but is not displayed.
	 * 
	 * @param p
	 */
	void deleteClassMessage(IModel<Period> mPeriod);

	/**
	 * This is an expensive method as the Event table is a very large table.  Call this only
	 * once per session as part of getting the bookmark.
	 * 
	 * @param person
	 * @param type
	 * @return Most recent event for Person of a particular type
	 */
	ISIEvent findLatestMatchingEvent(User person, String type);

	void toggleFlag(User person, Period period);

	void toggleFlag(User person);

	boolean isFlagged(User person, Period period);

	boolean isFlagged(User person);

	List<StudentFlag> getAllFlags();

	List<FeedbackMessage> getFeedbackMessages(IModel<Prompt> promptM,
			User student);

	void deleteFeedbackMessage(IModel<FeedbackMessage> mFeedbackMessage);

	void updateFeedbackMessage(IModel<FeedbackMessage> mFeedbackMessage,
			Page page);

	void saveFeedbackMessage(IModel<FeedbackMessage> mFeedbackMessage, IModel<Prompt> mPrompt);


	/**
	 * @param teacher
	 * @return list of prompts
	 * 
	 * Get all of the active teacher notes written by this teacher.
	 */
	List<ISIPrompt> getTeacherNotes(IModel<User> teacher);

	List<String> getPagesWithNotes(User student, boolean isUnread);

	List<String> getPagesWithNotes(User student);

	List<FeedbackMessage> getNotesForPage(User student, String loc);

	String locationOfFirstUnreadMessage(User student, ISIXmlSection sec);

	IModel<List<ISIResponse>> getAllNotebookResponsesByStudent(
			IModel<User> student);

	List<ISIResponse> getAllWhiteboardResponses();

	List<ISIResponse> getWhiteboardResponsesByPeriod(Period p);

	void clearWhiteboardForPeriod(Period p);

	void addToWhiteboard(ISIResponse resp, Page page);

	void removeFromWhiteboard(ISIResponse resp, Page page);

	void addToNotebook(ISIResponse resp, Page page);

	void removeFromNotebook(ISIResponse resp, Page page);

	/**
	 * Get the Response Collections
	 */

	List<String> getResponseCollectionNames(IModel<User> mUser);

	List<ISIPrompt> getResponseCollectionNamePrompts(IModel<User> mUser);

	List<ISIPrompt> getResponseCollectionPrompts(IModel<User> mUser,
			String collectionName);

	List<ISIResponse> getAllResponsesForPromptByStudent(IModel<Prompt> mPrompt, IModel<User> mUser);

	IModel<List<ISIResponse>> getAllResponsesForCollectionByStudent(String collectionName, UserModel mUser);

	IModel<List<ISIResponse>> getPeriodResponsesForPrompt(IModel<Prompt> mPrompt, IModel<Period> mPeriod);

}