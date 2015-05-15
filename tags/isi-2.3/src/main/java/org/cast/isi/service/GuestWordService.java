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
package org.cast.isi.service;

import java.util.Collections;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.cast.cwm.data.User;
import org.cast.isi.data.WordCard;

/**
 * Variant of WordService for guest users; doesn't do any DB calls.
 * @author bgoldowsky
 *
 */
public class GuestWordService implements IWordService  {
	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.IWordService#getWordCard(java.lang.Long)
	 */
	public IModel<WordCard> getWordCard (Long id) {
		throw new RuntimeException("Cannot get word card for guest user");
	}
	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.IWordService#getWordCard(java.lang.String, org.cast.cwm.data.User)
	 */
	public IModel<WordCard> getWordCard (String word, User user) {		
		throw new RuntimeException("Cannot get word card for guest user");
	}
	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.IWordService#listWordCards(org.cast.cwm.data.User)
	 */
	@SuppressWarnings("unchecked")
	public IModel<List<WordCard>> listWordCards(User user) {
		return Model.ofList(Collections.EMPTY_LIST);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.IWordService#getWordCardCreate(java.lang.String, org.cast.cwm.data.User, boolean)
	 */
	public IModel<WordCard> getWordCardCreate (String word, User user, boolean inGlossary) {
		throw new RuntimeException("Cannot get word card for guest user");
	}
}
