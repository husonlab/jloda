/*
 * AService.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.fx.util;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.scene.layout.Pane;
import jloda.fx.control.ProgressPane;
import jloda.fx.window.NotificationManager;
import jloda.util.Basic;
import jloda.util.progress.ProgressListener;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * a generic service
 * Daniel Huson, 1.2018
 *
 * @param <T>
 */
public class AService<T> extends Service<T> {
	private TaskWithProgressListener<T> task;
	private Callable<T> callable;
	private Pane progressParentPane;
	private final ProgressPane progressPane;

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
		setProgressParentPane(progressParentPane);

		progressPane = new ProgressPane(this);
		progressPane.setVisible(true);

		this.runningProperty().addListener((c, o, n) -> {
			if (getProgressParentPane() != null) {
				RunAfterAWhile.apply(progressPane, () ->
						Platform.runLater(() -> {
							if (n) {
								if (!getProgressParentPane().getChildren().contains(progressPane))
									getProgressParentPane().getChildren().add(progressPane);
							} else {
								getProgressParentPane().getChildren().remove(progressPane);
							}
						}));
			}
		});
		setOnFailed(e -> NotificationManager.showError("Computation failed: " + Basic.getShortName(AService.this.getException().getClass())
													   + (AService.this.getException().getMessage() != null ? ": " + AService.this.getException().getMessage() : "")));
	}

	@Override
	protected TaskWithProgressListener<T> createTask() {
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

	public Pane getProgressParentPane() {
		return progressParentPane;
	}

	public void setProgressParentPane(Pane progressParentPane) {
		if (this.progressParentPane != null)
			this.progressParentPane.getChildren().remove(progressPane);
		this.progressParentPane = progressParentPane;
		//   if(progressParentPane!=null)
		//       progressParentPane.getChildren().add(progressPane);
	}

	public static <T> void run(Callable<T> callable, Consumer<T> runOnSucceeded, Consumer<Throwable> runOnFailed) {
		run(callable, runOnSucceeded, runOnFailed, null);
	}

	public static <T> void run(Callable<T> callable, Consumer<T> runOnSucceeded, Consumer<Throwable> runOnFailed, Pane progressParentPane) {
		var service = new AService<>(callable);
		if (progressParentPane != null)
			service.setProgressParentPane(progressParentPane);
		if (runOnSucceeded != null)
			service.setOnSucceeded(e -> runOnSucceeded.accept(service.getValue()));
		if (runOnFailed != null)
			service.setOnFailed(e -> runOnFailed.accept(service.getException()));
		service.start();
	}
}
