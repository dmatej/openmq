/*
 * Copyright (c) 2000, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2020 Payara Services Ltd.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.messaging.jmq.jmsserver.config;

import java.util.*;
import java.io.*;
import com.sun.messaging.jmq.util.SizeString;
import com.sun.messaging.jmq.jmsserver.resources.*;
import com.sun.messaging.jmq.jmsserver.comm.CommGlobals;
import com.sun.messaging.jmq.util.log.*;

//##########################################################################
//##########################################################################
//#                                                                        #
//#                      Public Class UpdateProperties                     #
//#                                                                        #
//##########################################################################
//##########################################################################

/**
 * this is a subclass of properties which handles allows clients to register "ConfigListeners" which are notified when a
 * property changes.
 */

/*
 * XXX linda 7/17/00 REVISIT code needs to handle updating properties object before calling all methods
 */

public class UpdateProperties extends Properties {
    /**
     * 
     */
    private static final long serialVersionUID = 2654468190051766351L;
    private Logger logger = CommGlobals.getLogger();
    /**
     * Properties object which contains any properties which need to be stored in the instance property location
     */
    protected Properties storedprops = null;

    /**
     * location where the updated properties are stored
     */
    protected String storedloc = null;

    /**
     * string placed at the top of the instance property file
     */
    private static final String PROP_HEADER_STR = "#This file is automatically generated, DO NOT EDIT";

    // ------------------------------------------------------------------------
    // -- constructor methods --
    // ------------------------------------------------------------------------

    /**
     * default constructor.
     *
     * @throws IOException if the file can not be located
     */
    public UpdateProperties() {
        super(System.getProperties());
        storedprops = new Properties();
    }

    /**
     * loads a property file into the stored properties location. This method loads a property file into memory, and then
     * stored modified properties out to that location. Only one stored property file can be set on the properties object.
     * If the method is called a second time, an IllegalStateException will be thrown.
     *
     * @param location the place to load/store the updated property list;
     * @throws IllegalArgumentException if the location passed in is invalid (e.g. null) or this method has already been
     * called once.
     * @throws IOException if the file can not be loaded (this is not a fatal error, it may just indicate that the file is
     * new
     */
    public void loadStoredPropertiesFile(String location) throws IOException, IllegalArgumentException {
        setStoredPropertiesLocation(location);
        Properties props = readPropertiesFile(location);
        setStoredProperties(props);
    }

    /**
     *
     */
    protected void setStoredProperties(Properties props) throws IllegalArgumentException {
        putAll(props);
        storedprops.putAll(props);
    }

    protected void setStoredPropertiesLocation(String location) throws IllegalArgumentException {
        if (storedloc != null) {
            throw new IllegalArgumentException(
                    CommGlobals.getBrokerResources().getString(BrokerResources.X_INTERNAL_EXCEPTION, "Can not access more than one stored property location"));
        }

        if (location == null) {
            throw new IllegalArgumentException(
                    CommGlobals.getBrokerResources().getString(BrokerResources.X_INTERNAL_EXCEPTION, "Can not load property from null location"));
        }

        storedloc = location;
    }

    @Override
    public Object put(Object name, Object value) {
        Object o = super.get(name);
        Object retobj = null;
        if (o instanceof WatchedProperty) {
            WatchedProperty wp = (WatchedProperty) o;
            retobj = wp.getValue();
            wp.setValue((String) value);
        } else {
            retobj = super.put(name, value);
        }
        return retobj;
    }

    @Override
    public Object remove(Object key) {
        Object o = super.get(key);
        Object retobj = null;
        if (o instanceof WatchedProperty) {
            WatchedProperty wp = (WatchedProperty) o;
            retobj = wp.getValue();
            wp.setValue((String) null);
        } else {
            retobj = super.remove(key);
        }
        return retobj;
    }

    /**
     * loads a property file into this object overriding any previous properties. These properties are considered "default"
     * properties, and are only stored to the "stored property file" if they are changed.
     *
     * @param location path to the properties file which should be loaded
     * @throws IOException if the file can not be loaded
     */
    public void loadDefaultProperties(String location) throws IOException {

        readPropertiesFile(location, this);
    }

    /**
     * loads a property file into a new properties object and returns it.
     *
     * @param location path to the properties file which should be loaded
     * @return a new properties object.
     */
    protected Properties readPropertiesFile(String location) throws IOException {
        Properties props = new Properties();
        readPropertiesFile(location, props);
        return props;
    }

    /**
     * loads a property file into the passed in Properties object.
     *
     * @param location path to the properties file which should be loaded
     * @param props properties object to load the file into.
     */
    protected void readPropertiesFile(String location, Properties props) throws IOException {
        FileInputStream ifile = new FileInputStream(location);
        BufferedInputStream bfile = new BufferedInputStream(ifile);
        props.load(bfile);
        bfile.close();
        ifile.close();
    }

    // ------------------------------------------------------------------------
    // -- Methods for setting/Updating/Retrieving properties --
    // ------------------------------------------------------------------------

    /**
     * Writes out the updated instance property file when a property is changed.
     *
     * XXX - LKS 7/5/00 - How should IOExceptiones be handled ??
     *
     */
    private synchronized void saveUpdatedProperties() throws IOException

    {
        saveUpdatedProperties(storedprops);

    }

    protected void saveUpdatedProperties(Properties props) throws IOException {
        FileOutputStream cfile = new FileOutputStream(storedloc);
        BufferedOutputStream bos = new BufferedOutputStream(cfile);
        props.store(bos, PROP_HEADER_STR);
        bos.close();
        cfile.close();

    }

    /**
     * Copies all of the mappings from the specified Map to this Properties object. These mappings will replace any mappings
     * that this Properties object had for any of the keys currently in the specified Map.
     * <P>
     * No validation will be preformed by this method
     * <P>
     */
    @Override
    public void putAll(Map props) {
        // OK .. we know we are valid, so update
        Iterator itr = props.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry entry = (Map.Entry) itr.next();
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            Object prop = super.get(key);
            if (prop == null || !(prop instanceof WatchedProperty)) {
                super.setProperty(key, value);
                continue;
            }
            ((WatchedProperty) prop).setValue(value);
        }
    }

    /**
     * The correct method to be used to update a Configuration property. This method checks with any listeners to make sure
     * the property can be updated and then updates the property.
     * <P>
     *
     * XXX - LKS - 7/5/00 - look at update's return value
     *
     * @param key name of the property
     * @param value new value of the property
     *
     * @see java.util.Properties#setProperty(String, String)
     * @see #setProperty(String, String)
     * @see #updateProperties(Hashtable)
     * @see #updateProperty(String, String, boolean)
     *
     * @throws PropertyUpdateException if the property can not be set for some reason
     * @throws IOException if the property file can not be stored
     */
    public void updateProperty(String key, String value) throws PropertyUpdateException, IOException {
        updateProperty(key, value, true);
    }

    /**
     * Convenience method for updating a boolean property
     *
     * @see updateProperty(String, String, boolean);
     */
    public void updateBooleanProperty(String key, boolean value, boolean save) throws PropertyUpdateException, IOException {
        updateProperty(key, (Boolean.valueOf(value)).toString(), save);
    }

    /**
     * The correct method to be used to update a Configuration property. This method checks with any listeners to make sure
     * the property can be updated and then updates the property.
     * <P>
     *
     * XXX - LKS - 7/5/00 - look at update's return value
     *
     * @param key name of the property
     * @param value new value of the property
     * @param save set to true if the property file should be saved after update
     *
     * @see java.util.Properties#setProperty(String, String)
     * @see #setProperty(String, String)
     * @see #updateProperties(Hashtable)
     *
     * @throws PropertyUpdateException if the property can not be set for some reason
     * @throws IOException if the property file can not be stored
     */
    public void updateProperty(String key, String value, boolean save) throws PropertyUpdateException, IOException {
        // first get the old property
        Object prop = super.get(key);
        if (prop != null && prop instanceof WatchedProperty) {
            Vector listeners = ((WatchedProperty) prop).getListeners();
            if (listeners != null) {
                synchronized (listeners) {
                    for (int i = 0; i < listeners.size(); i++) {
                        // first validate its OK
                        ((ConfigListener) listeners.elementAt(i)).validate(key, value);
                    }
                    // now we know its valid, update
                    for (int i = 0; i < listeners.size(); i++) {
                        // now update
                        ((ConfigListener) listeners.elementAt(i)).update(key, value);
                    }
                }
            }
            ((WatchedProperty) prop).setValue(value);
        } else {
            super.setProperty(key, value);
        }

        storedprops.setProperty(key, value);

        if (save) {
            // now set the "stored" property file
            saveUpdatedProperties();
        }
    }

    public void updateRemoveProperty(String key, boolean save) throws IOException {
        super.remove(key);
        storedprops.remove(key);
        if (save) {
            saveUpdatedProperties();
        }
    }

    /**
     * This method updates a group of properties at once (calling the validate methods BEFORE calling the update methods for
     * all properties). This method is similar to the updateProperty method except it handles a group of properties.
     *
     * XXX - LKS - 7/5/00 - look at update's return value
     *
     * @see java.util.Properties#setProperty(String, String)
     * @see #setProperty(String, String)
     * @see #updateProperty(String, String)
     * @see #updateProperty(Hashtable, boolean)
     *
     * @throws PropertyUpdateException if the properties can not all be set for some reason
     * @throws IOException if the properties can not be stored
     */
    public void updateProperties(Hashtable values) throws PropertyUpdateException, IOException {
        updateProperties(values, true);
    }

    /**
     * This method updates a group of properties at once (calling the validate methods BEFORE calling the update methods for
     * all properties). This method is similar to the updateProperty method except it handles a group of properties.
     *
     * XXX - LKS - 7/5/00 - look at update's return value
     *
     * @see java.util.Properties#setProperty(String, String)
     * @see #setProperty(String, String)
     * @see #updateProperty(String, String)
     *
     * @throws PropertyUpdateException if the properties can not all be set for some reason
     * @throws IOException if the properties can not be stored
     */
    public void updateProperties(Hashtable values, boolean save) throws PropertyUpdateException, IOException {
        // first get the old property
        Enumeration _enum = values.keys();
        while (_enum.hasMoreElements()) {
            String key = (String) _enum.nextElement();
            String value = (String) values.get(key);
            Object prop = super.get(key);
            if (prop != null && prop instanceof WatchedProperty) {
                Vector listeners = ((WatchedProperty) prop).getListeners();
                if (listeners != null) {
                    synchronized (listeners) {
                        for (int i = 0; i < listeners.size(); i++) {
                            // first validate its OK
                            ((ConfigListener) listeners.get(i)).validate(key, value);
                        }
                    }
                }
            }
        }

        putAll(values);
        storedprops.putAll(values);

        // OK .. we know we are valid, so update
        _enum = values.keys();
        while (_enum.hasMoreElements()) {
            String key = (String) _enum.nextElement();
            Object prop = super.get(key);
            if (prop == null || !(prop instanceof WatchedProperty)) {
                continue;
            }
            Vector listeners = ((WatchedProperty) prop).getListeners();
            String value = (String) values.get(key);

            // now we know its valid, update
            if (listeners != null) {
                synchronized (listeners) {
                    for (int i = 0; i < listeners.size(); i++) {
                        // now update
                        ((ConfigListener) listeners.elementAt(i)).update(key, value);
                    }
                }
            }
        }

        // now set the "stored" property file
        if (save) {
            saveUpdatedProperties();
        }
    }

    /**
     * Returns the value of a property.
     *
     * @param key name of the property
     * @return the value of the property
     * @see java.util.Properties#getProperty(String)
     * @see java.util.Properties#getProperty(String,String)
     * @see #getProperty(String,String)
     * @see WatchedProperty#getValue()
     */
    @Override
    public String getProperty(String key) {
        Object prop = super.get(key);

        if (prop == null) {
            return null;
        }

        if (prop instanceof WatchedProperty) {
            return ((WatchedProperty) prop).getValue();
        }

        if (prop instanceof String) {
            return (String) prop;
        }

        return null;
    }

    /**
     * Returns the value of a property.
     *
     * @param key name of the property
     * @param default_value the value to return if the property is not set
     * @return the value of the property
     * @see java.util.Properties#getProperty(String)
     * @see java.util.Properties#getProperty(String,String)
     * @see #getProperty(String)
     * @see WatchedProperty#getValue()
     */
    @Override
    public String getProperty(String key, String default_value) {
        String prop = getProperty(key);

        if (prop == null) {
            return default_value;
        }

        return prop;
    }

    /**
     * overrides properties object list method to handle the WatchProperty object stored in the properties list
     *
     * @param out an output stream.
     */
    @Override
    public void list(PrintStream out) {
        out.println("-- listing properties --");
        Enumeration e = super.keys();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            Object val = super.get(key);
            String valstr = val.toString();
            if (valstr.length() > 60) {
                valstr = valstr.substring(0, 57) + "...";
            }
            out.println(key + "=" + valstr);
        }
    }

    /**
     * overrides properties object list method to handle the WatchProperty object stored in the properties list
     *
     * @param out an output stream.
     */
    @Override
    public void list(PrintWriter out) {
        out.println("-- listing properties --");
        Enumeration e = super.keys();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            Object val = super.get(key);
            String valstr = val.toString();
            if (valstr.length() > 60) {
                valstr = valstr.substring(0, 57) + "...";
            }
            out.println(key + "=" + valstr);
        }
    }

    /**
     * Returns a list of property names that match specified name prefix.
     *
     * @param namePrefix the name prefix
     * @return a list of all property names that match the name prefix
     */
    public List getPropertyNames(String namePrefix) {
        ArrayList list = new ArrayList();
        Enumeration e = super.keys();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            if (key.startsWith(namePrefix)) {
                list.add(key);
            }
        }
        return list;
    }

    // ------------------------------------------------------------------------
    // -- Methods for adding/removing ConfigListeners --
    // ------------------------------------------------------------------------

    /**
     * Adds a listener on a specific Property.
     * <P>
     * <I> WARNING </I> any object which listeners for a property to change <B> must </B> make sure it removes its listener
     * when the object is destroyed
     *
     * @param name name of the property to watch
     * @param listener listener to call when the property changes
     *
     * @see #removeListener(String,ConfigListener)
     * @see WatchedProperty#removeListener(ConfigListener)
     */

    public void addListener(String name, ConfigListener listener) {
        Object prop = super.get(name);
        WatchedProperty watcher = null;

        if (prop == null || prop instanceof String) {
            watcher = new WatchedProperty((String) prop);
            super.put(name, watcher);
        } else {
            watcher = (WatchedProperty) prop;
        }
        watcher.addListener(listener);

    }

    /**
     * Removes a listener on a specific Property.
     * <P>
     *
     * @param name name of the property which was watch
     * @param listener object which should be removed as a listener.
     */
    public void removeListener(String name, ConfigListener listener) {
        Object prop = super.get(name);
        if (prop instanceof WatchedProperty) {
            WatchedProperty watcher = (WatchedProperty) prop;
            watcher.removeListener(listener);
        }
    }

    // ------------------------------------------------------------------------
    // -- Convience Methods to return properties as different types --
    // ------------------------------------------------------------------------

    /**
     * Returns a vector of all entries in a , seperated property list.
     *
     * @param name name of the property to return
     * @return a vector of each entry in the , seperated property
     * @see #getArray(String)
     */

    public List getList(String name) {
        String prop = getProperty(name);
        if (prop == null) {
            return null;
        }
        StringTokenizer token = new StringTokenizer(prop, ",", false);
        List retv = new ArrayList();
        while (token.hasMoreElements()) {
            String newtoken = token.nextToken();
            // ok .. trim of leading and trailing spaces
            // trailing
            newtoken = newtoken.trim();
            // leading
            int start = 0;
            while (start < newtoken.length()) {
                if (!Character.isSpaceChar(newtoken.charAt(start))) {
                    break;
                }
                start++;
            }
            if (start > 0) {
                newtoken = newtoken.substring(start + 1);
            }
            if (newtoken.length() > 0) {
                retv.add(newtoken);
            }
        }
        return retv;
    }

    /**
     * Returns an array of all entries in a , seperated property list.
     *
     * @param name name of the property to return
     * @return an array of each entry in the , seperated property
     * @see #getList(String)
     */
    public String[] getArray(String name) {
        String prop = getProperty(name);
        if (prop == null) {
            return null;
        }
        StringTokenizer token = new StringTokenizer(prop, ",", false);
        int num = token.countTokens();
        String[] retv = new String[num];
        for (int i = 0; i < num; i++) {
            String newtoken = token.nextToken();
            // ok .. trim of leading and trailing spaces
            // trailing
            newtoken = newtoken.trim();
            // leading
            int start = 0;
            while (start < newtoken.length()) {
                if (!Character.isSpaceChar(newtoken.charAt(start))) {
                    break;
                }
                start++;
            }
            if (start > 0) {
                newtoken = newtoken.substring(start + 1);
            }
            retv[i] = newtoken;
        }
        return retv;
    }

    /**
     * Returns the passed in property as an long.
     *
     * @param name name of the property to return
     * @return an long converted property (or 0 if it can not be converted or doesnt exist)
     * @see #getLongProperty(String,long)
     */
    public long getLongProperty(String name) {
        return getLongProperty(name, 0);
    }

    /**
     * Returns the passed in property as an long.
     *
     * @param name name of the property to return
     * @param defval default value to return if the property can not be set or doesnt exist.
     * @return an long converted property (or the default value if it can not be converted or doesnt exist)
     * @see #getLongProperty(String)
     */
    public long getLongProperty(String name, long defval) {
        String prop = getProperty(name);
        if (prop == null) {
            return defval;
        }
        try {
            return Long.parseLong(prop);
        } catch (Exception ex) {
            logger.log(Logger.INFO, BrokerResources.E_BAD_PROPERTY_VALUE, name, ex);
        }
        return defval;
    }

    /**
     * Returns the passed in property as a float.
     *
     * @param name name of the property to return
     * @param defval default value to return if the property can not be set or doesnt exist.
     * @return a float converted property (or the default value if it can not be converted or doesnt exist)
     * @see #getPercentageProperty(String)
     */
    public float getPercentageProperty(String name, float defval) {
        String prop = getProperty(name);
        if (prop == null) {
            return defval;
        }
        try {
            return (Float.parseFloat(prop) / 100);
        } catch (Exception ex) {
            logger.log(Logger.INFO, BrokerResources.E_BAD_PROPERTY_VALUE, name, ex);
        }
        return defval;
    }

    /**
     * Returns the passed in property as an int.
     *
     * @param name name of the property to return
     * @return an int converted property (or 0 if it can not be converted or doesnt exist)
     * @see #getIntProperty(String,int)
     */
    public int getIntProperty(String name) {
        return getIntProperty(name, 0);
    }

    /**
     * Returns the passed in property as an int.
     *
     * @param name name of the property to return
     * @param defval default value to return if the property can not be set or doesnt exist.
     * @return an int converted property (or the default value if it can not be converted or doesnt exist)
     * @see #getIntProperty(String)
     */
    public int getIntProperty(String name, int defval) {
        String prop = getProperty(name);
        if (prop == null) {
            return defval;
        }
        try {
            return Integer.parseInt(prop);
        } catch (Exception ex) {
            logger.log(Logger.INFO, BrokerResources.E_BAD_PROPERTY_VALUE, name, ex);
        }
        return defval;
    }

    /**
     * Returns the passed in property as an SizeString.
     *
     * @param name name of the property to return
     * @return an SizeString converted property (or 0 if it can not be converted or doesnt exist)
     * @see #getSizeProperty(String,int)
     */
    public SizeString getSizeProperty(String name) {
        return getSizeProperty(name, 0);
    }

    /**
     * Returns the passed in property as an SizeString.
     *
     * @param name name of the property to return
     * @param defval default value to return if the property can not be set or doesnt exist. Value is in kbytes.
     * @return an int converted property (or the default value if it can not be converted or doesnt exist)
     * @see #getIntProperty(String)
     */
    public SizeString getSizeProperty(String name, long defval) {
        String prop = getProperty(name);
        if (prop == null) {
            return new SizeString(defval);
        }
        try {
            return new SizeString(prop);
        } catch (Exception ex) {
            logger.log(Logger.INFO, BrokerResources.E_BAD_PROPERTY_VALUE, name, ex);
        }
        return new SizeString(defval);
    }

    /**
     * Returns the passed in property as a boolean.
     *
     * @param name name of the property to return
     * @return a boolean converted property (or false if it can not be converted or doesnt exist)
     * @see #getBooleanProperty(String,boolean)
     */
    public boolean getBooleanProperty(String name) {
        return getBooleanProperty(name, false);
    }

    /**
     * Returns the passed in property as a boolean.
     *
     * @param name name of the property to return
     * @param defval default value to return if the property can not be set or doesnt exist.
     * @return a boolean converted property (or the default value if it can not be converted or doesnt exist)
     * @see #getBooleanProperty(String)
     */
    public boolean getBooleanProperty(String name, boolean defval) {
        String prop = getProperty(name);
        if (prop == null) {
            return defval;
        }
        try {
            return Boolean.parseBoolean(prop);
        } catch (Exception ex) {
            logger.log(Logger.INFO, BrokerResources.E_BAD_PROPERTY_VALUE, name, ex);
        }
        return defval;
    }

    /**
     * Return a generic java.util.Property object that represents the properties stored in this object. This is useful when
     * the caller truly needs a java.util.Property object for serialization, etc The returned object is a copy of the
     * property data. This is an expensive operation.
     */
    public Properties toProperties() {

        Properties props = new Properties();

        Enumeration e = super.keys();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            String val = this.getProperty(key);

            if (val == null) {
                val = "";
            }
            props.setProperty(key, val);
        }

        return props;
    }

}

//##########################################################################
//##########################################################################
//#                                                                        #
//#                      Private Class WatchedProperty                     #
//#                                                                        #
//##########################################################################
//##########################################################################

/**
 * This class is used by updateProperties in place of the normal value for a property to maintain the list of listeners
 * for that property.
 * <P>
 *
 * This class maintains both the value and the set of listeners
 */
class WatchedProperty {
    /**
     * value of the property
     */
    private String value;

    /**
     * set of listeners for the property
     */
    private Vector listeners = null;

    /**
     * create a new watched property object with a null value
     */
    WatchedProperty() {
        this(null);
    }

    /**
     * create a new watched property object
     *
     * @param value value of the property
     */
    WatchedProperty(String value) {
        this.value = value;
        listeners = new Vector();
    }

    /**
     * sets the value of the property object
     *
     * @param value the new value of the property
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * returns the value of the property object
     *
     * @return the current value of the property
     */
    public String getValue() {
        return value;
    }

    /**
     * adds a new config listener
     *
     * @param listener the listener object to add
     */
    public void addListener(ConfigListener listener) {
        listeners.addElement(listener);
    }

    /**
     * removes an existing config listener
     *
     * @param listener the listener object to remove as a listener
     */
    public void removeListener(ConfigListener listener) {
        listeners.removeElement(listener);
    }

    /**
     * clears out all config listener
     *
     */
    void clearListeners() {
        listeners = new Vector();
    }

    /**
     * returns the vector of all config listener
     *
     * @return the vector of listeners (if there are no listeners, it will return an empty vectory NOT a null vector)
     */
    Vector getListeners() {
        return this.listeners;
    }

    /**
     * prints out the string for a watched Property
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("WatchedProperty(").append(listeners.size()).append(") value = [\"").append(value).append("\"] {");
        for (int i = 0; i < listeners.size(); i++) {
            if (i > 0) {
                buf.append(", ");
            }
            buf.append(listeners.elementAt(i).getClass().toString());
        }
        buf.append('}');
        return buf.toString();
    }
}
