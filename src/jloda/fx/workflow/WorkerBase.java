/*
 * WorkerBase.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.fx.workflow;

import javafx.beans.property.*;
import javafx.concurrent.Worker;

public abstract class WorkerBase implements Worker<Boolean> {
	private final BooleanProperty running = new SimpleBooleanProperty(false);
	private final ObjectProperty<Worker.State> state = new SimpleObjectProperty<>(Worker.State.READY);
	private final ObjectProperty<Boolean> value = new SimpleObjectProperty<>(true);
	private final ObjectProperty<Throwable> exception = new SimpleObjectProperty<>(null);
	private final DoubleProperty workDone = new SimpleDoubleProperty(0);
	private final DoubleProperty totalWork = new SimpleDoubleProperty(0);
	private final DoubleProperty progress = new SimpleDoubleProperty(0);
	private final StringProperty message = new SimpleStringProperty(null);
	private final StringProperty title = new SimpleStringProperty();

	@Override
	public boolean isRunning() {
		return running.get();
	}

	@Override
	public ReadOnlyBooleanProperty runningProperty() {
		return running;
	}

	@Override
	public Worker.State getState() {
		return state.get();
	}

	@Override
	public ReadOnlyObjectProperty<Worker.State> stateProperty() {
		return state;
	}

	@Override
	public Boolean getValue() {
		return value.get();
	}

	@Override
	public ReadOnlyObjectProperty<Boolean> valueProperty() {
		return value;
	}

	@Override
	public Throwable getException() {
		return exception.get();
	}

	@Override
	public ReadOnlyObjectProperty<Throwable> exceptionProperty() {
		return exception;
	}

	@Override
	public double getWorkDone() {
		return workDone.get();
	}

	@Override
	public ReadOnlyDoubleProperty workDoneProperty() {
		return workDone;
	}

	@Override
	public double getTotalWork() {
		return totalWork.get();
	}

	@Override
	public ReadOnlyDoubleProperty totalWorkProperty() {
		return totalWork;
	}

	@Override
	public double getProgress() {
		return progress.get();
	}

	@Override
	public ReadOnlyDoubleProperty progressProperty() {
		return progress;
	}

	@Override
	public String getMessage() {
		return message.get();
	}

	@Override
	public ReadOnlyStringProperty messageProperty() {
		return message;
	}

	@Override
	public String getTitle() {
		return title.get();
	}

	@Override
	public StringProperty titleProperty() {
		return title;
	}

	@Override
	abstract public boolean cancel();

	protected void setRunning(boolean running) {
		this.running.set(running);
	}

	protected void setState(State state) {
		this.state.set(state);
	}

	protected void setValue(Boolean value) {
		this.value.set(value);
	}

	protected void setException(Throwable exception) {
		this.exception.set(exception);
	}

	protected void setWorkDone(double workDone) {
		this.workDone.set(workDone);
	}

	protected void setTotalWork(double totalWork) {
		this.totalWork.set(totalWork);
	}

	protected void setProgress(double progress) {
		this.progress.set(progress);
	}

	protected void setMessage(String message) {
		this.message.set(message);
	}

	protected void setTitle(String title) {
		this.title.set(title);
	}
}
