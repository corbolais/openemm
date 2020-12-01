/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint;

import org.agnitas.beans.MailingComponent;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Utils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.springws.jaxb.GetMailingContentRequest;
import com.agnitas.emm.springws.jaxb.GetMailingContentResponse;
import com.agnitas.emm.springws.jaxb.MailingContent;

@Endpoint
public class GetMailingContentEndpoint extends BaseEndpoint {
	private static final Logger classLogger = Logger.getLogger(GetMailingContentEndpoint.class);

	private MailingService mailingService;

	public GetMailingContentEndpoint(@Qualifier("MailingService") MailingService mailingService) {
		this.mailingService = mailingService;
	}

	@PayloadRoot(namespace = Utils.NAMESPACE_COM, localPart = "GetMailingContentRequest")
	public @ResponsePayload GetMailingContentResponse getMailingcontent(@RequestPayload GetMailingContentRequest request) {
		if (classLogger.isInfoEnabled()) {
			classLogger.info( "Entered MailingTemplatesContentEndpoint.getMailingcontent()");
		}
		
		GetMailingContentResponse response = new GetMailingContentResponse();

		int mailingId = request.getMailingId();
		int companyId = Utils.getUserCompany();

		GetMailingContentResponse.Items items = new GetMailingContentResponse.Items();
		for (MailingComponent component : mailingService.getMailingComponents(mailingId, companyId)) {
			MailingContent mailingContent = new MailingContent();
			mailingContent.setName(component.getComponentName());
			mailingContent.setContent(component.getEmmBlock());
			items.getItem().add(mailingContent);
		}
		response.setItems(items);
		
		if (classLogger.isInfoEnabled()) {
			classLogger.info( "Leaving GetMailingContentEndpoint.getMailingcontent()");
		}
		
		return response;
	}
}
