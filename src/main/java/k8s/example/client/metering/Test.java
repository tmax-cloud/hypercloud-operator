package k8s.example.client.metering;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import k8s.example.client.Constants;
import k8s.example.client.DataObject.User;

public class Test {
	public static CustomObjectsApi customObjectApi = new CustomObjectsApi();

	public static void main(String[] args) throws Exception {
		
		String token = "\"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJUbWF4LVByb0F1dGgiLCJpZCI6IndvbzQtdG1heC5jby5rciIsImV4cCI6MTU4NTg4OTE2NCwidXVpZCI6ImJjNDNkOTMwLTkwYTQtNDk3YS04YWM3LTk0ZDI4YmNmMGRkMyJ9.TQE3-OOBKWH69uIx8Q4Bnrx2giOOrOrnkfIhIVKM7lY\"";
		System.out.println( token.replaceAll("\"", ""));
	}

}
