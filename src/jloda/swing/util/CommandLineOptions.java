/*
 * CommandLineOptions.java Copyright (C) 2019. Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**@version $Id: CommandLineOptions.java,v 1.22 2007-07-15 11:02:36 huson Exp $
 *
 * Unix style command line option handling
 *
 *@author Daniel Huson
 * 11.02
 */
package jloda.swing.util;

import jloda.util.Basic;
import jloda.util.UsageException;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

/**
 * Unix style command line option handling
 * <p/>
 * Command-line mode:
 * Construct object, use getOption is query options, then call done() to verify that
 * all options have been found
 * <p/>
 * GUI mode:
 * (1) must specify -arggui as a boolean option using getOption
 * (2) all getOption calls must be contained in a do{} while() loop with condition !done()
 * GUI mode is used when commandline -arggui is provided
 */
public class CommandLineOptions {
    private String description;
    private String[] args;
    private boolean[] seen;
    private final List<String> usage;
    private final List<String> settings;
    private final List<String> options;
    private boolean exitOnHelp;

    private int stage = 0; // 0: doing normal commandline string processing
    // 1-2: gui processing, 1: collecting options to build GUI, 2: looping to get entered values
    private GUI gui = null;
    private boolean doHelp = false;

    /**
     * construct a command line options parser
     *
     * @param args
     */
    public CommandLineOptions(String[] args) {
        this(args, true);
    }

    /**
     * construct a command line options parser
     *
     * @param args
     * @param exitOnHelp
     */
    public CommandLineOptions(String[] args, boolean exitOnHelp) {
        description = "Main program";
        this.args = args;
        seen = new boolean[args.length];
        usage = new LinkedList<>();
        settings = new LinkedList<>();
        options = new LinkedList<>();
        this.exitOnHelp = exitOnHelp;

        // scan to see whether arguments are to be set by GUI, if so, set stage=1:
        for (String arg1 : args) {
            if (arg1.equals("-arggui")) {
                stage = 1;
                gui = new GUI();
                break;
            }
        }

        // scan to see whether arguments are for help:
        for (String arg : args) {
            if (arg.equals("-h")) {
                doHelp = true;
                break;
            }
        }
    }

    /**
     * Returns a string option
     *
     * @param label    the option label
     * @param describe a short description
     * @param def      the default value
     * @return the value following the label
     */
    public String getOption(String label, String describe, String def) throws UsageException {
        options.add(grow20(label));
        if (describe.charAt(0) != '!')
            usage.add(grow20(label + " <String>") + " (default=\"" + def + "\"): " + describe);
        else
            usage.add(null);
        String val = getStringOption(label, def, describe, false);
        if (describe.charAt(0) != '!') {
            settings.add("" + val);
            if (stage == 1) {
                gui.addRow(label, describe, val);
            }
        } else
            settings.add(null);
        return val;
    }

    /**
     * Returns a string option
     *
     * @param label    the option label
     * @param describe a short description
     * @param def      the default value
     * @return the value following the label
     */
    public String getOption(String label, String describe, String[] legalValues, String def)
            throws UsageException {
        options.add(grow20(label));
        StringBuilder str = new StringBuilder(grow20(label + " <String>") + " (default=\"" + def + "\", legal=");
        boolean first = true;
        for (String legalValue : legalValues) {
            if (first)
                first = false;
            else
                str.append(",");
            str.append("\"").append(legalValue).append("\"");
        }
        str.append(") ").append(describe);
        if (describe.charAt(0) != '!')
            usage.add(str.toString());
        else
            usage.add(null);
        String val = getStringOption(label, def, describe, false);
        boolean ok = false;
        for (String legalValue : legalValues)
            if (legalValue.equalsIgnoreCase(val)) {
                ok = true;
                break;
            }
        if (!ok)
            throw new UsageException("Option " + label + ": illegal value: " + val);

        if (describe.charAt(0) != '!') {
            settings.add("" + val);
            if (stage == 1) {
                gui.addRow(label, describe, val);
            }
        } else
            settings.add(null);
        return val;
    }

    /**
     * Returns the value of a mandatory string option
     *
     * @param label    the option label
     * @param describe a short description
     * @param def      the default value
     * @return the value following the label
     */
    public String getMandatoryOption(String label, String describe, String def)
            throws UsageException {
        options.add(grow20(label));
        usage.add(grow20(label + " <String>") + " (default=\"" + def + "\"): " + describe + " (mandatory option)");

        String val = getStringOption(label, def, describe, true);
        settings.add(val);
        if (stage == 1) {
            gui.addRow(label, describe + " (mandatory option)", val);
        }

        return val;
    }

    /**
     * Returns an integer option
     *
     * @param label    the option label
     * @param describe a short description
     * @param def      the default value
     * @return the value following the label
     */
    public int getOption(String label, String describe, int def)
            throws UsageException {
        options.add(grow20(label));
        if (describe.charAt(0) != '!')
            usage.add(grow20(label + " <int>") + " (default=" + def + "): " + describe);
        else
            usage.add(null);
        try {
            String val = getStringOption(label, Integer.toString(def), describe, false);
            if (describe.charAt(0) != '!') {
                settings.add("" + Integer.parseInt(val));
                if (stage == 1) {
                    gui.addRow(label, describe, val);
                }

            } else
                settings.add(null);
            return Integer.parseInt(val);
        } catch (Exception ex) {
            throw new UsageException("option  " + label + ": integer expected");
        }
    }

    /**
     * Returns the value of a mandatory integer option
     *
     * @param label    the option label
     * @param describe a short description
     * @param def      the default value
     * @return the value following the label
     */
    public int getMandatoryOption(String label, String describe, int def)
            throws UsageException {
        usage.add(grow20("mandatory: " + label + " <int>") + " (default=" + def + "): " + describe);
        options.add(grow20(label));
        try {
            String val = getStringOption(label, Integer.toString(def), describe, true);
            settings.add("" + Integer.parseInt(val));
            if (stage == 1) {
                gui.addRow(label, describe + " (mandatory option)", val);
            }
            return Integer.parseInt(val);
        } catch (Exception ex) {
            throw new UsageException("option  " + label + ": integer expected");
        }
    }


    /**
     * Returns an integer option
     *
     * @param label    the option label
     * @param describe a short description
     * @param def      the default value
     * @return the value following the label
     */
    public long getOption(String label, String describe, long def)
            throws UsageException {
        options.add(grow20(label));
        if (describe.charAt(0) != '!')
            usage.add(grow20(label + " <long>") + " (default=" + def + "): " + describe);
        else
            usage.add(null);
        try {
            String val = getStringOption(label, Long.toString(def), describe, false);
            if (describe.charAt(0) != '!') {
                settings.add("" + Long.parseLong(val));
                if (stage == 1) {
                    gui.addRow(label, describe, val);
                }

            } else
                settings.add(null);
            return Long.parseLong(val);
        } catch (Exception ex) {
            throw new UsageException("option  " + label + ": long expected");
        }
    }

    /**
     * Returns the value of a mandatory longeger option
     *
     * @param label    the option label
     * @param describe a short description
     * @param def      the default value
     * @return the value following the label
     */
    public long getMandatoryOption(String label, String describe, long def)
            throws UsageException {
        usage.add(grow20("mandatory: " + label + " <long>") + " (default=" + def + "): " + describe);
        options.add(grow20(label));
        try {
            String val = getStringOption(label, Long.toString(def), describe, true);
            settings.add("" + Long.parseLong(val));
            if (stage == 1) {
                gui.addRow(label, describe + " (mandatory option)", val);
            }
            return Long.parseLong(val);
        } catch (Exception ex) {
            throw new UsageException("option  " + label + ": Long expected");
        }
    }


    /**
     * Returns a double option
     *
     * @param label    the option label
     * @param describe a short description
     * @param def      the default value
     * @return the value following the label
     */
    public double getOption(String label, String describe, double def) throws UsageException {
        options.add(grow20(label));
        if (describe.charAt(0) != '!')
            usage.add(grow20(label + " <double>") + " (default=" + def + "): " + describe);
        else
            usage.add(null);
        try {
            String val = getStringOption(label, Double.toString(def), describe, false);
            if (describe.charAt(0) != '!') {
                settings.add("" + Double.parseDouble(val));
                if (stage == 1) {
                    gui.addRow(label, describe, val);
                }
            } else
                settings.add(null);
            return Double.parseDouble(val);
        } catch (Exception ex) {
            throw new UsageException("option  " + label + ": double expected");
        }
    }

    /**
     * Returns the value of a mandatory double option
     *
     * @param label    the option label
     * @param describe a short description
     * @param def      the default value
     * @return the value following the label
     */
    public double getMandatoryOption(String label, String describe, double def)
            throws UsageException {
        options.add(grow20(label));
        usage.add(grow20("mandatory: " + label + " <double>") + " (default=" + def + "): " + describe);
        try {
            String val = getStringOption(label, Double.toString(def), describe, true);
            settings.add("" + Double.parseDouble(val));
            if (stage == 1) {
                gui.addRow(label, describe + " (mandatory option)", val);
            }
            return Double.parseDouble(val);
        } catch (Exception ex) {
            throw new UsageException("option  " + label + ": double expected");
        }
    }

    /**
     * Returns a specified value, if the named label is a command line
     * option, otherwise returns the default value.
     * If description label starts with !, then this is a secret and undocumented option
     *
     * @param label    the option label
     * @param describe
     * @param result   the result returned if the option is present
     * @param def      the default value
     * @return the value following the label
     */
    public boolean getOption(String label, String describe, boolean result, boolean def) {
        if (label.startsWith("+")) {
            if (def)
                label = "-" + label.substring(1);
            else
                throw new RuntimeException("Internal error: '+' switch must have default=true");
        }

        options.add(grow20(label));
        if (describe.charAt(0) != '!')
            usage.add(grow20(label + " <switch>") + " (default=" + def + "): " + describe);
        else
            usage.add(null);
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.length() > 1 && arg.charAt(0) == '+' && label.length() > 1 && arg.substring(1).equals(label.substring(1)))
                args[i] = "-" + arg.substring(1);
            if (!seen[i] && args[i].equals(label)) {
                seen[i] = true;
                if (describe.charAt(0) != '!') {
                    settings.add("" + result);
                    if (stage == 1) {
                        gui.addRow(label, describe, "" + result);
                    }
                } else
                    settings.add(null);

                if (i + 1 < args.length && !seen[i + 1]) {
                    if (args[i + 1].equalsIgnoreCase("true")) {
                        seen[i + 1] = true;
                        return true;
                    } else if (args[i + 1].equalsIgnoreCase("false")) {
                        seen[i + 1] = true;
                        return false;
                    }
                }
                return result;
            }
        }
        if (describe.charAt(0) != '!') {
            settings.add("" + def);
            if (stage == 1) {
                gui.addRow(label, describe, "" + def);
            }
        } else
            settings.add(null);
        return def;
    }

    /**
     * Results a list of tokens following the given label.
     * All tokens following the given label are returned up until before
     * the next token that has not yet been grabbed by a call to getOption.
     *
     * @param label the option label
     * @param def   the default value
     * @return the tokens following the label
     */
    public List<String> getOption(String label, String describe, List<String> def) {
        options.add(grow20(label));
        if (describe.charAt(0) != '!')
            usage.add(grow20(label + " <String*>") + " (default=" + def + "): " + describe);
        else
            usage.add(null);
        List<String> result = new LinkedList<>();

        boolean found = false;
        for (int i = 0; i < seen.length; i++) {
            if (!found && !seen[i] && args[i].equals(label)) {
                seen[i] = true;
                found = true;
            } else if (found && !seen[i] && !args[i].startsWith("-")) {
                result.add(args[i]);
                seen[i] = true;
            } else if (found && (seen[i] || args[i].startsWith("-")))
                break;
        }
        if (found) {
            if (describe.charAt(0) != '!') {
                settings.add("" + result);
                if (stage == 1) {
                    gui.addRow(label, describe, "" + Basic.listAsString(result, " "));
                }
            } else
                settings.add(null);
            return result;
        } else {
            if (describe.charAt(0) != '!') {
                settings.add("" + def);
                if (stage == 1) {
                    gui.addRow(label, describe, "" + def);
                }
            } else
                settings.add(null);
            return def;
        }
    }

    /**
     * Results is a list of tokens following the given label.
     * All tokens following the given label are returned up until before
     * the next token that has not yet been grabbed by a call to getOption.
     *
     * @param label the option label
     * @param def   the default value
     * @return the tokens following the label
     */
    public String[] getOption(String label, String describe, String[] def)
            throws UsageException {
        List<String> result = getOption(label, describe, Arrays.asList(def));
        if (result == null)
            return null;
        else
            return result.toArray(new String[0]);
    }


    /**
     * Returns a mandatory list of tokens following the given label.
     * All tokens following the given label are returned up until before
     * the next token that has not yet been grabbed by a call to getOption.
     *
     * @param label the option label
     * @param def   the default value
     * @return the tokens following the label
     */
    public List<String> getMandatoryOption(String label, String describe, List<String> def)
            throws UsageException {
        options.add(grow20(label));
        if (describe.charAt(0) != '!')
            usage.add(grow20("mandatory: " + label + " <String*>") + " (default=" + def + "): " + describe);
        else usage.add(null);
        List<String> result = new LinkedList<>();

        boolean found = false;
        for (int i = 0; i < seen.length; i++) {
            if (!found && !seen[i] && args[i].equals(label)) {
                seen[i] = true;
                found = true;
            } else if (found && !seen[i] && !args[i].startsWith("-") && !args[i].startsWith("+")) {
                result.add(args[i]);
                seen[i] = true;
            } else if (found && (seen[i] || args[i].startsWith("-") || args[i].startsWith("+")))
                break;
        }
        if (found || stage == 1) {
            if (describe.charAt(0) != '!') {
                settings.add("" + result);
                if (stage == 1) {
                    gui.addRow(label, describe + " (mandatory option)", Basic.listAsString(result, " "));
                }
            } else
                settings.add(null);
            return result;
        } else {
            if (!doHelp)
                throw new UsageException("mandatory option: " + label + " (" + describe + ")");

            else
                exitOnHelp = true; // mandatory option missing, show help then quit
            return result;
        }
    }

    /**
     * Results a mandatory list of tokens following the given label.
     * All tokens following the given label are returned up until before
     * the next token that has not yet been grabbed by a call to getOption.
     *
     * @param label the option label
     * @param def   the default value
     * @return the tokens following the label
     */
    public String[] getMandatoryOption(String label, String describe, String[] def)
            throws UsageException {
        List<String> result = getMandatoryOption(label, describe, Arrays.asList(def));
        if (result == null)
            return null;
        else
            return result.toArray(new String[0]);
    }


    /* does the work */

    private String getStringOption(String label, String def, String describe, boolean mandatory) throws
            UsageException {
        for (int i = 0; i < seen.length; i++) {
            if (!seen[i] && args[i].equals(label)) {
                seen[i] = true;
                if (i + 1 == seen.length || seen[i + 1])
                    throw new UsageException
                            ("option " + label + ": missing argument" + " (" + describe + ")");
                else {
                    seen[i + 1] = true;
                    return args[i + 1];
                }
            }
        }
        if (mandatory && stage != 1) {
            if (doHelp)
                exitOnHelp = true;
            else
                throw new UsageException("mandatory option: " + label + " (" + describe + ")");
        }
        return def;
    }


    /**
     * Sets the program description
     *
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Call this after processing all options to check for superfluous
     * options
     */
    public boolean done() throws UsageException {
        if (stage == 0 || stage == 2) {
            try {
                boolean help = getOption("-h", "Show usage", true, false);
                if (help) {
                    StringBuilder str = new StringBuilder("\n" + description + "\n");
                    str.append("\nProgram usage:\n");
                    for (String anUsage : usage)
                        if (anUsage != null)
                            str.append("\t").append(anUsage).append("\n");
                    str.append("\n");
                    System.out.print(str);
                    if (getExitOnHelp())
                        System.exit(0);
                }
                for (int i = 0; i < args.length; i++) {
                    if (!seen[i]) {
                        StringBuilder str = new StringBuilder("\n" + description + "\n");
                        str.append("Illegal option: '").append(args[i]).append("'\n");
                        str.append("\nProgram usage:\n");
                        for (String anUsage : usage)
                            if (anUsage != null)
                                str.append("\t").append(anUsage).append("\n");
                        str.append("\n");
                        throw new UsageException(str.toString());
                    }
                }
            } catch (UsageException ex) {
                if (stage == 2) {
                    new Alert(null, "Usage exception: " + ex);
                    stage = 1;
                    return false;
                } else
                    throw ex;
            }
            return true;
        } else // stage==1: have setup GUI, now show the GUI, get the values, modify the arg string and rerun
        {
            gui.finishAndShow();
            args = gui.getArgs();
            System.err.println("Command-line arguments:");
            if (args != null) {
                for (String arg : args) {
                    System.err.print(" " + arg);
                }
                System.err.println();
            }

            stage = 2;
            // prepare to redo:
            if (args != null)
                seen = new boolean[args.length];
            usage.clear();
            settings.clear();
            options.clear();

            return false;
        }
    }

    /**
     * Gets the set options as a string
     *
     * @return the set options
     */
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("\n");
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i) != null && settings.get(i) != null)
                buf.append("\t").append(options.get(i)).append("= ").append(settings.get(i)).append("\n");
        }
        return buf.toString();
    }

    /**
     * Gets the set options as a string
     *
     * @return the set options
     */
    public String toOptionsString() {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i) != null && settings.get(i) != null) {
                buf.append(" ").append(options.get(i).trim()).append(" ").append(settings.get(i));
            }
        }
        return buf.toString();
    }

    /**
     * exit after displaying program help?
     *
     * @return exit on help?
     */
    public boolean getExitOnHelp() {
        return exitOnHelp;
    }

    /**
     * exit after displaying program help?
     *
     * @param exitOnHelp
     */
    public void setExitOnHelp(boolean exitOnHelp) {
        this.exitOnHelp = exitOnHelp;
    }

    /**
     * add a label to the usage message
     *
     * @param label
     */
    public void addLabel(String label) {
        options.add(null);
        usage.add("\n  " + label);
        settings.add(null);
        if (stage == 1)
            gui.addLabel(label);
    }

    /**
     * grow a label to length 20
     *
     * @param label
     * @return label of length at least 20
     */
    private String grow20(String label) {
        StringBuilder labelBuilder = new StringBuilder(label);
        while (labelBuilder.length() < 20) {
            labelBuilder.append(" ");
        }
        label = labelBuilder.toString();
        return label;
    }

    /**
     * gets the GUI
     *
     * @return the GUI or null
     */
    public JDialog getGUI() {
        return gui;
    }

    /**
     * the commandline option GUI
     */
    private class GUI extends JDialog {
        private final Vector<Vector<String>> data = new Vector<>();
        private JTable table = null;
        private String[] args = null;

        GUI() {
            super();
            setSize(800, 500);
            setLocation(100, 100);
            setModal(true);
            getContentPane().setLayout(new BorderLayout());
            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent windowEvent) {
                    System.exit(0);
                }
            });
        }

        void addRow(String label, String description, String defaultValue) {
            Vector<String> row = new Vector<>();
            row.add(label);
            row.add(description);
            row.add(defaultValue);
            data.add(row);
        }


        void addLabel(String label) {
            Vector<String> row = new Vector<>();
            row.add(label);
            row.add("");
            row.add("");
            data.add(row);
        }

        // finish gui and show

        private void finishAndShow() {
            table = new JTable(new AbstractTableModel() {
                private final String[] columnNames = {"Option", "Description", "Value"};

                public int getColumnCount() {
                    return columnNames.length;
                }

                public int getRowCount() {
                    return data.size();
                }

                public String getColumnName(int col) {
                    return columnNames[col];
                }

                public Object getValueAt(int row, int col) {
                    return ((Vector) data.elementAt(row)).elementAt(col);
                }

                public Class getColumnClass(int c) {
                    return String.class;
                }

                public boolean isCellEditable(int row, int col) {
                    return !(col < 2 || getValueAt(row, 1).equals(""));
                }

                public void setValueAt(Object value, int row, int col) {
                    data.elementAt(row).setElementAt((String) value, col);
                    fireTableCellUpdated(row, col);
                }
            });
            table.getColumnModel().getColumn(0).setPreferredWidth(200);
            table.getColumnModel().getColumn(1).setPreferredWidth(600);
            table.getColumnModel().getColumn(2).setPreferredWidth(100);
            table.setShowVerticalLines(true);
            table.setShowHorizontalLines(true);

            JScrollPane scrollPane = new JScrollPane(table);
            table.setPreferredScrollableViewportSize(new Dimension(400, 70));
            getContentPane().add(scrollPane, BorderLayout.CENTER);

            JPanel bottomPanel = new JPanel();
            bottomPanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 50));
            bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));

            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new AbstractAction() {
                public void actionPerformed(ActionEvent actionEvent) {
                    System.err.println("User canceled");
                    System.exit(0);
                }
            });
            bottomPanel.add(cancelButton);

            JButton applyButton = new JButton("Apply");
            applyButton.addActionListener(new AbstractAction() {
                public void actionPerformed(ActionEvent actionEvent) {
                    String missingOption = makeArgs();
                    if (missingOption == null)
                        setVisible(false);
                    else // mandatory options   missing
                    {
                        new Alert("Mandatory option '" + missingOption + "' has not been supplied");
                    }
                }
            });
            bottomPanel.add(applyButton);
            JPanel wrapper = new JPanel();
            wrapper.setLayout(new BorderLayout());
            wrapper.add(bottomPanel, BorderLayout.EAST);
            getContentPane().add(wrapper, BorderLayout.SOUTH);

            setTitle("Command line arguments for " + description); // this late because program description is set after GUi is constructed
            setModal(true);
            setVisible(true);
        }

        String[] getArgs() {
            return args;
        }

        /**
         * returns label for missing mandatory option, or null, if everything is fine
         *
         * @return missing option or null
         */
        private String makeArgs() {
            String missingOption = null;
            List<String> list = new LinkedList<>();
            boolean ok = false;
            for (int i = 0; i < table.getRowCount(); i++) {
                String label = (String) table.getModel().getValueAt(i, 0);
                if (!label.equals("-arggui"))
                    ok = true;
                String description = (String) table.getModel().getValueAt(i, 1);
                String value = (String) table.getModel().getValueAt(i, 2);

                if (value.length() > 0) {
                    list.add(label);
                    list.add(value);
                } else if (missingOption == null && description.contains("(mandatory option)"))
                    missingOption = label;
            }
            args = list.toArray(new String[0]);
            if (!ok)
                throw new RuntimeException("Internal error: not setup for -arggui option");
            return missingOption;
        }
    }


}


