/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 * For specific language governing the permissions and limitations under this
 * license, please see the license as well as any agreement you’ve entered into
 * with WSO2 governing the purchase of this software and any associated services.
 */

package com.wso2.openbanking.test.framework.filters;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.filter.log.LogDetail;
import io.restassured.internal.print.RequestPrinter;
import io.restassured.internal.print.ResponsePrinter;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.testng.Reporter;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 * Reporting filter for RestAssured.
 */
public class ReportingFilter implements Filter {

  private static final String heading = "<h3>%s</h3><";

  private final LogDetail logDetail;

  public ReportingFilter() {

    logDetail = LogDetail.ALL;
  }

  @Override
  public Response filter(FilterableRequestSpecification req, FilterableResponseSpecification res,
                         FilterContext ctx) {

    OutputStream outputStream = new ByteArrayOutputStream();
    RequestPrinter.print(req, req.getMethod(), req.getURI(), logDetail,
        new PrintStream(outputStream), true);

    Reporter.log(String.format(heading, "Request"));

    try {
      String raw = ((ByteArrayOutputStream) outputStream).toString("UTF-8");
      Reporter.log(formatOutput(raw));
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Unsupported Char type");
    }

    Response response = ctx.next(req, res);

    outputStream = new ByteArrayOutputStream();
    ResponsePrinter.print(response, response.body(), new PrintStream(outputStream),
        LogDetail.ALL, true);

    Reporter.log(String.format(heading, "Response"));

    try {
      String raw = ((ByteArrayOutputStream) outputStream).toString("UTF-8");
      Reporter.log(formatOutput(raw));
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Unsupported Char type");
    }

    return response;
  }

  private String formatOutput(String raw) {

    return raw
        .replaceAll(" ", "&nbsp;")
        .replaceAll("(\r\n|\n)", "<br />");
  }
}
