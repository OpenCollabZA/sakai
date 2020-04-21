package org.wits.elsi.user;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LDAPService {
	private static Log log = LogFactory.getLog(LDAPService.class);

    private final static String INIT_CTX = "com.sun.jndi.ldap.LdapCtxFactory";
    private String host;
    private String searchBase;
    private String dn;
    private String filter = "";
    private String password = "";
    private DirContext dirContext = null;
    private String surname = "";
    private String givenname = "";
    private String title = "";
    private String email = "";

    public LDAPService(String host, String searchBase, String dn, String password) {
        this.host = host;
        this.searchBase = searchBase;
        this.dn = dn;
        this.password = password;
    }

    public List<LDAPUser> search(String searchVal) {
        String parts[] = searchVal.split(" ");

        try {
            filter = "(|(sn=" + searchVal + "*)(givenName=" + searchVal + "*)(cn=" + searchVal + "*)(mail=" + searchVal + "*))";
            if (parts != null) {
                if (parts.length > 1) {

                    filter = "(displayName~=" + searchVal + ")";

                }
            }


            setInitialContext();
            return executeSearch();

        } catch (Exception ex) {
            log.error(ex);
        }
        return null;
    }

    public List<LDAPUser> searchByEmail(String searchVal) {
        try {
            filter = "(|(mail=" + email + "))";
            setInitialContext();
            return executeSearch();

        } catch (Exception ex) {
        	log.error(ex);
        }
        return null;
    }

    public LDAPUser getLDAPUser(String username) {
    	 //System.out.println("getLdapUSer -- Testing from -- Annoucments");
        try {
            filter = "(|(cn=" + username + "))";
            setInitialContext();
            return executeSearchIndividual();

        } catch (Exception ex) {
        	log.error(ex);
        }
        return null;
    }

    public LDAPUser getLDAPUserByEmail(String email) {
    	 //System.out.println("getLdapUSerByEmail -- Testing from -- Annoucments");
        try {
            filter = "(|(mail=" + email + "))";
            setInitialContext();
            return executeSearchIndividual();

        } catch (Exception ex) {
        	log.error(ex);
        }
        return null;
    }

    public boolean login(String username, String password) {
    	// System.out.println("login -- Testing from -- Annoucments");
        try {
            filter = "(cn=" + username + ")";
            setInitialContext();
            String dn = getDN();
            if (dn == null) {
                //System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!! login for "+username+" failed..DN is null, returning false");
                return false;
            } else {
                return authenticate(password, dn);
            }
        } catch (NamingException ex) {
        	log.error(ex);
        }
        return false;
    }

    private boolean authenticate(String password, String dn) {
    	 //System.out.println("authenticate -- Testing from -- Annoucments");
        try {
            Hashtable env = new Hashtable();
            env.put(Context.INITIAL_CONTEXT_FACTORY, INIT_CTX);
            env.put(Context.PROVIDER_URL, host);
            env.put(Context.SECURITY_AUTHENTICATION, "simple");
            env.put(Context.SECURITY_PRINCIPAL, dn);
            env.put(Context.SECURITY_CREDENTIALS, password);
            new InitialDirContext(env);
            return true;
        } catch (NamingException ex) {
        	log.error(ex);
        }
        return false;
    }

    private void setInitialContext() throws NamingException {
    	// System.out.println("setInitialContext -- Testing from -- Annoucments");
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, INIT_CTX);
        env.put(Context.PROVIDER_URL, host);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, dn);
        env.put(Context.SECURITY_CREDENTIALS, password);
        dirContext = new InitialDirContext(env);

    }

    private List<LDAPUser> executeSearch() {
    	// System.out.println("executeSearch -- Testing from -- Annoucments");
        List<LDAPUser> users = new ArrayList<LDAPUser>();
        try {
            if (dirContext == null) {
                setInitialContext();
            }
            SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);

            NamingEnumeration results = dirContext.search(searchBase, filter, constraints);
            int count = 0;
            while (results != null && results.hasMore()) {
                SearchResult sr = (SearchResult) results.next();
                LDAPUser lDAPUser = new LDAPUser();
                String dn = sr.getName();



                Attributes attr = sr.getAttributes();
                for (NamingEnumeration ne = attr.getAll(); ne.hasMore();) {
                    Attribute att = (Attribute) ne.next();
                    String attrId = att.getID();
                    String attValue = "";
                    for (Enumeration vals = att.getAll(); vals.hasMoreElements();) {
                        attValue += ((attValue.length() == 0 ? "" : "/") + (String) vals.nextElement());
                    }

                    if (attrId.equals("sn")) {
                        surname = attValue;
                        lDAPUser.setSurname(surname);
                    } else if (attrId.equals("cn")) {
                        String username = attValue;
                        lDAPUser.setUsername(username);
                    } else if (attrId.equals("givenName")) {
                        givenname = attValue;
                        lDAPUser.setGivenname(givenname);
                    } else if (attrId.equals("title")) {
                        title = attValue;
                        lDAPUser.setTitle(title);
                    } else if (attrId.equals("mail")) {
                        email = attValue;

                        lDAPUser.setEmail(email);
                    } else if (attrId.equals("distinguishedName")) {
                        String ddn = attValue;
                        lDAPUser.setDn(ddn);
                    }
                }

                users.add(lDAPUser);
                count++;

            }
        } catch (Exception e) {
        	log.error(e);
        }
        return users;
    }

    private LDAPUser executeSearchIndividual() {

        try {
            if (dirContext == null) {
                setInitialContext();
            }
            SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);

            NamingEnumeration results = dirContext.search(searchBase, filter, constraints);
            int count = 0;
            while (results != null && results.hasMore()) {
                SearchResult sr = (SearchResult) results.next();

                String dn = sr.getName();

                LDAPUser lDAPUser = new LDAPUser();
                Attributes attr = sr.getAttributes();
                for (NamingEnumeration ne = attr.getAll(); ne.hasMore();) {
                    Attribute att = (Attribute) ne.next();
                    String attrId = att.getID();
                    String attValue = "";
                    for (Enumeration vals = att.getAll(); vals.hasMoreElements();) {
                        attValue += ((attValue.length() == 0 ? "" : "/") + (String) vals.nextElement());
                    }

                    if (attrId.equals("sn")) {
                        surname = attValue;
                        lDAPUser.setSurname(surname);
                    } else if (attrId.equals("cn")) {
                        String username = attValue;
                        lDAPUser.setUsername(username);
                    } else if (attrId.equals("givenName")) {
                        givenname = attValue;
                        lDAPUser.setGivenname(givenname);
                    } else if (attrId.equals("title")) {
                        title = attValue;
                        lDAPUser.setTitle(title);
                    } else if (attrId.equals("mail")) {
                        email = attValue;

                        lDAPUser.setEmail(email);
                    } else if (attrId.equals("distinguishedName")) {
                        String ddn = attValue;
                        lDAPUser.setDn(ddn);
                    }
                }


                return lDAPUser;
            }
        } catch (Exception e) {
        	log.error(e);
        }
        return null;
    }

    public String getDN() {
        String ddn = null;
        try {
            if (dirContext == null) {
                setInitialContext();
            }
            SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);

            NamingEnumeration results = dirContext.search(searchBase, filter, constraints);
            int count = 0;
            while (results != null && results.hasMore()) {
                SearchResult sr = (SearchResult) results.next();
                String dn = sr.getName();

                Attributes attr = sr.getAttributes();
                for (NamingEnumeration ne = attr.getAll(); ne.hasMore();) {
                    Attribute att = (Attribute) ne.next();
                    String attrId = att.getID();
                    String attValue = "";
                    for (Enumeration vals = att.getAll(); vals.hasMoreElements();) {
                        try {
                            attValue += ((attValue.length() == 0 ? "" : "/") +  vals.nextElement());
                        } catch (Exception ex) {
                            log.info("Attrib "+attrId+" has invalid value "+attValue);
                            log.error(ex);
                        }
                    }
                    //System.out.println(attrId+"= "+attValue);
                    if (attrId.equals("sn")) {
                        surname = attValue;
                    } else if (attrId.equals("givenName")) {
                        givenname = attValue;
                    } else if (attrId.equals("title")) {
                        title = attValue;
                    } else if (attrId.equals("mail")) {
                        email = attValue;
                    } else if (attrId.equals("distinguishedName")) {
                        ddn = attValue;
                    }
                }
                count++;
                if (count > 0) {
                    break;
                }
            }
        } catch (Exception e) {
        	log.error(e);
        }
        return ddn;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getSearchBase() {
        return searchBase;
    }

    public void setSearchBase(String searchBase) {
        this.searchBase = searchBase;
    }

    public String getEmail() {
        return email;
    }

    public String getGivenname() {
        return givenname;
    }

    public String getSurname() {
        return surname;
    }
    /**
     * Common Active Directory LDAP bind errors:
     *
     * 80090308: LdapErr: DSID-0C09030B, comment: AcceptSecurityContext error,
     * data 525, v893 HEX: 0x525 - user not found DEC: 1317 - ERROR_NO_SUCH_USER
     * (The specified account does not exist.) NOTE: Returns when username is
     * invalid.
     *
     * 80090308: LdapErr: DSID-0C09030B, comment: AcceptSecurityContext error,
     * data 52e, v893 HEX: 0x52e - invalid credentials DEC: 1326 -
     * ERROR_LOGON_FAILURE (Logon failure: unknown user name or bad password.)
     * NOTE: Returns when username is valid but password/credential is invalid.
     * Will prevent most other errors from being displayed as noted.
     *
     * 80090308: LdapErr: DSID-0C09030B, comment: AcceptSecurityContext error,
     * data 530, v893 HEX: 0x530 - not permitted to logon at this time DEC: 1328
     * - ERROR_INVALID_LOGON_HOURS (Logon failure: account logon time
     * restriction violation.) NOTE: Returns only when presented with valid
     * username and password/credential.
     *
     * 80090308: LdapErr: DSID-0C09030B, comment: AcceptSecurityContext error,
     * data 531, v893 HEX: 0x531 - not permitted to logon from this workstation
     * DEC: 1329 - ERROR_INVALID_WORKSTATION (Logon failure: user not allowed to
     * log on to this computer.) LDAP[userWorkstations: <multivalued list of
     * workstation names>] NOTE: Returns only when presented with valid username
     * and password/credential.
     *
     * 80090308: LdapErr: DSID-0C09030B, comment: AcceptSecurityContext error,
     * data 532, v893 HEX: 0x532 - password expired DEC: 1330 -
     * ERROR_PASSWORD_EXPIRED (Logon failure: the specified account password has
     * expired.) LDAP[userAccountControl: <bitmask=0x00800000>] -
     * PASSWORDEXPIRED NOTE: Returns only when presented with valid username and
     * password/credential.
     *
     * 80090308: LdapErr: DSID-0C09030B, comment: AcceptSecurityContext error,
     * data 533, v893 HEX: 0x533 - account disabled DEC: 1331 -
     * ERROR_ACCOUNT_DISABLED (Logon failure: account currently disabled.)
     * LDAP[userAccountControl: <bitmask=0x00000002>] - ACCOUNTDISABLE NOTE:
     * Returns only when presented with valid username and password/credential.
     *
     * 80090308: LdapErr: DSID-0C09030B, comment: AcceptSecurityContext error,
     * data 701, v893 HEX: 0x701 - account expired DEC: 1793 -
     * ERROR_ACCOUNT_EXPIRED (The user's account has expired.)
     * LDAP[accountExpires: <value of -1, 0, or extemely large value indicates
     * account will not expire>] - ACCOUNTEXPIRED NOTE: Returns only when
     * presented with valid username and password/credential.
     *
     * 80090308: LdapErr: DSID-0C09030B, comment: AcceptSecurityContext error,
     * data 773, v893 HEX: 0x773 - user must reset password DEC: 1907 -
     * ERROR_PASSWORD_MUST_CHANGE (The user's password must be changed before
     * logging on the first time.) LDAP[pwdLastSet: <value of 0 indicates
     * admin-required password change>] - MUST_CHANGE_PASSWD NOTE: Returns only
     * when presented with valid username and password/credential.
     *
     * 80090308: LdapErr: DSID-0C09030B, comment: AcceptSecurityContext error,
     * data 775, v893 HEX: 0x775 - account locked out DEC: 1909 -
     * ERROR_ACCOUNT_LOCKED_OUT (The referenced account is currently locked out
     * and may not be logged on to.) LDAP[userAccountControl:
     * <bitmask=0x00000010>] - LOCKOUT NOTE: Returns even if invalid password is
     * presented
     *
     * The DEC: values are not presented in Portal logs; however, review of LDAP
     * activity combined with analysis of SystemOut.log and relevant
     * configuration tasks can help narrow down the root cause.
     *
     * Resolving the problem Use the codes above to verify the settings and
     * users in LDAP.
     */
}
