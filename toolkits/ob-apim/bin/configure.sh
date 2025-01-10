#!/bin/bash
# ------------------------------------------------------------------------
#
# Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
#
# WSO2 LLC. licenses this file to you under the Apache License,
# Version 2.0 (the "License"); you may not use this file except
# in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.
# ------------------------------------------------------------------------

# command to execute
# ./merge.sh <WSO2_OB_APIM_HOME>

source $(pwd)/../repository/conf/configure.properties
WSO2_OB_APIM_HOME=$1

# set toolkit home
cd ../
TOOLKIT_HOME=$(pwd)
echo "Toolkit Home: ${TOOLKIT_HOME}"

# set product home
if [ "${WSO2_OB_APIM_HOME}" == "" ]
  then
    cd ../
    WSO2_OB_APIM_HOME=$(pwd)
    echo "Product Home: ${WSO2_OB_APIM_HOME}"
fi

# validate product home
if [ ! -d "${WSO2_OB_APIM_HOME}/repository/components" ]; then
  echo -e "\n\aERROR:specified product path is not a valid carbon product path\n";
  exit 2;
else
  echo -e "\nValid carbon product path.\n";
fi

# read deployment.toml file
DEPLOYMENT_TOML_FILE=${TOOLKIT_HOME}/repository/resources/deployment.toml;
cp ${TOOLKIT_HOME}/${PRODUCT_CONF_PATH} ${DEPLOYMENT_TOML_FILE};

# read open-banking-berlin.xml file
OPENBANKING_BERLIN_XML_FILE=${WSO2_OB_APIM_HOME}/repository/conf/open-banking-berlin.xml;

configure_datasources() {
    if [ "${DB_TYPE}" == "mysql" ]
        then
            # APIM
            sed -i -e 's|DB_APIMGT_URL|jdbc:mysql://'${DB_HOST}':3306/'${DB_APIMGT}'?autoReconnect=true\&amp;useSSL=false|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_AM_CONFIG_URL|jdbc:mysql://'${DB_HOST}':3306/'${DB_AM_CONFIG}'?autoReconnect=true\&amp;useSSL=false|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_GOV_URL|jdbc:mysql://'${DB_HOST}':3306/'${DB_GOV}'?autoReconnect=true\&amp;useSSL=false|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_USER_STORE_URL|jdbc:mysql://'${DB_HOST}':3306/'${DB_USER_STORE}'?autoReconnect=true\&amp;useSSL=false|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_OB_STORE_URL|jdbc:mysql://'${DB_HOST}':3306/'${DB_OPEN_BANKING_STORE}'?autoReconnect=true\&amp;useSSL=false|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_USER|'${DB_USER}'|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_PASS|'${DB_PASS}'|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_DRIVER|'${DB_DRIVER}'|g' ${DEPLOYMENT_TOML_FILE}

        else
            # IS
            sed -i -e 's|DB_APIMGT_URL|jdbc:sqlserver://'${DB_HOST}':1433;databaseName='${DB_APIMGT}';encrypt=false|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_AM_CONFIG_URL|jdbc:sqlserver://'${DB_HOST}':1433;databaseName='${DB_AM_CONFIG}';encrypt=false|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_GOV_URL|jdbc:sqlserver://'${DB_HOST}':1433;databaseName='${DB_GOV}';encrypt=false|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_USER_STORE_URL|jdbc:sqlserver://'${DB_HOST}':1433;databaseName='${DB_USER_STORE}';encrypt=false|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_OB_STORE_URL|jdbc:sqlserver://'${DB_HOST}':1433;databaseName='${DB_OPEN_BANKING_STORE}';encrypt=false|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_USER|'${DB_USER}'|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_PASS|'${DB_PASS}'|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_DRIVER|'${DB_DRIVER}'|g' ${DEPLOYMENT_TOML_FILE}
    fi
}

echo -e "\nReplace hostnames \n"
echo -e "================================================\n"
sed -i -e 's|APIM_HOSTNAME|'${APIM_HOSTNAME}'|g' ${DEPLOYMENT_TOML_FILE}
sed -i -e 's|IS_HOSTNAME|'${IS_HOSTNAME}'|g' ${DEPLOYMENT_TOML_FILE}
sed -i -e 's|BI_HOSTNAME|'${BI_HOSTNAME}'|g' ${DEPLOYMENT_TOML_FILE}

sed -i -e 's|APIM_HOSTNAME|'${APIM_HOSTNAME}'|g' ${OPENBANKING_BERLIN_XML_FILE}

echo -e "\nConfigure datasources \n"
echo -e "================================================\n"
configure_datasources;

echo -e "\nCopy deployment.toml file to repository/conf \n"
echo -e "================================================\n"
cp ${DEPLOYMENT_TOML_FILE} ${WSO2_OB_APIM_HOME}/repository/conf/
rm ${DEPLOYMENT_TOML_FILE}
rm ${DEPLOYMENT_TOML_FILE}-e
