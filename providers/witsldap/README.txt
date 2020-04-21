README
======

REQUIREMENTS:

1. Go to $SAKAI_SCR/providers/pom.xml and add the following

<module>witsldap</module> in the modules section.

2. Then go to providers/component/pom.xml and add this dependency so that the jar will be copied to tomcat lib file.
   <dependency>
  	<groupId>org.sakaiproject</groupId>
  	<artifactId>sakai-witsldap-provider</artifactId>
  	<version>10.5</version>
   </dependency>

3. Then go to $SAKAI_SRC/providers/component/src/webap/WEB-INF/components.xml and add the following
at the end of the file

  <bean id="org.sakaiproject.user.api.UserDirectoryProvider"
			class="org.wits.elsi.user.WitsLdapUserDirectoryProvider"
			init-method="init"
			destroy-method="destroy"
			>
        <property name="userDirectoryService" ref="org.sakaiproject.user.api.UserDirectoryService" />
        <property name="securityService" ref="org.sakaiproject.authz.api.SecurityService" />
        <property name="siteService" ref="org.sakaiproject.site.api.SiteService" />
        <property name="toolManager" ref="org.sakaiproject.tool.api.ToolManager" />
        <property name="dataSource" ref="javax.sql.DataSource" />
        <property name="serverConfigurationService" ref="org.sakaiproject.component.api.ServerConfigurationService"/>
        <property name="sessionManager" ref="org.sakaiproject.tool.api.SessionManager"/>
        <property name="usageSessionService" ref="org.sakaiproject.event.api.UsageSessionService"/>
        <property name="eventTrackingService" ref="org.sakaiproject.event.api.EventTrackingService"/>
        <property name="authzGroupService" ref="org.sakaiproject.authz.api.AuthzGroupService"/>
        <property name="preferencesService" ref="org.sakaiproject.user.api.PreferencesService"/>   
        <property name="emailService" ref="org.sakaiproject.email.api.EmailService" />
    </bean>
4.  Go to $SAKAI_SRC/providers/witsldap and deploy by :

 mvn clean install sakai:deploy
    
5.  Go to $SAKAI_SRC/providers and deploy by :

 mvn clean install sakai:deploy

Restart tomcat and login using your wits credentials



