/**
 * JTableWithRowHeaders.java 
 * Copyright (C) 2016 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
