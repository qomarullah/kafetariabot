/*
 * This is the source code of Telegram Bot v. 2.0
 * It is licensed under GNU GPL v. 3 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Ruben Bermudez, 3/12/14.
 */
package org.telegram.database;

import com.mysql.jdbc.Statement;
import com.mytselbot.BotLogger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ruben Bermudez
 * @version 2.0
 * @brief Database Manager to perform database operations
 * @date 3/12/14
 */
public class DatabaseTsel {
    private static final String LOGTAG = "DATABASEMANAGER";

    private static volatile DatabaseTsel instance;
    private static volatile ConectionDB connetion;

    /**
     * Private constructor (due to Singleton)
     */
    private DatabaseTsel() {
        connetion = new ConectionDB();
       /* final int currentVersion = connetion.checkVersion();
        BotLogger.info(LOGTAG, "Current db version: " + currentVersion);
        if (currentVersion < CreationStrings.version) {
            recreateTable(currentVersion);
        }*/
    }

    /**
     * Get Singleton instance
     *
     * @return instance of the class
     */
    public static DatabaseTsel getInstance() {
        final DatabaseTsel currentInstance;
        if (instance == null) {
            synchronized (DatabaseTsel.class) {
                if (instance == null) {
                    instance = new DatabaseTsel();
                }
                currentInstance = instance;
            }
        } else {
            currentInstance = instance;
        }
        return currentInstance;
    }

    
 
    public String[] getMember(Integer userId) {
    	String[] options = new String[4];
        try {
            final PreparedStatement preparedStatement = connetion.getPreparedStatement("SELECT * FROM tsel_member WHERE userId = ?");
            preparedStatement.setInt(1, userId);
            final ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                options[0] = result.getString("username");
                options[1] = result.getString("phone");
                options[2] = result.getString("userid");
                options[3] = result.getString("language");
                
            } else {
                addNewMember(userId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return options;
    }

    private boolean addNewMember(Integer userId) {
        int updatedRows = 0;
        try {
            final PreparedStatement preparedStatement = connetion.getPreparedStatement("INSERT INTO tsel_member (userId) VALUES (?)");
            preparedStatement.setInt(1, userId);
            updatedRows = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return updatedRows > 0;
    }

    
    
    public boolean putMemberPhone(Integer userId, String phone, String username, String firstname) {
        int updatedRows = 0;
        //System.out.println("UPDATE tsel_member SET phone = '"+phone+"' WHERE userId ="+userId);
        
        try {
            final PreparedStatement preparedStatement = connetion.getPreparedStatement("UPDATE tsel_member SET phone = ?,  username = ?, firstname = ? WHERE userId = ?");
            preparedStatement.setString(1, phone);
            preparedStatement.setString(2, username);
            preparedStatement.setString(3, firstname);
    
            preparedStatement.setInt(4, userId);
            updatedRows = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return updatedRows > 0;
    }
    ////////////////////////////////////////////////////////////////////
    public String[] getAgent(Integer userId) {
    	String[] options = new String[4];
        try {
            final PreparedStatement preparedStatement = connetion.getPreparedStatement("SELECT * FROM tsel_agent WHERE userId = ?");
            preparedStatement.setInt(1, userId);
            final ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                options[0] = result.getString("username");
                options[1] = result.getString("phone");
                options[2] = result.getString("location");
                options[3] = result.getString("language");
                
            } else {
                addNewAgent(userId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return options;
    }

    private boolean addNewAgent(Integer userId) {
        int updatedRows = 0;
        try {
            final PreparedStatement preparedStatement = connetion.getPreparedStatement("INSERT INTO tsel_agent (userId) VALUES (?)");
            preparedStatement.setInt(1, userId);
            updatedRows = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return updatedRows > 0;
    }
    
    public boolean putAgentPhone(Integer userId, String phone, String username, String firstname) {
        int updatedRows = 0;
        //System.out.println("UPDATE tsel_agent SET phone = '"+phone+"' WHERE userId ="+userId);
        
        try {
            final PreparedStatement preparedStatement = connetion.getPreparedStatement("UPDATE tsel_agent SET phone = ?,  username = ?, firstname = ? WHERE userId = ?");
            preparedStatement.setString(1, phone);
            preparedStatement.setString(2, username);
            preparedStatement.setString(3, firstname);
            preparedStatement.setInt(4, userId);
            updatedRows = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return updatedRows > 0;
    }
    ////////////////////////////////////////////////////////////////////
   
    public boolean updateMenu(int number, String name, String merchant, int price) {
        boolean ok=false;
        String sql="";
        
        try {
	        	
	        	sql="UPDATE `tsel_menu` set `name`=?, `merchant`=?,`price`=? where number=?";
	        	
	            final PreparedStatement preparedStatement = connetion.getPreparedStatement(sql);
	            preparedStatement.setString(1, name);
	            preparedStatement.setString(2, merchant);
	            preparedStatement.setInt(3, price);
	            preparedStatement.setInt(4, number);
	            preparedStatement.executeUpdate();
		        ok=true;
        
        } catch (SQLException e) {
            e.printStackTrace();
            
        }
        return ok;
    }
    public HashMap<String,String> getMenuByNumber(int number) {
    	HashMap<String,String> res = new HashMap<String,String>();
        try {
        	String sql="SELECT * FROM `tsel_menu` where number= ?";
            final PreparedStatement preparedStatement = connetion.getPreparedStatement(sql);
            preparedStatement.setInt(1, number);
            
            final ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
               res.put("number", result.getString("number"));
               res.put("name", result.getString("name"));
               res.put("price", result.getString("price"));
               res.put("merchant", result.getString("merchant"));
                
                
            } 
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }
    
   

    
    public boolean getMenuPrevileges(int number, String username, long userid, String phone) {
    	boolean auth=false;
        try {
        	String sql="SELECT * FROM `tsel_menu` a left join `tsel_merchant_admin` b on a.merchant_id=b.merchant_id where a.number=? AND (b.username =? or b.userid=? or b.phone=?)";
        	final PreparedStatement preparedStatement = connetion.getPreparedStatement(sql);
            preparedStatement.setInt(1, number);
            preparedStatement.setString(2, username);
            preparedStatement.setLong(3, userid);
            preparedStatement.setString(4, phone);
            
             
            
            final ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
            	auth=true;
            } 
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return auth;
    }
    

    public ArrayList<HashMap<String,String>> getMenu() {
    	ArrayList<HashMap<String,String>> menu=new ArrayList<HashMap<String,String>>();
    	try {
        	String sql="SELECT * FROM `tsel_menu` order by number asc";
            final PreparedStatement preparedStatement = connetion.getPreparedStatement(sql);
            final ResultSet result = preparedStatement.executeQuery();
            while (result.next()) {
            	HashMap<String,String> res = new HashMap<String,String>();
            	res.put("number", result.getString("number"));
                res.put("name", result.getString("name"));
                res.put("price", result.getString("price"));
                res.put("merchant", result.getString("merchant"));
                res.put("merchant_id", result.getString("merchant_id"));
                
                menu.add(res);
             }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return menu;
    }
    

    public ArrayList<HashMap<String,String>> getMenuBySearch(String text) {
    	ArrayList<HashMap<String,String>> menu=new ArrayList<HashMap<String,String>>();
    	try {
        	String sql="SELECT * FROM `tsel_menu` where `name` like '%"+text+"%' order by number asc";
           PreparedStatement preparedStatement = connetion.getPreparedStatement(sql);
            
            ResultSet result = preparedStatement.executeQuery();
            while (result.next()) {
            	HashMap<String,String> res = new HashMap<String,String>();
            	res.put("number", result.getString("number"));
                res.put("name", result.getString("name"));
                res.put("price", result.getString("price"));
                res.put("merchant", result.getString("merchant"));
                res.put("merchant_id", result.getString("merchant_id"));
                
                menu.add(res);
             }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return menu;
    }
   
    public ArrayList<HashMap<String,String>> getMenuAdmin(String username, int userid, String phone) {
    	ArrayList<HashMap<String,String>> menu=new ArrayList<HashMap<String,String>>();
    	try {
    		//String sql="SELECT * FROM `tsel_menu` a left join `tsel_merchant_admin` b on a.merchant_id=b.id where b.username =? ";
    		String sql="SELECT * FROM `tsel_menu` a left join `tsel_merchant_admin` b on a.merchant_id=b.merchant_id where b.username =? or b.userid=? or b.phone=?";
        	final PreparedStatement preparedStatement = connetion.getPreparedStatement(sql);
        	preparedStatement.setString(1, username);
        	preparedStatement.setInt(2, userid);
        	preparedStatement.setString(3, phone);
        	
            final ResultSet result = preparedStatement.executeQuery();
            while (result.next()) {
            	HashMap<String,String> res = new HashMap<String,String>();
            	res.put("number", result.getString("a.number"));
                res.put("name", result.getString("a.name"));
                res.put("price", result.getString("a.price"));
                res.put("merchant", result.getString("a.merchant"));
                res.put("merchant_id", result.getString("a.merchant_id"));
                
                menu.add(res);
             }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return menu;
    }
    
    public int setMenuAdmin(int merchant_id, Integer userid, String username, String userphone) {
    	int id = 0;
        try {
        	String sql="replace INTO tsel_merchant_admin VALUES (NULL,?,?,?,?)";
        	
            final PreparedStatement preparedStatement = connetion.getPreparedStatement(sql);
            preparedStatement.setInt(1, merchant_id);
            preparedStatement.setInt(2, userid);
            preparedStatement.setString(3, username);
            preparedStatement.setString(4, userphone);
            id=preparedStatement.executeUpdate();
		       
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }
    
    ////////////////
    //ORDER
    public int addOrder(Integer userid, String username, String userphone, 
    		int agent_userid, String agent_username, String agent_userphone, 
    		Long chatid, Integer messageid, String item, double total,double fee, String payment, int status) {
    	int id = 0;
        int updatedRows = 0;
        try {
        	String sql="INSERT INTO tsel_order "
            		+ "(userid, username, userphone, agent_userid, agent_username,"
            		+ " agent_userphone, chatid, messageid, item, total, fee, payment, status, last_update) "
            		+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,NOW())";
        	
            final PreparedStatement preparedStatement = connetion.getPreparedStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setInt(1, userid);
            preparedStatement.setString(2, username);
            preparedStatement.setString(3, userphone);
            preparedStatement.setInt(4, agent_userid);
            preparedStatement.setString(5, agent_username);
            preparedStatement.setString(6, agent_userphone);
            preparedStatement.setLong(7, chatid);
            preparedStatement.setInt(8, messageid);
            preparedStatement.setString(9, item);
            preparedStatement.setDouble(10, total);
            preparedStatement.setDouble(11, fee);
            preparedStatement.setString(12, payment);
              preparedStatement.setInt(13, status);
               
            updatedRows = preparedStatement.executeUpdate();
            ResultSet rs=preparedStatement.getGeneratedKeys();
            if(rs.next()) {
	            //In this exp, the autoKey val is in 1st col
	            id=rs.getInt(1);
	            //now this's a real value of col Id
	            //System.out.println(id);
	           }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }
    
    public boolean updateOrder(int id, int userid, String username, String userphone, int status) {
    	boolean resp=false;
        try {

        	
        	String sql="update tsel_order set agent_userid=?, agent_username=?, agent_userphone=?, status=? "
            		+ "where id=? ";
        	//System.out.println(sqlx);
            final PreparedStatement preparedStatement = connetion.getPreparedStatement(sql);
            preparedStatement.setInt(1, userid);
            preparedStatement.setString(2, username);
            preparedStatement.setString(3, userphone);
            preparedStatement.setInt(4, 1);
            preparedStatement.setInt(5, id);
            int updatedRows = preparedStatement.executeUpdate();
            
            resp=true;
            
        } catch (SQLException e) {
            e.printStackTrace();
            resp=false;
        }
        return resp;
    }
    
    
    public HashMap<String,String> getOrder(int orderId) {
    	HashMap<String,String> resp = new HashMap<String,String>();
    	
        try {
        	String sql="SELECT * FROM `tsel_order` WHERE id= ?";
            final PreparedStatement preparedStatement = connetion.getPreparedStatement(sql);
            preparedStatement.setInt(1, orderId);
            final ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
               resp.put("id", rs.getString("id"));
               resp.put("userid", rs.getString("userid"));
               resp.put("username", rs.getString("username"));
               resp.put("userphone", rs.getString("userphone"));
               resp.put("agent_userid", rs.getString("agent_userid"));
               resp.put("agent_username", rs.getString("agent_username"));
               resp.put("agent_userphone", rs.getString("agent_userphone"));
               resp.put("chatid", rs.getString("chatid"));
               resp.put("messageid", rs.getString("messageid"));
               resp.put("item", rs.getString("item"));
               resp.put("total", rs.getString("total"));
               resp.put("fee", rs.getString("fee"));
               resp.put("payment", rs.getString("payment"));
               resp.put("status", rs.getString("status"));
               resp.put("last_update", rs.getString("last_update"));
               
                
            } 
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resp;
    }

    //FeedBack
////////////////
//ORDER
public int addFeedBack(Integer userid, String username, String userphone, String feedback) {
	int updatedRows = 0;
	try {
		String sql="INSERT INTO tsel_feedback "
	  		+ "(userid, username, userphone, feedback, last_update) "
	  		+ "VALUES (?,?,?,?,NOW())";
		
	  final PreparedStatement preparedStatement = connetion.getPreparedStatement(sql);
	  preparedStatement.setInt(1, userid);
	  preparedStatement.setString(2, username);
	  preparedStatement.setString(3, userphone);
	  preparedStatement.setString(4, feedback);
	  
	     
	  updatedRows = preparedStatement.executeUpdate();
	 
	} catch (SQLException e) {
	  e.printStackTrace();
	}
	return updatedRows;
	}
}
