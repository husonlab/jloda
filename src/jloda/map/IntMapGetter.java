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

package jloda.map;

import jloda.util.FileInputIterator;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * map-based int getter
 * Daniel 4.2015
 */
public class IntMapGetter implements IIntGetter {
    private final Map<Long, Integer> map;
    private long limit;

    public IntMapGetter(File file) throws IOException {
        map = new HashMap<>();

        final FileInputIterator it = new FileInputIterator(file.getPath());
        while (it.hasNext()) {
            String aLine = it.next().trim();
            if (!aLine.startsWith("#")) {
                int pos = aLine.indexOf('\t');
                if (pos > 0) {
                    long key = Long.parseLong(aLine.substring(0, pos));
                    if (key + 1 >= limit)
                        limit = key + 1;
                    map.put(key, Integer.parseInt(aLine.substring(pos + 1)));
                }
            }
        }
        it.close();
    }

    /**
     * gets value for given index
     *
     * @param index
     * @return value or 0
     */
    @Override
    public int get(long index) {
        Integer value = map.get(index);
        if (value != null)
            return value;
        else
            return 0;

    }

    /**
     * length of array
     *
     * @return array length
     * @throws java.io.IOException
     */
    @Override
    public long limit() {
        return limit;
    }

    /**
     * close the array
     */
    @Override
    public void close() {

    }
}
