package eu.brokeratcloud.common;

public class Util {
	public static boolean checkBoolean(String value, boolean defValue) {
		if (value==null || value.trim().equals("")) return defValue;
		value = value.trim().toLowerCase();
		if (value.equals("yes") || value.equals("true") || value.equals("on") || value.equals("1")) return true;
		if (value.equals("no") || value.equals("false") || value.equals("off") || value.equals("0")) return false;
		return defValue;
	}
}