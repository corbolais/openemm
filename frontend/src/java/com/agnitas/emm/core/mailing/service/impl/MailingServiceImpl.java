/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service.impl;

import java.beans.PropertyDescriptor;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.mail.internet.InternetAddress;

import org.agnitas.beans.DynamicTagContent;
import org.agnitas.beans.Mailing;
import org.agnitas.beans.MailingBase;
import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.Mediatype;
import org.agnitas.dao.MailinglistDao;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.commons.util.DateUtil;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.emm.core.mailing.service.MailingModel;
import org.agnitas.emm.core.mailing.service.MailingNotExistException;
import org.agnitas.emm.core.mailing.service.SendDateNotInFutureException;
import org.agnitas.emm.core.mailing.service.TemplateNotExistException;
import org.agnitas.emm.core.mailing.service.WorldMailingAlreadySentException;
import org.agnitas.emm.core.mailing.service.WorldMailingWithoutNormalTypeException;
import org.agnitas.emm.core.mailinglist.service.MailinglistNotExistException;
import org.agnitas.emm.core.mailinglist.service.impl.MailinglistException;
import org.agnitas.emm.core.mediatypes.factory.MediatypeFactory;
import org.agnitas.emm.core.target.service.TargetNotExistException;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.core.validator.annotation.Validate;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.DynTagException;
import org.agnitas.util.HtmlUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.annotation.Transactional;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComMailing;
import com.agnitas.beans.ComTrackableLink;
import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.MaildropEntry;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.beans.TargetLight;
import com.agnitas.beans.impl.MaildropEntryImpl;
import com.agnitas.dao.ComMailingComponentDao;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.ComTargetDao;
import com.agnitas.dao.DynamicTagDao;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.report.enums.fields.MailingTypes;
import com.agnitas.emm.core.workflow.beans.WorkflowIcon;
import com.agnitas.emm.core.workflow.beans.WorkflowIconType;
import com.agnitas.emm.core.workflow.service.util.WorkflowUtils;
import com.agnitas.service.ComMailingContentService;
import com.agnitas.util.OneOf;

public abstract class MailingServiceImpl implements MailingService, ApplicationContextAware {
	// TODO Remove ApplicationContextAware
	
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(MailingServiceImpl.class);
	private static final int DEFAULT_LOCKING_DURATION_SECONDS = 60; // 1 minute

	protected ConfigService configService;
    
	private JavaMailService javaMailService;

	protected ApplicationContext applicationContext;

	@Resource(name="MailingDao")
	protected ComMailingDao mailingDao;
	
	@Resource(name="MailinglistDao")
	private MailinglistDao mailinglistDao;
	
	@Resource(name="TargetDao")
	private ComTargetDao targetDao;

	/** DAO for accessing dyn-tags. */
	@Resource(name="DynamicTagDao")
	private DynamicTagDao dynamicTagDao;
	
	/** DAO accessing mailing components. */
	private ComMailingComponentDao mailingComponentDao;
	
	@Resource(name="MaildropService")
	private MaildropService maildropService;

	@Resource(name="MediatypeFactory")
	private MediatypeFactory mediatypeFactory;
	
	@Resource(name="MailingBaseService")
	private ComMailingBaseService mailingBaseService;
	
	@Resource(name="MailingContentService")
	protected ComMailingContentService mailingContentService;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	/**
	 * Set configuration service.
	 * 
	 * @param configService configuration service
	 */
	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	@Required
	public void setJavaMailService(JavaMailService javaMailService) {
		this.javaMailService = javaMailService;
	}

	@Required
	public void setMailingDao(ComMailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}
	
	@Required
	public void setMailingBaseService(ComMailingBaseService mailingBaseService) {
		this.mailingBaseService = mailingBaseService;
	}
	
	@Required
	public void setMailingContentService(ComMailingContentService mailingContentService) {
		this.mailingContentService = mailingContentService;
	}
	
	protected abstract MailingService getSelfReference();
		
	@Override
	@Transactional
	@Validate(groups = MailingModel.UpdateGroup.class)
	public void updateMailing(MailingModel model, List<UserAction> userActions) throws MailinglistException {
        List<String> actions = new LinkedList<>();
        ComMailing aMailing = (ComMailing) prepareMailingForAddOrUpdate(model, getMailing(model), actions);
		
       	List<String> dynNamesForDeletion = new Vector<>();
		try {
			aMailing.buildDependencies(true, dynNamesForDeletion, applicationContext);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
       	mailingDao.saveMailing(aMailing, false);
        mailingDao.updateStatus(aMailing.getId(), "edit");

		dynamicTagDao.markNamesAsDeleted( aMailing.getId(), dynNamesForDeletion);

		final String type = (aMailing.isIsTemplate() ? "edit template settings" : "edit mailing settings");
		final String description = getMailingDescription(aMailing);

        if (actions.isEmpty()) {
        	userActions.add(new UserAction(type, description));
        } else {
            for (String action : actions) {
				userActions.add(new UserAction(type, description + ". " + action));
            }
        }
    }

    @Override
	public List<LightweightMailing> getAllMailingNames(@VelocityCheck ComAdmin admin) {
		return mailingDao.getMailingNames(admin.getCompanyID(), admin.getAdminID(), 0);
	}

	@Override
	public int getMailGenerationMinutes(@VelocityCheck int companyID) {
		return configService.getIntegerValue(ConfigValue.MailGenerationTimeMinutes, companyID);
	}

	@Override
	public List<Mailing> getDuplicateMailing(List<WorkflowIcon> icons, @VelocityCheck int companyId) {
		Set<Integer> metIds = new HashSet<>();

        return icons.stream()
				.filter(this::isNonReusableMailingIcon)
                .map(WorkflowUtils::getMailingId)
                .filter(id -> id > 0 && !metIds.add(id))  // Ignore unique ids.
                .map(id -> mailingDao.getMailing(id, companyId))
                .collect(Collectors.toList());
	}

	/**
	 * Check if a given icon represents a mailing of non-reusable type (same mailing can't be used in more than one icon).
	 *
	 * @param icon a workflow icon to check.
	 * @return {@code true} if icon represents a mailing of non-reusable type or {@code false} otherwise.
	 */
	private boolean isNonReusableMailingIcon(WorkflowIcon icon) {
		switch (icon.getType()) {
			case WorkflowIconType.Constants.MAILING_ID:
			case WorkflowIconType.Constants.FOLLOWUP_MAILING_ID:
				return true;
			default:
				return false;
		}
	}

	@Override
	@Validate(groups = MailingModel.GetGroup.class)
	@Transactional
	public String getMailingStatus(MailingModel model) {
		int mailingID = model.getMailingId();
		int companyID = model.getCompanyId();

		if(!mailingDao.exist(mailingID, companyID, false)){
			throw new MailingNotExistException();
		}

		return mailingDao.getWorkStatus(model.getCompanyId(), mailingID);
	}

    @Override
    @Validate(groups = MailingModel.SendGroup.class)
    public void sendMailing(MailingModel model, List<UserAction> userActions) throws Exception {
    	MaildropEntry maildrop = addMaildropEntry(model, userActions);
        Mailing mailing = getMailing(model);
        if (maildrop.getGenStatus() == 1) {
            if (model.getMaildropStatus() == MaildropStatus.WORLD
                    && mailing.getMailingType() != MailingTypes.NORMAL.getCode()) {
                throw new WorldMailingWithoutNormalTypeException();
            }

            mailing.triggerMailing(maildrop.getId(), new Hashtable<>(), applicationContext);
        }

        if (!DateUtil.isDateForImmediateGeneration(maildrop.getGenDate()) && ((mailing.getMailingType() == MailingTypes.NORMAL.getCode()) ||
                (mailing.getMailingType() == MailingTypes.FOLLOW_UP.getCode()))) {
            if (maildrop.getStatus() == MaildropStatus.ADMIN.getCode()) {
                mailingDao.updateStatus(maildrop.getMailingID(), "admin");
            } else if (maildrop.getStatus() == MaildropStatus.TEST.getCode()) {
            	mailingDao.updateStatus(maildrop.getMailingID(), "test");
            } else {
                mailingDao.updateStatus(maildrop.getMailingID(), "scheduled");
            }
        }
    }

	@Override
	@Validate(groups = MailingModel.GetGroup.class)
	@Transactional
	public void deleteMailing(MailingModel model) {
		if (!mailingDao.exist(model.getMailingId(), model.getCompanyId(), model.isTemplate())) {
			throw model.isTemplate() ? new TemplateNotExistException() : new MailingNotExistException();
		}
		mailingDao.deleteMailing(model.getMailingId(), model.getCompanyId());
        mailingDao.updateStatus(model.getMailingId(), "disable");
	}
	
	@Override
	public List<MailingComponent> getMailingComponents(int mailingID, @VelocityCheck int companyID)
			throws MailingNotExistException {
		
		Mailing mailing = mailingDao.getMailing(mailingID, companyID);
		
		if (mailing == null || mailing.getId() != mailingID || mailing.getCompanyID() != companyID) {
			throw new MailingNotExistException();
		}
		
		return mailingComponentDao.getMailingComponents(mailingID, companyID, 0);
	}

	/**
	 * Set DAO accessing mailing components.
	 * 
	 * @param mailingComponentDao DAO accessing mailing components
	 */
	public void setMailingComponentDao(ComMailingComponentDao mailingComponentDao) {
		this.mailingComponentDao = mailingComponentDao;
	}
	
	protected Mailing prepareMailingForAddOrUpdate(MailingModel model, Mailing aMailing, List<String> actions) throws MailinglistNotExistException {
        final String editKeyword = "edit ";
        StringBuilder actionMessage = new StringBuilder(editKeyword);
		aMailing.setCompanyID(model.getCompanyId());
		aMailing.setDescription(model.getDescription() != null ? model.getDescription() : "");
		aMailing.setShortname(model.getShortname());
		aMailing.setIsTemplate(model.isTemplate());

		if (model.getMailinglistId() != 0) {
			if (!mailinglistDao.exist(model.getMailinglistId(), model.getCompanyId())) {
				throw new MailinglistNotExistException(model.getMailingId());
			}
            if (actions != null && aMailing.getMailinglistID() != model.getMailinglistId()) {
                actionMessage.append("mailing list changed from ").append(aMailing.getMailinglistID()).append(" to ").append(model.getMailinglistId());
                if (model.getMailinglistId() == 0) {
                	//send mail
                	String message = "Mailinglist ID in mailing (" + aMailing.getId() + ") was set to 0. Please check if the content still exists!";
                	javaMailService.sendEmail(configService.getValue(ConfigValue.Mailaddress_Error), "Mailinglist set to 0", message, HtmlUtils.replaceLineFeedsForHTML(message));
                }
                actions.add(actionMessage.toString());
                actionMessage.delete(editKeyword.length(), actionMessage.length());
            }
			aMailing.setMailinglistID(model.getMailinglistId());
		}

		aMailing.setTargetMode(model.getTargetMode().getValue());
		// aMailing.setTargetID(targetID);
		if (model.getTargetIDList() != null && model.getTargetIDList().size() > 0) {
			Set<Integer> tGroups = new HashSet<>();
			for (Integer targetID : model.getTargetIDList()) {
				if (targetDao.getTarget(targetID, model.getCompanyId()) == null) {
					throw new TargetNotExistException(targetID);
				}
				tGroups.add(targetID);
			}
            if (actions != null) {
                Set<Integer> oldGroups = new HashSet<>();
                if(aMailing.getTargetGroups() != null) {
                    oldGroups.addAll(aMailing.getTargetGroups());
                }
                Set<Integer> newGroups = new HashSet<>(tGroups);
                newGroups.removeAll(oldGroups);
                oldGroups.removeAll(tGroups);
                if(oldGroups.size() != 0){
                    actionMessage.append("removed ");
                    for (Integer next : oldGroups) {
                        actionMessage.append(next).append(", ");
                    }
                }
                if(newGroups.size() != 0){
                    actionMessage.append("added ");
                    for (Integer next : newGroups) {
                        actionMessage.append(next).append(", ");
                    }
                }
            }
            aMailing.setTargetGroups(tGroups);
        } else {
            if(actions != null && aMailing.getTargetGroups() != null && aMailing.getTargetGroups().size() > 0){
                actionMessage.append("removed ");
                for (Integer next : aMailing.getTargetGroups()) {
                    actionMessage.append(next).append(", ");
                }
            }
			aMailing.setTargetGroups(null);
		}
		
		if (actions != null) {
	        if (actionMessage.length() != editKeyword.length()){
	            actionMessage.delete(actionMessage.length()-2,actionMessage.length()); //remove last two characters: comma and space
	            actionMessage.insert(editKeyword.length(),"target groups ");
	            actions.add(actionMessage.toString());
	            actionMessage.delete(editKeyword.length(), actionMessage.length());
	        }
		}

        if (actions != null && aMailing.getMailingType() != model.getMailingType().getValue()){
            actionMessage.append("mailing type from ")
                    .append(MailingModel.getMailingType(aMailing.getMailingType()).getName())
                    .append(" to ")
                    .append(model.getMailingTypeString());
            actions.add(actionMessage.toString());
            actionMessage.delete(editKeyword.length(), actionMessage.length());
        }
        aMailing.setMailingType(model.getMailingType().getValue());

        MediatypeEmail paramEmail = aMailing.getEmailParam();
		if (paramEmail == null) {
			paramEmail = (MediatypeEmail) applicationContext.getBean("MediatypeEmail");
			paramEmail.setCompanyID(model.getCompanyId());
			paramEmail.setMailingID(aMailing.getId());
		}
		paramEmail.setStatus(Mediatype.STATUS_ACTIVE);
		paramEmail.setSubject(model.getSubject());
		try {
			InternetAddress adr = new InternetAddress(model.getSenderAddress(), model.getSenderName());
			paramEmail.setFromEmail(adr.getAddress());
			paramEmail.setFromFullname(adr.getPersonal());
			
			InternetAddress reply = new InternetAddress(model.getReplyToAddress(), model.getReplyToName());
			paramEmail.setReplyEmail(reply.getAddress());
			paramEmail.setReplyFullname(reply.getPersonal());
		} catch (UnsupportedEncodingException e) {
			logger.error("UnsupportedEncodingException in sender/reply address", e);
			throw new RuntimeException(e);
		}
		paramEmail.setCharset(model.getCharset());
		paramEmail.setMailFormat(model.getFormat().getValue());
		paramEmail.setLinefeed(model.getLinefeed());
		paramEmail.setPriority(1);
		
		paramEmail.setOnepixel(model.getOnePixel().getName());
		
		Map<Integer, Mediatype> mediatypes = aMailing.getMediatypes();
		mediatypes.put(0, paramEmail);
		aMailing.setMediatypes(mediatypes);
		
//		aMailing.setUseDynamicTemplate(model.isAutoUpdate());
		
		return aMailing;
	}

	/**
	 * Construct mailing description acceptable for user log
	 * @param mailing a mailing instance.
	 * @return mailing description including name and ID.
	 */
	protected String getMailingDescription(Mailing mailing) {
		return mailing.getShortname() + " (" + mailing.getId() + ")";
	}
	
	@Override
	public boolean isActiveIntervalMailing(final int mailingID) {
		return mailingDao.isActiveIntervalMailing(mailingID);
	}

	@Override
	@Transactional
	@Validate(groups = MailingModel.SendGroup.class)
	public MaildropEntry addMaildropEntry(MailingModel model, List<UserAction> userActions) throws Exception {
		if (!DateUtil.isValidSendDate(model.getSendDate())) {
			throw new SendDateNotInFutureException();
		}
		
        Calendar now = Calendar.getInstance();
        
		Mailing mailing = getMailing(model);
		
		if (model.getMaildropStatus() == MaildropStatus.WORLD && maildropService.isActiveMailing(mailing.getId(), mailing.getCompanyID())) {
			throw new WorldMailingAlreadySentException();
		}

		MaildropEntry maildrop = new MaildropEntryImpl();

		maildrop.setStatus(model.getMaildropStatus().getCode());
		maildrop.setMailingID(model.getMailingId());
		maildrop.setCompanyID(model.getCompanyId());
        maildrop.setStepping(model.getStepping());
		maildrop.setBlocksize(model.getBlocksize());

		maildrop.setSendDate(model.getSendDate());
		
		Calendar tmpGen = Calendar.getInstance();
        tmpGen.setTime(model.getSendDate());
        tmpGen.add(Calendar.MINUTE, -this.getMailGenerationMinutes(model.getCompanyId()));
        if(tmpGen.before(now)) {
            tmpGen=now;
        }
        maildrop.setGenDate(tmpGen.getTime());
		maildrop.setGenChangeDate(now.getTime());
		
		if( model.getMaildropStatus() == MaildropStatus.WORLD) {
			maildrop.setGenStatus(DateUtil.isDateForImmediateGeneration(maildrop.getGenDate()) ? 1 : 0);
		} else if( model.getMaildropStatus() == MaildropStatus.TEST || model.getMaildropStatus() == MaildropStatus.ADMIN) {
			maildrop.setGenStatus( 1);
		}

        mailing.getMaildropStatus().add(maildrop);

        mailingDao.saveMailing(mailing, false);
        if (logger.isInfoEnabled()) {
        	logger.info("send mailing id: " + mailing.getId() + " type: "+maildrop.getStatus());
        }

        SimpleDateFormat dateTimeFormat = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.UK);
        dateTimeFormat.applyPattern(dateTimeFormat.toPattern().replaceFirst("y+", "yyyy").replaceFirst(", ", " "));
		String strDate = dateTimeFormat.format(model.getSendDate());
		String description = String.format("Date: %s. Mailing %s(%d) %s", strDate, mailing.getShortname(), mailing.getId(), "normal");

        userActions.add(new UserAction("edit send date", description));

        return maildrop;
	}

	@Override
	public boolean isMailingWorldSent(int mailingID, @VelocityCheck int companyID) throws MailingNotExistException {
		Mailing mailing = mailingDao.getMailing(mailingID, companyID);
		
		if(mailing == null || mailing.getId() == 0) {
			throw new MailingNotExistException();
		}

		return this.maildropService.isActiveMailing(mailing.getId(), mailing.getCompanyID());
	}
	
	@Override
	@Transactional
	@Validate(groups = MailingModel.AddFromTemplateGroup.class)
	public int addMailingFromTemplate(MailingModel model) {
    	Mailing template = mailingDao.getMailing(model.getTemplateId(), model.getCompanyId());

    	if (template == null || !template.isIsTemplate()) {
    		throw new TemplateNotExistException();
    	}

		try {
			Mailing aMailing = cloneBean(template, "Mailing");

			aMailing.setId(0);
			aMailing.setDescription(model.getDescription() != null ? model.getDescription() : "");
			aMailing.setShortname(model.getShortname());
			aMailing.setIsTemplate(false);
			aMailing.setMailTemplateID(model.getTemplateId());

			// copy components
			for (MailingComponent compOrg : template.getComponents().values()) {
				MailingComponent compNew = cloneBean(compOrg, "MailingComponent");
				compNew.setId(0);
				compNew.setMailingID(0);
				
				if (compOrg.getBinaryBlock() != null) {
					compNew.setBinaryBlock(compOrg.getBinaryBlock(), compOrg.getMimeType());
				} else {
					compNew.setEmmBlock(compOrg.getEmmBlock(), compOrg.getMimeType());
				}
				
				aMailing.getComponents().put(compNew.getComponentName(), compNew);
			}

			// copy dyntags
			for (DynamicTag tagOrg : template.getDynTags().values()) {
				DynamicTag tagNew = cloneBean(tagOrg, "DynamicTag");
				tagNew.setId(0);
				tagNew.setMailingID(0);
				tagNew.setDynContent(new HashMap<Integer, DynamicTagContent>());
				for (DynamicTagContent contentOrg : tagOrg.getDynContent().values()) {
					DynamicTagContent contentNew = cloneBean(contentOrg, "DynamicTagContent");
					contentNew.setId(0);
					contentNew.setDynNameID(0);
					tagNew.addContent(contentNew);
				}
				aMailing.getDynTags().put(tagNew.getDynName(), tagNew);
			}

			// copy urls
			for (ComTrackableLink linkOrg : template.getTrackableLinks().values()) {
				ComTrackableLink linkNew =  cloneBean(linkOrg, "TrackableLink");
				linkNew.setId(0);
				linkNew.setMailingID(0);
				aMailing.getTrackableLinks().put(linkNew.getFullUrl(), linkNew);
			}

			// copy mediatypes
			for (Entry<Integer, Mediatype> entry : template.getMediatypes().entrySet()) {
				int mediaTypeCode = entry.getKey();

				if (mediatypeFactory.isTypeSupported(mediaTypeCode)) {
					Mediatype mediatypeNew = mediatypeFactory.create(mediaTypeCode);
					aMailing.getMediatypes().put(mediaTypeCode, cloneBean(mediatypeNew, entry.getValue()));
				}
			}

			aMailing.setUseDynamicTemplate(model.isAutoUpdate());

	        mailingDao.saveMailing(aMailing, false);

	        return aMailing.getId();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T cloneBean(T orig, String beanName) throws IllegalAccessException, InvocationTargetException {
		return cloneBean((T) applicationContext.getBean(beanName), orig);
	}

	private <T> T cloneBean(T dest, T orig) throws IllegalAccessException, InvocationTargetException {
		PropertyDescriptor[] origDescriptors = PropertyUtils.getPropertyDescriptors(orig);
		for (PropertyDescriptor descriptor : origDescriptors) {
			String name = descriptor.getName();
			if (PropertyUtils.isReadable(orig, name) && PropertyUtils.isWriteable(dest, name)) {
				try {
					Object value = PropertyUtils.getSimpleProperty(orig, name);
					if (!(value instanceof Collection<?>) && !(value instanceof Map<?, ?>)) {
						PropertyUtils.setSimpleProperty(dest, name, value);
					}
				} catch (NoSuchMethodException e) {
					logger.debug("Error writing to '" + name + "' on class '" + dest.getClass() + "'", e);
				}
			}
		}
		return dest;
	}

	@Override
	@Validate(groups = MailingModel.GetGroup.class)
	@Transactional
	public Mailing getMailing(MailingModel model) {
		Mailing mailing = mailingDao.getMailing(model.getMailingId(), model.getCompanyId());
		if (model.isTemplate() && (mailing == null || mailing.getId() == 0 || !mailing.isIsTemplate())) {
			throw new TemplateNotExistException();
		} else if (!model.isTemplate() && (mailing == null || mailing.getId() == 0 || mailing.isIsTemplate())) {
			throw new MailingNotExistException();
		}
		return mailing;
	}
	
	@Override
	@Transactional
	public ComMailing getMailing(final int companyID, final int mailingID) {
		final ComMailing mailing = mailingDao.getMailing(mailingID, companyID);
		
		if (mailing == null || mailing.getId() == 0) {
			throw new MailingNotExistException();
		} else {
			return mailing;
		}
	}

	@Override
	@Transactional
	@Validate(groups = MailingModel.AddGroup.class)
	public int addMailing(MailingModel model) throws MailinglistNotExistException {
		int result = 0;
		Mailing aMailing = prepareMailingForAddOrUpdate(model, (Mailing) applicationContext.getBean("Mailing"), null);

		MailingComponent comp = null;

		comp = (MailingComponent) applicationContext.getBean("MailingComponent");
		comp.setCompanyID(model.getCompanyId());
		comp.setComponentName("agnText");
		comp.setType(MailingComponent.TYPE_TEMPLATE);
		comp.setEmmBlock("[agnDYN name=\"emailText\"/]", "text/plain");
		aMailing.addComponent(comp);

		comp = (MailingComponent) applicationContext.getBean("MailingComponent");
		comp.setCompanyID(model.getCompanyId());
		comp.setComponentName("agnHtml");
		comp.setType(MailingComponent.TYPE_TEMPLATE);
		comp.setEmmBlock("[agnDYN name=\"emailHtml\"/]", "text/html");
		aMailing.addComponent(comp);

		try {
			aMailing.buildDependencies(true, applicationContext);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}

		mailingDao.saveMailing(aMailing, false);
		result = aMailing.getId();
		return result;
	}

	@Override
	@Validate(groups = MailingModel.CompanyGroup.class)
	public List<Mailing> getMailings(MailingModel model) {
		return mailingDao.getMailings(model.getCompanyId(), model.isTemplate());
	}

	@Override
	@Validate(groups = MailingModel.GetForMLID.class)
	@Transactional
	public List<Mailing> getMailingsForMLID(MailingModel model) throws MailinglistException {
		if (!mailinglistDao.exist(model.getMailinglistId(), model.getCompanyId())) {
			throw new MailinglistNotExistException(model.getMailinglistId());
		}
		return mailingDao.getMailingsForMLID(model.getCompanyId(), model.getMailinglistId());
	}

	@Override
	public boolean switchStatusmailOnErrorOnly(@VelocityCheck int companyId, int mailingId, boolean statusmailOnErrorOnly) {
		if (mailingId <= 0 || !mailingDao.exist(mailingId, companyId)) {
			return false;
		} else {
			return BooleanUtils.toBoolean(mailingDao.saveStatusmailOnErrorOnly(companyId, mailingId, statusmailOnErrorOnly));
		}
	}

	@Override
	public boolean isTextVersionRequired(@VelocityCheck int companyId, int mailingId) {
		return mailingDao.isTextVersionRequired(companyId, mailingId);
	}

	@Override
	public List<LightweightMailing> listAllActionBasedMailingsForMailinglist(final int companyID, int mailinglistID) {
		return this.mailingDao.listAllActionBasedMailingsForMailinglist(companyID, mailinglistID);
	}

	@Override
	public final LightweightMailing getLightweightMailing(final int companyId, final int mailingId) throws MailingNotExistException {
		return this.mailingDao.getLightweightMailing(companyId, mailingId);
	}

	@Override
	public final List<TargetLight> listTargetGroupsOfMailing(final int companyID, final int mailingID) throws MailingNotExistException {
		final ComMailing aMailing = getMailing(companyID, mailingID);
		
		final Collection<Integer> targetIdList = aMailing.getAllReferencedTargetGroups();

		return this.targetDao.getTargetLights(companyID, targetIdList, false);
	}

	@Override
	public String getTargetExpression(@VelocityCheck final int companyId, final int mailingId) {
		return mailingDao.getTargetExpression(mailingId, companyId);
	}

	@Override
	@Transactional
	public boolean tryToLock(ComAdmin admin, int mailingId) {
		return mailingDao.tryToLock(mailingId, admin.getAdminID(), admin.getCompanyID(), DEFAULT_LOCKING_DURATION_SECONDS, TimeUnit.SECONDS);
	}
	
	@Override
	public boolean isDeliveryComplete(final int companyID, final int mailingID) {
		final LightweightMailing mailing = this.mailingDao.getLightweightMailing(companyID, mailingID);

		return isDeliveryComplete(mailing);
	}
	
	@Override
	public final boolean isDeliveryComplete(final LightweightMailing mailing) {
		return mailing.getWorkStatus().isPresent() && OneOf.oneObjectOf(mailing.getWorkStatus().get(), "mailing.status.sent", "mailing.status.norecipients");
	}

	@Override
	public void updateStatus(int companyID, int mailingID, String status) {
		this.mailingDao.updateStatus(mailingID, status);
	}

	@Override
	public List<Integer> listFollowupMailingIds(int companyID, int mailingID, boolean includeUnscheduled) {
		return this.mailingDao.getFollowupMailings(mailingID, companyID, includeUnscheduled);
	}

    @Override
    public boolean generateMailingTextContentFromHtml(ComAdmin admin, int mailingId) throws Exception {
		Mailing mailing = mailingDao.getMailing(mailingId, admin.getCompanyID());
		if (mailing == null) {
			return false;
		}
		
		try {
			mailingContentService.generateTextContent(mailing);
		} catch (DynTagException e) {
			logger.error("Error occurred while generating text content: " + e.getMessage(), e);
			return false;
		}
		
		List<String> dynNamesForDeletion = new ArrayList<>();
		try {
			mailing.buildDependencies(true, dynNamesForDeletion, applicationContext);
		} catch (Exception e) {
			logger.error("Error occurred while building dependencies: " + e.getMessage(), e);
        	throw e;
		}
		
		mailingDao.cleanupContentForDynNames(mailingId, admin.getCompanyID(), dynNamesForDeletion);
		dynamicTagDao.markNamesAsDeleted(mailingId, dynNamesForDeletion);
		mailingBaseService.saveMailingWithUndo(mailing, admin.getAdminID(), false);
		
		return true;
    }

	@Override
	public List<LightweightMailing> getLightweightMailings(final ComAdmin admin) {
		return mailingDao.getLightweightMailings(admin.getCompanyID());
	}

	@Override
	public List<LightweightMailing> getLightweightIntervalMailings(final ComAdmin admin) {
		return mailingDao.getLightweightIntervalMailings(admin.getCompanyID());
	}

	@Override
	public List<LightweightMailing> getMailingsDependentOnTargetGroup(final int companyID, final int id) {
		return mailingDao.getMailingsDependentOnTargetGroup(companyID, id);
	}

	@Override
	public List<Mailing> getTemplates(ComAdmin admin) {
		return mailingDao.getTemplates(admin.getCompanyID(), 0);
	}

	@Override
	public List<MailingBase> getTemplatesWithPreview(final ComAdmin admin, final String sort, final String direction) {
		return mailingDao.getMailingTemplatesWithPreview(admin.getCompanyID(), 0, sort, direction);
	}

    @Override
    public List<MailingBase> getMailingsByStatusE(@VelocityCheck int companyId) {
        if (companyId > 0) {
        	return mailingDao.getMailingsByStatusE(companyId);
		} else {
        	return new ArrayList<>();
		}
    }
}
