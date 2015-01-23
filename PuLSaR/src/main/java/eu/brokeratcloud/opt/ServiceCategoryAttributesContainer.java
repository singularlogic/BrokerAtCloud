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
