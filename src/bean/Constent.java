package bean;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class Constent {

	public static final int port = 80;
	public static final int neiport = 8080;
	public static final Charset charest = Charset.forName("gbk");
	public static final CharsetDecoder decoder = charest.newDecoder();
	public static final int beat = 1;
	public static final int task = 2;
	public static final int response = 3;

}
