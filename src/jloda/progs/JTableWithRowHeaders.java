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

package jloda.progs;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

public class JTableWithRowHeaders extends JFrame {

    public JTableWithRowHeaders() {
        super("Row Header Test");
        setSize(300, 200);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        TableModel tm = new AbstractTableModel() {
            final String[] data = {"", "a", "b", "c", "d", "e"};

            final String[] headers = {"Row #", "Column 1", "Column 2", "Column 3", "Column 4", "Column 5"};

            public int getColumnCount() {
                return data.length;
            }

            public int getRowCount() {
                return 1000;
            }

            public String getColumnName(int col) {
                return headers[col];
            }

            public Object getValueAt(int row, int col) {
                return data[col] + row;
            }
        };

        TableColumnModel cm = new DefaultTableColumnModel() {
            boolean first = true;

            public void addColumn(TableColumn tc) {
                if (first) {
                    first = false;
                    return;
                }
                tc.setMinWidth(150);
                super.addColumn(tc);
            }
        };

        TableColumnModel rowHeaderModel = new DefaultTableColumnModel() {
            boolean first = true;

            public void addColumn(TableColumn tc) {
                if (first) {
                    tc.setMaxWidth(tc.getPreferredWidth());
                    super.addColumn(tc);
                    first = false;
                }
            }
        };

        JTable jt = new JTable(tm, cm);
        JTable headerColumn = new JTable(tm, rowHeaderModel);
        jt.createDefaultColumnsFromModel();
        headerColumn.createDefaultColumnsFromModel();

        jt.setSelectionModel(headerColumn.getSelectionModel());

        headerColumn.setBackground(Color.lightGray);
        headerColumn.setColumnSelectionAllowed(false);
        headerColumn.setCellSelectionEnabled(false);

        JViewport jv = new JViewport();
        jv.setView(headerColumn);
        jv.setPreferredSize(headerColumn.getMaximumSize());

        jt.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JScrollPane jsp = new JScrollPane(jt);
        jsp.setRowHeader(jv);
        jsp.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, headerColumn.getTableHeader());
        getContentPane().add(jsp, BorderLayout.CENTER);
    }

    public static void main(String args[]) {
        new JTableWithRowHeaders().setVisible(true);
    }
}
