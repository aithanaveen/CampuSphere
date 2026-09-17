package com.smartcampus.service;

import com.smartcampus.entity.Student;
import com.smartcampus.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Service
public class StudentService {
	
	private final PasswordEncoder passwordEncoder;

    private final StudentRepository studentRepository;

    public StudentService(
            StudentRepository studentRepository,
            PasswordEncoder passwordEncoder) {

        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Student createStudent(Student student) {

        student.setPassword(
                passwordEncoder.encode(student.getPassword()));

        if (student.getRole() == null || student.getRole().isBlank()) {
            student.setRole("STUDENT");
        }

        return studentRepository.save(student);
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Student not found"));
    }

    public Student updateStudent(Long id, Student updatedStudent) {

        Student existingStudent = getStudentById(id);

        existingStudent.setName(updatedStudent.getName());
        existingStudent.setEmail(updatedStudent.getEmail());
        existingStudent.setDepartment(updatedStudent.getDepartment());
        existingStudent.setYear(updatedStudent.getYear());
        existingStudent.setInterests(updatedStudent.getInterests());

        return studentRepository.save(existingStudent);
    }

    public Student updateProfileByEmail(String email, Student updatedStudent) {
        Student existingStudent = studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (updatedStudent.getName() != null && !updatedStudent.getName().isBlank()) {
            existingStudent.setName(updatedStudent.getName());
        }
        if (updatedStudent.getDepartment() != null) {
            existingStudent.setDepartment(updatedStudent.getDepartment());
        }
        if (updatedStudent.getYear() != null) {
            existingStudent.setYear(updatedStudent.getYear());
        }
        existingStudent.setInterests(updatedStudent.getInterests());

        return studentRepository.save(existingStudent);
    }

    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }
}