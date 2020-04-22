package k8s.example.client.metering;

import java.util.ArrayList;
import java.util.List;

import io.kubernetes.client.openapi.models.V1Namespace;

public class TimerMap {
	private static List<V1Namespace> nsTimerList = new ArrayList<>();
	
	public static void addTimerList( V1Namespace nameSpace ) {
		nsTimerList.add(nameSpace);
	}
	
	public static boolean isExists( V1Namespace nameSpace ) {
		if (nsTimerList.contains(nameSpace) ) return true;
		return false;
	}
	
	public static List<V1Namespace> getTimerList() {
		return nsTimerList;
	}
}



