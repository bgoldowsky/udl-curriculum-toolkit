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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.cast.cwm.data.PersistedObject;
import org.cast.cwm.data.User;
import org.hibernate.annotations.GenericGenerator;

@GenericGenerator(name="my_generator", strategy = "org.cast.cwm.CwmIdGenerator")
@Entity
public class SectionStatus extends PersistedObject {

	private static final long serialVersionUID = 1L;

	@Id @GeneratedValue(generator = "my_generator")
	private long id;
	
	@ManyToOne(optional=false)
	protected User user;
	
	@Column(nullable=false)
	protected String loc;
	
	@Column(nullable=false)
	protected Boolean completed;
	
	@Column(nullable=false)
	protected Boolean reviewed;
	
	@Column(nullable=false)
	protected int unreadTeacherMessages;
	
	@Column(nullable=false)
	protected int unreadStudentMessages;
	
	public SectionStatus() {
		super();
	}
	
	public SectionStatus (User person, String loc, Boolean completed) {
		this.user = person;
		this.loc = loc;
		this.completed = completed;
		this.reviewed = false;
		this.unreadTeacherMessages = 0;
		this.unreadStudentMessages = 0;
	}

	@Override
	public Long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User person) {
		this.user = person;
	}

	public String getLoc() {
		return loc;
	}

	public void setLoc (String loc) {
		this.loc = loc;
	}

	public Boolean getCompleted() {
		return completed;
	}

	public void setCompleted(Boolean completed) {
		this.completed = completed;
	}

	public Boolean getReviewed() {
		return reviewed;
	}

	public void setReviewed(Boolean reviewed) {
		this.reviewed = reviewed;
	}

	public int getUnreadTeacherMessages() {
		return unreadTeacherMessages;
	}

	public int getUnreadStudentMessages() {
		return unreadStudentMessages;
	}

	public void setUnreadTeacherMessages(int unreadTeacherMessages) {
		this.unreadTeacherMessages = unreadTeacherMessages;
	}

	public void setUnreadStudentMessages(int unreadStudentMessages) {
		this.unreadStudentMessages = unreadStudentMessages;
	}

	

}
