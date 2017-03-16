package com.mytselbot.db;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBHelper {

	Connection connection = null;

	 public static ArrayList<HashMap<String, String>> getMenu(String id) throws PropertyVetoException, SQLException, IOException, ClassNotFoundException {
		    ArrayList<HashMap<String, String>> arrayMenu = new ArrayList<HashMap<String, String>>();
		 	
		 	Connection connection = null;
	        Statement statement = null;
	        ResultSet resultSet = null;
	        try {
	            // fetch a connection
	            connection = DataSource.getInstance().getConnection();

	            if (connection != null) {
	                statement = connection.createStatement();
	                resultSet = statement.executeQuery("select * from tsel_menu where parent_id="+id+" order by order_id desc");
	                HashMap<String, String> menu = new HashMap<String, String>();
	                while (resultSet.next()) {
	                    menu.put("id", resultSet.getString("id"));
	                    menu.put("content", resultSet.getString("content"));
	                    menu.put("type", resultSet.getString("type"));
	                    menu.put("url_confirm", resultSet.getString("url_confirm"));
	                    menu.put("url", resultSet.getString("url"));
		                arrayMenu.add(menu);    
	                }
	            }

	        } catch (SQLException e) {
	            e.printStackTrace();
	        } finally {
	            if (resultSet != null) try { resultSet.close(); } catch (SQLException e) {e.printStackTrace();}
	            if (statement != null) try { statement.close(); } catch (SQLException e) {e.printStackTrace();}
	            if (connection != null) try { connection.close(); } catch (SQLException e) {e.printStackTrace();}
	        }
			return arrayMenu;
	    }
	 
	 

	    public int[] getMemberState(Integer userId, Long chatId) throws PropertyVetoException, SQLException, IOException, ClassNotFoundException {
	        
	    	Connection connection = null;
	        Statement statement = null;
	        ResultSet resultSet = null;
	        
	    	int[] state = new int[]{-1,-1};
	    	try {
	            // fetch a connection
	            connection = DataSource.getInstance().getConnection();
	            String sql="SELECT menu_id, parent_id FROM tsel_state WHERE userId ="+userId+"  AND chatId ="+chatId;
	           
	            if (connection != null) {
	                statement = connection.createStatement();
	                 resultSet = statement.executeQuery(sql);
	                if (resultSet.next()) {
	                	state[0] = resultSet.getInt("menu_id");
	   	                state[1] = resultSet.getInt("parent_id");
	                }
	            }
	            

	        } catch (SQLException e) {
	            e.printStackTrace();
	        } finally {
	            if (resultSet != null) try { resultSet.close(); } catch (SQLException e) {e.printStackTrace();}
	            if (statement != null) try { statement.close(); } catch (SQLException e) {e.printStackTrace();}
	            if (connection != null) try { connection.close(); } catch (SQLException e) {e.printStackTrace();}
	        }
	        return state;
	    }

	  
	    
}
