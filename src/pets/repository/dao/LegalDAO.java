package pets.repository.dao;

import java.sql.SQLException;
import java.util.List;

import pets.model.Legal;
import pets.model.enums.LegalType;

public interface LegalDAO {
	void insert(Legal legal) throws SQLException;
	public List<Legal> getAllLegals(LegalType filterType) throws SQLException;
	public void updateLegal(Legal legal) throws SQLException;
	public boolean deleteLegal(String personId) throws SQLException;
}
