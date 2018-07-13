package de.uni_jena.cs.fusion.util.maintainer;

/*-
 * #%L
 * LakeBase Semantic Service
 * %%
 * Copyright (C) 2018 Heinz Nixdorf Chair for Distributed Information Systems, Friedrich Schiller University Jena
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Maintainer implements Closeable {

	private final static Logger log = LoggerFactory.getLogger(Maintainer.class);

	private final ScheduledExecutorService executer;
	private final Vector<Maintainable> clients;
	private final Optional<String> name;

	private ScheduledFuture<?> future;

	public Maintainer(ScheduledExecutorService executer) {
		this(executer, null);
	}
	
	public Maintainer(ScheduledExecutorService executer, String name) {
		this.executer = executer;
		this.clients = new Vector<Maintainable>();
		this.name = Optional.ofNullable(name);
	}

	public Maintainer register(Maintainable client) {
		clients.add(client);
		return this;
	}

	public Maintainer schedule(long period, TimeUnit unit) {
		this.cancel();
		this.future = this.executer.scheduleWithFixedDelay(new MaintenanceTask(), period, period, unit);
		return this;
	}

	public Maintainer cancel() {
		if (this.future != null) {
			this.future.cancel(false);
		}
		return this;
	}

	private class MaintenanceTask implements Runnable {
		@Override
		public void run() {
			Maintainer.log.info("Maintenance ({}) started.", name.orElse("unnamed"));
			for (Maintainable client : clients) {
				try {
					client.maintain();
				} catch (MaintenanceException e) {
					Maintainer.log.warn("Maintenance ({}) of \"{}\" failed.", name.orElse("unnamed"), client, e);
				}
			}
			Maintainer.log.info("Maintenance ({}) completed.", name.orElse("unnamed"));
		}
	}

	@Override
	public void close() throws IOException {
		this.cancel();
	}
}
