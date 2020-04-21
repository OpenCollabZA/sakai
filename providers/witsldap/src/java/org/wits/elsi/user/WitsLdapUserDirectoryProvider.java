package org.wits.elsi.user;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.sql.DataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
//import org.sakaiproject.coursemanagement.api.CourseManagementAdministration;
//import org.sakaiproject.coursemanagement.api.CourseManagementAdministration;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.DisplayAdvisorUDP;
import org.sakaiproject.user.api.ExternalUserSearchUDP;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryProvider;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserFactory;
import org.sakaiproject.user.api.UsersShareEmailUDP;


/**
 *
 * @author davidwaf
 */
public class WitsLdapUserDirectoryProvider implements UserDirectoryProvider, UsersShareEmailUDP, DisplayAdvisorUDP, ExternalUserSearchUDP {

	private static Log log = LogFactory.getLog(StaffLDAP.class);
    
    /**
     * ldap.staff.host=ldap://thebe.ds.wits.ac.za:389
ldap.staff.searchbase=DC=ds,DC=WITS,DC=AC,DC=ZA
ldap.staff.dn=CN=_svc_KIM_blog,OU=ServiceAccounts,DC=ds,DC=WITS,DC=AC,DC=ZA
ldap.student.host=ldap://zethes.ss.wits.ac.za:389
ldap.student.searchbase=DC=ss,DC=WITS,DC=AC,DC=ZA
ldap.student.dn=CN=_svc_KIM_blog,OU=ServiceAccounts,DC=ds,DC=WITS,DC=AC,DC=ZA
ldap.password=QETUOwryip&&&

     */
     private String staffLDAPHost; //="ldap://thebe.ds.wits.ac.za:389";
     private String staffSearchBase; //="DC=ds,DC=WITS,DC=AC,DC=ZA";
     private String staffDN; //="CN=_svc_KIM_blog,OU=ServiceAccounts,DC=ds,DC=WITS,DC=AC,DC=ZA";
     private String studentLDAPHost; //="ldap://zethes.ss.wits.ac.za:389";
     private String studentSearchBase; //="DC=ss,DC=WITS,DC=AC,DC=ZA";
     private String studentDN; //="CN=_svc_KIM_blog,OU=ServiceAccounts,DC=ds,DC=WITS,DC=AC,DC=ZA";
     private String ldapPassword; //="QETUOwryip&&&";
     
    private UserDirectoryService userDirectoryService;
    private SecurityService securityService;
    private SiteService siteService;
    private ToolManager toolManager;
    private DataSource dataSource;
    private SessionManager sessionManager;
    private ServerConfigurationService serverConfigurationService;
    private UsageSessionService usageSessionService;
    private AuthzGroupService authzGroupService;
    private EventTrackingService eventTrackingService;
    private PreferencesService preferencesService;
    private WitsUserUtil witsUserUtil;
   // private CourseManagementAdministration courseManagementAdministration;
    private EmailService emailService;

    /**
     * *************************************************************************
     * Init and Destroy
     * ************************************************************************
     */
    /**
     * Final initialization, once all dependencies are set.
     */
    public void init() {
        try {
        	 //System.out.println("init -- Testing from -- Annoucments");
            witsUserUtil = new WitsUserUtil(this);
            staffLDAPHost = serverConfigurationService.getString("ldap.staff.host");
            studentLDAPHost = serverConfigurationService.getString("ldap.student.host");                       
            
            
             staffSearchBase = serverConfigurationService.getString("ldap.staff.searchbase");
             staffDN = serverConfigurationService.getString("ldap.staff.dn");             
             studentSearchBase = serverConfigurationService.getString("ldap.student.searchbase");
             studentDN = serverConfigurationService.getString("ldap.student.dn");
             ldapPassword = serverConfigurationService.getString("ldap.password");
            
            // System.out.println("Doing Host "+staffLDAPHost+" SearchB "+staffSearchBase+" staffDN "+staffDN+" pwd: "+ldapPassword);
             

        } catch (Throwable t) {
            log.warn(".init(): ", t);
        }
    }

    /**
     * Returns to uninitialized state. You can use this method to release
     * resources thet your Service allocated when Turbine shuts down.
     */
    public void destroy() {
        log.info("destroy()");
    } // destroy

    /**
     * Access a user object. Update the object with the information found.
     *
     * @param edit The user object (id is set) to fill in.
     * @return true if the user object was found and information updated, false
     * if not.
     */
    @Override
    public boolean getUser(UserEdit edit) {
    	// System.out.println("getUser -- Testing from -- Annoucments");
       /* String staffLDAPHost = serverConfigurationService.getString("ldap.staff.host");
        String staffSearchBase = serverConfigurationService.getString("ldap.staff.searchbase");
        String staffDN = serverConfigurationService.getString("ldap.staff.dn");
        String studentLDAPHost = serverConfigurationService.getString("ldap.student.host");
        String studentSearchBase = serverConfigurationService.getString("ldap.student.searchbase");
        String studentDN = serverConfigurationService.getString("ldap.student.dn");
        String ldapPassword = serverConfigurationService.getString("ldap.password");
*/
        if (edit == null) {
            return false;
        }

//        System.out.println(">>>>>>>>>> LDAP DEbug == always coming here: ");
        LDAPService ldapService = new LDAPService(staffLDAPHost, staffSearchBase, staffDN, ldapPassword);
        String type = "guest";
        boolean succeeded = false;
        LDAPUser lDAPUser = ldapService.getLDAPUser(edit.getEid());
        if (lDAPUser != null) {
            type = "registered";
            succeeded = true;
        } else {
            ldapService = new LDAPService(studentLDAPHost, studentSearchBase, studentDN, ldapPassword);
            lDAPUser = ldapService.getLDAPUser(edit.getEid());
            if (lDAPUser != null) {
                type = "student";
                succeeded = true;
            }
        }
        if (succeeded) {

            edit.setFirstName(lDAPUser.getGivenname());
            edit.setLastName(lDAPUser.getSurname());
            //edit.setEmail(lDAPUser.getEmail());

            if ("registered".equals(type)) {
                edit.setEmail(lDAPUser.getEmail());
            } else {
                edit.setEmail(lDAPUser.getUsername() + "@students.wits.ac.za");
            }
            edit.setType(type);

        }

        return succeeded;
    } // getUser

    /**
     * Access a collection of UserEdit objects; if the user is found, update the
     * information, otherwise remove the UserEdit object from the collection.
     *
     * @param users The UserEdit objects (with id set) to fill in or remove.
     */
    @Override
    public void getUsers(Collection users) {
    	// System.out.println("getUsers -- Testing from -- Annoucments");
        for (Iterator i = users.iterator(); i.hasNext();) {
            UserEdit user = (UserEdit) i.next();
            if (!getUser(user)) {
                i.remove();
            }
        }
    }

    /**
     * Search for externally provided users that match this criteria in eid,
     * email, first or last name. Returns a List of User objects. This list will
     * be empty if no results are returned or null if your external provider
     * does not implement this interface. The list will also be null if the LDAP
     * server returns an error, for example an '(11) Administrative Limit
     * Exceeded' or '(4) Sizelimit Exceeded', due to a search term being too
     * broad and returning too many results.
     *
     * @param creteria
     * @param first
     * @param last
     * @param factory
     * @return
     */
    public List<UserEdit> searchExternalUsers(String creteria, int first, int last, UserFactory factory) {
    	// System.out.println("searchExternal -- Testing from -- Annoucments");
        /*String staffLDAPHost = serverConfigurationService.getString("ldap.staff.host");
        String staffSearchBase = serverConfigurationService.getString("ldap.staff.searchbase");
        String staffDN = serverConfigurationService.getString("ldap.staff.dn");
        String studentLDAPHost = serverConfigurationService.getString("ldap.student.host");
        String studentSearchBase = serverConfigurationService.getString("ldap.student.searchbase");
        String studentDN = serverConfigurationService.getString("ldap.student.dn");
        String ldapPassword = serverConfigurationService.getString("ldap.password");
*/

        List<UserEdit> results = new ArrayList<UserEdit>();
        LDAPService ldapService = new LDAPService(staffLDAPHost, staffSearchBase, staffDN, ldapPassword);

        List<LDAPUser> staff = ldapService.search(creteria);
        ldapService = new LDAPService(studentLDAPHost, studentSearchBase, studentDN, ldapPassword);
        List<LDAPUser> students = ldapService.search(creteria);

        if (staff != null) {
            for (LDAPUser user : staff) {
                UserEdit userEdit = factory.newUser();
                userEdit.setEid(user.getUsername());
                userEdit.setEmail(user.getEmail());
                userEdit.setFirstName(user.getGivenname());
                userEdit.setLastName(user.getSurname());
                userEdit.setType("registered");
                results.add(userEdit);
            }
        }
        if (students != null) {
            for (LDAPUser user : students) {
                UserEdit userEdit = factory.newUser();
                userEdit.setEid(user.getUsername());
                userEdit.setEmail(user.getUsername() + "@students.wits.ac.za");//user.getEmail());
                userEdit.setFirstName(user.getGivenname());
                userEdit.setLastName(user.getSurname());
                userEdit.setType("student");
                results.add(userEdit);
            }
        }

        if (results.size() > 0 && (first < results.size() && last < results.size() && first > -1 && last > -1)) {
            results = results.subList(first, last);
        }
        return results;
    }

    /**
     * Find a user object who has this email address. Update the object with the
     * information found. <br /> Note: this method won't be used, because we are
     * a UsersShareEmailUPD.<br /> This is the sort of method to provide if your
     * external source has only a single user for any email address.
     *
     * @param email The email address string.
     * @return true if the user object was found and information updated, false
     * if not.
     */
    public boolean findUserByEmail(UserEdit edit, String email) {
      /*  String staffLDAPHost = serverConfigurationService.getString("ldap.staff.host");
        String staffSearchBase = serverConfigurationService.getString("ldap.staff.searchbase");
        String staffDN = serverConfigurationService.getString("ldap.staff.dn");
        String studentLDAPHost = serverConfigurationService.getString("ldap.student.host");
        String studentSearchBase = serverConfigurationService.getString("ldap.student.searchbase");
        String studentDN = serverConfigurationService.getString("ldap.student.dn");
        String ldapPassword = serverConfigurationService.getString("ldap.password");
*/
        if ((edit == null) || (email == null)) {
            return false;
        }
        LDAPService ldapService = new LDAPService(staffLDAPHost, staffSearchBase, staffDN, ldapPassword);
        LDAPUser user = ldapService.getLDAPUser(edit.getEid());
        boolean staff = true;
        if (user == null) {
            staff = false;
            ldapService = new LDAPService(studentLDAPHost, studentSearchBase, studentDN, ldapPassword);
            user = ldapService.getLDAPUser(edit.getEid());
        }
        if (user != null) {
            edit.setFirstName(user.getGivenname());
            edit.setLastName(user.getSurname());
            if (staff) {
                edit.setEmail(user.getEmail());
            } else {
                edit.setEmail(user.getUsername() + "@students.wits.ac.za");
            }
            return true;
        }

        return false;
    } // findUserByEmail

    /**
     * Find all user objects which have this email address.
     *
     * @param email The email address string.
     * @param factory Use this factory's newUser() method to create all the
     * UserEdit objects you populate and return in the return collection.
     * @return Collection (UserEdit) of user objects that have this email
     * address, or an empty Collection if there are none.
     */
    public Collection findUsersByEmail(String email, UserFactory factory) {
      /*  String staffLDAPHost = serverConfigurationService.getString("ldap.staff.host");
        String staffSearchBase = serverConfigurationService.getString("ldap.staff.searchbase");
        String staffDN = serverConfigurationService.getString("ldap.staff.dn");
        String studentLDAPHost = serverConfigurationService.getString("ldap.student.host");
        String studentSearchBase = serverConfigurationService.getString("ldap.student.searchbase");
        String studentDN = serverConfigurationService.getString("ldap.student.dn");
        String ldapPassword = serverConfigurationService.getString("ldap.password");
*/
        LDAPService ldapService = new LDAPService(staffLDAPHost, staffSearchBase, staffDN, ldapPassword);
        List<LDAPUser> users = ldapService.searchByEmail(email);

        if (users == null) {
            ldapService = new LDAPService(studentLDAPHost, studentSearchBase, studentDN, ldapPassword);
            users = ldapService.searchByEmail(email);
        }
        return users;
    }

    /**
     * Authenticate a user / password. If the user edit exists it may be
     * modified, and will be stored if...
     *
     * @param id The user id.
     * @param edit The UserEdit matching the id to be authenticated (and
     * updated) if we have one.
     * @param password The password.
     * @return true if authenticated, false if not.
     */
    public boolean authenticateUser(String userId, UserEdit edit, String password) {
      /*  String staffLDAPHost = serverConfigurationService.getString("ldap.staff.host");
        String staffSearchBase = serverConfigurationService.getString("ldap.staff.searchbase");
        String staffDN = serverConfigurationService.getString("ldap.staff.dn");
        String studentLDAPHost = serverConfigurationService.getString("ldap.student.host");
        String studentSearchBase = serverConfigurationService.getString("ldap.student.searchbase");
        String studentDN = serverConfigurationService.getString("ldap.student.dn");
        String ldapPassword = serverConfigurationService.getString("ldap.password");
*/
        /*  System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
         + "\nAuth params Host "+staffLDAPHost+" SearchB "+staffSearchBase+" staffDN "+staffDN+" pwd: "+ldapPassword+""
         + "+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
         */
        boolean retVal = false;
        if ((userId == null) || (password == null)) {
            // System.out.println("User and password is null");
            return retVal;
        }

        LDAPService ldapService = new LDAPService(staffLDAPHost, staffSearchBase, staffDN, ldapPassword);
        String type = "guest";
        boolean succeeded = false;
        if (ldapService.login(userId, password)) {
            type = "registered";
            //next lets auto enroll the courses 
            succeeded = true;
        } else {
            ldapService = new LDAPService(studentLDAPHost, studentSearchBase, studentDN, ldapPassword);
            if (ldapService.login(userId, password)) {
                type = "student";
                succeeded = true;
            }
        }


        if (succeeded) {

            /*            LDAPUser lDAPUser = ldapService.getLDAPUser(userId);
             //doPostLoginOps(userId, lDAPUser.getType());

             String emailAddress = lDAPUser.getEmail();
             if (!"registered".equals(type)) {
             emailAddress = userId + "@students.wits.ac.za";
             }
             try {
             userDirectoryService.addUser(null, userId,
             lDAPUser.getGivenname(), lDAPUser.getSurname(), emailAddress, null, type, null);
             log.info("Create User " + userId);

             } catch (UserAlreadyDefinedException ex2) {
             // log.error(ex2.getMessage(), ex2);
             } catch (UserIdInvalidException uii) {
             log.error(uii.getMessage(), uii);
             } catch (UserPermissionException upe) {
             log.error(upe.getMessage(), upe);
             } catch (Exception ex) {
             log.error(ex.getMessage(), ex);
             }

             */
            retVal = true;
        }





        //lets track down any admin logins
        /*Session sakaiSession = sessionManager.getCurrentSession();
         if ("admin".equalsIgnoreCase(sakaiSession.getUserId()) || "admin".equals(userId)) {
         //admin never gets here, so clear this
         sakaiSession.clear();
            
         String subject = "Alert: Illegal admin login !!!";
         String body = "Illegal Admin login/session detected. The system automatically switched into protective mode and blocked this session. <br /><br />";
         body += "User: " + userId;
         try {
         body += "Location: " + usageSessionService.getSession().getIpAddress() + "<br/>";
         body += "Host: " + usageSessionService.getSession().getHostName() + "<br/>";
         body += "Server: " + usageSessionService.getSession().getServer() + "<br/>";
         body += "User Agent " + usageSessionService.getSession().getUserAgent();
         } catch (Exception ex) {
        log.error(ex);
         }
         List<String> additionalHeaders = new ArrayList<String>();
         String fromEmail = "elearn@wits.ac.za";
         String replyto = "elearn@wits.ac.za";
         additionalHeaders.add("Content-Type: text/html");
         // send the email
         emailService.send(fromEmail, "davidwaf@gmail.com", subject, body, replyto, fromEmail, additionalHeaders);
         System.out.println(userId + " logged in as admin, sending mail notification with info: " + body);
           
         }*/
        // witsUserUtil.createMyWorkspace(userId);
        // doPostLogin(userId, "registered".equals(type));
        if (!retVal) {
            //System.out.println(userId + " login failed");
        }
        return retVal;
    } // authenticateUser

    private void xxdoPostLogin(String userId, boolean staff) {
        Session sakaiSession = null;
        Connection conn = null;
        User user = null;

        try {

            user = userDirectoryService.getUserByEid(userId);

            sakaiSession = sessionManager.getCurrentSession();
            sakaiSession.setUserId("ccadmin");
            sakaiSession.setUserEid("cccadmin");


            conn = dataSource.getConnection();
            Statement statement = conn.createStatement();
            String filter = "";// " where enabled = 1";
            boolean admin = false;
            if (user.getId().equals(UserDirectoryService.ADMIN_ID)) {
                filter = "";
                admin = true;
            }

            //lets make student member of the units registered for ..first
            String studentYOS = null;
            String sql = "select * from publicdashboard_joinablesites " + filter;
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                String siteId = rs.getString("siteid");
                int state = rs.getInt("enabled");
                String yos = rs.getString("yos");
                String targetGroup = rs.getString("targetgroup");

                try {
                    Site site = siteService.getSite(siteId);//npe ?
                    //only add if this is a student
                    if ("students".equals(targetGroup) && !staff) {
                        if (yos != null) {
                            if (yos.equalsIgnoreCase(studentYOS) || "all".equalsIgnoreCase(yos)) {
                                String role = site.getType().equals("course") ? "Student" : "access";
                                site.addMember(user.getId(), role, true, false);
                            }
                        } else {
                            String role = site.getType().equals("course") ? "Student" : "access";
                            site.addMember(user.getId(), role, true, false);
                        }

                    } else if ("staff".equals(targetGroup) && staff) {//only add if this is staff
                        String role = site.getType().equals("course") ? "Instructor" : "maintain";
                        site.addMember(user.getId(), role, true, false);

                    } else if ("all".equals(targetGroup)) {//doesn't matter..just add
                        String role = site.getType().equals("course") ? "Student" : "access";
                        site.addMember(user.getId(), role, true, false);

                    } else {

                        if (!staff) {
                            String role = site.getType().equals("course") ? "Student" : "access";
                            site.addMember(user.getId(), role, true, false);

                        }
                    }
                    if (!admin) {
                        if (state == 0) {
                            // site.removeMember(user.getId());
                        }
                    }

                    if (!staff && "staff".equals(targetGroup)) {
                        //  site.removeMember(user.getId());
                    }
                    //save site
                    siteService.save(site);

                } catch (Exception ex) {
                   log.error(ex);
                }
            }

        } catch (SQLException ex) {
            log.warn("Cannot get connection: " + ex);
        } catch (Exception ex) {
           log.error(ex);
        } finally {

            //System.out.println("finally from postlogin");
            if (sakaiSession != null) {
                try {
                    // sakaiSession.clear();
                    sakaiSession.setUserId(user.getId());
                    sakaiSession.setUserEid(user.getEid());
                } catch (Exception ex) {
                    //lets just clear...means something went really wrong
                    sakaiSession.clear();
                }
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                log.error(e);
            }
        }
    }

    private void xdoPostLogin(String userId, boolean staff) {
        Session sakaiSession = null;
        Connection conn = null;
        User user = null;

        try {
            //System.out.println("off to postlogin");
            //lets get the units you are registered for and stuff, if you are a student
           /* List<Unit> units = new ArrayList<Unit>();
             if (!staff) {
             try {
             Gson gson = new Gson();
             String unitsJson = witsUserUtil.getStudentUnitsJSon(userId);

             StudentUnits studentUnits = gson.fromJson(unitsJson, StudentUnits.class);
             units = studentUnits.getObjects();
             } catch (Exception ex) {
            log.error(ex);
             }
             }*/
            // update the user's externally provided realm definitions
            //authzGroupService.refreshUser("admin");

            // post the login event
            //eventTrackingService.post(eventTrackingService.newEvent(UsageSessionService.EVENT_LOGIN, null, true));

            user = userDirectoryService.getUserByEid(userId);

            sakaiSession = sessionManager.getCurrentSession();
            sakaiSession.setUserId("bbbbadmin");
            sakaiSession.setUserEid("yyyyadmin");


            conn = dataSource.getConnection();
            Statement statement = conn.createStatement();
            String filter = "";// " where enabled = 1";
            boolean admin = false;
            if (user.getId().equals(UserDirectoryService.ADMIN_ID)) {
                filter = "";
                admin = true;
            }

            //lets make student member of the units registered for ..first
            String studentYOS = null;
            /* for (Unit unit : units) {
             try {
             studentYOS = unit.getYearOfStudy();

             Site site = siteService.getSite(unit.getUnitCode());

             String slot = unit.getUnitClass();
             String teachingPeriod = unit.getTeachingPeriod();
             String fullCourseId = unit.getUnitCode() + "_" + slot + "_" + teachingPeriod;
             if (!staff) {
             Membership membership = courseManagementAdministration.addOrUpdateSectionMembership(userId, "S", fullCourseId, "active");

             }
             PreferencesEdit preferencesEdit = null;
             try {

             preferencesEdit = preferencesService.edit(user.getId());
             } catch (Exception ex) {
             preferencesEdit = preferencesService.add(user.getId());
             }

             ResourcePropertiesEdit props = preferencesEdit.getPropertiesEdit();
             props.addProperty("degreeCode", teachingPeriod);
             props.addProperty("slot", slot);
             props.addProperty("manualadd", "false");

             preferencesService.commit(preferencesEdit);
             //save site
             siteService.save(site);

             } catch (Exception ex) {
            log.error(ex);
             }
             }*/

            String sql = "select * from publicdashboard_joinablesites " + filter;
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                String siteId = rs.getString("siteid");
                int state = rs.getInt("enabled");
                String yos = rs.getString("yos");
                String targetGroup = rs.getString("targetgroup");

                try {
                    Site site = siteService.getSite(siteId);//npe ?
                    log.info("PRocssibf site " + site.getTitle());
                    log.info("Usrer == " + userId + " " + " YOS = " + yos + " tg = " + targetGroup);
                    //only add if this is a student
                    if ("students".equals(targetGroup) && !staff) {
                        if (yos != null) {
                            if (yos.equalsIgnoreCase(studentYOS) || "all".equalsIgnoreCase(yos)) {
                                String role = site.getType().equals("course") ? "Student" : "access";
                                site.addMember(user.getId(), role, true, false);
                            }
                        } else {
                            String role = site.getType().equals("course") ? "Student" : "access";
                            site.addMember(user.getId(), role, true, false);
                        }

                    } else if ("staff".equals(targetGroup) && staff) {//only add if this is staff
                        String role = site.getType().equals("course") ? "Student" : "access";
                        site.addMember(user.getId(), role, true, false);

                    } else if ("all".equals(targetGroup)) {//doesn't matter..just add
                        String role = site.getType().equals("course") ? "Student" : "access";
                        site.addMember(user.getId(), role, true, false);

                    } else {

                        if (!staff) {
                            String role = site.getType().equals("course") ? "Student" : "access";
                            site.addMember(user.getId(), role, true, false);

                        }
                    }
                    if (!admin) {
                        if (state == 0) {
                            // site.removeMember(user.getId());
                        }
                    }

                    if (!staff && "staff".equals(targetGroup)) {
                        //  site.removeMember(user.getId());
                    }
                    //save site
                    siteService.save(site);

                } catch (Exception ex) {
                   log.error(ex);
                }
            }

        } catch (SQLException ex) {
            log.warn("Cannot get connection: " + ex);
        } catch (Exception ex) {
           log.error(ex);
        } finally {

            //System.out.println("finally from postlogin");
            if (sakaiSession != null) {
                try {
                    // sakaiSession.clear();
                    sakaiSession.setUserId(user.getId());
                    sakaiSession.setUserEid(user.getEid());
                } catch (Exception ex) {
                    //lets just clear...means something went really wrong
                    sakaiSession.clear();
                }
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                log.error(e);
            }
        }


    }

    /**
     * {@inheritDoc}
     */
    public boolean authenticateWithProviderFirst(String id) {

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean createUserRecord(String id) {

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public String getDisplayId(User user) {
        return user.getEid();
    }

    /**
     * {@inheritDoc}
     */
    public String getDisplayName(User user) {
        // punt
        return user.getFirstName() + " " + user.getLastName();
    }

    /*  public String getLdapPassword() {
     return ldapPassword;
     }

     public void setLdapPassword(String ldapPassword) {
     this.ldapPassword = ldapPassword;
     }
     */
    public SecurityService getSecurityService() {
        return securityService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    /* public String getStaffDN() {
     return staffDN;
     }

     public void setStaffDN(String staffDN) {
     this.staffDN = staffDN;
     }

     public String getStaffLDAPHost() {
     return staffLDAPHost;
     }

     public void setStaffLDAPHost(String staffLDAPHost) {
     this.staffLDAPHost = staffLDAPHost;
     }

     public String getStaffSearchBase() {
     return staffSearchBase;
     }

     public void setStaffSearchBase(String staffSearchBase) {
     this.staffSearchBase = staffSearchBase;
     }

     public String getStudentDN() {
     return studentDN;
     }

     public void setStudentDN(String studentDN) {
     this.studentDN = studentDN;
     }

     public String getStudentLDAPHost() {
     return studentLDAPHost;
     }

     public void setStudentLDAPHost(String studentLDAPHost) {
     this.studentLDAPHost = studentLDAPHost;
     }

     public String getStudentSearchBase() {
     return studentSearchBase;
     }

     public void setStudentSearchBase(String studentSearchBase) {
     this.studentSearchBase = studentSearchBase;
     }
     */
    public UserDirectoryService getUserDirectoryService() {
        return userDirectoryService;
    }

    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public ToolManager getToolManager() {
        return toolManager;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setToolManager(ToolManager toolManager) {
        this.toolManager = toolManager;
    }

    public ServerConfigurationService getServerConfigurationService() {
        return serverConfigurationService;
    }

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;

    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public UsageSessionService getUsageSessionService() {
        return usageSessionService;
    }

    public void setUsageSessionService(UsageSessionService usageSessionService) {
        this.usageSessionService = usageSessionService;
    }

    public AuthzGroupService getAuthzGroupService() {
        return authzGroupService;
    }

    public void setAuthzGroupService(AuthzGroupService authzGroupService) {
        this.authzGroupService = authzGroupService;
    }

    public EventTrackingService getEventTrackingService() {
        return eventTrackingService;
    }

    public void setEventTrackingService(EventTrackingService eventTrackingService) {
        this.eventTrackingService = eventTrackingService;
    }

    public void setPreferencesService(PreferencesService preferencesService) {
        this.preferencesService = preferencesService;
    }

    public PreferencesService getPreferencesService() {
        return preferencesService;
    }
    /*

    public CourseManagementAdministration getCourseManagementAdministration() {
        return this.courseManagementAdministration;
    }

    public void setCourseManagementAdministration(CourseManagementAdministration courseManagementAdministration) {
        this.courseManagementAdministration = courseManagementAdministration;
    }*/

    public EmailService getEmailService() {
        return emailService;
    }

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }
}
