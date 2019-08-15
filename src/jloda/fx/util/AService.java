/*
 * AService.java Copyright (C) 2019. Daniel H. Huson
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

package jloda.fx.util;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.layout.Pane;
import jloda.fx.control.ProgressPane;
import jloda.fx.window.NotificationManager;
import jloda.util.ProgressListener;

import java.util.concurrent.Callable;

/**
 * a generic service
 * Daniel Huson, 1.2018
 *
 * @param <T>
 */
public class AService<T> extends Service<T> {
    private TaskWithProgressListener<T> task;
    private Callable<T> callable;

    public AService() {
        this(null, null);
    }

    public AService(Callable<T> callable) {
        this(callable, null);
    }

    public AService(final Pane progressParentPane) {
        this(null, progressParentPane);
    }

    public AService(Callable<T> callable, final Pane progressParentPane) {
        super();
        setExecutor(ProgramExecutorService.getInstance());
        setCallable(callable);

        if (progressParentPane != null) {
            final ProgressPane progressPane = new ProgressPane(this);
            this.runningProperty().addListener((c, o, n) -> {
                if (n)
                    progressParentPane.getChildren().add(progressPane);
                else
                    progressParentPane.getChildren().remove(progressPane);
            });
        }

        setOnFailed(e -> NotificationManager.showError("Computation failed: " + AService.this.getException().getMessage()));
    }

    @Override
    protected Task<T> createTask() {
        task = new TaskWithProgressListener<>() {
            @Override
            public T call() throws Exception {
                return callable.call();
            }
        };
        return task;
    }

    public ProgressListener getProgressListener() {
        return (task != null ? task.getProgressListener() : null);
    }

    public void setCallable(Callable<T> callable) {
        this.callable = callable;
    }

    public Callable<T> getCallable() {
        return callable;
    }
}
