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
public class CommandResultHolder {
    private static Log log = LogFactory.getLog(CommandResultHolder.class);
	private static final int defaultTimeout = SeleniumServer.timeoutInSeconds;
	private static final String poisonResult = "CommandResultHolder.POISON";
	protected static final String CMD_TIMED_OUT_MSG = "ERROR: Command timed out";
	protected static final String CMD_NULL_RESULT_MSG = "ERROR: Got a null result";

	private final String queueId;
	private final SingleEntryAsyncQueue<String> holder;
    
    public CommandResultHolder(String queueId, int timeoutInSeconds) {
      holder = new SingleEntryAsyncQueue<String>(timeoutInSeconds);
      holder.setPoison(poisonResult);
      this.queueId = queueId;
    }

	public CommandResultHolder(String queueId) {
	  this(queueId, defaultTimeout);
	}

    /**
     * Get a result out of the result holder (from the browser), waiting no
     * longer than the timeout.  
     * 
     * @return the result from the result holder
     */
    public String getResult() {
      String result = null;
      String hdr = "\t" + CommandQueue.getIdentification("commandResultHolder", queueId) + " getResult() ";
      if (log.isDebugEnabled()) {
        log.debug(hdr + "called");
      }

      // wait until data arrives before the timeout
      result = holder.pollToGetContentUntilTimeout();

      if (null == result) {
        // if there is no result, then it timed out.
		result = CMD_TIMED_OUT_MSG;
      } else if (holder.isPoison(result)) {
        // if queue was poisoned, then just return indicator of a null result.
        result = CMD_NULL_RESULT_MSG;
      }

      if (log.isDebugEnabled()) {
        StringBuilder msg = new StringBuilder(hdr + "-> " + result);
        if (CMD_TIMED_OUT_MSG.equals(result)) {
          msg.append(" after " + holder.getTimeoutInSeconds() + " seconds.");
        }
        log.debug(msg.toString());
      }
      
      return result;
    }
    
    public boolean putResult(String res) {
      return holder.putContent(res);
    }

    public boolean isEmpty() {
      return holder.isEmpty();
    }

    public String peek() {
      return holder.peek();
    }

    public void poisonPollers() {
      holder.poisonPollers();
    }
    
}
