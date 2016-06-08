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

import java.util.Map;
import java.util.Set;

public interface IConfiguration {
  static final String OAUTH2_FILTER_CONF_FILE = "oauth2.filter.config";
  static final String OAUTH2_PROVIDER_URL = "oauth2.provider.url";
  static final String OAUTH2_PROVIDER_USER = "oauth2.provider.user";
  static final String OAUTH2_PROVIDER_PASSWORD = "oauth2.provider.password";
  static final String OAUTH2_RESOURCE_REALM = "oauth2.resource.realm";
  static final String EXCLUDED_URLS = "excluded.urls";
  static final String TRUST_STORE = "javax.net.ssl.trustStore";
  static final String TRUST_STORE_PASSWORD = "javax.net.ssl.trustStorePassword";
  static final String PN_LOG4J_PROPERTIES_PATH = "log4j.properties.path";
  static final String PN_AUDITOR_CLASS = "auditor.class";
  static final String REMOTE_HOST_BASED_FILTERING = "remote.host.based.filtering";
  static final String REMOTE_HOST_PREFIX = "remote.host.based.filtering.";
  static final String REMOTE_HOSTS = REMOTE_HOST_PREFIX + "hosts";
  static final String REMOTE_HOST_AUTHORIZED_USER = REMOTE_HOST_PREFIX + "authorized.user";

  String getProviderURL();
  String getProviderUser();
  String getProviderUserPass();
  String getResourceRealm();
  String getExcludedURLs();
  String getTrustStore();
  String getTrustStorePassword();
  String getAuditorClass();
  String getLog4jPropertiesPath();
  boolean isRemoteHostBasedFilteringEnabled();
  String getAuthorizedAdminUser();
  Set<String> getRemoteHostsForAuthorizedAdminUser();

  public static class ConfigurationError extends Exception {
    public ConfigurationError() {
      super();
    }

    public ConfigurationError(String message) {
      super(message);
    }
  }
}
