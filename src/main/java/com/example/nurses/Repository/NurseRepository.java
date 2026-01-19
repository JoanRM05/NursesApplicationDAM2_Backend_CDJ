package com.example.nurses.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.nurses.Entity.Nurse;

@Repository
public interface NurseRepository extends JpaRepository<Nurse, Long> {
	Optional<Nurse> findByUserAndPass(String user,String pass);
	Optional<Nurse> findByName(String name);
}
