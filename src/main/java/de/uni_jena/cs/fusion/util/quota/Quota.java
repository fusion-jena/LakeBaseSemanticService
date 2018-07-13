package de.uni_jena.cs.fusion.util.quota;

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

public class Quota {

	private final long quota;
	private Integer used = 0;

	public Quota(long quota) {
		this.quota = quota;
	}

	/**
	 * Reserves one piece of the quota or waits, if the quota is exhausted.
	 * 
	 * @throws InterruptedException
	 */
	public void reserve() throws InterruptedException {
		boolean approval = false;
		while (!approval) {
			synchronized (used) {
				if (used < quota) {
					approval = true;
					used++;
				}
			}
			if (!approval) {
				// wait until an other thread released
				synchronized (this) {
					this.wait();
				}
			}
		}
	}

	/**
	 * Releases one piece of the quota and notifies a waiting thread.
	 */
	public void release() {
		synchronized (used) {
			used--;
		}
		synchronized (this) {
			this.notify();
		}
	}

}
