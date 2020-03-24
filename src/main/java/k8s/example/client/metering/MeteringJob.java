package k8s.example.client.metering;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
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
	long time = System.currentTimeMillis();
	Connection conn = null;
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		try {
			conn = getConnection();
		} catch (SQLException e) {
			System.out.println("SQL Exception : " + e.getMessage());
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.out.println("Class Not Found Exection");
			e.printStackTrace();
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		System.out.println( "============= Metring TIME =============" );
		System.out.println( "minute of hour	 : " + calendar.get(Calendar.MINUTE) );
		System.out.println( "hour of day	 : " + calendar.get(Calendar.HOUR_OF_DAY) );
		System.out.println( "day of month	 : " + calendar.get(Calendar.DAY_OF_MONTH) );
		System.out.println( "day of year	 : " + calendar.get(Calendar.DAY_OF_YEAR) );
		
		if ( calendar.get(Calendar.MINUTE) == 0 ) {
			// Insert to metering_hour
			insertMeteringHour();
		} else if ( calendar.get(Calendar.HOUR_OF_DAY) == 0 ) {
			// Insert to metering_day
			insertMeteringDay();
		} else if ( calendar.get(Calendar.DAY_OF_MONTH) == 1 ) {
			// Insert to metering_month
		} else if ( calendar.get(Calendar.DAY_OF_YEAR) == 1 ) {
			// Insert to metering_year
		}
		
		
		// Insert to metering (new data)
		makeMeteringMap();
		System.out.println( "============= Metering =============" );
		for( String key : meteringData.keySet() ){
			System.out.println( key + "/cpu : " + meteringData.get(key).getCpu() );
			System.out.println( key + "/memory : " + meteringData.get(key).getMemory() );
			System.out.println( key + "/storage : " + meteringData.get(key).getStorage() );
        }
		insertMeteringData();
		
	}
	
	@SuppressWarnings("resource")
	private void insertMeteringDay() {
		try {
			String insertQuery = "insert into metering.metering_day (" + 
					"select id, namespace, truncate(sum(cpu),2) as cpu, sum(memory) as memory, sum(storage) as storage, " + 
					"sum(public_ip) as public_ip, sum(private_ip) as private_ip, " + 
					"date_format(metering_time,'%Y-%m-%d 00:00:00') as metering_time, status from metering.metering_hour " + 
					"group by day(metering_time), namespace" + 
					")";
			LogPreparedStatement pstmt = new LogPreparedStatement( conn, insertQuery );
			pstmt.execute();
			
			String deleteQuery = "truncate metering.metering_hour";
			pstmt = new LogPreparedStatement( conn, deleteQuery );
			pstmt.execute();
			
			pstmt.close();
			conn.commit();

		} catch (SQLException e) {
			System.out.println("SQL Exception : " + e.getMessage());
			e.printStackTrace();
		}
	}
		
	@SuppressWarnings("resource")
	private void insertMeteringHour() {
		try {
			String insertQuery = "insert into metering.metering_hour (" + 
					"select id, namespace, truncate(sum(cpu)/count(*),2) as cpu, truncate(sum(memory)/count(*),0) as memory, truncate(sum(storage)/count(*),0) as storage, " + 
					"truncate(sum(public_ip)/count(*),0) as public_ip, truncate(sum(private_ip)/count(*),0) as private_ip, " + 
					"date_format(metering_time,'%Y-%m-%d %H:00:00') as metering_time, status from metering.metering " + 
					"group by hour(metering_time), namespace" + 
					")";
			LogPreparedStatement pstmt = new LogPreparedStatement( conn, insertQuery );
			pstmt.execute();
			
			String deleteQuery = "truncate metering.metering";
			pstmt = new LogPreparedStatement( conn, deleteQuery );
			pstmt.execute();
			
			pstmt.close();
			conn.commit();
			
		} catch (SQLException e) {
			System.out.println("SQL Exception : " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private void insertMeteringData() {
		try {
			String query = "insert into metering.metering (id,namespace,cpu,memory,storage,public_ip,private_ip,metering_time,status) "
					+ "values (?,?,truncate(?,2),?,?,?,?,?,?)";
			LogPreparedStatement pstmt = new LogPreparedStatement( conn, query );
			for( String key : meteringData.keySet() ){
				pstmt.setString( 1, UIDGenerator.getInstance().generate32( meteringData.get(key), 8, time ) );
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
			conn.commit();
			conn.close();
		} catch (SQLException e) {
			System.out.println("SQL Exception : " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private void makeMeteringMap() {
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
