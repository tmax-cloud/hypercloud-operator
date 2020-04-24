package k8s.example.client.metering;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.joda.time.DateTime;

public class Test {

	public static void main(String[] args) throws Exception {
		Calendar cal = Calendar.getInstance();
		cal.set(2020,3,22,10,52,0);
		Date timeToRun = new Date(cal.getTimeInMillis());
		Timer timer = new Timer("061d9446-39f2-4a8d-a091-038d0ff32fc0#catalog");

		timer.schedule(new TimerTask() {
			public void run() {
				System.out.println("wake!!!!!!!!!!!!!");
				System.out.println(Thread.currentThread().getName());
			}
		}, timeToRun);

		timer.schedule(new TimerTask() {
			public void run() {
				System.out.println("wake!!!!!!!!!!!!!");
				System.out.println(Thread.currentThread().getName());
			}
		}, timeToRun);

	}
}
