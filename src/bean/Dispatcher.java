package bean;

import intaface.Permission;

public class Dispatcher {

	String url;
	Permission permission;
	HttpBean request;

	public Dispatcher(HttpBean hb, Permission permission) {
		this.request = hb;
		this.url = hb.getUrl();
		this.permission=permission;
	}

	//
	public String dispatcher() {

		String data = "你请求的不包含特殊标记";

		if (permission.check(url) != "error") {
			if (url.contains("jsp")) {
				data = "你请求了一个jsp页面";
			} else if (url.contains("html")) {
				data = "你请求了一个html页面";
			} else if (url.contains(".txt")) {
				data = "你请求了一个txt文本页面";
			} else if (url.contains(".java")) {
				data = "你请求了一个Java文本";
			}
		} else {
			data = "error";
		}
		return data;
	}
}
