package com.biotechpay.lab.api;

import com.biotechpay.lab.application.ExerciseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exercises")
@CrossOrigin(origins = "*")
public class ExerciseController {

    private final ExerciseService exerciseService;

    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }

    @GetMapping("/{exerciseId}")
    public ResponseEntity<ExerciseService.ExerciseDetail> getExercise(@PathVariable String exerciseId) {
        return ResponseEntity.ok(exerciseService.getExerciseDetail(exerciseId));
    }

    @GetMapping("/{exerciseId}/hints/{index}")
    public ResponseEntity<ExerciseService.HintView> getHint(@PathVariable String exerciseId, @PathVariable int index) {
        return ResponseEntity.ok(exerciseService.getHint(exerciseId, index));
    }

    @GetMapping("/{exerciseId}/solution")
    public ResponseEntity<ExerciseService.SolutionView> getSolution(@PathVariable String exerciseId) {
        return ResponseEntity.ok(exerciseService.getSolution(exerciseId));
    }
}
