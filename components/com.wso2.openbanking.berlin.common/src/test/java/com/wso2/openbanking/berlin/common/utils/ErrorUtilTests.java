/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 *  This software is the property of WSO2 Inc. and its suppliers, if any.
 *  Dissemination of any information or reproduction of any material contained
 *  herein is strictly forbidden, unless permitted by WSO2 in accordance with
 *  the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 *  For specific language governing the permissions and limitations under this
 *  license, please see the license as well as any agreement you’ve entered into
 *  with WSO2 governing the purchase of this software and any associated services.
 */

package com.wso2.openbanking.berlin.common.utils;

import com.wso2.openbanking.berlin.common.models.TPPMessage;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * This contains unit tests for ErrorUtil class.
 */
public class ErrorUtilTests {

    @Test
    public void testConstructBerlinError() {

        JSONObject berlinError = ErrorUtil.constructBerlinError("samplePath", TPPMessage.CategoryEnum.ERROR,
                TPPMessage.CodeEnum.FORMAT_ERROR, "sampleText");

        Assert.assertEquals(((JSONObject) ((JSONArray) berlinError.get("tppMessages")).get(0)).get("path"),
                "samplePath");
        Assert.assertEquals(((JSONObject) ((JSONArray) berlinError.get("tppMessages")).get(0)).get("code"),
                TPPMessage.CodeEnum.FORMAT_ERROR.toString());
        Assert.assertEquals(((JSONObject) ((JSONArray) berlinError.get("tppMessages")).get(0)).get("text"),
                "sampleText");
        Assert.assertEquals(((JSONObject) ((JSONArray) berlinError.get("tppMessages")).get(0)).get("category"),
                TPPMessage.CategoryEnum.ERROR.toString());
    }

    @Test
    public void testConstructBerlinErrorFromList() {

        List<TPPMessage> tppErrorsList = new ArrayList<>();
        TPPMessage tppMessage1 = new TPPMessage();
        tppMessage1.setPath("samplePath1");
        tppMessage1.setCode(TPPMessage.CodeEnum.FORMAT_ERROR);
        tppMessage1.setText("sampleText1");
        tppMessage1.setCategory(TPPMessage.CategoryEnum.ERROR);

        TPPMessage tppMessage2 = new TPPMessage();
        tppMessage2.setPath("samplePath2");
        tppMessage2.setCode(TPPMessage.CodeEnum.CONSENT_UNKNOWN);
        tppMessage2.setText("sampleText2");
        tppMessage2.setCategory(TPPMessage.CategoryEnum.WARNING);

        tppErrorsList.add(tppMessage1);
        tppErrorsList.add(tppMessage2);

        JSONObject berlinError = ErrorUtil.constructBerlinError(tppErrorsList);

        Assert.assertEquals(((JSONObject) ((JSONArray) berlinError.get("tppMessages")).get(0)).get("path"),
                "samplePath1");
        Assert.assertEquals(((JSONObject) ((JSONArray) berlinError.get("tppMessages")).get(0)).get("code"),
                TPPMessage.CodeEnum.FORMAT_ERROR.toString());
        Assert.assertEquals(((JSONObject) ((JSONArray) berlinError.get("tppMessages")).get(0)).get("text"),
                "sampleText1");
        Assert.assertEquals(((JSONObject) ((JSONArray) berlinError.get("tppMessages")).get(0)).get("category"),
                TPPMessage.CategoryEnum.ERROR.toString());

        Assert.assertEquals(((JSONObject) ((JSONArray) berlinError.get("tppMessages")).get(1)).get("path"),
                "samplePath2");
        Assert.assertEquals(((JSONObject) ((JSONArray) berlinError.get("tppMessages")).get(1)).get("code"),
                TPPMessage.CodeEnum.CONSENT_UNKNOWN.toString());
        Assert.assertEquals(((JSONObject) ((JSONArray) berlinError.get("tppMessages")).get(1)).get("text"),
                "sampleText2");
        Assert.assertEquals(((JSONObject) ((JSONArray) berlinError.get("tppMessages")).get(1)).get("category"),
                TPPMessage.CategoryEnum.WARNING.toString());
    }

    @Test
    public void testConstructTPPMessage() {

        TPPMessage tppMessage = ErrorUtil.constructTPPMessage("samplePath", TPPMessage.CategoryEnum.ERROR,
                TPPMessage.CodeEnum.FORMAT_ERROR, "sampleText");

        Assert.assertEquals(tppMessage.getPath(), "samplePath");
        Assert.assertEquals(tppMessage.getCategory().toString(), TPPMessage.CategoryEnum.ERROR.toString());
        Assert.assertEquals(tppMessage.getCode().toString(), TPPMessage.CodeEnum.FORMAT_ERROR.toString());
        Assert.assertEquals(tppMessage.getText(), "sampleText");
    }

    @Test
    public void testConstructBerlinErrorWithNullValue() {

        JSONObject berlinError = ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR,
                TPPMessage.CodeEnum.FORMAT_ERROR, "sampleText");

        Assert.assertNull(((JSONObject) ((JSONArray) berlinError.get("tppMessages")).get(0)).get("path"));
        Assert.assertEquals(((JSONObject) ((JSONArray) berlinError.get("tppMessages")).get(0)).get("code"),
                TPPMessage.CodeEnum.FORMAT_ERROR.toString());
        Assert.assertEquals(((JSONObject) ((JSONArray) berlinError.get("tppMessages")).get(0)).get("text"),
                "sampleText");
        Assert.assertEquals(((JSONObject) ((JSONArray) berlinError.get("tppMessages")).get(0)).get("category"),
                TPPMessage.CategoryEnum.ERROR.toString());
    }
}
