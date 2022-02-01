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

import React, {useEffect, useState} from "react";
import "../css/Buttons.css";
import "../css/Search.css";
import Row from "react-bootstrap/Row";
import {FontAwesomeIcon} from '@fortawesome/react-fontawesome'
import {faBars, faSearch, faTimes} from '@fortawesome/free-solid-svg-icons'
import 'react-date-range/dist/styles.css'; // main style file
import 'react-date-range/dist/theme/default.css'; // theme css file
import {Col} from "react-bootstrap";
import {useDispatch, useSelector} from "react-redux";
import {getConsentsForSearch, setSearchObject, setSearchUtilState} from "../store/actions";
import {CONFIG} from "../config";
import DateRange from "react-date-range/dist/components/DateRange";
import moment from "moment";


export const AdvanceSearch = () => {

    const dispatch = useDispatch();

    let searchObj = useSelector(state => state.searchObject);
    const currentUser = useSelector(state => state.currentUser.user);
    const searchOnClickState = useSelector(state => state.searchUtilState);
    const appInfo = useSelector((state) => state.appInfo.appInfo);

    const [calendarVisibility, setCalendarVisibility] = useState(false);
    const [advanceSearchVisibility, setAdvanceSearchVisibility] = useState(false);

    let limitCount = window.localStorage.getItem("postsPerPage") || CONFIG.NUMBER_OF_CONSENTS;
    const [searchLimit, setSearchLimit] = useState(limitCount);
    const [searchOffset, setSearchOffset] = useState(0);
    const [softwareId, setSoftwareId] = useState("");
    const [consentId, setConsentId] = useState("");
    const [dateRange, setDateRange] = useState("");
    const [searchUser, setSearchUser] = useState("");
    const [dateState, setDateState] = useState([
        {
            startDate: new Date(),
            endDate: new Date(),
            key: "selection"
        }
    ]);

    useEffect(() => {
        setSearchLimit(searchObj.limit);
        setSearchOffset(searchObj.offset);
        setDateRange(searchObj.dateRange);
        setConsentId(searchObj.consentIDs);
        setSearchUser(searchObj.userIDs);
        setSoftwareId(searchObj.clientIDs);
        setAdvanceSearchVisibility(!searchObj.hideAdvanceSearchOptions)
    }, [searchObj]);

    useEffect(() => {
        let elements = document.getElementsByClassName("searchcontent");
        if (advanceSearchVisibility === true) {
            for (var i = 0; i < elements.length; i += 1) {
                document.getElementsByClassName("searchcontent")[i].style.opacity = "1";
                document.getElementsByClassName("searchcontent")[i].style.height = "auto";
                document.getElementsByClassName("searchcontent")[i].style.display = "block";
            }
        } else {
            for (i = 0; i < elements.length; i += 1) {
                document.getElementsByClassName("searchcontent")[i].style.opacity = "0";
                document.getElementsByClassName("searchcontent")[i].style.height = "0";
                document.getElementsByClassName("searchcontent")[i].style.display = "none";
            }
        }
    }, [advanceSearchVisibility]);


    function doSearchConsents(search) {
        dispatch(getConsentsForSearch(search, currentUser, appInfo));
    }

    return (
        <div>
            <Row>
                <Col>
                    <button
                        className="sBorder"
                        style={{display: "inline-block"}}
                        title="view advance search options"
                        onClick={() => {
                            let a = {...searchObj, hideAdvanceSearchOptions: !searchObj.hideAdvanceSearchOptions}
                            dispatch(setSearchObject(a));
                        }}
                    >
                        <FontAwesomeIcon icon={faBars} className="sIcon"/>
                    </button>
                </Col>
            </Row>

            <div className="searchcontent">
                <br/>

                <Row>
                    <Col>
                        {/* Software Product */}
                        <input
                            type="text"
                            className="inputBox"
                            id="inputSearch"
                            placeholder="Service Provider"
                            value={softwareId}
                            onChange={(e) => {
                                // softwareId is converted to clientId in API call
                                setSoftwareId(e.target.value);
                            }}
                        ></input>
                    </Col>

                    <Col>
                        {/* Account Id */}
                        <input
                            type="text"
                            className="inputBox"
                            id="inputSearchDebtor"
                            placeholder="Consent Id"
                            value={consentId}
                            onChange={(e) => {
                                setConsentId(e.target.value);
                            }}
                            // onKeyDown={handleKeyDown}
                        ></input>
                    </Col>

                    <Col>
                        {/* Consent Staged Period */}
                        <input
                            type="text"
                            className="inputBox"
                            id="inputSearchDate"
                            placeholder="Consent Staged Period"
                            value={dateRange}
                            onChange={(e) => {
                                console.log(e.target.value);
                            }}
                            onClick={() => {
                                var elems = document.getElementsByClassName("calandarArea");
                                if (!calendarVisibility) {
                                    for (var i = 0; i < elems.length; i += 1) {
                                        document.getElementsByClassName("calandarArea")[
                                            i
                                            ].style.display = "block";
                                        setCalendarVisibility(true)
                                    }
                                } else {
                                    for (i = 0; i < elems.length; i += 1) {
                                        document.getElementsByClassName("calandarArea")[
                                            i
                                            ].style.display = "none";
                                        setCalendarVisibility(false)
                                    }
                                }
                            }}
                            readOnly="readonly"
                        ></input>

                        <br/>

                        <div className="calandarArea">
                            <br/>
                            <DateRange
                                editableDateInputs={true}
                                onChange={(item) => {
                                    setDateState([item.selection]);
                                    let dateStateVal = moment(item.selection.startDate).format("DD-MMM-YYYY") +
                                        " / " + moment(item.selection.endDate).format("DD-MMM-YYYY");
                                    setDateRange(dateStateVal);
                                }}
                                moveRangeOnFirstSelection={false}
                                ranges={dateState}
                                className="calander"
                                startDatePlaceholder="Consent sharing period start date"
                                endDatePlaceholder="Consent sharing period end date"
                            />
                        </div>
                        <br/>
                    </Col>

                    <Col>
                        {/* User Id */}
                        {currentUser.role === "customerCareOfficer" ? (
                            <input
                                type="text"
                                className="inputBox"
                                id="inputSearchUser"
                                placeholder="User Id"
                                value={searchUser}
                                onChange={(e) => {
                                    setSearchUser(e.target.value);
                                }}
                            ></input>
                        ) : (
                            <> </>
                        )}
                    </Col>
                    <Col>
                        <button
                            className="sBorder"
                            style={{margin: "10px", padding: "0.25em 1.75em"}}
                            title="submit search"
                            onClick={() => {
                                let search = {
                                    ...searchObj,
                                    limit: searchLimit,
                                    offset: 0,
                                    dateRange: dateRange,
                                    consentIDs: consentId,
                                    userIDs: searchUser,
                                    clientIDs: softwareId,
                                }
                                dispatch(setSearchObject(search))
                                dispatch(setSearchUtilState(!searchOnClickState))
                                doSearchConsents(search)
                            }}
                        >
                            <p className="sIcon">
                                <FontAwesomeIcon icon={faSearch} className="sIcon"/>
                            </p>
                        </button>
                        <button
                            className="sBorder"
                            title="reset search"
                            onClick={() => {
                                let search = {
                                    ...searchObj,
                                    limit: searchLimit,
                                    offset: 0,
                                    dateRange: "",
                                    consentIDs: "",
                                    userIDs: "",
                                    clientIDs: "",
                                }
                                dispatch(setSearchObject(search))
                                let elems = document.getElementsByClassName("calandarArea");
                                for (var i = 0; i < elems.length; i += 1) {
                                    document.getElementsByClassName("calandarArea")[
                                        i
                                        ].style.display = "none";
                                    setCalendarVisibility(false)
                                }

                                setDateState([
                                    {
                                        startDate: new Date(),
                                        endDate: new Date(),
                                        key: "selection"
                                    }
                                ]);
                                doSearchConsents(search)
                            }}
                        >
                            <FontAwesomeIcon icon={faTimes} className="sIcon"/>
                        </button>
                    </Col>
                </Row>
            </div>
        </div>
    );
};
