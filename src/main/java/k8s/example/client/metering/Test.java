package k8s.example.client.metering;

import java.util.Calendar;

import io.kubernetes.client.openapi.apis.CustomObjectsApi;

public class Test {
	public static CustomObjectsApi customObjectApi = new CustomObjectsApi();

	public static void main(String[] args) throws Exception {
		long time = System.currentTimeMillis();

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		
		System.out.println( "============= User Clear Time =============" );
		System.out.println( "minute of hour	 : " + calendar.get(Calendar.MINUTE) );
		System.out.println( "what day	 : " + calendar.get(Calendar.DAY_OF_WEEK) );
		System.out.println( "hour of day	 : " + calendar.get(Calendar.HOUR_OF_DAY) );
		System.out.println( "day of month	 : " + calendar.get(Calendar.DAY_OF_MONTH) );
		System.out.println( "day of year	 : " + calendar.get(Calendar.DAY_OF_YEAR) );
	}

}
