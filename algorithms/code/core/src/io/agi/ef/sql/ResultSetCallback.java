package io.agi.ef.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by dave on 17/02/16.
 */
public interface ResultSetCallback {

    void onResultSet( ResultSet rs ) throws SQLException;
}
