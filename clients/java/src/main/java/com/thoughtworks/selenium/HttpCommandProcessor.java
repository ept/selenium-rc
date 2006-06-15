/*
 * Copyright 2006 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.thoughtworks.selenium;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;

/**
 * Sends commands and retrieves results via HTTP.
 * @author Ben Griffiths, Jez Humble
 */
public class HttpCommandProcessor implements CommandProcessor {

    private String pathToServlet;
    private String browserStartCommand;
    private String browserURL;
    private String sessionId;
    
    /** Specifies a server host/port, a command to launch the browser, and a starting URL for the browser.
     * 
     * @param serverHost - the host name on which the Selenium Server resides
     * @param serverPort - the port on which the Selenium Server is listening
     * @param browserStartCommand - the command string used to launch the browser, e.g. "*firefox" or "c:\\program files\\internet explorer\\iexplore.exe"
     * @param browserURL - the starting URL including just a domain name.  We'll start the browser pointing at the Selenium resources on this URL,
     * e.g. "http://www.google.com" would send the browser to "http://www.google.com/selenium-server/core/SeleneseRunner.html"
     */
    public HttpCommandProcessor(String serverHost, int serverPort, String browserStartCommand, String browserURL) {
        this.pathToServlet = "http://" + serverHost + 
        ":"+ Integer.toString(serverPort) + "/selenium-server/driver/";
        this.browserStartCommand = browserStartCommand;
        this.browserURL = browserURL;
    }
    
    /** Specifies the URL to the CommandBridge servlet, a command to launch the browser, and a starting URL for the browser.
     * 
     * @param pathToServlet - the URL of the Selenium Server Driver, e.g. "http://localhost:4444/selenium-server/driver/" (don't forget the final slash!)
     * @param browserStartCommand - the command string used to launch the browser, e.g. "*firefox" or "c:\\program files\\internet explorer\\iexplore.exe"
     * @param browserURL - the starting URL including just a domain name.  We'll start the browser pointing at the Selenium resources on this URL,
     */
    public HttpCommandProcessor(String pathToServlet, String browserStartCommand, String browserURL) {
        this.pathToServlet = pathToServlet;
        this.browserStartCommand = browserStartCommand;
        this.browserURL = browserURL;
    }

    public String doCommand(String commandName, String[] args) {
        DefaultSeleneseCommand command = new DefaultSeleneseCommand(commandName,args);
        String result = executeCommandOnServlet(command.getCommandURLString());
        if (result == null) {
            throw new NullPointerException("Selenium Bug! result must not be null");
        }
        if (!result.startsWith("OK")) {
            throw new SeleniumException(result);
        }
        return result;
    }

    /** Sends the specified command string to the bridge servlet */  
    public String executeCommandOnServlet(String command) {
        InputStream is = null;
        try {
            is = getCommandResponse(command, is);
            return stringContentsOfInputStream(is);
        } catch (IOException e) {
            throw new UnsupportedOperationException("Catch body broken: IOException from " + command + " -> " + e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String stringContentsOfInputStream(InputStream is) throws IOException {
        StringBuffer sb = new StringBuffer();
        InputStreamReader r = new InputStreamReader(is, "UTF-8");
        int c;
        while ((c = r.read()) != -1) {
            sb.append((char) c);
        }
        return sb.toString();
    }

    private InputStream getCommandResponse(String command, InputStream is) throws IOException {
        int responsecode = 301;
        while (responsecode == 301) {
            URL result = buildCommandURL(command, pathToServlet);
            HttpURLConnection uc = (HttpURLConnection) result.openConnection();
            uc.setInstanceFollowRedirects(false);
            responsecode = uc.getResponseCode();
            if (responsecode == 301) {
                pathToServlet = uc.getRequestProperty("Location");
            } else if (responsecode != 200) {
                throw new SeleniumException(uc.getResponseMessage());
            } else {
                is = uc.getInputStream();
            }
        }
        return is;
    }

    private URL buildCommandURL(String command, String servletUrl) throws MalformedURLException {
        StringBuffer sb = new StringBuffer();
        sb.append(servletUrl);
        sb.append("?");
        sb.append(command);
        if (sessionId != null) {
            sb.append("&sessionId=");
            sb.append(DefaultSeleneseCommand.urlEncode(sessionId));
        }
        URL result = new URL(sb.toString());
        return result;
    }

    public void start() {
        String result = getString("getNewBrowserSession", new String[]{browserStartCommand, browserURL});
        sessionId = result;
        
    }

    public void stop() {
        doCommand("testComplete", null);
        sessionId = null;
    }

    public String getString(String commandName, String[] args) {
        String result = doCommand(commandName, args);
        if (result.length() > "OK,".length()) {
            return result.substring("OK,".length());
        }
        System.out.println("WARNING: getString(" + commandName + ") saw a bad result " + result);
        return "";
    }

    public String[] getStringArray(String commandName, String[] args) {
        String result = getString(commandName, args);
        return parseCSV(result);
    }

    private static String[] parseCSV(String input) {
        ArrayList<String> output = new ArrayList<String>();
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case ',':
                    output.add(sb.toString());
                    sb = new StringBuffer();
                    continue;
                case '\\':
                    i++;
                    c = input.charAt(i);
                    // fall through to:
                default:
                    sb.append(c);
            }  
        }
        output.add(sb.toString());
        return output.toArray(new String[0]);
    }
    
    public Number getNumber(String commandName, String[] args) {
        String result = getString(commandName, args);
        Number n;
        try {
            n = NumberFormat.getInstance().parse(result);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return n;
    }

    public Number[] getNumberArray(String commandName, String[] args) {
        String[] result = getStringArray(commandName, args);
        Number[] n = new Number[result.length];
        for (int i = 0; i < result.length; i++) {
            try {
                n[i] = NumberFormat.getInstance().parse(result[i]);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        return n;
    }

    public boolean getBoolean(String commandName, String[] args) {
        String result = getString(commandName, args);
        boolean b;
        if ("true".equals(result)) {
            b = true;
            return b;
        }
        if ("false".equals(result)) {
            b = false;
            return b;
        }
        throw new RuntimeException("result was neither 'true' nor 'false': " + result);
    }

    public boolean[] getBooleanArray(String commandName, String[] args) {
        String[] result = getStringArray(commandName, args);
        boolean[] b = new boolean[result.length];
        for (int i = 0; i < result.length; i++) {
            if ("true".equals(result)) {
                b[i] = true;
                continue;
            }
            if ("false".equals(result)) {
                b[i] = false;
                continue;
            }
            throw new RuntimeException("result was neither 'true' nor 'false': " + result);
        }
        return b;
    }
}
