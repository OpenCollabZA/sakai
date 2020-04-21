/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wits.elsi.user;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;

/**
 *
 * @author davidwaf
 */
public class WitsUserUtil {

    private static Log log = LogFactory.getLog(WitsUserUtil.class);
    private WitsLdapUserDirectoryProvider witsLdapUserDirectoryProvider;

    public WitsUserUtil(WitsLdapUserDirectoryProvider witsLdapUserDirectoryProvider) {
        this.witsLdapUserDirectoryProvider = witsLdapUserDirectoryProvider;
    }


    public void createMyWorkspace(String userId) {
        SiteService siteService = witsLdapUserDirectoryProvider.getSiteService();

        try {
            User user = witsLdapUserDirectoryProvider.getUserDirectoryService().getUserByEid(userId);
            String myWorkspaceId = siteService.getUserSiteId(user.getId());
            Site siteEdit = null;
            try {
                siteEdit = siteService.getSite(myWorkspaceId);
                log.info("My works space exists");
            } catch (IdUnusedException e) {

                log.info("No workspace for user: " + myWorkspaceId + ", creating it...");
                siteEdit = siteService.addSite(myWorkspaceId, "user");
                siteEdit.setTitle("My Workspace");
                siteEdit.setJoinable(false);

                siteEdit.setPublished(true);
                siteEdit.setPubView(false);
               

            }

        } catch (Exception ex) {
        	log.error(ex);
        }
    }
    /*
     * This method is used to get the course offerings that are saved in sakai
     * @param String siteid: The site id @param String siteCS: The site's course
     * offerings (usually empty string) @return String: List of all the
     * offerings in saka, returned as a concatenated string.
     */

    public String getCourseOfferings(String siteid, String siteCS, SiteService siteService) {
        // get the group provider id
        try {
            Site site = siteService.getSite(siteid);

            String requestedCourses = site.getProviderGroupId();
            siteCS = requestedCourses.replaceAll("\\+", ";");
        } catch (IdUnusedException ex) {
        	log.error(ex);
        } catch (NullPointerException npe) {
            siteCS = "";
        }

        return siteCS;
    }

    public String getStudentUnitsJSon(String studentNumber) throws Exception {
        ServerConfigurationService serverConfigurationService = witsLdapUserDirectoryProvider.getServerConfigurationService();
        studentNumber = studentNumber.toUpperCase();
        String studentWebService = "virtus.elearn.cloud.wits.ac.za";
        String user = serverConfigurationService.getString("wims.username");
        String password = serverConfigurationService.getString("wims.password");
        String proxyHost = serverConfigurationService.getString("wits.proxy");
        int proxyPort = serverConfigurationService.getInt("wits.proxyport", 80);
        HttpHost targetHost = new HttpHost(studentWebService, 8180, "http");


        DefaultHttpClient httpclient = new DefaultHttpClient();
        try {


            HttpHost proxy = new HttpHost(proxyHost, proxyPort);

            httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);


            httpclient.getCredentialsProvider().setCredentials(
                    new AuthScope(targetHost.getHostName(), targetHost.getPort()),
                    new UsernamePasswordCredentials(user, password));

            // Create AuthCache instance
            AuthCache authCache = new BasicAuthCache();
            // Generate BASIC scheme object and add it to the local
            // auth cache
            BasicScheme basicAuth = new BasicScheme();
            authCache.put(targetHost, basicAuth);

            // Add AuthCache to the execution context
            BasicHttpContext localcontext = new BasicHttpContext();
            localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);

            HttpGet httpget = new HttpGet("/wits-wims-services/wims/student/units/" + studentNumber);

            HttpResponse response = httpclient.execute(targetHost, httpget, localcontext);

            return (convertStreamToString(response.getEntity().getContent()));


        } finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            httpclient.getConnectionManager().shutdown();
        }

    }

    public String convertStreamToString(InputStream is)
            throws IOException {
        /*
         * To convert the InputStream to String we use the
         * Reader.read(char[] buffer) method. We iterate until the
         * Reader return -1 which means there's no more data to
         * read. We use the StringWriter class to produce the string.
         */
        if (is != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(
                        new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
            }
            return writer.toString();
        } else {
            return "";
        }
    }
}
