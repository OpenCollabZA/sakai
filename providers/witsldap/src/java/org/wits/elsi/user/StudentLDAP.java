package org.wits.elsi.user;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

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

public class StudentLDAP {
	private static Log log = LogFactory.getLog(StudentLDAP.class);

    private final static String INIT_CTX = "com.sun.jndi.ldap.LdapCtxFactory";
    private String host = "";
    private String searchBase = "";
    private String dn = "";
    private String filter = "";
    private String password = "";
    private DirContext dirContext = null;
    private String surname = "";
    private String givenname = "";
    private String title = "";
    private String email = "";

    public StudentLDAP(String host, String searchBase, String dn, String password) {
        this.host = host;
        this.searchBase = searchBase;
        this.dn = dn;
        this.password = password;
    }

    public boolean login(String username, String password) throws NamingException {
        filter = "(cn=" + username + ")";
        setInitialContext();
        String dn = executeSearch();

        if (dn == null) {
            return false;
        } else {
            return authenticate(password, dn);
        }

    }

    public static void main(String args[]) throws NamingException {
        //   StudentLDAP studentLDAP = new StudentLDAP();
        //  System.out.println(" logged on ? " + studentLDAP.login("0708741t", "naledi97"));
    }

    private boolean authenticate(String password, String dn) {

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


        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, INIT_CTX);
        env.put(Context.PROVIDER_URL, host);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, dn);
        env.put(Context.SECURITY_CREDENTIALS, password);
        dirContext = new InitialDirContext(env);

    }

    public String executeSearch() {
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
                        attValue += ((attValue.length() == 0 ? "" : "/") + (String) vals.nextElement());
                    }
                    //  System.out.println(attrId + "= " + attValue);
                    if ("accountExpires".equals(attrId)) {
                        String accountExpires = attValue;
                        final long DIFF_NET_JAVA_FOR_DATES = 11644473600000L + 24 * 60 * 60 * 1000;

                        long adAccountExpires = Long.parseLong(accountExpires);
                        long milliseconds = (adAccountExpires / 10000) - DIFF_NET_JAVA_FOR_DATES;
                        Date accountExpiresDate = new Date(milliseconds);
                        log.info("Account expires: " + accountExpiresDate);
                        Date today = new Date();
                        long diff = today.getTime() - accountExpiresDate.getTime();
                        long diffDays = diff / (24 * 60 * 60 * 1000);
                        log.info("Days remaining before password expiry: " + diffDays);
                        if (diffDays < 8) {
                            log.info("WARN: Your passwords is expiring in " + diffDays);
                        }
                    }
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
                //System.out.println(surname + "," + givenname + "," + title + "," + email);
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGivenname() {
        return givenname;
    }

    public void setGivenname(String givenname) {
        this.givenname = givenname;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
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
}
