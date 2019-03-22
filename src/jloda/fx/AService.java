/*
 *  Copyright (C) 2015 Daniel H. Huson
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

package jloda.fx;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.layout.Pane;
import jloda.util.ProgressListener;

import java.util.concurrent.Callable;

/**
 * a generic service
 * Daniel Huson, 1.2018
 *
 * @param <T>
 */
public class AService<T> extends Service<T> {
    private Callable<T> callable;
    private final TaskWithProgressListener<T> task;

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
        task = new TaskWithProgressListener<T>() {
            @Override
            public T call() throws Exception {
                return AService.this.callable != null ? AService.this.callable.call() : null;
            }
        };

        if (progressParentPane != null) {
            final ProgressPane progressPane = new ProgressPane(this);
            this.runningProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> c, Boolean o, Boolean n) {
                    if (n)
                        progressParentPane.getChildren().add(progressPane);
                    else
                        progressParentPane.getChildren().remove(progressPane);
                }
            });
        }

        setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent e) {
                NotificationManager.showError("Computation failed: " + AService.this.getException().getMessage());
            }
        });

    }

    @Override
    protected Task<T> createTask() {
        return task;
    }

    public ProgressListener getProgressListener() {
        return (task != null ? task.getProgressListener() : null);
    }

    public Callable<T> getCallable() {
        return callable;
    }

    public void setCallable(Callable<T> callable) {
        this.callable = callable;
    }
}
