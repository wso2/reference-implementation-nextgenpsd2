/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.wso2.openbanking.test.framework.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

/**
 * PSU Configuration Reader Class.
 */
public class PsuConfigReader {

  private static final ConfigParser configParser = ConfigParser.getInstance();
  private static final Log log = LogFactory.getLog(AppConfigReader.class);
  public static Integer psuNumber;

  /**
   * Get the index of the relevant PSU from PSUList in test-config.xml.
   *
   * @return index of the corresponding PSU.
   */
  public static Integer getPsuNumber() {

    return psuNumber;
  }

  /**
   * Set the index of the corresponding PSU (Index of PSUInfo tag).
   * PSUInfo[0] - Common PSU
   * PSUInfo[1] - PSU with AISP role
   * PSUInfo[2] - PSU with PISP role
   * PSUInfo[3] - PSU with CBPII role
   *
   * @param psuNumber
   */
  public void setPsuNumber(Integer psuNumber) {

    PsuConfigReader.psuNumber = psuNumber;
  }

  /**
   * Get username of the the given PSU.
   *
   * @return clientid
   */
  public static String getPSU() {

    if (configParser.getPSU().getClass().toString().contains("java.lang.String")) {
      return configParser.getPSU().toString();

    } else {
      List<Object[]> listObj = (List<Object[]>) configParser.getPSU();

      if (getPsuNumber() == null) {
        return String.valueOf(listObj.get(0));
      } else {
        return String.valueOf(listObj.get(psuNumber));
      }
    }
  }

  /**
   * Get password of the the given PSU.
   *
   * @return clientid
   */
  public static String getPSUPassword() {

    if (configParser.getPSUPassword().getClass().toString().contains("java.lang.String")) {
      return configParser.getPSUPassword().toString();

    } else {
      List<Object[]> listObj = (List<Object[]>) configParser.getPSUPassword();

      if (getPsuNumber() == null) {
        return String.valueOf(listObj.get(0));
      } else {
        return String.valueOf(listObj.get(psuNumber));
      }
    }
  }
}
