#Configure test-config.xml

The test-config.xml file contains input data which need to execute the test suite. This file need to be configured as below,
before start the test suite execution.

1. Get a copy of the test-config-example.xml file to the open-banking-test-suite/toolkit-berlin-test/resources.
2. Rename xml file as "test-config.xml". (Do not commit this file to the repository)
3. Configure created test-config.xml file as below.

   - `<SolutionVersion>` = The version of the Open Banking Solution. (Eg: For OB-300 the SolutionVersion value should
     be 3.0.0)
   - `<OBSpec>` = The specific OB Specification (Eg: OBSpec accepts values BG)
   - `<ApiVersion>` = Version of the API

   - `<Provisioning><Enabled>` = true (if true; The test suite executes the "API publish and subscribe test" in
     com.wso2.openbanking.common.integration.test package)
   - `<Provisioning><ProvisionFilePath>` = Path to the corresponding provisioning yaml file which resides in the
     resources directory in com.wso2.openbanking.common.integration.test package. This should be configured if
     Provisioning.Enabled=true
     (Eg: For Berlin 1.3.8 APIs - <<Path.To.Directory>>/berlin-test-config-provisioning.yaml)

   - `<Server>`
      - `<BaseURL>` = https://<<am_host>>:8243
      - `<GatewayURL>` = https://<<am_host>>:9443
      - `<AuthorisationServerURL>` = https://<<is_host>>:9446

   - `<ApplicationConfigList><AppConfig>` = Able to define multiple TPP configurations with the tags.
     (There are 2 TPP configurations available by default)
   - `<Application.KeyStore>` (Configure according to the steps provided in the README.md file in the resource
     directory)
      - `<Location>` = Path to the signing.jks
      - `<Alias>` = Alias of the application keystore
      - `<Password>` = Password of the application keystore (Eg: wso2carbon)
      - `<SigningKid>` = Key Id of the signing certificate.

   - `<Transport>` (Configure according to the steps provided in the README.md file in resource directory)
      - `<MTLSEnabled>` = true (Set to true when the setup is configured in MTLS)

      - `<KeyStore>`
         - `<Location>` = Path to the transport.jks
         - `<Password>` = Password of the transport keystore (Eg: wso2carbon)

   - `<Application>`
      - `<ClientID>` = Application Client Id
      - `<ClientSecret>` = Application Client Secret
      - `<RedirectURL>` = Application Redirect URL (Eg: https://www.google.com/redirects/redirect1)

   - `<Transport><Truststore>`
      - `<Location>` = Path to the client-truststore.jks of wso2-obam.
      - `<Password>` = Password of the client-truststore.jks keystore (Eg: wso2carbon)

   - `<NonRegulatoryApplication>`(This need to be configured only for the Non Regulatory Application related tests)
      - `<ClientID>` = Client Id of the Non Regulatory Application
      - `<ClientSecret>` = Client Secret of the Non Regulatory Application
      - `<RedirectURL>` = Redirect URL of the Non Regulatory Application (Eg: https://www.google.com/redirects/redirect1)

   - `<PSUList><PSUInfo>` = Able to define multiple PSU configurations with the tags.
     (There are 2 TPP configurations available by default)
      - `<Psu>` = PSU User Name
      - `<PsuPassword>` = PSU Password

   - `<PublisherInfo>` = Publisher Credentials
   - `<TPPInfo>` = TPP Credentials
   - `<BasicAuthInfo>` = Basic Authentication Credentials

   - `<BrowserAutomation>`
      - `<BrowserPreference>` = Preferred browser to run the tests. (Eg: firefox/chrome. Using firefox if not
        specified)
      - `<HeadlessEnabled>` = true (Execute UI automated tests in Headless mode)
      - `<FirefoxDriverLocation>` = Path to the geckodriver - geckodriver version should be compatible with the
        version of the firefox driver installed in your computer.
        (Eg: open-banking-test-suite/test-artifacts/selenium-libs)

   - `<Common>`
      - `<SigningAlgorithm>` = Signing Algorithm (Eg: PS256)

   - `<ConsentApi>`
      - `<AudienceValue>` = Audience value (Eg: https://<<host>>:9446/oauth2/token)
