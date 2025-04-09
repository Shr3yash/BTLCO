package com.batelco.migration.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class QueryExecutor {

    public static ResultSet executeQuery(Connection connection, String sqlQuery) throws SQLException {
        // Use the provided connection to prepare and execute the query
        PreparedStatement stmt = connection.prepareStatement(sqlQuery);
        return stmt.executeQuery();
    }
}
