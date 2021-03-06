/**
 * Copyright (C) 2016, CERN
 * This software is distributed under the terms of the GNU General Public
 * Licence version 3 (GPL Version 3), copied verbatim in the file "LICENSE".
 * In applying this license, CERN does not waive the privileges and immunities
 * granted to it by virtue of its status as Intergovernmental Organization
 * or submit itself to any jurisdiction.
 */
package ch.cern.db.flume.sink.kite.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.avro.Schema;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.cern.db.flume.sink.kite.util.InferSchemaFromTable;


public class InferSchemaFromTableTest {
	
	String connection_url = "jdbc:hsqldb:mem:testdb";
	Connection connection = null;
	
	@Before
	public void setup(){
		try {
			connection = DriverManager.getConnection(connection_url, "sa", "");
			
			Statement statement = connection.createStatement();
			statement.execute("DROP TABLE IF EXISTS audit_data_table;");
			statement.execute("CREATE TABLE audit_data_table ("
					+ "id INTEGER, "
					+ "return_code BIGINT, "
					+ "name VARCHAR(20) NOT NULL,"
					+ "lastname VARCHAR(20) NULL"
					+ ");");
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}	
	}
	
	@Test
	public void generateSchema() throws SQLException{
		
		InferSchemaFromTable generator = new InferSchemaFromTable();
		generator.configure(new String[]{
				"-dc", "org.hsqldb.jdbc.JDBCDriver",
				"-c", connection_url,
				"-t", "AUDIT_DATA_TABLE",
				"-u", "sa",
				"-p", "",
				});
		
		Schema schema = generator.getSchema();
		
		Assert.assertEquals("{\"type\":\"record\",\"name\":\"log\",\"fields\":["
				+ "{\"name\":\"ID\",\"type\":[\"int\",\"null\"],\"doc\":\"SQL type: 4\"},"
				+ "{\"name\":\"RETURN_CODE\",\"type\":[\"int\",\"null\"],\"doc\":\"SQL type: -5\"},"
				+ "{\"name\":\"NAME\",\"type\":\"string\",\"doc\":\"SQL type: 12\"},"
				+ "{\"name\":\"LASTNAME\",\"type\":[\"string\",\"null\"],\"doc\":\"SQL type: 12\"}"
				+ "]}", 
				schema.toString(false));
	}
	
	@Test(expected=SQLException.class)
	public void noTableFound() throws SQLException{
		
		InferSchemaFromTable generator = new InferSchemaFromTable();
		generator.configure(new String[]{
				"-dc", "org.hsqldb.jdbc.JDBCDriver",
				"-c", connection_url,
				"-t", "AUDIT_DATA_TABLE_no_exists",
				"-u", "sa",
				"-p", ""
				});
		
		generator.getSchema();
		Assert.fail();
	}
	
	@After
	public void cleanUp(){
		try {
			connection.close();
		} catch (SQLException e) {
		}
	}
}
