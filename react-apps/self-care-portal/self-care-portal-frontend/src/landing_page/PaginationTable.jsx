/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Commercial License available at http://wso2.com/licenses. For specific
 * language governing the permissions and limitations under this license,
 * please see the license as well as any agreement you’ve entered into with
 * WSO2 governing the purchase of this software and any associated services.
 */

import "../css/Pagination.css";
import React, {useEffect, useState} from "react";
import Col from "react-bootstrap/Col";
import Row from "react-bootstrap/Row";
import Dropdown from "react-bootstrap/Dropdown";
import DropdownButton from "react-bootstrap/DropdownButton";
import {getConsentsForSearch, setSearchObject} from "../store/actions";
import {useDispatch, useSelector} from "react-redux";
import ReactPaginate from 'react-paginate';

export const PaginationTable = ({currentTab}) => {

    const dispatch = useDispatch();
    const currentUser = useSelector(state => state.currentUser.user);
    let searchObj = useSelector(state => state.searchObject);
    const consentMetadata = useSelector((state) => state.consent.metadata);
    const searchOnClickState = useSelector(state => state.searchUtilState);
    const appInfo = useSelector((state) => state.appInfo.appInfo);

    const [postsPerPage, setPostsPerPage] = useState(searchObj.limit);
    const [noOfPages, setNoOfPages] = useState(1);
    const [currentPage, setCurrentPage] = useState(0);

    useEffect(() => {
        window.localStorage.setItem("postsPerPage", JSON.stringify(postsPerPage));
        let search = {
            ...searchObj,
            limit: postsPerPage,
            offset: 0
        }
        dispatch(setSearchObject(search))
        doSearchConsents(search)
        setCurrentPage(0);
    }, [postsPerPage]);

    useEffect(() => {
        setPostsPerPage(searchObj.limit);
    }, [searchObj]);

    useEffect(() => {
        setCurrentPage(0);
    }, [searchOnClickState]);

    function calculateNoOfPages() {
        if (Math.ceil(consentMetadata.total / postsPerPage) == 0) {
            return 1;
        } else {
            return Math.ceil(consentMetadata.total / postsPerPage);
        }
    }

    useEffect(() => {
        setNoOfPages(calculateNoOfPages());
    }, [consentMetadata.total, consentMetadata.count, postsPerPage]);

    function handlePagination(selectedPage) {
        let offset;
        if (selectedPage == 0) {
            offset = 0;
        } else {
            offset = selectedPage * postsPerPage;
        }
        let search = {
            ...searchObj,
            offset: offset
        }
        dispatch(setSearchObject(search))
        doSearchConsents(search)
        setCurrentPage(selectedPage);
    }

    function doSearchConsents(search) {
        dispatch(getConsentsForSearch(search, currentUser, appInfo));
    }

    // to reset the page to 1 when tab changes
    useEffect(() => {
        setCurrentPage(0);
    }, [currentTab]);

    const handleSelectNoOfPostsPerPage = (e) => {
        setPostsPerPage(e);
    };

    return (
        <Row className="paginationRow">
            <Col className="postsPerPageCol">
                <p>Rows per page</p>

                <DropdownButton
                    alignRight
                    title={postsPerPage}
                    id="postsPerPageDropdown"
                    onSelect={handleSelectNoOfPostsPerPage}
                    className="filterDropdown"
                >
                    <Dropdown.Item className="drop" eventKey="5">5</Dropdown.Item>
                    <Dropdown.Item eventKey="10">10</Dropdown.Item>
                    <Dropdown.Item eventKey="15">15</Dropdown.Item>
                    <Dropdown.Item eventKey="20">20</Dropdown.Item>
                </DropdownButton>
            </Col>
            <Col>
                <ReactPaginate
                    pageCount={noOfPages}
                    pageRangeDisplayed={3}
                    marginPagesDisplayed={1}
                    onPageChange={(e) => handlePagination(e.selected)}
                    forcePage={currentPage}
                    containerClassName={"pagination"}
                    previousLinkClassName={"pagination__link"}
                    nextLinkClassName={"pagination__link"}
                    disabledClassName={"pagination__link--disabled"}
                    activeClassName={"pagination__link--active"}
                />
            </Col>
        </Row>
    );
};
