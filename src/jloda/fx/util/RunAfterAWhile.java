/*
 * RunAfterAWhile.java Copyright (C) 2022 Daniel H. Huson
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

import jloda.util.Pair;

import java.util.*;

/**
 * executes a runnable with a delay after the last time a given key has been supplied
 * Daniel Huson, 11.2021
 */
public class RunAfterAWhile {
	private static final RunAfterAWhile instance;
	public static final long DELAY = 200L;

	static {
		instance = new RunAfterAWhile();
	}

	private final Map<Object, Pair<Long, Runnable>> keyTimeRunnableMap;

	private RunAfterAWhile() {
		keyTimeRunnableMap = new HashMap<>();

		var timer = new Timer(true);
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				synchronized (keyTimeRunnableMap) {
					var time = System.currentTimeMillis();
					var toDelete = new ArrayList<>();
					for (var entry : keyTimeRunnableMap.entrySet()) {
						if (time > entry.getValue().getFirst() + DELAY) {
							toDelete.add(entry.getKey());
							ProgramExecutorService.submit(entry.getValue().getSecond());
						}
					}
					toDelete.forEach(keyTimeRunnableMap.keySet()::remove);
				}
			}
		}, DELAY / 2, DELAY / 2);
	}

	/**
	 * apply to a given key and runnable pair. The runnable will be executed after a delay, unless the same key is submitted again
	 *
	 * @param key      the key
	 * @param runnable the runnable
	 */
	public static void apply(Object key, Runnable runnable) {
		synchronized (instance.keyTimeRunnableMap) {
			instance.keyTimeRunnableMap.put(key, new Pair<>(System.currentTimeMillis(), runnable));
		}
	}
}
