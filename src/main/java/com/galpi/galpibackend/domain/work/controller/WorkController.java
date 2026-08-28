package com.galpi.galpibackend.domain.work.controller;

import com.galpi.galpibackend.domain.work.dto.WorkResponse;
import com.galpi.galpibackend.domain.work.service.WorkService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/works")
public class WorkController {

    private final WorkService workService;

    public WorkController(WorkService workService) {
        this.workService = workService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkResponse> getWork(@PathVariable Long id) {
        WorkResponse response = workService.getWork(id);
        return ResponseEntity.ok(response);
    }
}
