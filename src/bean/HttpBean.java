package bean;

import java.util.Map;

public class HttpBean {

	private String url;
	private boolean iswebsocket;
	private Map attribute;

	public void insertAttr(String name, String value) {
		this.attribute.put(name, value);
	}

	public String getAttr(String name) {
		return (String) this.attribute.get(name);
	}

	public String getUrl() {
		return url;
	}

	public boolean isIswebsocket() {
		return iswebsocket;
	}

	public Map getAttribute() {
		return attribute;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setIswebsocket(boolean iswebsocket) {
		this.iswebsocket = iswebsocket;
	}

	public void setAttribute(Map attribute) {
		this.attribute = attribute;
	}

}
