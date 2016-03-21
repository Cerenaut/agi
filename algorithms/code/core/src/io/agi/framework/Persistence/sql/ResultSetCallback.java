package io.agi.framework.Persistence.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Generic way to add a callback to store results from a query, allowing the complexity of JDBC access to be hidden
 * away in utility code.
 *
 * Created by dave on 17/02/16.
 */
public interface ResultSetCallback {

    void onResultSet( ResultSet rs ) throws SQLException;
}
