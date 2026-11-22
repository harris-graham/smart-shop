package com.smartshop.repository;

import com.smartshop.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

    Optional<Employee> findByEmpuname(String empuname);

    Optional<Employee> findByEmpunameIgnoreCase(String empuname);

    boolean existsByEmpunameIgnoreCase(String empuname);

    List<Employee> findAllByOrderByEmpidAsc();

    @Query("""
            select e from Employee e
            where lower(e.empname) like lower(concat('%', :query, '%'))
               or lower(e.empuname) like lower(concat('%', :query, '%'))
            order by e.empid asc
            """)
    List<Employee> search(@Param("query") String query);
}
