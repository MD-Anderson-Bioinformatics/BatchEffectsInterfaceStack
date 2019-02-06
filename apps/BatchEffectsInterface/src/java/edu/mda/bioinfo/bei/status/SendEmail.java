/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mda.bioinfo.bei.status;

import edu.mda.bioinfo.bei.servlets.BEIproperties;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServlet;

/**
 *
 * @author linux
 */
public class SendEmail
{

	static public void sendEmail(String theJobId, HttpServlet theServlet) throws IOException, MessagingException
	{
		HashMap<String, String> map = JobStatus.getJobMap(theJobId);
		String email = map.get("email");
		BEIproperties.getResponseString(theServlet);
		String host = BEIproperties.getProperty("smtpHost", theServlet);
		String port = BEIproperties.getProperty("smtpPort", theServlet);
		if (((null != email) && (!"".equals(email)))
				&& ((null != host) && (!"".equals(host)))
				&& ((null != port) && (!"".equals(port))))
		{
			String subject = "BEI: Update for " + theJobId + " from " + BEIproperties.getProperty("serverTitle", theServlet);
			/////////////////// 
			String emailBody = "";
			emailBody = emailBody + "Update for Batch Effects Interface from " + BEIproperties.getProperty("serverTitle", theServlet) + "\n\n";
			emailBody = emailBody + "Update for Job Id " + theJobId + "\n\n";
			emailBody = emailBody + "Tagged as " + map.get("tag") + "\n\n";
			emailBody = emailBody + "Status is " + map.get("status") + "\n\n";
			emailBody = emailBody + "Status message is " + map.get("message") + "\n\n";
			emailBody = emailBody + "Owner is " + map.get("owner") + "\n\n";
			emailBody = emailBody + "\n\n";
			emailBody = emailBody + "Last log file tail is:" + "\n\n";
			emailBody = emailBody + "\n\n";
			emailBody = emailBody + map.get("tail") + "\n\n";
			///////////////////
			internalSendEmail(host, port, email, subject, emailBody, theServlet);
		}
	}

	static protected void internalSendEmail(String theServer, String thePort, String theEmail, String theSubject, String theBody, HttpServlet theServlet) throws AddressException, MessagingException
	{
		if (null != theServlet)
		{
			theServlet.log("Sending email to " + theEmail);
		}
		// Get system properties
		Properties properties = System.getProperties();

		// Setup mail server
		properties.setProperty("mail.smtp.host", theServer);
		properties.setProperty("mail.smtp.port", thePort);

		// Get the default Session object.
		Session session = Session.getDefaultInstance(properties);

		// Create a default MimeMessage object.
		MimeMessage message = new MimeMessage(session);

		// Set From: header field of the header.
		message.setFrom(new InternetAddress(theEmail));

		// Set To: header field of the header.
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(theEmail));

		// Set Subject: header field
		message.setSubject(theSubject);

		// Now set the actual message
		message.setText(theBody);

		// Send message
		Transport.send(message);
		if (null != theServlet)
		{
			theServlet.log("Sent email to " + theEmail);
		}
	}
}
