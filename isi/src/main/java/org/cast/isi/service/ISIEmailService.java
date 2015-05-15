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

import com.google.inject.Inject;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.User;
import org.cast.cwm.service.EmailService;
import org.cast.cwm.xml.XmlDocument;
import org.cast.cwm.xml.XmlSectionModel;
import org.cast.cwm.xml.service.IXmlService;
import org.cast.cwm.xml.transform.TransformParameters;
import org.cast.isi.ISIApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

public class ISIEmailService extends EmailService {

	// Known message IDs
	public static String EMAIL_CONFIRM = "registerConfirm";
	public static String EMAIL_CONFIRM_TEACHER = "registerConfirmTeacher";
	public static String EMAIL_CONFIRM_RESEND = "registerConfirmResend";
	public static String EMAIL_FORGOT = "forgotPassword";

	protected static final Logger log = LoggerFactory.getLogger(ISIEmailService.class);

	@Inject
	private IXmlService xmlService;

	public ISIEmailService() {
		super();
        Injector.get().inject(this);
	}
	
	public static ISIEmailService get() {
		return (ISIEmailService) EmailService.get();
	}
	
	public static void useAsServiceInstance() {
		EmailService.instance = new ISIEmailService();
	}

	public void sendXmlEmail (IModel<User> mUser, String messageId, String uri) {
		this.sendXmlEmail(mUser, messageId, uri, null);
	}

	/**
	 * Send an email whose subject line and contents are based on the text in the "emails" XML file.
	 * 
	 * These variables (expressed as <samp> elements) in the XML file will be substituted with relevant values:
	 *   recipientEmail : the email address that the message is being sent to
	 *   url : replaced with the URL of the application + the uri argument given
	 *   fullname : replaced with full name of the given user
	 *   username : replaced with username of the given user
	 *   any key in the variables map : the value it is mapped to
	 *   
	 * @param recipient email address to be sent to
	 * @param user the User used for replacements (generally the sender)
	 * @param messageId an ID string, generally one of the constant values defined above
	 * @param uri replacement variable
	 * @param mStudentUser the default student User if this is a new teacher 
	 */	
	public void sendXmlEmail (IModel<User> mUser, String messageId, String uri, String studentPassword) {
		String recipient = mUser.getObject().getEmail();

		XmlDocument emailXml = ISIApplication.get().getEmailContent();
		TransformParameters params = new TransformParameters();
		params.put("subDelimiter", subDelimiter);

		XmlSectionModel sectionModel = new XmlSectionModel(emailXml.getById(messageId));
		Element emailContent = xmlService.getTransformed(sectionModel, ISIApplication.get().getEMAIL_TRANSFORMER(), params)
					.getElement();
		
		// Message substitution variables
		String fullUrl = "";
		if (uri != null)
			fullUrl = ISIApplication.get().getUrl() + uri;
		Map<String, String> variables = new HashMap<String, String>();
		variables.put("url", fullUrl);
		variables.put("fullname", mUser.getObject().getFullName());
		variables.put("username", mUser.getObject().getUsername());		
		variables.put("recipientEmail", mUser.getObject().getEmail());
		if (studentPassword != null) {
			variables.put("defaultPeriod", mUser.getObject().getPeriods().first().getName()); // there is only one period when this email is generated
			variables.put("defaultStudentUsername", mUser.getObject().getPeriods().first().getName()); //default user has username = default period name
			variables.put("defaultStudentPassword", studentPassword);
		}
		
		EmailMessage emailMessage = new EmailMessage();
		emailMessage.setFrom(ISIApplication.get().getMailFromAddress());
		emailMessage.setTo(recipient);

		//Element emailContent = emailXml.getById(messageId).getElement();
		emailMessage.setSubject(substituteVars(emailContent.getElementsByTagNameNS("http://www.w3.org/1999/xhtml", "head").item(0).getTextContent(),
				variables).replaceAll("[ \\t][ \\t]+", " "));

		emailMessage.setBody(substituteVars(emailContent.getElementsByTagNameNS("http://www.w3.org/1999/xhtml", "body").item(0).getTextContent(),
				 variables).replaceAll("[ \\t][ \\t]+", " "));
		
		log.debug(emailMessage.toString());
		sendMail(emailMessage, variables);

	}

}