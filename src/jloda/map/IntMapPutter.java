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

import jloda.util.Basic;
import jloda.util.FileInputIterator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * map based int putter
 * Daniel Huson, 4.2015
 */
public class IntMapPutter implements IIntGetter, IIntPutter {
    private final Map<Long, Integer> map;
    private long limit;
    private final File file;
    private boolean mustWriteOnClose = false;

    public IntMapPutter(File file) throws IOException {
        this.file = file;
        map = new HashMap<>();

        if (file.exists()) {
            final FileInputIterator it = new FileInputIterator(file.getPath());
            while (it.hasNext()) {
                String aLine = it.next().trim();
                if (!aLine.startsWith("#")) {
                    int pos = aLine.indexOf('\t');
                    if (pos > 0) {
                        long index = Long.parseLong(aLine.substring(0, pos));
                        if (index + 1 >= limit)
                            limit = index + 1;
                        map.put(index, Integer.parseInt(aLine.substring(pos + 1)));
                    }
                }
            }
            it.close();
        }
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
     * puts value for given index
     *
     * @param index
     * @param value
     */
    @Override
    public void put(long index, int value) {
        if (index + 1 >= limit)
            limit = index + 1;
        map.put(index, value);
        if (!mustWriteOnClose)
            mustWriteOnClose = true;
    }

    /**
     * length of array
     * todo: limit can be incorrect if getMap() was used to change values
     *
     * @return array length
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
        if (mustWriteOnClose) {
            try {
                BufferedWriter w = new BufferedWriter(new FileWriter(file));
                for (Long key : map.keySet()) {
                    w.write(key + "\t" + map.get(key) + "\n");
                }
                w.close();
            } catch (IOException e) {
                Basic.caught(e);
            }
        }
    }

}
