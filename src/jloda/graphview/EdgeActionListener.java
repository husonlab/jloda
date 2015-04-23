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

package jloda.graphview;

/**
 * @version $Id: EdgeActionListener.java,v 1.5 2007-01-05 17:38:14 huson Exp $
 *
 * Actions performed during interaction.
 *
 * @author Daniel Huson
 * 6.2001
 */

import jloda.graph.Edge;
import jloda.graph.EdgeSet;

//import jloda.util.*;

/**
 * Interface for actions performed on edges during GraphView interaction.
 */
public interface EdgeActionListener {
    /**
     * Called when creating a new edge.
     *
     * @param e Edge
     */
    void doNew(Edge e);

    /**
     * Called when deleting a new edge.
     *
     * @param e Edge
     */
    void doDelete(Edge e);

    /**
     * Called when edges are clicked on.
     *
     * @param edges  EdgeSet
     * @param clicks int
     */
    void doClick(EdgeSet edges, int clicks);

    /**
     * Called when edge labels are clicked on.
     *
     * @param edges  EdgeSet
     * @param clicks int
     */
    void doClickLabel(EdgeSet edges, int clicks);


    /**
     * Called when edges are pressed.
     *
     * @param edges EdgeSet
     */
    void doPress(EdgeSet edges);

    /**
     * Called when edges are released.
     *
     * @param edges EdgeSet
     */
    void doRelease(EdgeSet edges);

    /**
     * Called when edges are selected.
     *
     * @param edges EdgeSet
     */
    void doSelect(EdgeSet edges);

    /**
     * Called when edges are de-selected.
     *
     * @param edges EdgeSet
     */
    void doDeselect(EdgeSet edges);

    /**
     * Called when edge labels were moved
     *
     * @param edges EdgeSet
     */
    void doLabelMoved(EdgeSet edges);

}

// EOF
