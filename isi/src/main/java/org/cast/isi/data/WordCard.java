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

import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import lombok.Getter;
import lombok.Setter;

import org.apache.wicket.util.string.Strings;
import org.cast.cwm.data.PersistedObject;
import org.cast.cwm.data.User;
import org.cast.cwm.glossary.IGlossaryEntry;
import org.cast.cwm.xml.ICacheableModel;
import org.cast.cwm.xml.IXmlPointer;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Index;

@Entity
@GenericGenerator(name="my_generator", strategy = "org.cast.cwm.CwmIdGenerator")
public class WordCard extends PersistedObject implements IGlossaryEntry {
	
	private static final long serialVersionUID = 1L;
	
	@Id @GeneratedValue(generator = "my_generator")
	@Getter 
	private Long id;
	
	@Column(nullable=false)
	@Getter
	private String headword;
	
	@Column(nullable=false)
	@Getter
	private String sortForm;
	
	@Column(nullable=false)
	@Getter @Setter 
	private boolean current = true;
	
	@Column(nullable=false)
	@Getter @Setter
	private boolean glossaryWord = false;
	
	@ManyToOne(optional=false)
	@Index(name="wordcard_creator_idx")
	@Getter @Setter
	private User creator;
	
	protected WordCard() { /* Default Constructor for Datastore */ }
	
	public WordCard(String name, User creator) {
		this.creator = creator;
		this.current = true;
		setHeadword(name);
	}
	
	public void setHeadword(String name) {
		this.headword = normalizeWord(name);
		this.sortForm = makeSortKey(name);
	}
	
	/** Normalize whitespace and capitalization of word.
	 * @param string representing word
	 * @return normalized string
	 */
	public static String normalizeWord (String text) {
		if (text == null)
			return null;
		return Strings.capitalize(text.replaceAll("\\s+", " ").trim());
	}
	
	/** Make a sort key for the word - all lowercase for ease of searching and sorting */
	public static String makeSortKey (String text) {
		if (text == null)
			return null;
		return normalizeWord(text).toLowerCase();
	}

	public Collection<String> getAlternateForms() {
		return null;
	}

	public String getIdentifier() {
		return id.toString();
	}

	public String getShortDef() {
		return null;
	}

	public ICacheableModel<? extends IXmlPointer> getXmlPointer() {
		return null;
	}
	
}
