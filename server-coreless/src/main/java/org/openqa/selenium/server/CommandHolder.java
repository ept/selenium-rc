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

package org.openqa.selenium.server;

import org.apache.commons.logging.Log;
import org.mortbay.log.LogFactory;

/**
 * <p>Holds the command to be next run in the browser</p>
 *
 * @author Jennifer Bevan
 * @version $Revision: 734 $
 */
public class CommandHolder {
	private static final int defaultTimeout = 10; // seconds
	private static final RemoteCommand poisonCommand 
	    = new DefaultRemoteCommand("CommandHolder.POISION", "", "");
	protected static final String RETRY_CMD_STRING = "retryLast";
	protected static final RemoteCommand retryCommand
        = new DefaultRemoteCommand(RETRY_CMD_STRING, "", "", "");

    private static Log log = LogFactory.getLog(CommandHolder.class);
    
	private final String queueId;
	private final SingleEntryAsyncQueue<RemoteCommand> holder;
    
    public CommandHolder(String queueId, int timeoutInSeconds) {
      holder = new SingleEntryAsyncQueue<RemoteCommand>(timeoutInSeconds);
      holder.setPoison(poisonCommand);
      this.queueId = queueId;
    }

	public CommandHolder(String queueId) {
	  this(queueId, defaultTimeout);
	}

    /**
     * Get, and remove from the holder, the next command to run.
     * If the next command doesn't show up within timeoutInSeconds seconds,
     * then return a "retry" command.
     * 
     * @return the next command to execute.
     */
    public RemoteCommand getCommand() {
      RemoteCommand sc = null;
      String hdr = "\t" + CommandQueue.getIdentification("commandHolder", queueId) + " getCommand() ";
      log.debug(hdr + "called");
      
      // wait until data arrives before the timeout
      sc = holder.pollToGetContentUntilTimeout();
      
      if (null == sc) {
        // if there is no new command, send a retryLast. 
        // Purpose: to get around the 2-connections per host issue
        // by sending a request in response to the frame's looking for
        // work -- this allows frame to close the connection.
        sc = retryCommand;
      } else if (holder.isPoison(sc)) {
        // if the queue was poisoned, just exit with a null command.
        sc = null;
      }

      log.debug(hdr + "-> " + ((null == sc) ? "null" : sc.toString())); 
      
      return sc;
    }
    
    public boolean putCommand(RemoteCommand cmd) {
      log.debug("\t" + CommandQueue.getIdentification("commandHolder", queueId) + " putCommand() ");
      return holder.putContent(cmd);
    }

    public boolean isEmpty() {
      return holder.isEmpty();
    }

    public RemoteCommand peek() {
      return holder.peek();
    }

    public void poisonPollers() {
      String hdr = "\t" + CommandQueue.getIdentification("commandHolder", queueId) + " poisonPollers() ";
      log.debug(hdr + " poisoning pollers");
      holder.poisonPollers();
    }
}
