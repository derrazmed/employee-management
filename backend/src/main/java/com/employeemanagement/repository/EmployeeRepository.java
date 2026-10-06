package com.employeemanagement.repository;

import com.employeemanagement.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query("""
            SELECT e FROM Employee e
            WHERE (:search = ''
                    OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:department = '' OR e.department = :department)
              AND (:jobTitle = '' OR e.jobTitle = :jobTitle)
            """)
    Page<Employee> search(
            @Param("search") String search,
            @Param("department") String department,
            @Param("jobTitle") String jobTitle,
            Pageable pageable
    );

    @Query("""
            SELECT DISTINCT e.department FROM Employee e
            WHERE e.department IS NOT NULL AND e.department <> ''
            ORDER BY e.department
            """)
    List<String> findDistinctDepartments();

    @Query("""
            SELECT DISTINCT e.jobTitle FROM Employee e
            WHERE e.jobTitle IS NOT NULL AND e.jobTitle <> ''
            ORDER BY e.jobTitle
            """)
    List<String> findDistinctJobTitles();
}
