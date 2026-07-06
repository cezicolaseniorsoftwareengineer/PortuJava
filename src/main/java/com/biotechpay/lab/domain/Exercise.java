package com.biotechpay.lab.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exercises")
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String exerciseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    @JsonIgnore
    private LearningModule module;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 8000)
    private String statementMarkdown;

    @Column(nullable = false, length = 4000)
    private String codeContract;

    @Column(nullable = false, length = 4000)
    private String starterCode;

    @Column(length = 4000)
    private String referenceSolution;

    @Column(length = 30)
    private String difficulty;

    private int sortOrder;
    private int estimatedMinutes = 10;

    @OneToMany(mappedBy = "exercise", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("sortOrder ASC")
    private List<TestCase> testCases = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "exercise_hints", joinColumns = @JoinColumn(name = "exercise_id"))
    @OrderColumn(name = "hint_order")
    @Column(name = "hint_text", length = 2000)
    private List<String> hints = new ArrayList<>();

    public Exercise() {}

    public Exercise(String exerciseId, LearningModule module, String title, String statementMarkdown,
                     String codeContract, String starterCode, String difficulty, int sortOrder, int estimatedMinutes) {
        this.exerciseId = exerciseId;
        this.module = module;
        this.title = title;
        this.statementMarkdown = statementMarkdown;
        this.codeContract = codeContract;
        this.starterCode = starterCode;
        this.difficulty = difficulty;
        this.sortOrder = sortOrder;
        this.estimatedMinutes = estimatedMinutes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getExerciseId() { return exerciseId; }
    public void setExerciseId(String exerciseId) { this.exerciseId = exerciseId; }

    public LearningModule getModule() { return module; }
    public void setModule(LearningModule module) { this.module = module; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getStatementMarkdown() { return statementMarkdown; }
    public void setStatementMarkdown(String statementMarkdown) { this.statementMarkdown = statementMarkdown; }

    public String getCodeContract() { return codeContract; }
    public void setCodeContract(String codeContract) { this.codeContract = codeContract; }

    public String getStarterCode() { return starterCode; }
    public void setStarterCode(String starterCode) { this.starterCode = starterCode; }

    public String getReferenceSolution() { return referenceSolution; }
    public void setReferenceSolution(String referenceSolution) { this.referenceSolution = referenceSolution; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

    public int getEstimatedMinutes() { return estimatedMinutes > 0 ? estimatedMinutes : 10; }
    public void setEstimatedMinutes(int estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }

    public List<TestCase> getTestCases() { return testCases; }
    public void setTestCases(List<TestCase> testCases) { this.testCases = testCases; }

    public List<String> getHints() { return hints; }
    public void setHints(List<String> hints) { this.hints = hints; }
}
