package lemon.engine.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import lemon.engine.reflection.ReflectionUtil;

public class SqlUtil{
	
	private SqlUtil(){}
	
	public static boolean load(){
		return ReflectionUtil.getClass("com.mysql.jdbc", "Driver")!=null;
	}
	public static Connection createConnection(String hostname, String port, String database, String username, String password){
		try {
			return DriverManager.getConnection("jdbc:mysql://"+hostname+"/"+database+"?user="+username+"&password="+password);
		} catch (SQLException e) {
			return null;
		}
	}
	public static boolean closeConnection(Connection connection){
		if(connection!=null){
			try {
				connection.close();
				return true;
			} catch (SQLException e) {
				return false;
			}
		}
		return true;
	}
	public static PreparedStatement prepareStatement(Connection connection, String line){
		try {
			return connection.prepareStatement(line);
		} catch (SQLException e) {
			return null;
		}
	}
	public static boolean setValues(PreparedStatement statement, Object... values){
		if(values!=null){
			for(int i=0;i<values.length;++i){
				try {
					statement.setObject(i, values[i]);
				} catch (SQLException e) {
					return false;
				}
			}
		}
		return true;
	}
	public static int sendStatement(Connection connection, PreparedStatement statement){
		try {
			return statement.executeUpdate();
		} catch (SQLException e) {
			return -1;
		}
	}
	public static ResultSet sendQuery(Connection connection, PreparedStatement statement){
		try {
			return statement.executeQuery();
		} catch (SQLException e) {
			return null;
		}
	}
}
