package k8s.example.client.metering;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class TimerMap {
	private List<Timer> timerList = new ArrayList<>();
	
	public void addTimerList( Timer timer ) {
		timerList.add(timer);
	}
}



