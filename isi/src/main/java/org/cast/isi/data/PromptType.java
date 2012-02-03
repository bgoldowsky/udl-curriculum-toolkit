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
package org.cast.isi.data;

/**
 * The type of a {@link Prompt} object.  Google App Engine does not support polymorphic
 * queries so this distinguishes different prompt objects.
 * 
 * @author jbrookover
 *
 */
public enum PromptType {

	/**
	 * Response groups, stop-and-think, cloze passage etc.
	 */
	RESPONSEAREA, 
	
	/**
	 * Feedback comments between teachers and students.
	 */
	FEEDBACK,
	
	/**
	 * Page highlighting.  Generally, one per page.
	 */
	PAGEHIGHLIGHT, 
	
	/**
	 * User editable highlight label.  Just one prompt for the application.
	 */
	HIGHLIGHTLABEL,
	
	/**
	 * Page notes.  Generally, one per page.
	 */
	PAGE_NOTES,

	/**
	 * Notebook notes.  Just one prompt for the application.
	 */
	NOTEBOOK_NOTES,
	
	/**
	 * MyQuestions.  One prompt for each question.
	 */
	MY_QUESTIONS,
	
	/**
	 * Notes, by a teacher, about a specific student.
	 */
	TEACHER_NOTES,
	
	/**
	 * A Word Card - user definitions
	 */
	WORD_CARD, 
	
	/**
	 * For a given rating panel this is the thumbs up, down, middle prompt
	 */
	RATING_THUMB,

	/**
	 * For a given rating panel this is the comment
	 */
	RATING_COMMENT,
	
	/**
	 * For a given rating panel this is the affect rating prompt (bored, happy, etc.)
	 */
	RATING_AFFECT,

	/**
	 * Applet Prompt type.
	 */
	APPLET,

	/**
	 * TASK Prompt type - this is temporary till we implement extensible enums for prompt - ldm.
	 */
	TASK


}
