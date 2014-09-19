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

		String data = "������Ĳ�����������";

		if (permission.check(url) != "error") {
			if (url.contains("jsp")) {
				data = "��������һ��jspҳ��";
			} else if (url.contains("html")) {
				data = "��������һ��htmlҳ��";
			} else if (url.contains(".txt")) {
				data = "��������һ��txt�ı�ҳ��";
			} else if (url.contains(".java")) {
				data = "��������һ��Java�ı�";
			}
		} else {
			data = "error";
		}
		return data;
	}
}
