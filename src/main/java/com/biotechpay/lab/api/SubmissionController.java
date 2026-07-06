package com.biotechpay.lab.api;

import com.biotechpay.lab.application.JavaCodeCompiler;
import com.biotechpay.lab.application.SubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/submissions")
@CrossOrigin(origins = "*")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping
    public ResponseEntity<SubmissionService.SubmissionResult> submit(@RequestBody SubmitRequest request) {
        return ResponseEntity.ok(submissionService.submit(request.exerciseId(), request.code()));
    }

    @PostMapping("/scratch-run")
    public ResponseEntity<JavaCodeCompiler.CompileAndRunResult> scratchRun(@RequestBody ScratchRunRequest request) {
        return ResponseEntity.ok(submissionService.scratchRun(request.code()));
    }

    @GetMapping("/exercise/{exerciseId}/history")
    public ResponseEntity<List<SubmissionService.SubmissionHistoryEntry>> history(@PathVariable String exerciseId) {
        return ResponseEntity.ok(submissionService.getHistory(exerciseId));
    }

    public record SubmitRequest(String exerciseId, String code) {}

    public record ScratchRunRequest(String code) {}
}
