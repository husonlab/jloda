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

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.concurrent.Callable;

/**
 * a generic service
 * Daniel Huson, 1.2018
 *
 * @param <T>
 */
public class CallableService<T> extends Service<T> {
    private Callable<T> callable;

    public CallableService() {
        super();
        setExecutor(ProgramExecutorService.getInstance());
    }

    public CallableService(Callable<T> callable) {
        super();
        setExecutor(ProgramExecutorService.getInstance());
        setCallable(callable);
    }

    @Override
    protected Task<T> createTask() {
        return new Task<T>() {
            @Override
            protected T call() throws Exception {
                if (callable != null)
                    return callable.call();
                else
                    return null;
            }
        };
    }

    public Callable<T> getCallable() {
        return callable;
    }

    public void setCallable(Callable<T> callable) {
        this.callable = callable;
    }
}
