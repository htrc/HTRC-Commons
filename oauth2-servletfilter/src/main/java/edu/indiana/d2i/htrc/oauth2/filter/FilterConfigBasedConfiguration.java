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

import javax.servlet.FilterConfig;
import java.util.List;
import java.util.Map;

public class FilterConfigBasedConfiguration implements IConfiguration {

  private final FilterConfig rawConfig;

  public FilterConfigBasedConfiguration(FilterConfig rawConfig) {
    this.rawConfig = rawConfig;
  }


  @Override
  public String getProviderURL() {
    return rawConfig.getInitParameter(OAUTH2_PROVIDER_URL);
  }

  @Override
  public String getProviderUser() {
    return rawConfig.getInitParameter(OAUTH2_PROVIDER_USER);
  }

  @Override
  public String getProviderUserPass() {
    return rawConfig.getInitParameter(OAUTH2_PROVIDER_PASSWORD);
  }

  @Override
  public String getResourceRealm() {
    return rawConfig.getInitParameter(OAUTH2_RESOURCE_REALM);
  }

  @Override
  public String getExcludedURLs() {
    return rawConfig.getInitParameter(EXCLUDED_URLS);
  }

  @Override
  public String getTrustStore() {
    return rawConfig.getInitParameter(TRUST_STORE);
  }

  @Override
  public String getTrustStorePassword() {
    return rawConfig.getInitParameter(TRUST_STORE_PASSWORD);
  }

  @Override
  public String getAuditorClass() {
    return rawConfig.getInitParameter(PN_AUDITOR_CLASS);
  }

  @Override
  public String getLog4jPropertiesPath() {
    return rawConfig.getInitParameter(PN_LOG4J_PROPERTIES_PATH);
  }

  @Override
  public boolean isRemoteHostBasedFilteringEnabled() {
    String value = rawConfig.getInitParameter(REMOTE_HOST_BASED_FILTERING);

    return value != null && value.equals("true");
  }

  @Override
  public Map<String, List<String>> getAuthorizedUsersToHostsMap() {
    return null;
  }
}
