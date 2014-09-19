package util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import bean.Constent;

public class rwUtil {

	public static String readFromByteBuffer2Str(ByteBuffer bt){
		CharBuffer cb=	Constent.charest.decode(bt);
		return cb.toString();
	}
	
}
