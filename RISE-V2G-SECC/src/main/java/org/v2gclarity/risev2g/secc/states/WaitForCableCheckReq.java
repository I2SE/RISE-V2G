/*******************************************************************************
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-207  V2G Clarity (Dr.-Ing. Marc Mültin) 
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *******************************************************************************/
package org.v2gclarity.risev2g.secc.states;

import org.v2gclarity.risev2g.secc.evseController.IDCEVSEController;
import org.v2gclarity.risev2g.secc.session.V2GCommunicationSessionSECC;
import org.v2gclarity.risev2g.shared.enumerations.V2GMessages;
import org.v2gclarity.risev2g.shared.messageHandling.ReactionToIncomingMessage;
import org.v2gclarity.risev2g.shared.v2gMessages.msgDef.CableCheckReqType;
import org.v2gclarity.risev2g.shared.v2gMessages.msgDef.CableCheckResType;
import org.v2gclarity.risev2g.shared.v2gMessages.msgDef.EVSENotificationType;
import org.v2gclarity.risev2g.shared.v2gMessages.msgDef.EVSEProcessingType;
import org.v2gclarity.risev2g.shared.v2gMessages.msgDef.V2GMessage;

public class WaitForCableCheckReq extends ServerState {

	private CableCheckResType cableCheckRes;
	private boolean evseProcessingFinished;
	
	public WaitForCableCheckReq(V2GCommunicationSessionSECC commSessionContext) {
		super(commSessionContext);
		cableCheckRes = new CableCheckResType();
	}
	
	@Override
	public ReactionToIncomingMessage processIncomingMessage(Object message) {
		if (isIncomingMessageValid(message, CableCheckReqType.class, cableCheckRes)) {
			V2GMessage v2gMessageReq = (V2GMessage) message;
			CableCheckReqType cableCheckReq = 
					(CableCheckReqType) v2gMessageReq.getBody().getBodyElement().getValue();
			
			// TODO how to react to failure status of DCEVStatus of cableCheckReq?
			
			/*
			 * TODO we need a timeout mechanism here so that a response can be sent within 2s
			 * the DCEVSEStatus should be generated according to already available values
			 * (if EVSEProcessing == ONGOING, maybe because of EVSE_IsolationMonitoringActive,
			 * within a certain timeout, then the status must be different)
			 */
			setEvseProcessingFinished(true);
			
			if (isEvseProcessingFinished()) {
				cableCheckRes.setEVSEProcessing(EVSEProcessingType.FINISHED);
				cableCheckRes.setDCEVSEStatus(
						((IDCEVSEController) getCommSessionContext().getDCEvseController()).getDCEVSEStatus(EVSENotificationType.NONE)
						);
				return getSendMessage(cableCheckRes, V2GMessages.PRE_CHARGE_REQ);
			} else {
				cableCheckRes.setEVSEProcessing(EVSEProcessingType.ONGOING);
				return getSendMessage(cableCheckRes, V2GMessages.CABLE_CHECK_REQ);
			}
		} else {
			setMandatoryFieldsForFailedRes();
		}
		
		return getSendMessage(cableCheckRes, V2GMessages.NONE);
	}
	

	public boolean isEvseProcessingFinished() {
		return evseProcessingFinished;
	}

	public void setEvseProcessingFinished(boolean evseProcessingFinished) {
		this.evseProcessingFinished = evseProcessingFinished;
	}

	
	@Override
	protected void setMandatoryFieldsForFailedRes() {
		cableCheckRes.setEVSEProcessing(EVSEProcessingType.FINISHED);
		cableCheckRes.setDCEVSEStatus(
				((IDCEVSEController) getCommSessionContext().getDCEvseController()).getDCEVSEStatus(EVSENotificationType.NONE)
				);
	}

}
