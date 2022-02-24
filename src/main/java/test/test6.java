package test;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class test6 {

	public static void main(String[] args) throws Throwable{

		Connection con=test.getConnection();
		test6.addIdToDir(con,args[0],args[1],args[2],args[3]);
		test6.removeIdFromDir(con, args[2],args[3]);
		con.close();
	}

	public static void addIdToDir(String masterId,String massterPassword,String id,String dir) throws SQLException, ClassNotFoundException, IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
		Connection con=test.getConnection();
		addIdToDir(con, masterId, massterPassword, id, dir);
		con.close();
	}
	public static void addIdToDir(Connection con,String masterId,String massterPassword,String id,String dir) throws SQLException, ClassNotFoundException, IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{

		Key keyOfPrivateKey=test.getKeyOfprivateKey(con,masterId,massterPassword);
		if(keyOfPrivateKey==null){
			return;
		}
		Key privateKey=test.getPrivateKey(con,masterId,keyOfPrivateKey);
		Key commonKey=test.getCommonKey(con, masterId, dir, privateKey);
		Key publicKey=test.getPublicKey(con, id);
		byte[] commonBytes=test.objectToBytes(commonKey);
		byte[] commonBytesEncrypted=test.encryptRSA(commonBytes,publicKey);
//		String sql = "insert into test3 values(?,?,?)";
		String sql = "insert into test3 (id,dir,common) values(?,?,?)";
		PreparedStatement stmt = con.prepareStatement(sql);
		stmt.setString(1, id);
		stmt.setString(2, dir);
		stmt.setBytes(3, commonBytesEncrypted);
		stmt.execute();
		stmt.close();
	}
	public static void removeIdFromDir(String id,String dir) throws SQLException{
		Connection con=test.getConnection();
		removeIdFromDir(con, id, dir);
		con.close();
	}
	public static void removeIdFromDir(Connection con,String id,String dir) throws SQLException{
		String sql = "delete from test3 where id=? and dir=?";
		PreparedStatement stmt = con.prepareStatement(sql);
		stmt.setString(1, id);
		stmt.setString(2, dir);
		stmt.execute();
		stmt.close();
	}


}