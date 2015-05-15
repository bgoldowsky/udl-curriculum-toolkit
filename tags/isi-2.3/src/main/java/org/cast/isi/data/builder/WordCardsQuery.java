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
package org.cast.isi.data.builder;

import net.databinder.models.hib.QueryBuilder;

import org.cast.cwm.data.User;
import org.cast.isi.data.WordCard;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * Query builder for searching for word cards.
 * 
 * @author jbrookover
 */
public class WordCardsQuery implements QueryBuilder {

	private static final long serialVersionUID = 1L;
	private String word;
	private Long userId;

	public Query build(Session hibernateSession) {
		Query q;

		if (userId == null)
			throw new IllegalArgumentException("Must supply a user for query");

		if (word != null) {
			// Search for a single word card
			q = hibernateSession
				.createQuery("from WordCard wc where wc.creator.id=:userId and wc.sortForm = :word")
				.setLong("userId", userId)
				.setString("word", word);
		} else {
			// Search for all words for the user
			q = hibernateSession
				.createQuery("from WordCard wc where wc.creator.id=:userId order by wc.sortForm")
				.setLong("userId", userId);
		}

		q.setCacheable(true);

		return q;
	}

	/**
	 * Set the specific word to search for.  If not set or null,
	 * this will return all matching words.
	 * 
	 * @param word
	 * @return
	 */
	public WordCardsQuery setWord(String word) {
		this.word = WordCard.makeSortKey(word);
		return this;
	}

	/**
	 * Set the user whose words are searched for.
	 * 
	 * @param p
	 * @return
	 */
	public WordCardsQuery setUser(User p) {
		if (p != null)
			userId = p.getId();
		return this;
	}

	
}