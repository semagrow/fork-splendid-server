package de.uni_koblenz.west.splendid;

import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.apache.commons.httpclient.HttpClient;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.Graph;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.repository.manager.RepositoryInfo;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryFactory;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.config.RepositoryRegistry;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.n3.N3Writer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_koblenz.west.splendid.test.config.ConfigurationException;

public class SplendidRepositoryManager extends RepositoryManager {

	Logger log = LoggerFactory.getLogger(this.getClass());

	private Repository repo = null;

  private String configFile = "/etc/splendid/repository.ttl";

	@Override
	public Repository getRepository(String id) {
		if (this.repo == null) {
			try {
				this.repo = getRepositoryInstance(loadRepositoryConfig(configFile));
				this.repo.initialize();
			}catch(ConfigurationException e) {
					logger.error("Configuration error: " + e.getMessage());
			}catch(RepositoryException e) {
					logger.error(e.getMessage());
			}
		}
		return this.repo;
	}

	@Override
	protected void cleanUpRepository(String arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	protected Repository createRepository(String arg0) throws RepositoryConfigException, RepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Repository createSystemRepository() throws RepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<RepositoryInfo> getAllRepositoryInfos(boolean arg0) throws RepositoryException {
		// TODO Auto-generated method stub
		return null;
	}


	public HttpClient getHttpClient() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URL getLocation() throws MalformedURLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RepositoryInfo getRepositoryInfo(String arg0) throws RepositoryException {
		// TODO Auto-generated method stub
		return null;
	}


	public void setHttpClient(HttpClient arg0) {
		// TODO Auto-generated method stub

	}


	/**
	 * Loads the repository configuration model from the specified configuration file.
	 *
	 * @param configFile The file which contains the configuration data.
	 * @return The configuration data model.
	 * @throws ConfigurationException If the configuration data is invalid or incomplete.
	 */
	private Graph loadRepositoryConfig(String configFile) throws ConfigurationException {
		File file = new File(configFile);
		String baseURI = file.toURI().toString();
		RDFFormat format = Rio.getParserFormatForFileName(configFile);
		if (format == null)
			throw new ConfigurationException("unknown RDF format of repository config: " + file);

		try {
			Graph model = new GraphImpl();
			RDFParser parser = Rio.createParser(format);
			parser.setRDFHandler(new StatementCollector(model));
			parser.parse(new FileReader(file), baseURI);
			return model;

		} catch (UnsupportedRDFormatException e) {
			throw new ConfigurationException("cannot load repository config, unsupported RDF format (" + format + "): " + file);
		} catch (RDFParseException e) {
			throw new ConfigurationException("cannot load repository config, RDF parser error: " + e.getMessage() + ": " + file);
		} catch (RDFHandlerException e) {
			throw new ConfigurationException("cannot load repository config, RDF handler error: " + e.getMessage() + ": " + file);
		} catch (IOException e) {
			throw new ConfigurationException("cannot load repository config, IO error: " + e.getMessage());
		}
	}

	/**
	 * Returns a (un-initialized) Repository instance that has been configured
	 * based on the supplied configuration data.
	 *
	 * @param configuration The repository configuration data.
	 * @return The created (but un-initialized) repository.
	 * @throws ConfigurationException If no repository could be created due to
	 *         invalid or incomplete configuration data.
	 */
	private Repository getRepositoryInstance(Graph configuration) throws ConfigurationException {

		RepositoryConfig repoConfig = null;
		try {

			// read configuration
			repoConfig = RepositoryConfig.create(configuration, null);
			repoConfig.validate();
			RepositoryImplConfig repoImplConfig = repoConfig.getRepositoryImplConfig();

			// initialize repository factory
			RepositoryRegistry registry = RepositoryRegistry.getInstance();
			RepositoryFactory factory = registry.get(repoImplConfig.getType());
			if (factory == null) {
				throw new ConfigurationException("Unsupported repository type: "
						+ repoImplConfig.getType()
						+ " in repository definition (id:" + repoConfig.getID()
						+ ", title:" + repoConfig.getTitle() + ")");
			}

			// create repository
			return factory.getRepository(repoImplConfig);

		} catch (RepositoryConfigException e) {
			String reason = "error creating repository";
			if (repoConfig != null)
				reason += " (id:" + repoConfig.getID() + ", title:" + repoConfig.getTitle() + ")";
			throw new ConfigurationException(reason + ": " + e.getMessage());
		}
	}


}
