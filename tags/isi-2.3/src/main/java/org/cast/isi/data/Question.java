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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import org.cast.cwm.data.PersistedObject;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.User;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;

/**
 * General shared questions for the entire application or individual questions that users create.
 * Each question has a relationship to a {@link Prompt} object.
 * 
 * @author lynnmccormack
 *
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@GenericGenerator(name="my_generator", strategy = "org.cast.cwm.CwmIdGenerator")
@Getter
@Setter
@Table(name="question")
public class Question extends PersistedObject {
	
	private static final long serialVersionUID = 1L;
	
	@Id @GeneratedValue(generator = "my_generator")
	@Setter(AccessLevel.NONE) 
	private Long id;
	
	@Column(nullable=false)
	protected String text; // The text of the question
	
	@ManyToOne(optional=true)
	protected User owner; // The user who created the question. Null if shared (authored).
	
	@Column(nullable=true)
	private String xmlId; //  use in xml file for update

	@Column(nullable=true)
	protected String sectionId; // what part of the curriculum does this appear in?
	
	@Column(nullable=false)
	protected Date insertTime;
	
	protected Boolean active;
	
	@ManyToOne(optional=false)
	protected ISIPrompt prompt;
	
	public Question() {
	}
	
	public Question (String text, User owner, String sectionId) {
		this.text = text;
		this.owner = owner;
		this.sectionId = sectionId;
		this.insertTime = new Date();
		this.active = true;
		
//		if (owner == null)
//			this.setName("question_system_" + ISIApplication.get().getQuestionService().getNumQuestions(owner));
//		else
//			this.setName("question_" + owner.getId() + "_" +  ISIApplication.get().getQuestionService().getNumQuestions(owner));
	}
	
}

