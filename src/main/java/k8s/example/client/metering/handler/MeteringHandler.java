package k8s.example.client.metering.handler;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD.GeneralHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;
import io.kubernetes.client.openapi.ApiException;
import k8s.example.client.Constants;
import k8s.example.client.ErrorCode;
import k8s.example.client.StringUtil;
import k8s.example.client.DataObject.Client;
import k8s.example.client.DataObject.LoginInDO;
import k8s.example.client.DataObject.Token;
import k8s.example.client.DataObject.UserCR;
import k8s.example.client.Util;
import k8s.example.client.k8s.K8sApiCaller;
import k8s.example.client.k8s.util.LogPreparedStatement;
import k8s.example.client.metering.models.Metering;
import k8s.example.client.metering.util.SimpleUtil;

public class MeteringHandler extends GeneralHandler {
	@Override
    public Response get(
      UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		System.out.println("***** GET /metering");
		
		// Get Query Parameter
		String offset = SimpleUtil.getQueryParameter( session.getParameters(), Constants.QUERY_PARAMETER_OFFSET );
		String limit = SimpleUtil.getQueryParameter( session.getParameters(), Constants.QUERY_PARAMETER_LIMIT );
		String namespace = SimpleUtil.getQueryParameter( session.getParameters(), Constants.QUERY_PARAMETER_NAMESPACE );
		String timeUnit = SimpleUtil.getQueryParameter( session.getParameters(), Constants.QUERY_PARAMETER_TIMEUNIT );
		String startTime = SimpleUtil.getQueryParameter( session.getParameters(), Constants.QUERY_PARAMETER_STARTTIME );
		String endTime = SimpleUtil.getQueryParameter( session.getParameters(), Constants.QUERY_PARAMETER_ENDTIME );
		List<String> sort = SimpleUtil.getQueryParameterArray( session.getParameters(), Constants.QUERY_PARAMETER_SORT );

		if ( StringUtil.isEmpty(timeUnit) || !(timeUnit.equals("hour")||timeUnit.equals("day")||timeUnit.equals("month")||timeUnit.equals("year")) ) {
			timeUnit = "day"; // default time unit
		}
		StringBuilder sb = new StringBuilder();
		makeTimeRange( timeUnit, startTime, endTime, sb );
		
		if ( StringUtil.isNotEmpty( namespace ) ){
			sb.append( " and namespace like '%" + namespace + "%'" );
		}
		if ( sort != null && sort.size() > 0 ){
			sb.append( " order by " );
			for ( String s : sort ) {
				String order = " asc, ";
				if (s.charAt(0) == '-') {
					order = " desc, ";
					s = s.substring(1, s.length());
				}
				sb.append(s);
				sb.append(order);
			}
			sb.append( "metering_time desc" );
		} else {
			sb.append( " order by metering_time desc" );
		}
		if ( StringUtil.isNotEmpty( limit ) ){
			sb.append( " limit " + limit );
		} else {
			sb.append( " limit 100" );
		}
		if ( StringUtil.isNotEmpty( offset ) ){
			sb.append( " offset " + offset );
		} else {
			sb.append( " offset 0" );
		}
		
		List<Metering> metering = new ArrayList<>();
		try {
			metering = getMeteringData( sb.toString() );
		} catch (SQLException e) {
			System.out.println("SQL Exception : " + e.getMessage());
			e.printStackTrace();
			return Util.setCors(NanoHTTPD.newFixedLengthResponse(Status.EXPECTATION_FAILED, NanoHTTPD.MIME_HTML, "SQL Exception"));
		} catch (ClassNotFoundException e) {
			System.out.println("Class Not Found Exection");
			e.printStackTrace();
			return Util.setCors(NanoHTTPD.newFixedLengthResponse(Status.EXPECTATION_FAILED, NanoHTTPD.MIME_HTML, "Class Not Found Exection"));
		}

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String outDO = gson.toJson(metering).toString();
		System.out.println( "=== Metering ===" );
		System.out.println( outDO );
		
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(Status.OK, NanoHTTPD.MIME_HTML, outDO));
    }
	
	@Override
    public Response other(
      String method, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		System.out.println("***** OPTIONS /metering");
		
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(""));
    }
	
	private List< Metering > getMeteringData( String query ) throws SQLException, ClassNotFoundException {
		Connection conn = getConnection();
		LogPreparedStatement pstmt = new LogPreparedStatement( conn, query );
		System.out.println( "=== Qeury ===" );
		System.out.println( query );
		ResultSet rs = pstmt.executeQuery();
		
		List< Metering > meteringList = new ArrayList<>();
		Metering metering = null;
		while( rs.next() ){
			metering = new Metering();
			metering.setId( rs.getString("id") );
			metering.setNamespace( rs.getString( "namespace" ) );
			metering.setCpu( rs.getDouble( "cpu" ) );
			metering.setMemory( rs.getLong( "memory" ) );
			metering.setStorage( rs.getLong( "storage" ) );
			metering.setPublicIp( rs.getInt( "public_ip" ) );
			metering.setPrivateIp( rs.getInt( "private_ip" ) );
			if ( rs.getTimestamp( "metering_time" ) != null) {
				metering.setMeteringTime( rs.getTimestamp( "metering_time" ).getTime() );
			}
			meteringList.add(metering);
		}
		
		rs.close();
		pstmt.close();
		conn.close();
		
		return meteringList;
	}
	
	private void makeTimeRange( String unit, String startTime, String endTime, StringBuilder sb ) {
		/*
	 	cal.set(Calendar.MONTH, 0);
	    cal.set(Calendar.DAY_OF_MONTH, 1);
	    cal.set(Calendar.HOUR_OF_DAY, 0);
	    cal.set(Calendar.MINUTE, 0);
	    cal.set(Calendar.SECOND, 0);
	    cal.set(Calendar.MILLISECOND, 0);
		 */
		long start = 0;
		if (StringUtil.isNotEmpty(startTime)) {
			start = Integer.parseInt(startTime);
		}
		long end = System.currentTimeMillis();
		if (StringUtil.isNotEmpty(endTime)) {
			end = Integer.parseInt(endTime);
		}
	    
		Calendar calStart = Calendar.getInstance();calStart.setTime(new Date(start));calStart.set(Calendar.MINUTE, 0);calStart.set(Calendar.SECOND, 0);calStart.set(Calendar.MILLISECOND, 0);
		Calendar calEnd = Calendar.getInstance();calEnd.setTime(new Date(end));calEnd.set(Calendar.MINUTE, 0);calEnd.set(Calendar.SECOND, 0);calEnd.set(Calendar.MILLISECOND, 0);
		
		switch(unit) {
		case "hour" : 
			sb.append("select * from metering.metering_hour");
			break;
		case "day" : 
			calStart.set(Calendar.HOUR_OF_DAY, 0);
			calEnd.set(Calendar.HOUR_OF_DAY, 0);
			sb.append("select * from metering.metering_day");
			break;
		case "month" : 
			calStart.set(Calendar.DAY_OF_MONTH, 1);
			calEnd.set(Calendar.DAY_OF_MONTH, 1);
			sb.append("select * from metering.metering_month");
			break;
		case "year" : 
			calStart.set(Calendar.MONTH, 0);
			calEnd.set(Calendar.MONTH, 0);
			sb.append("select * from metering.metering_year");
			break;
		}
		sb.append(" where metering_time between '" + new Timestamp(calStart.getTime().getTime()) + "' and '" + new Timestamp(calEnd.getTime().getTime()) + "'");
	}
	
	private Connection getConnection() throws SQLException, ClassNotFoundException {
		Class.forName( Constants.JDBC_DRIVER );
		return DriverManager.getConnection( Constants.DB_URL, Constants.USERNAME, System.getenv( "DB_PASSWORD" ) );
	}
}