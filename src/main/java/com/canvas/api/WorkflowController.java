package com.canvas.api;

import com.canvas.domain.*;
import com.canvas.dto.WorkflowDtos;
import com.canvas.repo.*;
import com.canvas.workflow.GraphValidator;
import com.canvas.workflow.WorkflowOrchestrator;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class WorkflowController {
    private final WorkflowRepo workflowRepo;
    private final WorkflowRunRepo runRepo;
    private final NodeRunRepo nodeRunRepo;
    private final NodeLogRepo nodeLogRepo;
    private final GraphValidator validator;
    private final WorkflowOrchestrator orchestrator;
    private final ObjectMapper mapper;

    public WorkflowController(WorkflowRepo workflowRepo, WorkflowRunRepo runRepo, NodeRunRepo nodeRunRepo, NodeLogRepo nodeLogRepo,
                              GraphValidator validator, WorkflowOrchestrator orchestrator, ObjectMapper mapper) {
        this.workflowRepo = workflowRepo; this.runRepo = runRepo; this.nodeRunRepo = nodeRunRepo; this.nodeLogRepo = nodeLogRepo;
        this.validator = validator; this.orchestrator = orchestrator; this.mapper = mapper;
    }

    @PostMapping("/workflows")
    public WorkflowEntity createWorkflow(@Valid @RequestBody WorkflowDtos.WorkflowCreate req) throws Exception {
        validator.validate(req.graphJson());
        WorkflowEntity w = new WorkflowEntity();
        w.setName(req.name()); w.setGraphJson(mapper.writeValueAsString(req.graphJson()));
        return workflowRepo.save(w);
    }

    @GetMapping("/workflows")
    public List<WorkflowEntity> listWorkflows() { return workflowRepo.findAll(); }

    @PutMapping("/workflows/{id}")
    public WorkflowEntity updateWorkflow(@PathVariable String id, @Valid @RequestBody WorkflowDtos.WorkflowCreate req) throws Exception {
        WorkflowEntity w = workflowRepo.findById(id).orElseThrow();
        validator.validate(req.graphJson());
        w.setName(req.name()); w.setGraphJson(mapper.writeValueAsString(req.graphJson())); w.setVersion(w.getVersion() + 1);
        return workflowRepo.save(w);
    }

    @PostMapping("/workflows/{id}/validate")
    public Map<String, Object> validateWorkflow(@PathVariable String id) throws Exception {
        WorkflowEntity w = workflowRepo.findById(id).orElseThrow();
        try {
            validator.validate(mapper.readTree(w.getGraphJson()));
            return Map.of("valid", true);
        } catch (Exception e) {
            return Map.of("valid", false, "error", e.getMessage());
        }
    }

    @PostMapping("/workflows/{id}/runs")
    public Map<String, String> runWorkflow(@PathVariable String id, @RequestBody WorkflowDtos.RunCreate req) throws Exception {
        WorkflowEntity w = workflowRepo.findById(id).orElseThrow();
        String runId = orchestrator.run(w, req.inputs() == null ? Map.of() : req.inputs());
        return Map.of("run_id", runId);
    }

    @GetMapping("/runs/{runId}")
    public Map<String, Object> getRun(@PathVariable String runId) {
        return Map.of("run", runRepo.findById(runId).orElseThrow(), "node_runs", nodeRunRepo.findByRunId(runId));
    }

    @GetMapping("/runs/{runId}/events")
    public List<NodeLogEntity> events(@PathVariable String runId, @RequestParam(defaultValue = "0") Long lastId) {
        List<String> nodeRunIds = nodeRunRepo.findByRunId(runId).stream().map(NodeRunEntity::getId).toList();
        if (nodeRunIds.isEmpty()) return List.of();
        return nodeLogRepo.findByNodeRunIdInAndIdGreaterThanOrderByIdAsc(nodeRunIds, lastId);
    }
}
