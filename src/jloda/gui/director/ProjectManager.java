/**
 * ProjectManager.java
 * Copyright (C) 2016 Daniel H. Huson
 * <p>
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jloda.gui.director;


import jloda.gui.find.SearchManager;
import jloda.util.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.*;


/**
 * manages all the different projects
 *
 * @author huson
 *         Date: 01-Dec-2003
 */
public class ProjectManager {
    final static private List<IDirector> projects = Collections.synchronizedList(new LinkedList<IDirector>());
    final static private Map<IDirector, List<IDirectableViewer>> viewersList = new HashMap<>();
    final static private List<Pair<IDirector, JMenu>> dirAndWindowMenuPairs = Collections.synchronizedList(new LinkedList<Pair<IDirector, JMenu>>());
    final static private Map<JMenu, Integer> menu2baseSize = new HashMap<>();
    final static private List<IProjectsChangedListener> projectsChangedListeners = Collections.synchronizedList(new LinkedList<IProjectsChangedListener>());
    private static boolean exitOnEmpty = true;
    final static private HashSet<JMenu> windowMenusUnderControl = new HashSet<>();

    static final BitSet currentIDs = new BitSet();
    public static final int NEWEST = -1; // pass this to getProject to get newest project

    private static boolean isQuitting = false;

    /**
     * remove a project director
     *
     * @param dir
     */
    static public void removeProject(IDirector dir) {
        synchronized (projects) {
            projects.remove(dir);
            viewersList.remove(dir);
            // any given project uses more than one menu, we need to delete them all
            List<Pair<IDirector, JMenu>> toDelete = new LinkedList<>();
            for (Pair<IDirector, JMenu> pair : dirAndWindowMenuPairs) {
                if (dir == pair.getFirst())
                    toDelete.add(pair);
            }
            for (Pair<IDirector, JMenu> pair : toDelete) {
                windowMenusUnderControl.remove(pair.getSecond());
                dirAndWindowMenuPairs.remove(pair);
            }
        }
        fireProjectsChanged();

        synchronized (projects) {
            currentIDs.clear(dir.getID());
        }

        if (isExitOnEmpty() && projects.isEmpty()) {
            ProgramProperties.store();
            System.exit(0);
        }
    }

    /**
     * add a new project
     *  @param dir    director
     * @param viewer the main viewer associated with the director
     */
    static public IDirector addProject(final IDirector dir, final IMainViewer viewer) {
        try {
            synchronized (projects) {
                final int id = getNextID();
                currentIDs.set(id);
                dir.setID(id);

                projects.add(dir);

                viewersList.put(dir, new LinkedList<IDirectableViewer>());

                if (viewer != null) {
                    final JMenu menu = viewer.getWindowMenu();
                    if (menu != null && !dir.isInternalDocument()) {
                        if (!windowMenusUnderControl.contains(menu)) {
                            Pair<IDirector, JMenu> pair = new Pair<>(dir, menu);
                            dirAndWindowMenuPairs.add(pair);
                            windowMenusUnderControl.add(menu);
                            menu2baseSize.put(menu, menu.getItemCount());
                        }
                    }
                    dir.addViewer(viewer);
                }
            }
            fireProjectsChanged();
        } catch (Exception ex) {
            Basic.caught(ex);
        }
        return dir;
    }

    /**
     * use this to add additional viewers that have a window menu that they want keep upto date
     *
     * @param dir
     * @param menu
     */
    public static void addAnotherWindowWithWindowMenu(IDirector dir, JMenu menu) {
        if (dir != null && !dir.isInternalDocument() && !windowMenusUnderControl.contains(menu)) {
            synchronized (projects) {
                dirAndWindowMenuPairs.add(new Pair<>(dir, menu));
                menu2baseSize.put(menu, menu.getItemCount());
            }
            fireProjectsChanged();
        }
    }

    /**
     * gets the number of open projects
     *
     * @return number of open projects
     */
    public static int getNumberOfProjects() {
        return projects.size();
    }

    /**
     * close all projects
     */
    public static void closeAll() throws CanceledException {
        while (!projects.isEmpty()) {
            synchronized (projects) {
                // close in reverse order to save the geometry of older windows later
                IDirector dir = projects.get(projects.size() - 1);
                dir.close();
            }
        }
    }

    /**
     * call this whenever a project opens or closes a window.
     * Add or move frame and update all window menus
     *
     * @param dir
     * @param viewer0
     * @param opened  true, if window opened, false if closed
     */
    public static void projectWindowChanged(IDirector dir, IDirectableViewer viewer0, boolean opened) {
        final List<IDirectableViewer> viewers0 = viewersList.get(dir);

        if (viewers0 != null) {
            if (opened)
                viewers0.add(viewer0);
            else
                viewers0.remove(viewer0);
        }
        if (!dir.isInternalDocument())
            updateWindowMenus();
    }

    private static void fireProjectsChanged() {
        for (IProjectsChangedListener projectsChangedListener : projectsChangedListeners)
            projectsChangedListener.doHasChanged();
    }

    /**
     * add a projects changed listener
     *
     * @param projectsChangedListener
     */
    public static void addProjectsChangedListener(IProjectsChangedListener projectsChangedListener) {
        projectsChangedListeners.add(projectsChangedListener);
    }

    /**
     * remove a projects changed listener
     *
     * @param projectsChangedListener
     */
    public static void removeProjectsChangedListener(IProjectsChangedListener projectsChangedListener) {
        projectsChangedListeners.remove(projectsChangedListener);
    }

    /**
     * update all window menus
     */
    public static void updateWindowMenus() {
        synchronized (projects) {
            for (final Pair<IDirector, JMenu> pair : dirAndWindowMenuPairs) {
                final JMenu menu = pair.getSecond();
                final int windowMenuBaseSize = menu2baseSize.get(menu);
                char mnenomicKey = '1';

                // remove all windows from menu:
                while (menu.getItemCount() > windowMenuBaseSize) {
                    menu.remove(windowMenuBaseSize);
                }

                for (final IDirector proj : projects) {
                    if (!proj.isInternalDocument()) {
                        final List<IDirectableViewer> viewers = viewersList.get(proj);
                        if (viewers != null) {
                            boolean first = true;
                            try {
                                for (final IDirectableViewer viewer : viewers) {
                                    if (viewer instanceof SearchManager)
                                        continue; // don't show search managers in menu
                                    final JFrame frame = viewer.getFrame();
                                    final AbstractAction action = new AbstractAction() {
                                        public void actionPerformed(ActionEvent e) {
                                            frame.setVisible(true);
                                            frame.setState(JFrame.NORMAL);
                                            frame.toFront();
                                        }
                                    };
                                    String title = frame.getTitle();
                                    int pos = title.indexOf(" - ");
                                    if (pos != -1)
                                        title = title.substring(0, pos);
                                    if (viewer instanceof IMainViewer && mnenomicKey <= '9') {
                                        action.putValue(AbstractAction.NAME, mnenomicKey + " " + title);
                                        action.putValue(AbstractAction.MNEMONIC_KEY, (int) mnenomicKey);
                                        mnenomicKey++;
                                    } else
                                        action.putValue(AbstractAction.NAME, "  " + title);
                                    action.putValue(AbstractAction.SMALL_ICON, ResourceManager.getIcon("Empty16.gif"));
                                    action.putValue(AbstractAction.SHORT_DESCRIPTION, "Bring to front: " + title);
                                    if (first) {
                                        menu.addSeparator();
                                        first = false;
                                    }
                                    menu.add(action);
                                }
                            } catch (ConcurrentModificationException ex) {
                                // System.err.println("ConcurrentModificationException");
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * gets the list of open projects
     *
     * @return projects
     */
    public static List<IDirector> getProjects() {
        return projects;
    }

    /**
     * get the project associated with the given project id
     *
     * @param pid
     * @return project
     */
    public static IDirector getProject(int pid) {
        synchronized (projects) {
            if (pid == NEWEST) {
                if (projects.size() > 0)
                    return projects.get(projects.size() - 1);
            } else {
                for (IDirector dir : projects) {
                    if (dir.getID() == pid)
                        return dir;
                }
            }
        }
        return null;
    }

    /**
     * gets the next assignable project ID
     *
     * @return next assignable ID
     */
    public static int getNextID() {
        return currentIDs.nextClearBit(1);
    }

    public static void setExitOnEmpty(boolean b) {
        exitOnEmpty = b;
    }

    public static boolean isExitOnEmpty() {
        return exitOnEmpty;
    }

    public static int getWindowMenuBaseSize(JMenu windowMenu) {
        Integer value = menu2baseSize.get(windowMenu);
        if (value == null)
            return 0;
        else
            return value;
    }

    /**
     * makes the file name unique
     *
     * @param name
     * @return unique version of file name
     */
    public static String getUniqueName(String name) {
        try {
            boolean ok = false;
            int i = 1;
            String newName = name;
            synchronized (projects) {
                while (!ok) {
                    ok = true;
                    for (IDirector dir : projects) {
                        if (i > 1) {
                            newName = Basic.getFileBaseName(name) + "-" + i + Basic.getFileSuffix(name);
                        }
                        String title = dir.getMainViewer().getTitle();
                        if (title != null && title.startsWith(newName)) {
                            ok = false;
                            break;
                        }
                    }
                    i++;
                }
            }
            return newName;
        } catch (Exception ex) { // if any thing goes  wrong, just return the original name
            return name;
        }
    }

    /**
     * attempt to quit program. If quit canceled and no projects open, opens a new empty document.
     * Programs that use this method for quitting must set setQuitting to false if the user chooses not to quit
     *
     * @param runOnQuitCanceled
     */
    public static void doQuit(final Runnable runJustBeforeQuit, final Runnable runOnQuitCanceled) {
        setQuitting(true);
        setExitOnEmpty(false);
        try {
            while (!projects.isEmpty() && isQuitting()) {
                synchronized (projects) {
                    // close in reverse order to save the geometry of older windows later
                    int oldSize = projects.size();
                    IDirector dir = projects.get(projects.size() - 1);
                    dir.close();
                    if (projects.size() == oldSize) // somehow failed to remove from list of projects...
                        projects.remove(projects.size() - 1);
                }
            }
            if (isQuitting()) {
                try {
                    if (runJustBeforeQuit != null)
                        runJustBeforeQuit.run();
                } catch (Exception ex) {
                    Basic.caught(ex);
                }
                ProgramProperties.store();
                System.exit(0);
            }
        } catch (CanceledException ex) {
        } finally {
            if (projects.isEmpty()) {
                runOnQuitCanceled.run();
            }
            setQuitting(false);
        }
    }

    public static boolean isQuitting() {
        return isQuitting;
    }

    public static void setQuitting(boolean quitting) {
        isQuitting = quitting;
    }

    private final static HashSet<String> previouslySelectedNodeLabels = new HashSet<>();

    public static Set<String> getPreviouslySelectedNodeLabels() {
        return previouslySelectedNodeLabels;
    }
}

