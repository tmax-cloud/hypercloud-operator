package k8s.example.client.metering;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;

import k8s.example.client.Util;

public class Test {

	public static void main(String[] args) throws Exception {
		System.out.println(System.currentTimeMillis());
		System.out.println(new Timestamp(System.currentTimeMillis()));
		
	    Date d = new Date(System.currentTimeMillis());
	    Calendar cal = Calendar.getInstance();
	    cal.setTime(d);
	    cal.set(Calendar.MONTH, 0);
	    cal.set(Calendar.DAY_OF_MONTH, 1);
	    cal.set(Calendar.HOUR_OF_DAY, 0);
	    cal.set(Calendar.MINUTE, 0);
	    cal.set(Calendar.SECOND, 0);
	    cal.set(Calendar.MILLISECOND, 0);

		System.out.println(cal.getTime().getTime());
	    System.out.println(new Timestamp(cal.getTime().getTime()));
	    
	    
	    System.out.println(Util.Crypto.encryptSHA256("tmax@23" + "cloud@tmax.co.kr" + "618a8891-fcda-4cc7-9757-036d43a6403b"));

	}

}
