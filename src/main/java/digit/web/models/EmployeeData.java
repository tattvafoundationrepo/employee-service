
package digit.web.models;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeData {
    private String empCode;
    private String empCity;
    private String empDepartment;
    private String empDesignation;
    private String empDistrict;
    private Long empDob;
    private String empEmail;
    private String empEmptype;
    private String empFname;
    private String empGender;
    private Long empJoining;
    private String empLname;
    private String empMname;
    private String empMob;
    private String empPlaceofpost;
    private Long empPostal;
    private Long empRetirement;
    private String empStreet1;
    private String empStreet2;
    private String status;
    private Long createdAt;
    private Long updatedAt;
    private String createdBy;
    private String updatedBy;

}
