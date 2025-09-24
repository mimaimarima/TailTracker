package pets.repository.daoimpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import pets.model.Sesion;
import pets.repository.dao.SesionDAO;

public class SesionDAOI implements SesionDAO {
	
	private Connection connection;

	public SesionDAOI (Connection connection) {
        this.connection = connection;
    }
	public void insert(Sesion ses) throws SQLException {
		String sql = "INSERT INTO session (start, ends, user_id) VALUES (?,?,?::uuid)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setTimestamp(1, Timestamp.valueOf(ses.getStart()));
			stmt.setTimestamp(2, Timestamp.valueOf(ses.getEnd()));
			stmt.setString(3, ses.getUserId());
	        stmt.executeUpdate(); 
	        }
	}
	

}
