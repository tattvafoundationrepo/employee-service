package digit.service;

import digit.config.EmployeeServiceConfiguration;
import digit.kafka.Producer;
import digit.repository.EmployeeRepository;
import digit.web.models.*;
import digit.web.models.request.EmployeeCriteriaRequest;
import digit.web.models.request.EmployeeDataRequest;
import digit.web.models.request.EmployeeRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeServiceConfiguration configuration;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private EmployeeRepository repository;

    @Autowired
    private Producer producer;


    public void saveEmployeeData(EmployeeDataRequest request) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json; charset=UTF-8");
        headers.set("Authorization", "Basic " + encodeCredentials(configuration.getUserName(), configuration.getPassword()));
        for (String empId : request.getEmpId()) {
            HttpEntity<String> reqEnt = new HttpEntity<>("{\"EMP_ID\": \"" + empId + "\"}", headers);
            System.out.println("save employee request: " + headers + " " + reqEnt);

            EmployeeResponse empRes = restTemplate.postForObject(configuration.getURL(), reqEnt, EmployeeResponse.class);
            EmpData empData = empRes.getEmpData();

            String empCode = repository.getEmpCode(empId);

            EmployeeCriteriaRequest employeeCriteriaRequest = new EmployeeCriteriaRequest();
            employeeCriteriaRequest.setEmpId(empId);
            employeeCriteriaRequest.setRequestInfo(request.getRequestInfo());
            employeeCriteriaRequest.setCreatedBy(request.getCreatedBy());
            employeeCriteriaRequest.setCreatedAt(request.getCreatedAt());
            employeeCriteriaRequest.setUpdatedBy(request.getUpdatedBy());
            employeeCriteriaRequest.setUpdatedAt(request.getUpdatedAt());
            upsertEmployeeData(empData, employeeCriteriaRequest, empCode);//db save
   //HRMS
            EmployeeSearchCriteria searchCriteria=new EmployeeSearchCriteria();
            searchCriteria.setEmpCode(empId);

            Employees employees = new Employees();
            employees.setEmployeeStatus("EMPLOYED");
            employees.setEmployeeType("PERMANENT");
            employees.setCode(empId);
            employees.setTenantId(request.getTenantId());
            EmployeeUser user = new EmployeeUser();

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            String encodedName = Base64.getEncoder().encodeToString(
                    (empData.getEmpFname() + " " + empData.getEmpMname() + " " + empData.getEmpLname()).getBytes()
            );
            String encodedMobileNumber = Base64.getEncoder().encodeToString(empData.getEmpMob().getBytes());
            String encodedDob = Base64.getEncoder().encodeToString(empData.getEmpDob().getBytes());
            String encodedGender = Base64.getEncoder().encodeToString(
                    (getGender(empData.getEmpGender())).getBytes()
            );
            String encodedFatherOrHusbandName = Base64.getEncoder().encodeToString(
                    (empData.getEmpMname() + " " + empData.getEmpLname()).getBytes()
            );
            String encodedCorrespondenceAddress = Base64.getEncoder().encodeToString(
                    (empData.getEmpPlaceofpost() + " " + empData.getEmpStreet1() + " " + empData.getEmpStreet2() + " " + empData.getEmpCity() + " " + empData.getEmpPostal()).getBytes()
            );


            try {
                String decodedDob = new String(Base64.getDecoder().decode(encodedDob));
                user.setDob(dateFormat.parse(decodedDob).getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }


            user.setName(new String(Base64.getDecoder().decode(encodedName)));
            user.setMobileNumber(new String(Base64.getDecoder().decode(encodedMobileNumber)));
            user.setGender(new String(Base64.getDecoder().decode(encodedGender)));
            user.setFatherOrHusbandName(new String(Base64.getDecoder().decode(encodedFatherOrHusbandName)));
            user.setCorrespondenceAddress(new String(Base64.getDecoder().decode(encodedCorrespondenceAddress)));
            user.setTenantId(request.getTenantId());


            List<EmployeeRole> role = Arrays.asList(new EmployeeRole("EMPLOYEE", "EMPLOYEE", "mh.mumbai"));
            user.setRoles(role);


            employees.setUser(user);


            List<Jurisdictions> jurisdictions = new ArrayList<>();
            Jurisdictions jurisdiction = new Jurisdictions();
            jurisdiction.setBoundary(request.getBoundary());
            jurisdiction.setTenantId(request.getTenantId());
            jurisdiction.setHierarchy(request.getHierarchy());//ADMIN
            jurisdiction.setBoundaryType(request.getBoundaryType());
            jurisdictions.add(jurisdiction);
            employees.setJurisdictions(jurisdictions);


            List<Assignments> assignments = new ArrayList<>();
            Assignments assignment = new Assignments();
            assignment.setCurrentAssignment(true);
            assignment.setDepartment(request.getEmpDepartment());
            try {

                assignment.setFromDate(dateFormat.parse(empData.getEmpJoining()).getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            assignment.setDesignation(request.getEmpDesignationCode());
            assignments.add(assignment);
            employees.setAssignments(assignments);


            employees.setEducation(new ArrayList<>());
            employees.setTests(new ArrayList<>());
            employees.setServiceHistory(new ArrayList<>());


            List<Employees> employees1 = new ArrayList<>();
            employees1.add(employees);


            EmployeeRequest employeeRequest1 = new EmployeeRequest();
            employeeRequest1.setRequestInfo(request.getRequestInfo());
            employeeRequest1.setEmployees(employees1);


            String URL = getEmployeeSaveUrl() + "?tenantId=" + request.getTenantId();
            EmployeeData employeeData = repository.getEmployeeData(searchCriteria);
            if (employeeData.getEmpCode() != null && ("New Record".equalsIgnoreCase(employeeData.getStatus()) || "Update".equalsIgnoreCase(employeeData.getStatus()))) {
                try {
                    restTemplate.postForObject(URL.toString(), employeeRequest1, Map.class);

                    employeeData.setStatus("PROCESSED");

                } catch (HttpClientErrorException e) {
                    String response = e.getResponseBodyAsString();
                    if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                        if (response.contains("ERR_HRMS_USER_EXIST_USERNAME")) {
                            employeeData.setStatus("PROCESSED");
                            e.printStackTrace();
                        }
                    }

                }
                employeeCriteriaRequest.setEmployeeData(employeeData);
                producer.push("save-employee-data", employeeCriteriaRequest);
            }
            // for this block we need to put URL
//            else if (employeeData.getEmpCode() != null && employeeData.getStatus().equalsIgnoreCase("Update")) {
//
//                restTemplate.put(URL.toString(), employeeRequest1, Map.class);
//                employeeData.setStatus("PROCESSED");
//                producer.push("save-employee-data", employeeData);
//
//            }

        }

    }


    private static String encodeCredentials(String username, String password) {
        String credentials = username + ":" + password;
        return Base64.getEncoder().encodeToString(credentials.getBytes());
    }

    private String getEmployeeSaveUrl() {
        return configuration.getHrmsHost() + configuration.getHrmsCreateEndpoint();
    }

    private String getGender(String gender) {
        if (gender.equalsIgnoreCase("M")) {
            return "MALE";
        } else if (gender.equalsIgnoreCase("F")) {
            return "FEMALE";
        } else {
            return "TRANSGENDER";
        }
    }


    private void upsertEmployeeData(EmpData empData, EmployeeCriteriaRequest request, String empCode) {
        EmployeeData employeeData = mapData(empData);
        employeeData.setEmpCode(request.getEmpId());
        employeeData.setCreatedBy(request.getCreatedBy());
        employeeData.setCreatedAt(request.getCreatedAt());
        employeeData.setUpdatedAt(request.getUpdatedAt());
        employeeData.setUpdatedBy(request.getUpdatedBy());
        if (empCode != null) {
            employeeData.setStatus("Update");
        } else {
            employeeData.setStatus("New Record");
        }
        request.setEmployeeData(employeeData);
        producer.push("save-employee-data", request);
    }

    private EmployeeData mapData(EmpData empData) {
        EmployeeData employeeData = new EmployeeData();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        employeeData.setEmpCity(empData.getEmpCity());
        employeeData.setEmpDepartment(empData.getEmpDepartment());
        employeeData.setEmpDesignation(empData.getEmpDesignation());
        employeeData.setEmpDistrict(empData.getEmpDistrict());
        try {

            employeeData.setEmpDob(dateFormat.parse(empData.getEmpDob()).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        employeeData.setEmpEmail(empData.getEmpEmail());
        employeeData.setEmpEmptype(empData.getEmpEmptype());
        employeeData.setEmpFname(empData.getEmpFname());
        employeeData.setEmpGender(empData.getEmpGender());
        try {

            employeeData.setEmpJoining(dateFormat.parse(empData.getEmpJoining()).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        employeeData.setEmpLname(empData.getEmpLname());
        employeeData.setEmpMname(empData.getEmpMname());
        employeeData.setEmpMob(empData.getEmpMob());
        employeeData.setEmpPlaceofpost(empData.getEmpPlaceofpost());
        employeeData.setEmpPostal(empData.getEmpPostal());

        try {

            employeeData.setEmpRetirement(dateFormat.parse(empData.getEmpRetirement()).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        employeeData.setEmpStreet1(empData.getEmpStreet1());
        employeeData.setEmpStreet2(empData.getEmpStreet2());
        return employeeData;

    }

}
