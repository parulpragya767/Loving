package com.lovingapp.loving.infra.datasource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.AbstractDataSource;

import com.lovingapp.loving.infra.security.DbCurrentUserContext;

public class RlsAwareDataSource extends AbstractDataSource {

    private static final String SET_RLS_SQL = "SET LOCAL app.current_user_id = ?";

    private final DataSource targetDataSource;

    public RlsAwareDataSource(DataSource targetDataSource) {
        this.targetDataSource = targetDataSource;
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = targetDataSource.getConnection();
        applyRlsContext(connection);
        return connection;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection connection = targetDataSource.getConnection(username, password);
        applyRlsContext(connection);
        return connection;
    }

    private void applyRlsContext(Connection connection) throws SQLException {
        UUID currentUserId = DbCurrentUserContext.getCurrentUserId();

        if (currentUserId == null) {
            throw new IllegalStateException(
                    "Missing DbCurrentUserContext: userId must be set before database access");
        }

        try (PreparedStatement ps = connection.prepareStatement(SET_RLS_SQL)) {
            ps.setObject(1, currentUserId);
            ps.execute();
        }
    }
}
