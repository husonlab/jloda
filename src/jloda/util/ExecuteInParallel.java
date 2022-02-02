/*
 * ExecuteInParallel.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.util;

import jloda.util.progress.ProgressListener;
import jloda.util.progress.ProgressSilent;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * run jobs in parallel on a fixed number of threads
 * Daniel Huson, 9.2020
 */
public class ExecuteInParallel {
    /**
     * run a collection of jobs and collect the results
     */
    public static <S, T> void apply(Collection<S> jobs, FunctionWithException<S, Collection<T>> computation, Collection<T> results, int numberOfCores) throws Exception {
        apply(jobs, computation, results, numberOfCores, new ProgressSilent());
    }

    /**
     * run a collection of jobs and collect the results
     */
    public static <S, T> void apply(Collection<S> jobs, FunctionWithException<S, Collection<T>> computation, Collection<T> results, int numberOfCores, ProgressListener progress) throws Exception {
		final Single<Exception> exception = new Single<>();
		final ConcurrentLinkedQueue<T> queue = new ConcurrentLinkedQueue<>();

        final ExecutorService service = Executors.newFixedThreadPool(numberOfCores);
        try {
            progress.setMaximum(jobs.size());
            progress.setProgress(0);
            jobs.forEach(job -> service.submit(() -> {
                if (exception.isNull()) {
                    try {
						queue.addAll(computation.apply(job));
						progress.incrementProgress();
                    } catch (Exception e) {
                        exception.setIfCurrentValueIsNull(e);
                    }
                }
            }));
            service.shutdown();
            service.awaitTermination(1000, TimeUnit.DAYS);
        } catch (Exception e) {
            exception.setIfCurrentValueIsNull(e);
        } finally {
            service.shutdownNow();
        }
        if (exception.isNotNull())
            throw exception.get();
		results.addAll(queue);
    }


	/**
     * run a collection of jobs
     */
    public static <S, T> void apply(Collection<S> jobs, ConsumerWithException<S> computation, int numberOfCores) throws Exception {
        apply(jobs, computation, numberOfCores, new ProgressSilent());
    }

    /**
     * run a collection of jobs
     */
    public static <S, T> void apply(Collection<S> jobs, ConsumerWithException<S> computation, int numberOfCores, ProgressListener progress) throws Exception {
        progress.setMaximum(jobs.size());
        progress.setProgress(0);
        if(jobs.size()==1)
            computation.accept(jobs.iterator().next());
        else if(jobs.size()>1) {
            final Single<Exception> exception = new Single<>();
            final ExecutorService service = Executors.newFixedThreadPool(Math.max(1, numberOfCores));
            try {
                 jobs.forEach(job -> service.submit(() -> {
                    if (exception.isNull()) {
                        try {
                            computation.accept(job);
                            synchronized (progress) {
                                progress.incrementProgress();
                            }
                        } catch (Exception e) {
                            exception.setIfCurrentValueIsNull(e);
                        }
                    }
                }));
                service.shutdown();
                service.awaitTermination(1000, TimeUnit.DAYS);
            } catch (Exception e) {
                exception.setIfCurrentValueIsNull(e);
            } finally {
                service.shutdownNow();
            }
            if (exception.isNotNull())
                throw exception.get();
        }
        progress.reportTaskCompleted();
    }

    public interface FunctionWithException<S, T> {
        T apply(S input) throws CanceledException;
    }

    public interface ConsumerWithException<S> {
        void accept(S input) throws IOException;
    }
}
