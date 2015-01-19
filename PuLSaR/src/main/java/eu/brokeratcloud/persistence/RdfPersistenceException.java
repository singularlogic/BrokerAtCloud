package eu.brokeratcloud.persistence;

public class RdfPersistenceException extends RuntimeException {
	public RdfPersistenceException() { super(); }
	public RdfPersistenceException(String mesg) { super(mesg); }
	public RdfPersistenceException(Throwable e) { super(e); }
	public RdfPersistenceException(String mesg, Throwable e) { super(mesg, e); }
}