package com.meli.obterdiploma.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.meli.obterdiploma.exception.StudentNotFoundException;
import com.meli.obterdiploma.model.StudentDTO;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

@Repository
public class StudentDAO implements IStudentDAO {

    private String SCOPE;
    private Set<StudentDTO> students;

    public StudentDAO() {
        Properties properties = new Properties();
        try {
            properties.load(new ClassPathResource("application.properties").getInputStream());
            this.SCOPE = properties.getProperty("api.scope");
            this.loadData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public StudentDTO save(StudentDTO student) {
        Optional<StudentDTO> studentDTO = students.stream()
                .filter(s -> Objects.equals(s.getId(), student.getId())).findFirst();
        if (student.getId() != null && studentDTO.isEmpty()) {
            throw new StudentNotFoundException(student.getId());
        }
        if (studentDTO.isEmpty()) {
            student.setId((this.students.size() + 1L));
        }
        students.add(student);
        this.saveData();
        return student;
    }

    @Override
    public void delete(Long id) {
        StudentDTO found = this.findById(id);
        students.remove(found);
        this.saveData();
    }

    public boolean exists(StudentDTO stu) {
        boolean ret = false;
        try {
            ret = this.findById(stu.getId()) != null;
        } catch (StudentNotFoundException e) {
        }
        return ret;
    }

    @Override
    public StudentDTO findById(Long id) {
        return students.stream()
                .filter(stu -> stu.getId().equals(id))
                .findFirst().orElseThrow(() -> new StudentNotFoundException(id));
    }

    private void loadData() {
        Set<StudentDTO> loadedData = new HashSet<>();
        ObjectMapper objectMapper = new ObjectMapper();
        File file;
        try {
            file = ResourceUtils.getFile("./src/" + SCOPE + "/resources/users.json");
            loadedData = objectMapper.readValue(file, new TypeReference<Set<StudentDTO>>() {
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Failed while initializing DB, check your resources files");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed while initializing DB, check your JSON formatting.");
        }
        this.students = loadedData;
    }

    private void saveData() {
        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        try {
            File file = ResourceUtils.getFile("./src/" + SCOPE + "/resources/users.json");
            objectMapper.writeValue(file, this.students);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Failed while writing to DB, check your resources files");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed while writing to DB, check your JSON formatting.");
        }
    }
}
