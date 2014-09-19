package intefaceImp;

import intaface.Permission;

public class NonPermission implements Permission {

	@Override
	public String check(String url) {
		// TODO Auto-generated method stub
		return "true";
	}

}
