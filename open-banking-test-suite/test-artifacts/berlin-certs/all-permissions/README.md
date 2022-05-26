1. signing.jks
- alias = tpp1-signing
- password = wso2carbon
- org-id = PSDGB-OB-Unknown0015800001HQQrZAAX

2. transport.jks
- alias = tpp1-transport
- password = wso2carbon
- org-id = PSDGB-OB-Unknown0015800001HQQrZAAX

###### Important:
- Make sure to set the Revocation Validation Exclusion confing in open-banking.xml.
- Configure IssuerDN of the certificate under the <RevocationValidationExcludedIssuers><IssuerDN>.
- You can extract IssuerDN via command :
  `openssl x509 -in <cert file> -noout -text`

- Make sure to import cert.pem which resides in `open-banking-test-suite/test-artifacts/obie-info/ca-certs` folder
  to the client-truststore.jks.
