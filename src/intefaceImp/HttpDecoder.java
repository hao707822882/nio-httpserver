package intefaceImp;

import intaface.Decoder;
import bean.HttpBean;

public class HttpDecoder implements Decoder {

	HttpBean hb;

	@Override
	public Object decode(String content) {
		hb = new HttpBean();
		String[] data = content.split("\r\n");
		System.out.print("第一行是*****");
		System.out.println(data[0]);
		String[] head1 = data[0].split(" ");
		String[] attr = null;
		String[] attrs = null;
		if (head1[1].contains("?")) {
			attr = head1[1].split("&");
			hb.setUrl(attr[0]);
		} else {
			hb.setUrl(head1[1]);
		}
		if (head1[1].contains("&")) {
			attrs = attr[1].split("=");
			for (int i = 0; i < attrs.length; i++) {
				hb.insertAttr(attrs[i], attrs[i + 1]);
			}
		}
		System.out.println(data.length);
		return hb;
	}

}
