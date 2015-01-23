package eu.brokeratcloud.common.user;

import eu.brokeratcloud.persistence.annotations.*;

@RdfSubject(
	name="PROVIDER"
)
public class Provider extends User implements ServiceProvider {
	public String toString() {
		return 	"Provider: "+super.toString();
	}
}
