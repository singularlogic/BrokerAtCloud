package eu.brokeratcloud.common.user;

import eu.brokeratcloud.common.BrokerObject;
import eu.brokeratcloud.persistence.annotations.*;

@RdfSubject(
	name="USER"
)
public class User extends BrokerObject {
	public String toString() {
		return 	"User: {\n"+super.toString()+"}\n";
	}
}
