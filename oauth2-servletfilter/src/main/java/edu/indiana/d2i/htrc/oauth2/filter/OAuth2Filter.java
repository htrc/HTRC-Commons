/*
*
* Copyright 2013 The Trustees of Indiana University
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


import edu.indiana.d2i.htrc.audit.Auditor;
import edu.indiana.d2i.htrc.audit.AuditorFactory;
import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.error.OAuthError;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.OAuthResponse;
import org.apache.amber.oauth2.common.message.types.TokenType;
import org.apache.amber.oauth2.common.utils.OAuthUtils;
import org.apache.amber.oauth2.rs.request.OAuthAccessResourceRequest;
import org.apache.amber.oauth2.rs.response.OAuthRSResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO_OAuth2AccessToken;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationResponseDTO;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.*;

public class OAuth2Filter implements Filter {
  private static Log log = LogFactory.getLog(OAuth2Filter.class);

  public static final String OAUTH2_FILTER_CONF_FILE = "oauth2.filter.config";
  public static final String TRUST_STORE = "javax.net.ssl.trustStore";
  public static final String TRUST_STORE_PASSWORD = "javax.net.ssl.trustStorePassword";
  public static final String KEY_REMOTE_USER = "remoteuser";

  private String providerUrl;
  private String userName;
  private String password;
  private String realm;
  private String trustStore;
  private String trustStorePassword;
  protected AuditorFactory auditorFactory;
  private List<String> excludedUrls;
  private boolean remoteHostBasedFilteringEnabled;
  private String authorizedAdminUser;
  private Set<String> filteredRemoteHostsForAdminUser;

  /**
   * Setup servlet filter instance after reading filter configuration in web.xml. In a production scenario where
   * WSO2 IS is deployed with a valid certificate filter doesn't need trust store and trust store password
   * configuration values.
   * <p>
   * Also it's better have a web app specific user account to use with filter. This will make it easier to audit the
   * security logs.
   *
   * @param filterConfig OAuth2 filter configuration
   * @throws ServletException
   */
  public void init(FilterConfig filterConfig) throws ServletException {
    IConfiguration configuration;
    try {
      String filterConfigFile = filterConfig.getInitParameter(OAUTH2_FILTER_CONF_FILE);
      if (filterConfigFile != null && !filterConfigFile.isEmpty() && new File(filterConfigFile).exists()) {
        configuration = new FileBasedConfiguration(filterConfigFile);
      } else {
        configuration = new FilterConfigBasedConfiguration(filterConfig);
      }

      AuditorFactory.init(configuration.getAuditorClass());
      auditorFactory = new AuditorFactory();

      if (configuration.getLog4jPropertiesPath() != null) {
        PropertyConfigurator.configure(configuration.getLog4jPropertiesPath());
      }

      providerUrl = configuration.getProviderURL();
      if (providerUrl == null || providerUrl.isEmpty()) {
        throw new IConfiguration.ConfigurationError("Cannot find OAuth2 provider URL in filter configuration!");
      }

      userName = configuration.getProviderUser();
      if (userName == null || userName.isEmpty()) {
        throw new IConfiguration.ConfigurationError("Cannot find OAuth2 provider username in filter configuration!");
      }

      password = configuration.getProviderUserPass();
      if (password == null || password.isEmpty()) {
        throw new IConfiguration.ConfigurationError("Cannot find OAuth2 provider password in filter configuration!");
      }

      // Trust store can be used when WSO2 IS is deployed with self-signed certificates
      trustStore = configuration.getTrustStore();
      trustStorePassword = configuration.getTrustStorePassword();

      if (trustStore != null && trustStorePassword != null) {
        System.setProperty(TRUST_STORE, trustStore);
        System.setProperty(TRUST_STORE_PASSWORD, trustStorePassword);
      }

      realm = configuration.getResourceRealm();

      String excludedUrlsParam = configuration.getExcludedURLs();
      if (excludedUrlsParam != null) {
        String[] urls = excludedUrlsParam.split(";");
        excludedUrls = Arrays.asList(urls);
      } else {
        excludedUrls = Collections.emptyList();
      }

      remoteHostBasedFilteringEnabled = configuration.isRemoteHostBasedFilteringEnabled();
      authorizedAdminUser = configuration.getAuthorizedAdminUser();
      filteredRemoteHostsForAdminUser = configuration.getRemoteHostsForAuthorizedAdminUser();
    } catch (Exception e) {
      log.error("OAuth2 filter initialization failed.", e);
      throw new ServletException(e);
    }

  }

  /**
   * Extract the access token from servlet request header and validate it via WSO2 IS. If token is invalid or
   * expired filter will throw a error and stop of handing over the request to next filter in chain. If there is no
   * token header filter will reject the request without any checks.
   *
   * @param servletRequest  in-coming request object
   * @param servletResponse servlet response object
   * @param filterChain     servlet filter chain
   * @throws IOException
   * @throws ServletException
   */
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
    String errMsg = null;
    String requestId = randomUUID();

    HttpServletRequest req = (HttpServletRequest) servletRequest;
    HttpServletResponse res = (HttpServletResponse) servletResponse;

    for (String excludedUrl : excludedUrls) {
      if (req.getRequestURL().indexOf(excludedUrl) > -1) {
        log.info("Ignoring filter for request url: " + req.getRequestURL());
        filterChain.doFilter(servletRequest, servletResponse);
        return;
      }
    }

    OAuth2RequestWrapper modifiedRequest = new OAuth2RequestWrapper(req);

    modifiedRequest.setRequestId(requestId);
    modifiedRequest.setRemoteAddress(req.getRemoteAddr());

    ContextExtractor contextExtractor = new ContextExtractor(req);
    Auditor auditor = AuditorFactory.getAuditor(contextExtractor.getContextMap());

    String accessToken = null;
    OAuth2TokenValidationResponseDTO responseDTO = null;

    try {
      OAuthAccessResourceRequest accessResourceRequest = new OAuthAccessResourceRequest(req, TokenType.BEARER);
      accessToken = accessResourceRequest.getAccessToken();

      auditor.log("OAUTH2_FILTER_REQUEST_RECEIVED", requestId, accessToken);

      Enumeration headerNames = req.getHeaderNames();
      while (headerNames.hasMoreElements()) {
        String headerName = (String) headerNames.nextElement();
        auditor.log(headerName, ":", req.getHeader(headerName));
      }

      responseDTO = validateToken(accessToken);

      List<String> registered_user = new ArrayList<String>();

      registered_user.add(responseDTO.getAuthorizedUser());

      if (responseDTO.getAuthorizedUser() != null) {
        modifiedRequest.setRemoteUser(responseDTO.getAuthorizedUser());
      }

      // remote host validation
      if (responseDTO.getAuthorizedUser() != null &&
          remoteHostBasedFilteringEnabled &&
          responseDTO.getAuthorizedUser().equals(authorizedAdminUser) &&
          !(filteredRemoteHostsForAdminUser.contains(req.getRemoteAddr()) ||
              filteredRemoteHostsForAdminUser.contains(req.getRemoteHost()))) {
        errMsg = "Remote host based validation failure.";
        log.error(errMsg);
        auditor.log("OAUTH2_FILTER_UNAUTHENTICATED_REQUEST", requestId, accessToken);
        auditor.error(errMsg, accessToken, responseDTO.getAuthorizedUser(), req.getRemoteAddr());
        throw new ServletException("Remote host based validation failure.");
      }

      Map<String, List<String>> contextMap = contextExtractor.getContextMap();
      if (responseDTO.getAuthorizedUser() != null) {
        contextMap.put(KEY_REMOTE_USER, registered_user);
      }

      // We need to create new auditor instance after we create the context map out of servlet request.
      auditor = AuditorFactory.getAuditor(contextMap);

      auditor.log("OAUTH2_FILTER_REQUEST_AUTHENTICATED", requestId, accessToken);
    } catch (OAuthProblemException e) {
      errMsg = "OAuth exception.";
      log.error(errMsg, e);
      auditor.log("OAUTH2_FILTER_UNAUTHENTICATED_REQUEST", requestId, accessToken);
      auditor.error(errMsg, accessToken, e.getError(), e.getMessage());
      respondWithError(res, e);
      return;
    } catch (OAuthSystemException e) {
      errMsg = "OAuth system exception.";
      log.error(errMsg, e);
      auditor.log("OAUTH2_FILTER_UNAUTHENTICATED_REQUEST", requestId, accessToken);
      auditor.error(errMsg, accessToken, e.getMessage());
      throw new ServletException(e);
    } catch (RemoteException re) {
      errMsg = "Error occurred while invoking token validation service.";
      log.error(errMsg, re);
      auditor.log("OAUTH2_FILTER_UNAUTHENTICATED_REQUEST", requestId, accessToken);
      auditor.error(errMsg, accessToken, re.getMessage());
      throw new ServletException(errMsg, re);
    }


    filterChain.doFilter(modifiedRequest, servletResponse);
  }

  public void destroy() {

  }

  private void respondWithError(HttpServletResponse resp, OAuthProblemException error)
      throws IOException, ServletException {

    OAuthResponse oauthResponse = null;

    try {
      if (OAuthUtils.isEmpty(error.getError())) {
        oauthResponse = OAuthRSResponse.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
            .setRealm(realm)
            .buildHeaderMessage();

      } else {

        int responseCode = 401;
        if (error.getError().equals(OAuthError.CodeResponse.INVALID_REQUEST)) {
          responseCode = 400;
        } else if (error.getError().equals(OAuthError.ResourceResponse.INSUFFICIENT_SCOPE)) {
          responseCode = 403;
        }

        oauthResponse = OAuthRSResponse
            .errorResponse(responseCode)
            .setRealm(realm)
            .setError(error.getError())
            .setErrorDescription(error.getDescription())
            .setErrorUri(error.getUri())
            .buildBodyMessage();
      }
      resp.addHeader(OAuth.HeaderType.WWW_AUTHENTICATE,
          oauthResponse.getHeader(OAuth.HeaderType.WWW_AUTHENTICATE));
      resp.setContentType("text/html");
      resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      resp.getWriter().println(oauthResponse.getBody());

    } catch (OAuthSystemException e) {
      throw new ServletException(e);
    }
  }

  private OAuth2TokenValidationResponseDTO validateToken(String accessTokenStr) throws RemoteException, OAuthProblemException {
    OAuth2TokenValidationServiceClient client = new OAuth2TokenValidationServiceClient(providerUrl, userName, password);

    OAuth2TokenValidationRequestDTO_OAuth2AccessToken accessToken = new OAuth2TokenValidationRequestDTO_OAuth2AccessToken();
    accessToken.setIdentifier(accessTokenStr);
    accessToken.setTokenType("bearer");
    OAuth2TokenValidationRequestDTO oauthReq = new OAuth2TokenValidationRequestDTO();
    oauthReq.setAccessToken(accessToken);

    return client.validateAuthenticationRequest(oauthReq);
  }

  private String randomUUID() {
    return UUID.randomUUID().toString();
  }


}
