/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 *  This software is the property of WSO2 Inc. and its suppliers, if any.
 *  Dissemination of any information or reproduction of any material contained
 *  herein is strictly forbidden, unless permitted by WSO2 in accordance with
 *  the WSO2 Commercial License available at http://wso2.com/licenses. For specific
 *  language governing the permissions and limitations under this license,
 *  please see the license as well as any agreement you’ve entered into with
 *  WSO2 governing the purchase of this software and any associated services.
 *
 */

package com.wso2.openbanking.scp.webapp.util;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.identity.util.HTTPClientUtils;
import com.wso2.openbanking.scp.webapp.exception.TokenGenerationException;
import com.wso2.openbanking.scp.webapp.model.SCPError;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONObject;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@PrepareForTest(HTTPClientUtils.class)
public class UtilsTest extends PowerMockTestCase {


    private static final String RESPONSE_STRING = "{\"access_token\":\"dummy-token\"}";
    private static final String REQUEST_URL = "http://localhost:9446";
    private static final String DUMMY_COOKIE = "dummy-cookie";

    @Test(description = "if valid request sent, return json response")
    public void testSendRequestWithValidRequest() throws IOException, TokenGenerationException, OpenBankingException {
        // mock
        StatusLine statusLineMock = Mockito.mock(StatusLine.class);
        HttpEntity httpEntityMock = Mockito.mock(HttpEntity.class);
        CloseableHttpResponse httpResponseMock = Mockito.mock(CloseableHttpResponse.class);
        CloseableHttpClient closeableHttpClientMock = Mockito.mock(CloseableHttpClient.class);

        // when
        Mockito.doReturn(HttpStatus.SC_OK).when(statusLineMock).getStatusCode();

        InputStream inStream = new ByteArrayInputStream(RESPONSE_STRING.getBytes(StandardCharsets.UTF_8));
        Mockito.doReturn(inStream).when(httpEntityMock).getContent();

        Mockito.doReturn(statusLineMock).when(httpResponseMock).getStatusLine();
        Mockito.doReturn(httpEntityMock).when(httpResponseMock).getEntity();

        Mockito.doReturn(httpResponseMock).when(closeableHttpClientMock).execute(Mockito.any(HttpGet.class));

        PowerMockito.mockStatic(HTTPClientUtils.class);
        PowerMockito.when(HTTPClientUtils.getHttpsClient()).thenReturn(closeableHttpClientMock);

        // assert
        JSONObject responseJson = Utils.sendRequest(new HttpGet(REQUEST_URL));
        Assert.assertEquals(responseJson.get("access_token"), "dummy-token");
    }

    @Test(description = "if invalid response received, throw TokenGenerationException",
            expectedExceptions = TokenGenerationException.class)
    public void testSendRequestWithInvalidRequest() throws IOException, TokenGenerationException, OpenBankingException {
        // mock
        StatusLine statusLineMock = Mockito.mock(StatusLine.class);
        HttpEntity httpEntityMock = Mockito.mock(HttpEntity.class);
        CloseableHttpResponse httpResponseMock = Mockito.mock(CloseableHttpResponse.class);
        CloseableHttpClient closeableHttpClientMock = Mockito.mock(CloseableHttpClient.class);

        // when
        Mockito.doReturn(HttpStatus.SC_BAD_REQUEST).when(statusLineMock).getStatusCode();

        InputStream inStream = new ByteArrayInputStream(RESPONSE_STRING.getBytes(StandardCharsets.UTF_8));
        Mockito.doReturn(inStream).when(httpEntityMock).getContent();

        Mockito.doReturn(statusLineMock).when(httpResponseMock).getStatusLine();
        Mockito.doReturn(httpEntityMock).when(httpResponseMock).getEntity();

        Mockito.doReturn(httpResponseMock).when(closeableHttpClientMock).execute(Mockito.any(HttpGet.class));

        PowerMockito.mockStatic(HTTPClientUtils.class);
        PowerMockito.when(HTTPClientUtils.getHttpsClient()).thenReturn(closeableHttpClientMock);

        // assert
        JSONObject responseJson = Utils.sendRequest(new HttpGet(REQUEST_URL));
        Assert.assertEquals(responseJson.get("access_token"), "dummy-token");
    }

    @Test(description = "if valid request, return json response")
    public void testSendTokenRequestWithValidReq() throws IOException, TokenGenerationException, OpenBankingException {
        // mock
        StatusLine statusLineMock = Mockito.mock(StatusLine.class);
        HttpEntity httpEntityMock = Mockito.mock(HttpEntity.class);
        CloseableHttpResponse httpResponseMock = Mockito.mock(CloseableHttpResponse.class);
        CloseableHttpClient closeableHttpClientMock = Mockito.mock(CloseableHttpClient.class);

        // when
        Mockito.doReturn(HttpStatus.SC_OK).when(statusLineMock).getStatusCode();

        InputStream inStream = new ByteArrayInputStream(RESPONSE_STRING.getBytes(StandardCharsets.UTF_8));
        Mockito.doReturn(inStream).when(httpEntityMock).getContent();

        Mockito.doReturn(statusLineMock).when(httpResponseMock).getStatusLine();
        Mockito.doReturn(httpEntityMock).when(httpResponseMock).getEntity();

        Mockito.doReturn(httpResponseMock).when(closeableHttpClientMock).execute(Mockito.any(HttpPost.class));

        PowerMockito.mockStatic(HTTPClientUtils.class);
        PowerMockito.when(HTTPClientUtils.getHttpsClient()).thenReturn(closeableHttpClientMock);

        // assert
        JSONObject responseJson = Utils.sendTokenRequest(new HttpPost(REQUEST_URL));
        Assert.assertEquals(responseJson.get("access_token"), "dummy-token");
    }

    @Test(description = "if invalid request, throw TokenGenerationException",
            expectedExceptions = TokenGenerationException.class)
    public void testSendTokenRequestWithInvalidReq()
            throws IOException, TokenGenerationException, OpenBankingException {
        // mock
        StatusLine statusLineMock = Mockito.mock(StatusLine.class);
        HttpEntity httpEntityMock = Mockito.mock(HttpEntity.class);
        CloseableHttpResponse httpResponseMock = Mockito.mock(CloseableHttpResponse.class);
        CloseableHttpClient closeableHttpClientMock = Mockito.mock(CloseableHttpClient.class);

        // when
        Mockito.doReturn(HttpStatus.SC_BAD_REQUEST).when(statusLineMock).getStatusCode();

        InputStream inStream = new ByteArrayInputStream(RESPONSE_STRING.getBytes(StandardCharsets.UTF_8));
        Mockito.doReturn(inStream).when(httpEntityMock).getContent();

        Mockito.doReturn(statusLineMock).when(httpResponseMock).getStatusLine();
        Mockito.doReturn(httpEntityMock).when(httpResponseMock).getEntity();

        Mockito.doReturn(httpResponseMock).when(closeableHttpClientMock).execute(Mockito.any(HttpPost.class));

        PowerMockito.mockStatic(HTTPClientUtils.class);
        PowerMockito.when(HTTPClientUtils.getHttpsClient()).thenReturn(closeableHttpClientMock);

        // assert
        Utils.sendTokenRequest(new HttpPost(REQUEST_URL));
    }

    @Test
    public void testGetHttpUriRequest() {
        Assert.assertTrue(Utils
                .getHttpUriRequest(REQUEST_URL, "GET", "?query=query") instanceof HttpGet);
        Assert.assertTrue(Utils
                .getHttpUriRequest(REQUEST_URL, "DELETE", "?query=query") instanceof HttpDelete);
    }

    @Test
    public void testGetCookieFromRequest() {
        // mock
        HttpServletRequest requestMock = Mockito.mock(HttpServletRequest.class);

        // when
        Cookie cookie = new Cookie(DUMMY_COOKIE, DUMMY_COOKIE);
        Mockito.when(requestMock.getCookies()).thenReturn(new Cookie[]{cookie});

        // assert
        Assert.assertTrue(Utils.getCookieFromRequest(requestMock, DUMMY_COOKIE).isPresent());
        Assert.assertFalse(Utils.getCookieFromRequest(requestMock, "invalid cookie name").isPresent());
    }

    @Test
    public void testFormatDateToEncodedString() {
        LocalDateTime localDateTime = LocalDateTime.now();
        String expected = localDateTime.format(DateTimeFormatter.ofPattern(Constants.SCP_TOKEN_VALIDITY_DATE_FORMAT));
        String actual = Utils.formatDateToEncodedString(localDateTime);

        Assert.assertEquals(new String(Base64.getDecoder().decode(actual)), expected);
    }

    @Test
    public void testParseEncodedStringToDate() {
        LocalDateTime expectedDate = LocalDateTime.now();
        String encodedDate = Utils.formatDateToEncodedString(expectedDate);

        Assert.assertEquals(Utils.parseEncodedStringToDate(encodedDate), expectedDate);
    }

    @Test
    public void testSendErrorToFrontend() throws IOException {
        // mock
        HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);

        // when
        Mockito.doNothing().when(resp).sendRedirect(Mockito.anyString());

        // assert
        SCPError error = new SCPError("Error Message!", "Error Description");
        final String errorUrlFormat = "https://localhost:9446/consentmgr/error?message=%s&description=%s";
        Utils.sendErrorToFrontend(error, errorUrlFormat, resp);

        Mockito.verify(resp, Mockito.times(1)).sendRedirect(Mockito.anyString());
    }
}
