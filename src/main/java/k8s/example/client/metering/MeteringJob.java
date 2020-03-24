package k8s.example.client.metering;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import k8s.example.client.Constants;
import k8s.example.client.k8s.util.LogPreparedStatement;
import k8s.example.client.metering.models.Metering;
import k8s.example.client.metering.models.Metric;
import k8s.example.client.metering.models.MetricDataList;
import k8s.example.client.metering.util.UIDGenerator;

public class MeteringJob implements Job{
	
	private Map<String,Metering> meteringData = new HashMap<>();
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		MetricDataList cpu = getMeteringData("sum(kube_pod_container_resource_requests{resource=\"cpu\"})by(namespace)");
		for ( Metric metric : cpu.getResult() ) {
			if ( meteringData.containsKey(metric.getMetric().get("namespace"))) {
				meteringData.get(metric.getMetric().get("namespace")).setCpu(Double.parseDouble(metric.getValue().get(1)));
			} else {
				Metering metering = new Metering();
				metering.setNamespace(metric.getMetric().get("namespace"));
				metering.setCpu(Double.parseDouble(metric.getValue().get(1)));
				meteringData.put(metric.getMetric().get("namespace"), metering);
			}
		}
		MetricDataList memory = getMeteringData("sum(kube_pod_container_resource_requests{resource=\"memory\"})by(namespace)");
		for ( Metric metric : memory.getResult() ) {
			if ( meteringData.containsKey(metric.getMetric().get("namespace"))) {
				meteringData.get(metric.getMetric().get("namespace")).setMemory(Long.parseLong(metric.getValue().get(1)));
			} else {
				Metering metering = new Metering();
				metering.setNamespace(metric.getMetric().get("namespace"));
				metering.setMemory(Long.parseLong(metric.getValue().get(1)));
				meteringData.put(metric.getMetric().get("namespace"), metering);
			}
		}
		MetricDataList storage = getMeteringData("sum(kube_persistentvolumeclaim_resource_requests_storage_bytes)by(namespace)");
		for ( Metric metric : storage.getResult() ) {
			if ( meteringData.containsKey(metric.getMetric().get("namespace"))) {
				meteringData.get(metric.getMetric().get("namespace")).setStorage(Long.parseLong(metric.getValue().get(1)));
			} else {
				Metering metering = new Metering();
				metering.setNamespace(metric.getMetric().get("namespace"));
				metering.setStorage(Long.parseLong(metric.getValue().get(1)));
				meteringData.put(metric.getMetric().get("namespace"), metering);
			}
		}
		
		System.out.println( "============= Metering =============" );
		for( String key : meteringData.keySet() ){
			System.out.println( key + "/cpu : " + meteringData.get(key).getCpu() );
			System.out.println( key + "/memory : " + meteringData.get(key).getMemory() );
			System.out.println( key + "/storage : " + meteringData.get(key).getStorage() );
        }
		
		long time = System.currentTimeMillis();
		
		Connection conn;
		try {
			conn = getConnection();
			String query = "insert into metering.metering (id,namespace,cpu,memory,storage,public_ip,private_ip,metering_time,status) "
					+ "values (?,?,?,?,?,?,?,?,?)";
			LogPreparedStatement pstmt = new LogPreparedStatement( conn, query );
			for( String key : meteringData.keySet() ){
				pstmt.setString( 1, UIDGenerator.getInstance().generate32( context, 8, time ) );
				pstmt.setString( 2, key );
				pstmt.setDouble( 3, meteringData.get(key).getCpu() );
				pstmt.setLong( 4, meteringData.get(key).getMemory() );
				pstmt.setLong( 5, meteringData.get(key).getStorage() );
				pstmt.setInt( 6, meteringData.get(key).getPublicIp() );
				pstmt.setInt( 7, meteringData.get(key).getPrivateIp() );
				pstmt.setTimestamp( 8, new Timestamp(time) );
				pstmt.setString( 9, "Success" );
				pstmt.addBatch();
			}

			pstmt.executeBatch();
			
			pstmt.close();
			conn.close();
			
		} catch (SQLException e) {
			System.out.println("SQL Exception : " + e.getMessage());
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.out.println("Class Not Found Exection");
			e.printStackTrace();
		}
	}
	
	private MetricDataList getMeteringData(String query) {
		MetricDataList metricObject = null;
		try {
			OkHttpClient client = new OkHttpClient();
			URL url = new URL("http://prometheus-k8s.monitoring:9090/api/v1/query");
			HttpUrl.Builder httpBuilder = HttpUrl.get(url).newBuilder();
			httpBuilder.addQueryParameter("query", query);

			Request request = new Request.Builder()
					//.addHeader("x-api-key", RestTestCommon.API_KEY)
					.url(httpBuilder.build())
					.build();

			Response response = client.newCall(request).execute(); 
			String message = response.body().string();
			//System.out.println(message);

			JsonParser parser = new JsonParser();
			String metricData = parser.parse( message ).getAsJsonObject().get("data").toString();
			metricObject = new Gson().fromJson(metricData, MetricDataList.class);
			
		} catch (Exception e){
			System.out.println("Exception : " + e.getMessage());
		}
		return metricObject;
	}

	private Connection getConnection() throws SQLException, ClassNotFoundException {
		Class.forName( Constants.JDBC_DRIVER );
		return DriverManager.getConnection( Constants.DB_URL, Constants.USERNAME, System.getenv( "DB_PASSWORD" ) );
	}
}
