keytool -genkeypair -alias secure-server -keyalg RSA -dname "CN=localhost,OU=myorg,O=myorg,L=mycity,S=mystate,C=es" -keypass secret -keystore server-keystore.jks -storepass secret -validity 3650
keytool -genkeypair -alias secure-client -keyalg RSA -dname "CN=codependent-client,OU=myorg,O=myorg,L=mycity,S=mystate,C=es" -keypass secret -keystore client-keystore.jks -storepass secret -validity 3650

keytool -exportcert -alias secure-client -file client-public.cer -keystore client-keystore.jks -storepass secret -validity 3650
keytool -importcert -keystore server-truststore.jks -alias clientcert -file client-public.cer -storepass secret -validity 3650

keytool -exportcert -alias secure-server -file server-public.cer -keystore server-keystore.jks -storepass secret -validity 3650
keytool -importcert -keystore client-truststore.jks -alias servercert -file server-public.cer -storepass secret -validity 3650