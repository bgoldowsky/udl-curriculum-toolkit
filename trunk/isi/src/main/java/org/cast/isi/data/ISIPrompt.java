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

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.User;
import org.cast.cwm.xml.TransformResult;
import org.cast.cwm.xml.XmlSectionModel;
import org.cast.cwm.xml.service.XmlService;
import org.cast.isi.DynamicComponentRemoverFilter;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

@Entity
@Getter
@Setter
@ToString

public class ISIPrompt extends Prompt implements Comparable<ISIPrompt> {

	private static final long serialVersionUID = 1L;
	protected static final Logger log = LoggerFactory.getLogger(ISIPrompt.class);

	@Index(name="prompt_type_idx")
	@Enumerated(EnumType.STRING)
	private PromptType type;
	
	@Index(name="prompt_location_idx")
	@ManyToOne(optional=true)
	private ContentElement contentElement;
	
	@Index(name="prompt_targetuser_idx")
	@ManyToOne(optional=true)
	private User targetUser;
	
	@Index(name="prompt_identifier_idx")
	private String identifier; // Generic identifier (e.g. a glossary word)

	@Index(name="prompt_collection_idx")
	private String collectionName; // grouping of prompts for MyModels, ResponseCollections (or whatever it is named)
	
	protected ISIPrompt() {/* Empty constructor for datastore */}
	
	public ISIPrompt(PromptType type) {
		this.type = type;
	}
	
	/*
	 * Return the actual question asked in the content that corresponds to this question.
	 * Used for example in the Notebook to provide some context for responses copied there.
	 * 
	 * This requires some knowledge of how the XSL treats prompts, the IDs it generates, etc.
	 * FIXME feels quite inefficient and hacky.  Can we do this better with new cwm-xml?
	 *  
	 * @return String with HTML markup
	 */
	public String getQuestionHTML () {
		String question = "Question Not Available";
		if (getType().equals(PromptType.PAGE_NOTES)) {
			question = "<em>Page Notes</em>";
		} else if (getType().equals(PromptType.RESPONSEAREA)) {
			// TODO: Modify Schema so getElementById works instead of looping through
			String divId = "prompt_" + getContentElement().getXmlId();
			TransformResult html = XmlService.get().getTransformed(new XmlSectionModel(getContentElement().getContentLocObject().getSection()), "student");
			NodeList elements = html.getElement().getOwnerDocument().getElementsByTagName("div");
			Element prompt = null;
			for (int i = 0; i < elements.getLength(); i++) {
				if (((Element) elements.item(i)).getAttribute("id").equals(divId))
					prompt = (Element) elements.item(i);
			}

			if (prompt == null) {
				log.warn("Couldn't find DOM element for prompt {}", divId);
			} else {
				DOMImplementationRegistry registry;
				try {
					registry = DOMImplementationRegistry.newInstance();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				DOMImplementationLS impl = (DOMImplementationLS) registry
				.getDOMImplementation("LS");
				if (impl == null) {
					log.error("Could not find appropriate DOM implementation");
				} else {
					LSSerializer writer = impl.createLSSerializer();
					writer.getDomConfig().setParameter("xml-declaration", false);
					// We're not dealing with dynamic components in HTML (eg, glossary links) - remove them.
					writer.setFilter(DynamicComponentRemoverFilter.get());
					question = writer.writeToString(prompt);
				}
			}
		} else {
			log.warn("Don't know how to find question for prompt type {}", getType());
		}
		return question;
	}

	// Order by the order of the respective ContentElements.  If none, fall back to ID order.
	// Fallback could fail, and throw an exception, if objects are transient.
	public int compareTo(ISIPrompt o) {
		if (this.equals(o))
			return 0;
		if (contentElement == null && o.getContentElement() == null) {
			if (this.isTransient() || o.isTransient())
				throw new IllegalArgumentException("Can't compare transient prompts");
			return getId().compareTo(o.getId());
		}
		if (contentElement == null)
			return -1;
		
		// added these two if statements to sort PAGE_NOTES prompts after other
		// response area prompts on the page
		if (contentElement.getXmlId() == null)
			return 1;
		if (o.contentElement.getXmlId() == null)
			return -1;
		return contentElement.compareTo(o.getContentElement());
	}

}
