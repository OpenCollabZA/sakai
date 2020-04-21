/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wits.elsi.user;

import java.util.Properties;

/**
 *
 * @author davidwaf
 */
public class Config {

    private  Properties theProperties;

    public Properties getTheProperties() {
        return theProperties;
    }

    public void setTheProperties(Properties theProperties) {
        this.theProperties = theProperties;
    }

    public  String getProperty(String key) {
        if(key.equals("turnitin.intf.version")){
            return "0.3 mercury - May 27 2011";
        }
       return theProperties.getProperty(key);
    }
}
