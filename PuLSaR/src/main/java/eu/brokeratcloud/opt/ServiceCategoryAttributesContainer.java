/*
 * #%L
 * Preference-based cLoud Service Recommender (PuLSaR) - Broker@Cloud optimisation engine
 * %%
 * Copyright (C) 2014 - 2016 Information Management Unit, Institute of Communication and Computer Systems, National Technical University of Athens
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
package eu.brokeratcloud.opt;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceCategoryAttributesContainer {
	@XmlAttribute
	protected String serviceCategory;
	@XmlAttribute
	protected String serviceCategoryName;
	@XmlAttribute
	protected ServiceCategoryAttribute[] serviceCategoryAttributes;
	
	public String getServiceCategory() { return serviceCategory; }
	public String getServiceCategoryName() { return serviceCategoryName; }
	public ServiceCategoryAttribute[] getServiceCategoryAttributes() { return serviceCategoryAttributes; }
	public void setServiceCategory(String s) { serviceCategory = s; }
	public void setServiceCategoryName(String s) { serviceCategoryName = s; }
	public void setServiceCategoryAttributes(ServiceCategoryAttribute[] a) { serviceCategoryAttributes = a; }
	
	public String toString() {
		return 	"ServiceCategoryAttributesContainer: {\n"+
				"\tservice-category = ("+serviceCategory+") "+serviceCategoryName+"\n\tattributes = "+java.util.Arrays.deepToString(serviceCategoryAttributes)+
				"}\n";
	}
}
