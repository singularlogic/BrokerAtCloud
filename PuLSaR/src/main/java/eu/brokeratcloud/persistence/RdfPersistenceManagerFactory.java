package eu.brokeratcloud.persistence;

public class RdfPersistenceManagerFactory {
	protected static RdfPersistenceManager instance;
	public static RdfPersistenceManager createRdfPersistenceManager() throws java.io.IOException {
		if (instance==null) instance = new RdfPersistenceManagerImpl();
		return instance;
	}
}
