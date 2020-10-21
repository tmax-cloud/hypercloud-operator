package k8s.example.client.metering;

import java.util.Random;

public class Test {

	public static void main(String[] args) throws Exception {
        
		String msg = "[Web발신]\n" +
				"[인증번호:8094] - HyperAuth\n" +
				"(타인노출금지)";
		System.out.println(msg.substring(14,18));


	}
}
