package org.apache.commons.mail;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.Rule;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.Map;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

public class EmailTest {

	// Mocking with concrete class
	private concreteEmail mockEmail;
	
	// Used for expected exception evaluation
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	
	// Setup new mockEmail before each test case
	@Before
	public void setUp() {
		mockEmail = new concreteEmail();
	}
	
	// Destroy mockEmail after each test case
	@After
	public void tearDown() {
		mockEmail = null;
	}
	
	
	/* 
	 * Test trying to add an empty bcc
	 * EmailException with message "Address List provided was invalid" is expected
	 */
	@Test
	public void testEmptyBcc() throws EmailException {
		thrown.expect(EmailException.class);
		thrown.expectMessage("Address List provided was invalid");
		String bccList[] = {};
		
		mockEmail.addBcc(bccList);
	}
	
	/* 
	 * Test trying to add valid list of bcc addresses
	 * expected size is 2 and emails match those passed in function
	 */
	@Test
	public void testBccList() throws EmailException {
		String bccList[] = {"email1@test.com", "email2@test.com"};
		mockEmail.addBcc(bccList);
		assertEquals(2, mockEmail.getBccAddresses().size());
		assertEquals("email1@test.com", mockEmail.getBccAddresses().get(0).toString());
		assertEquals("email2@test.com", mockEmail.getBccAddresses().get(1).toString());
	}
	
	/* 
	 * Test trying to add cc to email
	 * Expected email matches that passed in the function
	 */
	@Test
	public void testAddCc() throws EmailException {
		mockEmail.addCc("email@test.com");
		assertEquals("email@test.com", mockEmail.getCcAddresses().get(0).getAddress());
	}
	
	/* 
	 * Test trying to add an empty header
	 * IllegalArgumentException expected for null parameters
	 */
	@Test
	public void testEmptyHeader() throws IllegalArgumentException {
		thrown.expect(IllegalArgumentException.class);
		mockEmail.addHeader(null, null);
	}
	
	/* 
	 * Test trying to add header without a name
	 * IllegalArgumentException expected for null parameter in name
	 */
	@Test
	public void testEmptyHeaderName() throws IllegalArgumentException {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("name can not be null or empty");
		mockEmail.addHeader(null, "value");
	}
	
	/* 
	 * Test trying to add header without a value
	 * IllegalArgumentException expected for null parameter in value
	 */
	@Test
	public void testEmptyHeaderValue() throws IllegalArgumentException {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("value can not be null or empty");
		mockEmail.addHeader("name", null);
	}
	
	/* 
	 * Test trying to add valid header
	 * Expected headers in the email are equal to the ground truth
	 */
	@Test
	public void testValidHeader() throws IllegalArgumentException {
		Map<String, String> groundTruth = Map.of("name", "value");
		mockEmail.addHeader("name", "value");
		assertEquals(groundTruth, mockEmail.headers);
	}

	/* 
	 * Test trying to add valid reply email
	 * Expected email matches the parameter that was passed in the function
	 */
	@Test 
	public void testReplyEmail() throws EmailException {
		mockEmail.addReplyTo("email@test.com");
		assertEquals("email@test.com", mockEmail.getReplyToAddresses().get(0).toString());
	}
	
	/* 
	 * Test trying to build the mime message
	 * Expected the mime message will be built and therefore not be null
	 */
	@Test 
	public void testBuildMimeMessage() throws EmailException {
		mockEmail.setHostName("host@test.com");
		mockEmail.setSubject("test subject");
		mockEmail.setContent(mockEmail, "Test Content");
		mockEmail.setFrom("sender@test.com");
		mockEmail.addTo("receiver@test.com");
		mockEmail.addCc("cc1@test.com");
		mockEmail.addBcc("bcc1@test.com");
		mockEmail.addReplyTo("reply1@test.com");
		mockEmail.addHeader("name", "value");
		
		Date date = new Date();
		mockEmail.setSentDate(date);
		
		mockEmail.buildMimeMessage();
		assertNotNull(mockEmail.getMimeMessage());
	}
	
	/* 
	 * Test trying to build a mime message after it has already been built
	 * IllegalStateException expected for non-null mime message
	 */
	@Test 
	public void testDuplicateBuildMimeMessage() throws EmailException {
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("The MimeMessage is already built.");
		mockEmail.setHostName("host@test.com");
		mockEmail.setFrom("sender@test.com");
		mockEmail.addTo("receiver@test.com");
		
		mockEmail.buildMimeMessage();
		mockEmail.buildMimeMessage();
	}
	
	/* 
	 * Test trying to build a mime message 
	 * IllegalStateException expected for non-null mime message
	 */
	@Test 
	public void testBuildMimeMessageMultipart() throws EmailException, MessagingException {
		mockEmail.setHostName("host@test.com");
		mockEmail.setSubject("test subject");
		mockEmail.setFrom("sender@test.com");
		mockEmail.addCc("cc1@test.com");
		
		MimeMultipart bodyContent = new MimeMultipart();
		MimeBodyPart text = new MimeBodyPart();
		text.setText("Test Email Body");
		bodyContent.addBodyPart(text);
		
		mockEmail.setContent(bodyContent);
		
		mockEmail.buildMimeMessage();
		assertNotNull(mockEmail.getMimeMessage());
	}
	
	/* 
	 * Test trying to build the mime message with a mimeMultiPart
	 * Expected the mime message will be built and therefore not be null
	 */
	@Test 
	public void testBuildMimeMessageMultipartWithContent() throws EmailException, MessagingException {
		mockEmail.setHostName("host@test.com");
		mockEmail.setSubject("test subject");
		mockEmail.setFrom("sender@test.com");
		mockEmail.setCharset("UTF-8");
		
		String ccList[] = {"cc1@test.com", "cc2@test.com"};
		mockEmail.addCc(ccList);
		
		MimeMultipart bodyContent = new MimeMultipart();
		MimeBodyPart text = new MimeBodyPart();
		text.setText("Test Email Body");
		bodyContent.addBodyPart(text);
		
		mockEmail.contentType = EmailConstants.TEXT_PLAIN;
		mockEmail.setContent(bodyContent);
		
		mockEmail.buildMimeMessage();
		assertNotNull(mockEmail.getMimeMessage());
	}
	
	/* 
	 * Test trying to build a mime message with no sender specified 
	 * EmailException expected with message "From address required"
	 */
	@Test 
	public void testBuildMimeMessageNoSender() throws EmailException {
		thrown.expect(EmailException.class);
		thrown.expectMessage("From address required");
		mockEmail.setHostName("host@test.com");
		
		String receivers[] = {"receiver1@test.com", "receiver2@test.com"};
		mockEmail.addTo(receivers);
		
		mockEmail.buildMimeMessage();
	}
	
	/* 
	 * Test trying to build a mime message with no receiver specified 
	 * EmailException expected with message "At least one receiver address required"
	 */
	@Test 
	public void testBuildMimeMessageNoReceiver() throws EmailException {
		thrown.expect(EmailException.class);
		thrown.expectMessage("At least one receiver address required");
		mockEmail.setHostName("host@test.com");
		mockEmail.setFrom("sender@test.com");
		
		mockEmail.buildMimeMessage();
	}
	
	/* 
	 * Test trying to set valid hostname
	 * Expected hostname matches that passed in function parameter
	 */	
	@Test 
	public void testValidHostName() {
		mockEmail.setHostName("host@test.com");
		assertEquals("host@test.com", mockEmail.getHostName());
	}
	
	/* 
	 * Test trying to set valid hostname via session properties
	 * Expected hostname matches that passed in function parameter
	 */	
	@Test 
	public void testSessionHostName() {
		Properties prop = new Properties();
		prop.put( "mail.smtp.host", "host@test.com");
		Session mailSession = Session.getDefaultInstance(prop);
		mockEmail.setMailSession(mailSession);
		
		assertEquals("host@test.com", mockEmail.getHostName());
	}
	
	/* 
	 * Test empty hostname
	 * Expected hostname is null
	 */	
	@Test 
	public void testNullHostName() {
		assertNull(mockEmail.getHostName());
	}
	
	/* 
	 * Test trying to get invalid mail session (no hostname)
	 * EmailException expected with message "Cannot find valid hostname for mail session"
	 */	
	@Test
	public void testInvalidGetSession() throws EmailException {
		thrown.expect(EmailException.class);
		thrown.expectMessage("Cannot find valid hostname for mail session");
		
		mockEmail.setHostName(null);
		mockEmail.getMailSession();        
	}
	
	/* 
	 * Test trying to get valid mail session after setting values with respective functions
	 * Expected that values in mockEmail match ground truth properties values
	 */
	@Test
	public void testGetSession() throws EmailException {
        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.host", "smtp.example.com");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.required", "false");
        props.put("mail.smtp.starttls.enable", "false");
        props.put("mail.smtp.sendpartial", "false");
        props.put("mail.smtps.sendpartial", "false");
        props.put("mail.smtp.port", "587");
        props.put("mail.debug", "false");
        props.put("mail.smtp.ssl.checkserveridentity", "true");
        props.put("mail.smtp.from", "smtp.bounce@example.com");
        Session mailSession = Session.getInstance(props);
        
        mockEmail.setAuthentication("username", "password");
        mockEmail.setHostName("smtp.example.com");
        mockEmail.setSSLOnConnect(true);
        mockEmail.setStartTLSRequired(false);
        mockEmail.setStartTLSEnabled(false);
        mockEmail.setSendPartial(false);
        mockEmail.setSSLCheckServerIdentity(true);
        mockEmail.setSmtpPort(587);
        mockEmail.setSslSmtpPort("587");
        mockEmail.setSocketTimeout(0);
        mockEmail.setFrom("your_email@example.com");
        mockEmail.setBounceAddress("smtp.bounce@example.com");
        
        assertEquals(mailSession.getProperty("mail.transport.protocol"), mockEmail.getMailSession().getProperty("mail.transport.protocol"));
        assertEquals(mailSession.getProperty("mail.smtp.host"), mockEmail.getMailSession().getProperty("mail.smtp.host"));
        assertEquals(mailSession.getProperty("mail.smtp.auth"), mockEmail.getMailSession().getProperty("mail.smtp.auth"));
        assertEquals(mailSession.getProperty("mail.smtp.auth"), mockEmail.getMailSession().getProperty("mail.smtp.auth"));
        assertEquals(mailSession.getProperty("mail.smtp.starttls.required"), mockEmail.getMailSession().getProperty("mail.smtp.starttls.required"));
        assertEquals(mailSession.getProperty("mail.smtp.starttls.enable"), mockEmail.getMailSession().getProperty("mail.smtp.starttls.enable"));
        assertEquals(mailSession.getProperty("mail.smtp.sendpartial"), mockEmail.getMailSession().getProperty("mail.smtp.sendpartial"));
        assertEquals(mailSession.getProperty("mail.smtps.sendpartial"), mockEmail.getMailSession().getProperty("mail.smtps.sendpartial"));
        assertEquals(mailSession.getProperty("mail.smtp.port"), mockEmail.getMailSession().getProperty("mail.smtp.port"));
        assertEquals(mailSession.getProperty("mail.debug"), mockEmail.getMailSession().getProperty("mail.debug"));
        assertEquals(mailSession.getProperty("mail.smtp.ssl.checkserveridentity"), mockEmail.getMailSession().getProperty("mail.smtp.ssl.checkserveridentity"));
        assertEquals(mailSession.getProperty("mail.smtp.from"), mockEmail.getMailSession().getProperty("mail.smtp.from"));
	}
	
	/* 
	 * Test trying to get valid mail session after setting values via properties 
	 * Expected that properties returned by email is equal to properties used to set email
	 */
	@Test
	public void testGetSessionWithSession() throws EmailException {
        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.host", "smtp.example.com");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.sendpartial", "true");
        props.put("mail.smtps.sendpartial", "true");
        props.put("mail.smtp.port", "587");
        props.put("mail.user", "your_username");
        props.put("mail.password", "your_password");
        props.put("mail.debug", "false");
        props.put("mail.smtp.from", "smtp.bounce@example.com");
        props.put("mail.smtp.timeout", "5");
        Session mailSession = Session.getInstance(props);
        mockEmail.setMailSession(mailSession);
        
        assertEquals(mailSession, mockEmail.getMailSession());
	}
	
	/* 
	 * Test trying to set sent date
	 * Expected that the email sent date matches the current date (given that it is null for this test)
	 */
	@Test
	public void testGetSentDate() {
		Date testDate = new Date();
		assertEquals(testDate, mockEmail.getSentDate());
	}
	
	/* 
	 * Test trying to get the socket connection timeout for this instance
	 * Expected that value returned will match set value (0 in this instance)
	 */
	@Test
	public void testGetSocketConnectionTimeout() {
		mockEmail.setSocketConnectionTimeout(0);
		assertEquals(0, mockEmail.getSocketConnectionTimeout());
	}
	
	
}
