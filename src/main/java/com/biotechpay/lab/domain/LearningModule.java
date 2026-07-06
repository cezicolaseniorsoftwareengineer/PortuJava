package com.biotechpay.lab.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "modules")
public class LearningModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String moduleCode;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 30)
    private String paradigm;

    @Column(length = 2000)
    private String description;

    private int sortOrder;

    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("sortOrder ASC")
    private List<Exercise> exercises = new ArrayList<>();

    public LearningModule() {}

    public LearningModule(String moduleCode, String title, String paradigm, String description, int sortOrder) {
        this.moduleCode = moduleCode;
        this.title = title;
        this.paradigm = paradigm;
        this.description = description;
        this.sortOrder = sortOrder;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getModuleCode() { return moduleCode; }
    public void setModuleCode(String moduleCode) { this.moduleCode = moduleCode; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getParadigm() { return paradigm; }
    public void setParadigm(String paradigm) { this.paradigm = paradigm; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

    public List<Exercise> getExercises() { return exercises; }
    public void setExercises(List<Exercise> exercises) { this.exercises = exercises; }
}
