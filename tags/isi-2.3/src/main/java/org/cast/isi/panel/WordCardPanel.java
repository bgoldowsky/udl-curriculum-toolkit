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
package org.cast.isi.panel;

import org.apache.wicket.model.IModel;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.ResponseMetadata;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISISession;
import org.cast.isi.data.ISIPrompt;
import org.cast.isi.data.PromptType;
import org.cast.isi.data.WordCard;
import org.cast.isi.service.ISIResponseService;

/**
 * Display a user defined word {@link WordCard} with the associated response areas.
 */
public class WordCardPanel extends ISIPanel {
	
	private static final long serialVersionUID = 1L;
	protected ResponseMetadata wordCardMetadata;

	protected IModel<ISIPrompt> mPrompt;
	protected IModel<User> mUser;
	protected IModel<WordCard> mWordcard;

	private boolean isTeacher;
	
	public WordCardPanel(String id, IModel<WordCard> mWordcard) {
		super(id);
		mUser = ISISession.get().getTargetUserModel();
		this.mWordcard = mWordcard;
		setWordCardMetadata(wordCardMetadata);
		
		isTeacher = ISISession.get().getUser().getRole().subsumes(Role.TEACHER);

		addWordCardResponses();
	}

	/**
	 * Add a response area for this user defined word.  These are responses are NOT associated with reading pages.
	 */
	protected void addWordCardResponses () {

		IModel<Prompt> mPrompt = ISIResponseService.get().getOrCreatePrompt(PromptType.WORD_CARD, mUser, mWordcard.getObject().getId().toString());
		ResponseList responseList = new ResponseList("responseList", mPrompt, wordCardMetadata, null, mUser);
		String glossaryContext = (isTeacher) ? "glossary.teacher" : "glossary";
		responseList.setContext(glossaryContext);
		responseList.setAllowEdit(!isTeacher);
		responseList.setAllowNotebook(false);
		responseList.setAllowWhiteboard(false);
		add(responseList);
		ResponseButtons responseButtons = new ResponseButtons("responseButtons", mPrompt, wordCardMetadata, null);
		responseButtons.setContext(glossaryContext);
		add(responseButtons.setVisible(!isTeacher));
	}

	public void setWordCardMetadata(ResponseMetadata wordCardMetadata) {
		this.wordCardMetadata = ISIApplication.get().getResponseMetadata();
	}	
}
