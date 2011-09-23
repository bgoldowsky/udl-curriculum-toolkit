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

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.cast.cwm.data.Period;
import org.cast.cwm.data.PersistedObject;
import org.cast.cwm.data.User;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;

@NamedQueries({
	@NamedQuery(
			name="ClassMessage.queryByPeriod",
			query="select m from ClassMessage m where m.period=:period and current=TRUE")
})

@Entity
@GenericGenerator(name="my_generator", strategy = "org.cast.cwm.CwmIdGenerator")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ClassMessage extends PersistedObject {

	private static final long serialVersionUID = 1L;
	
	@Id @GeneratedValue(generator = "my_generator")
	private Long id;
	
	private String message;

	@ManyToOne(optional=false)
	private User author;
	
	private Date timestamp;
	
	@ManyToOne(optional=false)
	private Period period;
	
	private boolean current;
	
	public ClassMessage() {
		
	}
	
	@Override
	public Long getId() {
		return id;
	}

	public String getMessage() {
		return message;
	}

	public User getAuthor() {
		return author;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public Period getPeriod() {
		return period;
	}

	public boolean isCurrent() {
		return current;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setAuthor(User author) {
		this.author = author;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public void setPeriod(Period period) {
		this.period = period;
	}

	public void setCurrent(boolean current) {
		this.current = current;
	}

}
