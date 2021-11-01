**How to Execute Berlin Test Suite.**

1. Configure the test-config.xml file according to the instructions given in README.md file which resides in 
   open-banking-test-suite/toolkit-berlin-test/resources.

2. Then goto corresponding test package. As an example if you want to run Accounts tests, then goto
   open-banking-test-suite/toolkit-berlin-test/com.wso2.openbanking.toolkit.berlin.test/integration.tests/accounts 
   directory.

3. Run accounts test suite using the mvn command by providing the api version as a command line option. 
   We can use one of the following commands to execute the tests.

mvn clean install -DgroupToRun=`apiVersion` -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn -fae -B -f pom.xml

mvn test -DgroupToRun=`apiVersion`


**How to Provide New API Version Support to Berlin Test Suite.**

1. Define the API version under the groups attribute list in @Test annotation. As an example, if the existing test case 
   in the common_test package supports the new API version, then add the API version to the groups list. 
   common_test package contains the common test cases for more than one API versions.

2. If a test case is only specific to the corresponding API version, then add the test case to the version specific 
   package with the relevant sub scenario. Also make sure to add the class reference to the testng.xml file.

3. If a test case in a version specific directory supports more than one API versions, make sure to remove the 
   test case from the version specific package and add the test case to the common_test package.
