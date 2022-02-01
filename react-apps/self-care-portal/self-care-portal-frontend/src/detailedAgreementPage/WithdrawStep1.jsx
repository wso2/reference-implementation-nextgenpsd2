/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Commercial License available at http://wso2.com/licenses. For specific
 * language governing the permissions and limitations under this license,
 * please see the license as well as any agreement you’ve entered into with
 * WSO2 governing the purchase of this software and any associated services.
 */

import React, {useEffect} from "react";
import {Link, useLocation} from "react-router-dom";
import Container from "react-bootstrap/Container";
import "../css/Buttons.css";
import "../css/DetailedAgreement.css";
import "../css/withdrawal.css";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faExclamationTriangle} from "@fortawesome/free-solid-svg-icons";
import {useSelector} from "react-redux";
import {withdrawLang, specConfigurations} from "../specConfigs";
import ProgressBar from "react-bootstrap/ProgressBar";
import {FourOhFourError} from "../errorPage";
import {getDisplayName} from "../services";

export const WithdrawStep1 = ({ match }) => {

  const consents = useSelector((state) => state.consent.consents);
  const appInfo = useSelector((state) => state.appInfo.appInfo);

  useEffect(() => {
    window.history.pushState(null, "", '/consentmgr');
    window.onpopstate = function () {
      window.location.href='/consentmgr';
    };
  }, []);

  const matchedConsentId = match.params.id;

  var matchedConsent;
  var applicationName;
  var consentStatus;
  var consentConsentId;
  var consent;

  matchedConsent = consents.data.filter(
    (consent) => consent.consentId === matchedConsentId
  );

  consent = matchedConsent[0];
  applicationName = getDisplayName(appInfo, consent.clientId);
  consentStatus = consent.currentStatus;
  consentConsentId= consent.consentId;

  const location = useLocation();

  return (
    <>
      {consentStatus.toLowerCase() === specConfigurations.status.authorised.toLowerCase() ? (
        <Container fluid className="withdrawContainer">
          <div className="withdrawTitle">
            <FontAwesomeIcon
              className="withdrawWarnIcon fa-5x"
              icon={faExclamationTriangle}
            />
            <h4 className="withdrawalHeading">
              Stop sharing data with {applicationName}
            </h4>
            <ProgressBar now={50} label="1" />
            <p className="infoHeading">{withdrawLang.infoHeading}</p>
          </div>
          <div className="withdrawInfo">
            <h6 className="subHeadings">
              <li>{withdrawLang.impactHeading}</li>
            </h6>
            <p>{withdrawLang.impactInfo}</p>
            <h6 className="subHeadings">
              <li>{withdrawLang.sharedDataHandling}</li>
            </h6>
            <p>{withdrawLang.sharedDataHandlingPara1}</p>
            <p>{withdrawLang.sharedDataHandlingPara2}</p>
          </div>

          <div className="actionButtons" id="withdrawStep1ActionBtns">
            <div className="actionBtnDiv">
              <Link
                to = {`/consentmgr/${consentConsentId }`}
                className="comButton"
                id="withdrawFlowBackBtn"
              >
                {withdrawLang.backBtn}
              </Link>
            </div>
            <div className="actionBtnDiv">
              <Link
                className="withdrawBtn"
                id="withdrawBtn1"
                to={{
                  pathname: `/consentmgr/${consentConsentId}/withdrawal-step-2`,
                  state: { prevPath: location.pathname },
                }}
              >
                {withdrawLang.nextBtnStep1}
              </Link>
            </div>
          </div>
        </Container>
      ) : (
        <FourOhFourError />
      )}
    </>
  );
};
