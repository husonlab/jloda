/*
 * ReorderListDialog.java Copyright (C) 2019. Daniel H. Huson
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

package jloda.swing.util;

import jloda.util.ProgramProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

/**
 * Dialog for reordering a list of objects
 * <p/>
 * Wei Wu and Daniel Huson, 6.2008
 */
public class ReorderListDialog extends JDialog implements DropTargetListener, ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = 5234260814801310243L;
    private final Vector originalList = new Vector();//save inputted objects in Vector
    private boolean beApplied = false;//label for action from button apply

    private final boolean showCopy; // show the copy button?

    private final JList originalJlist;
    private final JList reorderedJlist;
    private final JButton copy;
    private final JButton flip;
    private final JButton rotateUp;
    private final JButton rotateDown;
    private final JButton apply;
    private final JButton cancel;

    /*
      * implements for interface DrogTargetListener
     */

    public void dragEnter(DropTargetDragEvent event) {

    }

    public void dragExit(DropTargetEvent event) {

    }

    public void dragOver(DropTargetDragEvent event) {

    }

    public void drop(DropTargetDropEvent event) {
        //get the current context of the reordered list
        DefaultListModel dropmodel = new DefaultListModel();
        for (int i = 0; i < reorderedJlist.getModel().getSize(); i++) {
            dropmodel.addElement(reorderedJlist.getModel().getElementAt(i));
        }

        //position to insert
        int insertIndex = reorderedJlist.locationToIndex(event.getLocation());
        //default inserted position is the current index. wenn now it is the last index,
        //it must be confirmed, which it is between the current last or the future last index
        //a new dialog about the insert position at tail of the list
        if (insertIndex == reorderedJlist.getModel().getSize() - 1) {
            Object[] options = {"Current last position", "Last position after", "Cancel"};
            int n = JOptionPane.showOptionDialog(reorderedJlist,
                    "Would you like to drop the selection at",
                    "Drop at the current tail postion ",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE, ProgramProperties.getProgramIcon(), options, options[1]);
            if ((n != JOptionPane.CANCEL_OPTION) && (n != JOptionPane.CLOSED_OPTION)) insertIndex += n;
            else {
                event.getDropTargetContext().dropComplete(true);
                return;
            }
        }

        //offset for new insert position after deleting old elements
        int offsetForNewInsertPosition = 0;
        //create a container saving the removed element, in order to add them at new postions again after deleting
        Vector toBeRemoved = new Vector();

        /*
                  //at first calculate the offset ands save the indize which elements will be removed

                  System.out.println(tokens.countTokens()+" items "+"selected");
                  while(tokens.hasMoreTokens()){
                      Object nextElement = tokens.nextElement();
                      int j = dropmodel.indexOf(nextElement);
                      System.out.println(j+" "+nextElement.hashCode());
                      if (j < insertIndex) offsetForNewInsertPosition++;
                      toBeRemoved.add(j);
                  }
                  */

        //then delete the selected elements from the list
        int i = 0;
        for (int j = 0; j < reorderedJlist.getSelectedIndices().length; j++) {
            int pointer = reorderedJlist.getSelectedIndices()[j];
            toBeRemoved.add(dropmodel.getElementAt(pointer - i));
            dropmodel.removeElementAt(pointer - i);
            if (pointer < insertIndex) offsetForNewInsertPosition++;
            i++;
        }
        //insert the removed elements at new position again
        for (Object ins : toBeRemoved) {
            dropmodel.add(insertIndex - offsetForNewInsertPosition, ins);
            insertIndex++;
        }

        /*
                  StringTokenizer tokensForAdding = new StringTokenizer(data, "\n") ;
                  while(tokensForAdding.hasMoreTokens()){
                      Object nextElement = tokensForAdding.nextElement();
                      System.out.println("insert["+(insertIndex-offsetForNewInsertPosition)+"] "+nextElement);
                      dropmodel.add(insertIndex-offsetForNewInsertPosition, nextElement);
                      insertIndex++;
                  }
                  */

        reorderedJlist.setModel(dropmodel);
        reorderedJlist.updateUI();
        event.getDropTargetContext().dropComplete(true);
    }

    public void dropActionChanged(DropTargetDragEvent event) {

    }

    /*
      * implement for ActionListerner that buttons use
      * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
      */

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == apply) {
            beApplied = true;
            dispose();
        } else if (e.getSource() == cancel) {
            //label for action form button cancel
            boolean beCancelled = true;
            dispose();
        } else if (e.getSource() == copy) {
            DefaultListModel model = new DefaultListModel();
            model.clear();
            for (Object anOriginalList : originalList) {
                model.addElement(anOriginalList);
            }
            reorderedJlist.setModel(model);
        } else if (e.getSource() == flip) {
            DefaultListModel model1 = new DefaultListModel();
            for (int i = 0; i < reorderedJlist.getModel().getSize(); i++) {
                model1.addElement(reorderedJlist.getModel().getElementAt(i));
            }
            if (model1.getSize() == 0) {
                for (Object anOriginalList : originalList) {
                    model1.addElement(anOriginalList);
                }
            }
            int size = model1.getSize();
            for (int i = 0; i < size - 1; i++) {
                Object element = model1.get(size - 2 - i);
                model1.addElement(element);
            }
            for (int j = 0; j < size - 1; j++) {
                model1.removeElementAt(0);
            }
            reorderedJlist.setModel(model1);
        } else if (e.getSource() == rotateUp) {
            DefaultListModel model2 = new DefaultListModel();
            for (int i = 0; i < reorderedJlist.getModel().getSize(); i++) {
                model2.addElement(reorderedJlist.getModel().getElementAt(i));
            }
            if (model2.getSize() == 0) {
                for (Object anOriginalList : originalList) {
                    model2.addElement(anOriginalList);
                }
            }
            model2.add(model2.getSize(), model2.get(0));
            model2.removeElementAt(0);
            reorderedJlist.setModel(model2);
        } else if (e.getSource() == rotateDown) {
            DefaultListModel model3 = new DefaultListModel();
            for (int i = 0; i < reorderedJlist.getModel().getSize(); i++) {
                model3.addElement(reorderedJlist.getModel().getElementAt(i));
            }
            if (model3.getSize() == 0) {
                for (Object anOriginalList : originalList) {
                    model3.addElement(anOriginalList);
                }
            }
            model3.add(0, model3.get(model3.getSize() - 1));
            model3.removeElementAt(model3.getSize() - 1);
            reorderedJlist.setModel(model3);
        }
    }

    /*
      * constructor
      */

    public ReorderListDialog(String title, boolean showCopy) {
        super();
        setTitle(title);
        setModal(true);
        this.showCopy = showCopy;

        /*
           * left list
           */
        /*
         *attributes for JDialog
         */
        JLabel originalLabel = new JLabel("Original");
        originalLabel.setHorizontalAlignment(SwingConstants.CENTER);

        originalJlist = new JList(originalList.toArray());
        originalJlist.setLayoutOrientation(JList.VERTICAL);
        JScrollPane scroll1 = new JScrollPane(originalJlist);
        scroll1.setPreferredSize(new Dimension(240, 300));

        /*
        * right list
        */
        JLabel reorderedLabel = new JLabel("Reordered");
        reorderedLabel.setHorizontalAlignment(SwingConstants.CENTER);

        reorderedJlist = new JList();
        reorderedJlist.setLayoutOrientation(JList.VERTICAL);
        reorderedJlist.setAutoscrolls(true);
        //set dnd on this Jlist
        reorderedJlist.setDragEnabled(true);
        // reorderedJlist.setDropMode(DropMode.ON_OR_INSERT );
        reorderedJlist.setAutoscrolls(true);
        new DropTarget(reorderedJlist, this);

        JScrollPane scroll2 = new JScrollPane(reorderedJlist);
        scroll2.setPreferredSize(new Dimension(240, 300));

        /*
           * buttons in the middle
           */
        copy = new JButton("Copy=>");
        copy.setMinimumSize(new Dimension(120, 30));
        copy.setMaximumSize(new Dimension(120, 30));
        copy.setPreferredSize(new Dimension(120, 30));
        copy.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        copy.addActionListener(this);

        flip = new JButton("Swap=>");
        flip.setMinimumSize(new Dimension(120, 30));
        flip.setMaximumSize(new Dimension(120, 30));
        flip.setPreferredSize(new Dimension(120, 30));
        flip.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        flip.addActionListener(this);


        rotateDown = new JButton("Rotate Down=>");
        rotateDown.setMinimumSize(new Dimension(120, 30));
        rotateDown.setMaximumSize(new Dimension(120, 30));
        rotateDown.setPreferredSize(new Dimension(120, 30));
        rotateDown.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        rotateDown.addActionListener(this);


        rotateUp = new JButton("Rotate Up=>");
        rotateUp.setMinimumSize(new Dimension(120, 30));
        rotateUp.setMaximumSize(new Dimension(120, 30));
        rotateUp.setPreferredSize(new Dimension(120, 30));
        rotateUp.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        rotateUp.addActionListener(this);

        /*
           * botton apply, cancel
           */
        cancel = new JButton("Cancel");
        cancel.setMinimumSize(new Dimension(100, 30));
        cancel.setMaximumSize(new Dimension(100, 30));
        cancel.setPreferredSize(new Dimension(100, 30));
        cancel.setDisplayedMnemonicIndex(0);
        cancel.addActionListener(this);
        getRootPane().setDefaultButton(cancel);

        apply = new JButton("Apply");
        apply.setMinimumSize(new Dimension(100, 30));
        apply.setMaximumSize(new Dimension(100, 30));
        apply.setPreferredSize(new Dimension(100, 30));
        apply.setDisplayedMnemonicIndex(0);
        apply.addActionListener(this);

        JPanel panel = new JPanel();

        /*
        * layout for the dialog
        */
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        panel.setLayout(gbl);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.gridheight = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.LAST_LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbl.setConstraints(originalLabel, gbc);
        panel.add(originalLabel);

        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.gridheight = 1;
        gbl.setConstraints(reorderedLabel, gbc);
        panel.add(reorderedLabel);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.gridheight = 5;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        gbl.setConstraints(scroll1, gbc);
        panel.add(scroll1);

        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.gridheight = 5;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        gbl.setConstraints(scroll2, gbc);
        panel.add(scroll2);

        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.CENTER;


        if (showCopy) {
            gbc.gridx = 3;
            gbc.gridy++;
            gbl.setConstraints(copy, gbc);
            panel.add(copy);
        }

        gbc.gridx = 3;
        gbc.gridy++;
        gbl.setConstraints(flip, gbc);
        panel.add(flip);

        gbc.gridx = 3;
        gbc.gridy++;
        gbl.setConstraints(rotateUp, gbc);
        panel.add(rotateUp);

        gbc.gridx = 3;
        gbc.gridy++;
        gbl.setConstraints(rotateDown, gbc);
        panel.add(rotateDown);

        gbc.gridx = 5;
        gbc.gridy = 6;
        gbl.setConstraints(cancel, gbc);
        panel.add(cancel);

        gbc.gridx = 6;
        gbc.gridy = 6;
        gbl.setConstraints(apply, gbc);
        panel.add(apply);

        this.setLayout(new BorderLayout());
        this.add(panel, BorderLayout.CENTER);
        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        this.pack();
        this.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - this.getSize().width) / 2, (Toolkit.getDefaultToolkit().getScreenSize().height - this.getSize().height) / 2);
    }

    /**
     * constructor
     *
     * @param title
     */
    public ReorderListDialog(String title) {
        this(title, true);
    }

    /**
     * constructor
     */
    public ReorderListDialog() {
        this("Reorder", true);
    }

    /**
     * show the dialog for the given list of objects
     *
     * @param original
     * @return reordered list
     */
    public List show(List original) {
        //load input into the link Jlist
        originalList.addAll(original);
        DefaultListModel model = new DefaultListModel();
        for (Object anOriginal : original) {
            model.addElement(anOriginal);
        }
        this.originalJlist.setModel(model);

        if (!showCopy) {
            model = new DefaultListModel();
            for (Object anOriginalList : originalList) {
                model.addElement(anOriginalList);
            }
            reorderedJlist.setModel(model);
        }

        this.setVisible(true);
        this.toFront();

        if (beApplied) {
            Vector returnedList = new Vector();
            for (int i = 0; i < reorderedJlist.getModel().getSize(); i++) {
                returnedList.add(reorderedJlist.getModel().getElementAt(i));
            }
            return returnedList;
        }
        //only one case that beCancelled is true now
        else {
            return null;
        }
    }

    /**
     * test program
     *
     * @param args
     */
    static public void main(String[] args) throws Exception {
        final Vector superClasses = new Vector();
        Class rootClass = javax.swing.JList.class;
        for (Class cls = rootClass; cls != null; cls = cls.getSuperclass()) {
            superClasses.add(cls);
        }

        SwingUtilities.invokeAndWait(() -> {
            ReorderListDialog test = new ReorderListDialog("ReorderListDialog", false);
            List output = test.show(superClasses);
            System.out.println(output);
        });
        System.exit(0);
    }

}
