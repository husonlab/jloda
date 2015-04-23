/**
 * Copyright 2015, Daniel Huson
 * Author Daniel Huson
 *(Some files contain contributions from other authors, who are then mentioned separately)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

/** A unique time stamp at every call
 * @version $Id: TimeStamp.java,v 1.2 2006-06-06 18:56:04 huson Exp $
 * @author Daniel Huson
 * 5.03
 */

package jloda.util;

public class TimeStamp {
    private static long prevTimeStamp = 0;

    /**
     * Returns the next tick of the timestamp clock
     *
     * @return the next tick of the timestamp clock
     */
    public static long get() {
        return ++prevTimeStamp;
    }
}
