package eu.brokeratcloud.common.user;

import eu.brokeratcloud.persistence.annotations.*;

@RdfSubject(
	name="CONSUMER"
)
public class Consumer extends User implements ServiceConsumer {
	public String toString() {
		return 	"Consumer: "+super.toString();
	}
}
