/*
*
* Copyright 2016 The Trustees of Indiana University
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*/
package edu.indiana.d2i.htrc.oauth2.filter;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class FileBasedConfiguration implements IConfiguration {
  private final Properties properties = new Properties();

  public FileBasedConfiguration(String propertyFile) throws IOException {
    properties.load(new FileInputStream(propertyFile));
  }

  @Override
  public String getProviderURL() {
    return properties.getProperty(OAUTH2_PROVIDER_URL);
  }

  @Override
  public String getProviderUser() {
    return properties.getProperty(OAUTH2_PROVIDER_USER);
  }

  @Override
  public String getProviderUserPass() {
    return properties.getProperty(OAUTH2_PROVIDER_PASSWORD);
  }

  @Override
  public String getResourceRealm() {
    return properties.getProperty(OAUTH2_RESOURCE_REALM);
  }

  @Override
  public String getExcludedURLs() {
    return properties.getProperty(EXCLUDED_URLS);
  }

  @Override
  public String getTrustStore() {
    return properties.getProperty(TRUST_STORE);
  }

  @Override
  public String getTrustStorePassword() {
    return properties.getProperty(TRUST_STORE_PASSWORD);
  }

  @Override
  public String getAuditorClass() {
    return properties.getProperty(PN_AUDITOR_CLASS);
  }

  @Override
  public String getLog4jPropertiesPath() {
    return properties.getProperty(PN_LOG4J_PROPERTIES_PATH);
  }

  @Override
  public boolean isRemoteHostBasedFilteringEnabled() {
    String value = properties.getProperty(REMOTE_HOST_BASED_FILTERING);

    return value != null && value.equals("true");
  }

  @Override
  public String getAuthorizedAdminUser() {
    return properties.getProperty(REMOTE_HOST_AUTHORIZED_USER);
  }

  @Override
  public Set<String> getRemoteHostsForAuthorizedAdminUser() {
    String remoteHosts = properties.getProperty(REMOTE_HOSTS);
    return Utils.getHostsFromCommaSeparatedStr(remoteHosts);
  }
}
