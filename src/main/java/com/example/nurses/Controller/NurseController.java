package com.example.nurses.Controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.nurses.Repository.NurseRepository;
import com.example.nurses.Entity.Nurse;

@RestController
@RequestMapping("/nurse")
public class NurseController {

	@Autowired
	private NurseRepository nurseRepository;

	@GetMapping("/index")
	public @ResponseBody ResponseEntity<List<Nurse>> getAll() {
		try {
			return ResponseEntity.ok(nurseRepository.findAll());
		} catch (Exception e) {
			return ResponseEntity.status(404).build();
		}
	}

	@GetMapping("/name")
	public ResponseEntity<Nurse> findByName(@RequestParam String name) {
		Optional<Nurse> optionalNurse = nurseRepository.findByName(name);
		if (optionalNurse.isPresent()) {
			return ResponseEntity.ok(optionalNurse.get());
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@PostMapping("/login")
	public ResponseEntity<Nurse> login(@RequestBody Nurse nurse) {
		Optional<Nurse> optionalNurse  = nurseRepository.findByUserAndPass(nurse.getUser(), nurse.getPass());
		if (optionalNurse.isPresent()) {
			return ResponseEntity.ok(optionalNurse.get());
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}

	@PostMapping()
	public ResponseEntity<Nurse> createNurse(@RequestBody Nurse nurse) {
		try {
			Nurse _nurse = nurseRepository.save(
					new Nurse(nurse.getName(), nurse.getSurname(), nurse.getEmail(), nurse.getUser(), nurse.getPass()));
			return ResponseEntity.status(HttpStatus.CREATED).body(_nurse);
		} catch (Exception e) {
			return ResponseEntity.badRequest().build();
		}
	}

	@GetMapping("/{id}")
	public ResponseEntity<Nurse> findById(@PathVariable Long id) {
		Optional<Nurse> nurse = nurseRepository.findById(id);
		if (nurse.isPresent()) {
			return ResponseEntity.ok(nurse.get());
		} else {
			return ResponseEntity.notFound().build();
		}

	}

	@PutMapping("/{id}")
	public ResponseEntity<Void> updateNurse(@PathVariable long id, @RequestBody Nurse updatedNurse) {
		try {
			Optional<Nurse> originalNurse = nurseRepository.findById(id);
			if (originalNurse.isPresent()) {
				Nurse nurse = originalNurse.get();
				if (updatedNurse.getName() != null) {
					nurse.setName(updatedNurse.getName());
				}
				if (updatedNurse.getUser() != null) {
					nurse.setUser(updatedNurse.getUser());
				}
				if (updatedNurse.getEmail() != null) {
					nurse.setEmail(updatedNurse.getEmail());
				}
				if (updatedNurse.getSurname() != null) {
					nurse.setSurname(updatedNurse.getSurname());
				}
				if (updatedNurse.getPass() != null) {
					nurse.setPass(updatedNurse.getPass());
				}
				nurseRepository.save(nurse);
				return ResponseEntity.ok().build();
			} else {
				return ResponseEntity.notFound().build();
			}
		} catch (Exception e) {
			return ResponseEntity.badRequest().build();
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteNurse(@PathVariable Long id) {
		if (nurseRepository.existsById(id)) {
			nurseRepository.deleteById(id);
			return ResponseEntity.ok().build();
		} else {
			return ResponseEntity.notFound().build();
		}
	}
	
	@PostMapping(
		    value = "/{id}/upload",
		    consumes = "multipart/form-data",
		    produces = "application/json"
		)
		public @ResponseBody ResponseEntity<Nurse> uploadFile(
		        @PathVariable Long id,
		        @RequestParam("file") MultipartFile file
		) {

		    Optional<Nurse> optionalNurse = nurseRepository.findById(id);

		    if (!optionalNurse.isPresent()) {
		        return ResponseEntity.notFound().build();
		    }

		    try {
		        String filePath = saveFile(file);

		        Nurse nurse = optionalNurse.get();
		        nurse.setImageUrl(filePath);
		        nurseRepository.save(nurse);

		        return ResponseEntity.ok(nurse);
		    } catch (Exception e) {
		        return ResponseEntity.badRequest().build();
		    }
		}

	private String saveFile(MultipartFile file) {
	    String folder = "uploads/";
	    Path path = Paths.get(folder);

	    try {
	        if (!Files.exists(path)) {
	            Files.createDirectories(path);
	        }

	        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
	        Path filePath = path.resolve(fileName);

	        file.transferTo(filePath.toFile());

	        return filePath.toString();

	    } catch (IOException e) {
	        throw new RuntimeException("Error al guardar el archivo", e);
	    }
	}
}
