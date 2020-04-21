package k8s.example.client.metering;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.joda.time.DateTime;

public class Test {

	public static void main(String[] args) throws Exception {
		Date timeToRun = new Date(System.currentTimeMillis() + 5000);
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
