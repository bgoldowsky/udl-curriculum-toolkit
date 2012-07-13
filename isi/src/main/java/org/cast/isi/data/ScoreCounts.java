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

import java.io.Serializable;
import lombok.Getter;

/**
 * A simple data holder for score counts used in summary pages
 * 
 * @author droby
 *
 */
@Getter
public class ScoreCounts implements Serializable {

	private static final long serialVersionUID = 1L;

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

}
