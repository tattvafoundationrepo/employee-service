package digit.repository.rowMapper;

import digit.web.models.EmployeeData;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class EmployeeRowMapper implements ResultSetExtractor<EmployeeData> {

    @Override
    public EmployeeData extractData(ResultSet rs) throws SQLException, DataAccessException {
        EmployeeData employeeData = new EmployeeData();
        while (rs.next()) {
            employeeData = EmployeeData.builder().
                    empCode(rs.getString("empCode")).
                    empCity(rs.getString("empCity"))
                    .empDepartment(rs.getString("empDepartment"))
                    .empDesignation(rs.getString("empDesignation"))
                    .empDistrict(rs.getString("empDistrict"))
                    .empDob(rs.getLong("empDob"))
                    .empEmail(rs.getString("empEmail")).
                    empEmptype(rs.getString("empEmptype")).
                    empFname(rs.getString("empFname")).
                    empGender(rs.getString("empGender"))
                    .empJoining(rs.getLong("empJoining")).
                    empLname(rs.getString("empLname")).
                    empMname(rs.getString("empMname")).
                    empMob(rs.getString("empMob")).
                    empPlaceofpost(rs.getString("empPlaceofpost")).empPostal(rs.getLong("empPostal"))
                    .empRetirement(rs.getLong("empRetirement")).
                    empStreet1(rs.getString("empStreet1"))
                    .empStreet2(rs.getString("empStreet2")).
                    status(rs.getString("status")).
                    createdAt(rs.getLong("createdAt"))
                    .updatedAt(rs.getLong("updatedAt"))
                    .createdBy(rs.getString("createdBy"))
                    .updatedBy(rs.getString("updatedBy"))
                    .build();

        }

        return employeeData;
    }
}
