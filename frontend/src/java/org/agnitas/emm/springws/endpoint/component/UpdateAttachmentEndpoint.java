/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.component;

import javax.annotation.Resource;

import org.agnitas.beans.MailingComponent;
import org.agnitas.emm.core.component.service.ComponentModel;
import org.agnitas.emm.core.component.service.ComponentService;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.ObjectFactory;
import org.agnitas.emm.springws.jaxb.UpdateAttachmentRequest;
import org.agnitas.emm.springws.jaxb.UpdateAttachmentResponse;
import org.springframework.ws.server.endpoint.AbstractMarshallingPayloadEndpoint;

public class UpdateAttachmentEndpoint extends AbstractMarshallingPayloadEndpoint {

	@Resource
	private ComponentService componentService;
	@Resource
	private ObjectFactory objectFactory;
	
	@Override
	protected Object invokeInternal(Object arg0) throws Exception {
		UpdateAttachmentRequest request = (UpdateAttachmentRequest) arg0;
		
		ComponentModel model = new ComponentModel();
		model.setCompanyId(Utils.getUserCompany());
		model.setComponentId(request.getComponentID());
		model.setMimeType(request.getMimeType());
		model.setComponentType(MailingComponent.TYPE_ATTACHMENT);
		model.setComponentName(request.getComponentName());
		model.setData(request.getData());

		UpdateAttachmentResponse response = objectFactory.createUpdateAttachmentResponse();
		componentService.updateComponent(model);
		return response;
	}

}
