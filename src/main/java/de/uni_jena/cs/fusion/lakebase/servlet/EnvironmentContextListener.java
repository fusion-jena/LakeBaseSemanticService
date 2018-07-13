package de.uni_jena.cs.fusion.lakebase.servlet;

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

import java.io.IOException;

import javax.naming.InitialContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_jena.cs.fusion.lakebase.Environment;

public class EnvironmentContextListener implements ServletContextListener {

	private static final Logger log = LoggerFactory.getLogger(EnvironmentContextListener.class);

	@SuppressWarnings({ "resource" })
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext context = sce.getServletContext();
		try {
			DataSource dataSource = (DataSource) InitialContext.doLookup("java:/comp/env/jdbc/lakebase");
			context.setAttribute("environment", new Environment(dataSource));
		} catch (Exception e) {
			log.error("Failed to initialize environment.", e);
			try {
				((Environment) context.getAttribute("environment")).close();
			} catch (IOException | NullPointerException e1) {
				log.error("Failed to close environment.", e1);
				throw new RuntimeException(e);
			}
			throw new RuntimeException(e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		ServletContext context = sce.getServletContext();
		try {
			Environment environment = (Environment) context.getAttribute("environment");
			if (environment != null) {
				environment.close();
			}
		} catch (IOException e) {
			log.error("Failed to close environment.", e);
		}
	}

}
