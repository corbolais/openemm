/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend;

import	java.io.File;
import	java.sql.SQLException;
import	java.sql.Timestamp;
import	java.util.HashMap;
import	java.util.List;
import	java.util.Map;

import	org.agnitas.backend.dao.MailingDAO;
import	org.agnitas.util.Log;

/**
 * Thes class keeps track of all mailing related
 * configurations
 */
public class Mailing {
	/** refrence to global configuration				*/
	private Data			data;
	/** the mailing_id						*/
	private long			id;
	/** database representtion for mailing				*/
	private MailingDAO		mailing;
	/** the name of the mailing					*/
	@SuppressWarnings("unused")
	private String			name;
	/** default encoding for all blocks				*/
	private String			defaultEncoding;
	/** default character set for all blocks			*/
	private String			defaultCharset;
	/** used block size						*/
	private int			blockSize;
	/** steps in seconds to delay sending of each blocksize		*/
	private int			stepping;
	/** number of blocks per entity					*/
	private int			blocksPerStep;
	/** start stepping at this block				*/
	private int			startBlockForStep;
	/** maximum number of bytes per output file			*/
	private long			maxBytesPerOutputFile;
	/** create a UUID per recipient					*/
	private boolean			createUUID;
	/** the subject for this mailing				*/
	private String			subject;
	/** the sender address for this mailing				*/
	private EMail			fromEmail;
	/** the optional reply-to address for this mailing		*/
	private EMail			replyTo;
	/** the envelope address					*/
	private EMail			envelopeFrom;
	/** the encoding for this mailing				*/
	private String			encoding;
	/** the charachter set for this mailing				*/
	private String			charset;
	/** domain used to build message-ids				*/
	private String			domain;
	/** boundary part to build multipart messages			*/
	private String			boundary;
	/** content of the X-Mailer: header				*/
	private String			mailer;
	/** output directories for admin and test sendings		*/
	private Map <String, String>	outputDirectories;
	/** path to accounting logfile					*/
	private String			accountLogfile;
	/** path to bounce logfile					*/
	private String			bounceLogfile;
	/** path to messageID logfile					*/
	private String			messageIDLogfile;
	/** name of system MTA						*/
	private String			messageTransferAgent;

	public Mailing (Data nData) {
		data = nData;
		defaultEncoding = "quoted-printable";
		defaultCharset = "UTF-8";
		blockSize = 1000;
		stepping = 0;
		blocksPerStep = 1;
		startBlockForStep = 1;
		domain = Data.fqdn;
		boundary = "AGNITAS";
		mailer = "Agnitas AG";
		outputDirectories = new HashMap <> ();
		accountLogfile = StringOps.makePath ("$home", "log", "account.log");
		bounceLogfile = StringOps.makePath ("$home", "log", "extbounce.log");
		messageIDLogfile = StringOps.makePath ("$home", "log", "messageid.log");
	}
	
	public Mailing done () {
		return null;
	}
	public boolean exists () {
		return mailing != null;
	}
	public long id () {
		return id;
	}
	
	public void id (long nId) throws SQLException {
		id = nId;
		mailing = new MailingDAO (data.dbase, id);
		if (mailing.mailingID () == 0L) {
			mailing = null;
		}
	}
	public long mailingID () {
		return exists () ? mailing.mailingID () : 0L;
	}
	public long companyID () {
		return exists () ? mailing.companyID () : 0L;
	}
	public long mailinglistID () {
		return exists () ? mailing.mailinglistID () : 0L;
	}
	public long mailtemplateID () {
		return exists () ? mailing.mailtemplateID () : 0L;
	}
	public String name () {
		return exists () ? mailing.shortName () : null;
	}
	public Timestamp creationDate () {
		return exists () ? mailing.creationDate () : null;
	}
	public boolean deleted () {
		return exists () ? mailing.deleted () : false;
	}
	public String targetExpression () {
		return exists () ? mailing.targetExpression () : null;
	}
	public long splitID () {
		return exists () ? mailing.splitID () : 0L;
	}
	public int mailingType () {
		return exists () ? mailing.mailingType () : 0;
	}
	public String workStatus () {
		return exists () ? mailing.workStatus () : null;
	}
	public boolean workStatus (String newWorkStatus) throws SQLException {
		return exists () ? mailing.workStatus (data.dbase, newWorkStatus) : false;
	}
	public void setWorkStatus (String newWorkStatus) {
		if (data.maildropStatus.isWorldMailing ()) {
			String	oldWorkStatus = workStatus ();
			try {
				if (data.mailing.workStatus (newWorkStatus)) {
					data.logging (Log.INFO, "workstatus", "Updated working status from " + oldWorkStatus + " to " + newWorkStatus);
				} else {
					data.logging (Log.WARNING, "workstatus", "Failed to update working status from " + oldWorkStatus + " to " + newWorkStatus);
				}
			} catch (Exception e) {
				data.logging (Log.ERROR, "workstatus", "Failed to update working status from " + oldWorkStatus + " to " + newWorkStatus + " due to " + e.toString (), e);
			}
		}
	}
	public int priority () {
		return exists () ? mailing.priority () : -1;
	}
	public boolean frequencyCounterDisabled () {
		return exists () ? mailing.frequencyCounterDisabled () : false;
	}
	public boolean isWorkflowMailing () {
		return exists () ? mailing.isWorkflowMailing () : false;
	}
	public List <Media> media () {
		return exists () ? mailing.media () : null;
	}
	public Map <String, String> info () {
		return exists () ? mailing.info () : null;
	}
	public Map <String, String> item () {
		return exists () ? mailing.item () : null;
	}
	public long sourceTemplateID () {
		return exists () ? mailing.sourceTemplateID () : 0L;
	}
	public int sourceTemplatePriority () {
		return exists () ? mailing.sourceTemplatePriority () : 0;
	}
	
	public String defaultEncoding () {
		return defaultEncoding;
	}
	
	public String defaultCharset () {
		return defaultCharset;
	}
	
	public int blockSize () {
		return blockSize;
	}
	
	public void blockSize (int nBlockSize) {
		if ((nBlockSize > 0) && (nBlockSize != blockSize)) {
			blockSize = nBlockSize;
			blocksPerStep = 1;
		}
	}
	
	public int stepping () {
		return stepping;
	}
	
	public void stepping (int nStepping) {
		if ((nStepping >= 0) && (nStepping != stepping)) {
			stepping = nStepping;
		}
	}

	public int blocksPerStep () {
		return blocksPerStep;
	}
	
	public int startBlockForStep () {
		return startBlockForStep;
	}

	public long maxBytesPerOutputFile () {
		return maxBytesPerOutputFile;
	}
	
	public boolean createUUID () {
		return createUUID;
	}
	
	public void createUUID (boolean nCreateUUID) {
		createUUID = nCreateUUID;
	}
	
	public String subject () {
		return subject;
	}

	public void subject (String nSubject) {
		subject = nSubject;
	}
	
	public EMail fromEmail () {
		return fromEmail;
	}
	
	public void fromEmail (EMail nFromEmail) {
		fromEmail = nFromEmail;
	}
	
	public EMail replyTo () {
		return replyTo;
	}
	
	public void replyTo (EMail nReplyTo) {
		replyTo = nReplyTo;
	}

	public EMail envelopeFrom () {
		return envelopeFrom;
	}
	
	public void envelopeFrom (EMail nEnvelopeFrom) {
		envelopeFrom = nEnvelopeFrom;
	}
	
	public String encoding () {
		return encoding;
	}
	
	public void encoding (String nEncoding) {
		encoding = nEncoding;
	}
	
	public String charset () {
		return charset;
	}
	
	public void charset (String nCharset) {
		charset = nCharset;
	}
	
	public String domain () {
		return domain;
	}
	
	public void domain (String nDomain) {
		domain = nDomain;
	}
	
	public String boundary () {
		return boundary;
	}
	
	public void boundary (String nBoundary) {
		boundary = nBoundary;
	}
	
	public String mailer () {
		return mailer;
	}
	
	public void mailer (String nMailer) {
		mailer = nMailer;
	}

	public String accountLogfile () {
		return accountLogfile;
	}

	public String bounceLogfile () {
		return bounceLogfile;
	}
	
	public String messageIDLogfile () {
		return messageIDLogfile;
	}

	public String messageTransferAgent () {
		return messageTransferAgent;
	}

	/** increase starting block
	 */
	public void increaseStartblockForStepping () {
		++startBlockForStep;
	}

	/**
	 * checks if sender email is valid
	 */
	public boolean fromEmailIsValid () {
		return fromEmail != null && fromEmail.valid (false);
	}
	
	/**
	 * Get full sender email expression, if set, null otherwise
	 * 
	 * @return the senders email addresse, if set
	 */
	public String getFromEmailFull () {
		return fromEmail != null ? fromEmail.full : null;
	}
	
	/**
	 * Get sender email suitable for header
	 * 
	 * @return the header version fo the sender
	 */
	public String getFromEmailForHeader () {
		if (fromEmail == null) {
			return null;
		} else if (! fromEmail.full.equals (fromEmail.pure)) {
			return fromEmail.full_puny;
		} else {
			return "<" + fromEmail.full_puny + ">";
		}
	}
	
	/**
	 * Get reply to for header, if different from sender email
	 * 
	 * @return the header version for reply to
	 */
	public String getReplyToForHeader () {
		if (replyTo != null && replyTo.valid (true) && ((fromEmail == null) || (! fromEmail.full.equals (replyTo.full)))) {
			return replyTo.full_puny;
		}
		return null;
	}
	
	/**
	 * Set envelope from, if not already defined to fromEmail
	 */
	public void setEnvelopeFrom () {
		if (envelopeFrom == null) {
			envelopeFrom = fromEmail;
		}
	}
	
	/**
	 * Get envelope address
	 * @return the punycoded envelope address
	 */
	public String getEnvelopeFrom () {
		String	temp, env;

		env = null;
		if ((temp = data.company.info ("envelope-from", id)) != null) {
			EMail	temail = new EMail (temp);
			
			env = temail.pure_puny;
		}
		if ((env == null) && (envelopeFrom != null)) {
			env = envelopeFrom.pure_puny;
		}
		if (env == null) {
			env = fromEmail != null ? fromEmail.pure_puny : null;
		}
		return env;
	}
	
	/**
	 * Set encoding, if not already definied
	 */
	public void setEncoding () {
		if ((encoding == null) || (encoding.length () == 0)) {
			encoding = defaultEncoding;
		}
	}

	/**
	 * Set charset, if not already definied
	 */
	public void setCharset () {
		if ((charset == null) || (charset.length () == 0)) {
			charset = defaultCharset;
		}
	}
	
	/**
	 * returns the X-Mailer: header content
	 * 
	 * @return mailer name
	 */
	public String makeMailer () {
		if ((mailer != null) && (data.company.name () != null)) {
			return StringOps.replace (mailer, "[agnMANDANT]", data.company.name ());
		}
		return mailer;
	}
	
	/**
	 * returns an output directory for a given name
	 * 
	 * @param lookupName the name to lookup
	 * @return the path to the output directory or null
	 */
	public String outputDirectory (String lookupName) {
		return outputDirectories.get (lookupName);
	}
	
	/**
	 * return an output directory for company specific
	 * output writing
	 * 
	 * @param lookupName the name to lookup
	 * @return the path to the output directory or null
	 */
	public String outputDirectoryForCompany (String lookupName) {
		String	path = outputDirectory (lookupName);
		
		if (path != null) {
			String	companyPath = path + data.company.id ();
			try {
				if ((new File (companyPath)).isDirectory ()) {
					return companyPath;
				}
			} catch (Exception e) {
				// do nothing
			}
			return path + "0";
		}
		return path;
	}
	
	/**
	 * Write all mailing related settings to logfile
	 */
	public void logSettings () {
		data.logging (Log.DEBUG, "init", "\tmailing.id = " + id);
		if (exists ()) {
			data.logging (Log.DEBUG, "init", "\tmailing.mailingID = " + mailing.mailingID ());
			data.logging (Log.DEBUG, "init", "\tmailing.companyID = " + mailing.companyID ());
			data.logging (Log.DEBUG, "init", "\tmailing.mailinglistID = " + mailing.mailinglistID ());
			data.logging (Log.DEBUG, "init", "\tmailing.mailtemplateID = " + mailing.mailtemplateID ());
			data.logging (Log.DEBUG, "init", "\tmailing.isTemplate = " + mailing.isTemplate ());
			data.logging (Log.DEBUG, "init", "\tmailing.deleted = " + mailing.deleted ());
			data.logging (Log.DEBUG, "init", "\tmailing.name = " + mailing.shortName ());
			data.logging (Log.DEBUG, "init", "\tmailing.creationDate = " + mailing.creationDate ());
			data.logging (Log.DEBUG, "init", "\tmailing.targetExpression = " + mailing.targetExpression ());
			data.logging (Log.DEBUG, "init", "\tmailing.splitID = " + mailing.splitID ());
			data.logging (Log.DEBUG, "init", "\tmailing.mailingType = " + mailing.mailingType ());
			data.logging (Log.DEBUG, "init", "\tmailing.workStatus = " + mailing.workStatus ());
			data.logging (Log.DEBUG, "init", "\tmailing.priority = " + mailing.priority ());
			data.logging (Log.DEBUG, "init", "\tmailing.frequencyCounterDisabled = " + mailing.frequencyCounterDisabled ());
			data.logging (Log.DEBUG, "init", "\tmailing.isWorkflowMailing = " + mailing.isWorkflowMailing ());
			data.logging (Log.DEBUG, "init", "\tmailing.sourceTemplateID = " + mailing.sourceTemplateID ());
			data.logging (Log.DEBUG, "init", "\tmailing.sourceTemplatePriority = " + mailing.sourceTemplatePriority ());
		}
		data.logging (Log.DEBUG, "init", "\tmailing.defaultEncoding = " + defaultEncoding);
		data.logging (Log.DEBUG, "init", "\tmailing.defaultCharset = " + defaultCharset);
		data.logging (Log.DEBUG, "init", "\tmailing.blockSize = " + blockSize);
		data.logging (Log.DEBUG, "init", "\tmailing.stepping = " + stepping);
		data.logging (Log.DEBUG, "init", "\tmailing.blocksPerStep = " + blocksPerStep);
		data.logging (Log.DEBUG, "init", "\tmailing.startBlockForStep = " + startBlockForStep);
		data.logging (Log.DEBUG, "init", "\tmailing.maxBytesPerOutputFile = " + maxBytesPerOutputFile);
		data.logging (Log.DEBUG, "init", "\tmailing.createUUID = " + createUUID);
		data.logging (Log.DEBUG, "init", "\tmailing.subject = " + (subject == null ? "*not set*" : subject));
		data.logging (Log.DEBUG, "init", "\tmailing.fromEmail = " + (fromEmail == null ? "*not set*" : fromEmail.toString ()));
		data.logging (Log.DEBUG, "init", "\tmailing.replyTo = " + (replyTo == null ? "*not set*" : replyTo.toString ()));
		data.logging (Log.DEBUG, "init", "\tmailing.envelopeFrom = " + (envelopeFrom == null ? "*not set*" : envelopeFrom.toString ()));
		data.logging (Log.DEBUG, "init", "\tmailing.encoding = " + encoding);
		data.logging (Log.DEBUG, "init", "\tmailing.charset = " + charset);
		data.logging (Log.DEBUG, "init", "\tmailing.domain = " + domain);
		data.logging (Log.DEBUG, "init", "\tmailing.boundary = " + boundary);
		data.logging (Log.DEBUG, "init", "\tmailing.mailer = " + mailer);
		outputDirectories
			.entrySet ()
			.stream ()
			.forEach ((e) -> data.logging (Log.DEBUG, "init", "\tmailing.outputDirectory[" + e.getKey () + "] = " + e.getValue ()));
		data.logging (Log.DEBUG, "init", "\tmailing.accountLogfile = " + accountLogfile);
		data.logging (Log.DEBUG, "init", "\tmailing.bounceLogfile = " + bounceLogfile);
		data.logging (Log.DEBUG, "init", "\tmailing.messageIDLogfile = " + messageIDLogfile);
		data.logging (Log.DEBUG, "init", "\tmailing.messageTransferAgent = " + (messageTransferAgent == null ? "*not set*" : messageTransferAgent));
	}

	/**
	 * Configure from external resource
	 * 
	 * @param cfg the configuration
	 */
	public void configure (Config cfg) {
		defaultEncoding = cfg.cget ("DEFAULT_ENCODING", defaultEncoding);
		defaultCharset = cfg.cget ("DEFAULT_CHARSET", defaultCharset);
		blockSize = cfg.cget ("BLOCKSIZE", blockSize);
		maxBytesPerOutputFile = cfg.cget ("MAX_BYTES_PER_OUTPUT_FILE", maxBytesPerOutputFile);
		createUUID = cfg.cget ("CREATE_UUID", createUUID);
		domain = cfg.cget ("DOMAIN", domain);
		boundary = cfg.cget ("BOUNDARY", boundary);
		mailer = cfg.cget ("MAILER", mailer);
		for (String key : cfg.getKeys ()) {
			if (key.endsWith ("DIR") && (key.length () > 3)) {
				outputDirectories.put (key.substring (0, key.length () - 3).toLowerCase (), cfg.cget (key));
			}
		}
		accountLogfile = cfg.cget ("ACCOUNT_LOGFILE", accountLogfile);
		bounceLogfile = cfg.cget ("BOUNCE_LOGFILE", bounceLogfile);
		messageIDLogfile = cfg.cget ("MESSAGEID_LOGFILE", messageIDLogfile);
		messageTransferAgent = cfg.cget ("MESSAGE_TRANSFER_AGENT", cfg.cget ("MTA", System.getenv ("MTA")));
	}
}
