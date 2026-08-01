package com.mediahub.royalty.controller;

import com.mediahub.royalty.model.RoyaltyStatement;
import com.mediahub.royalty.service.RoyaltyStatementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/royalty-statements")
public class RoyaltyStatementController {

    private static final Logger logger = LoggerFactory.getLogger(RoyaltyStatementController.class);
    private final RoyaltyStatementService service;

    public RoyaltyStatementController(RoyaltyStatementService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<RoyaltyStatement> generateStatement(
            @RequestBody RoyaltyStatement statement) {
        logger.info("POST /api/royalty-statements - Generate statement for CreatorID: {}", statement.getCreatorID());
        RoyaltyStatement created = service.generateStatement(statement);
        logger.info("Statement generated successfully with ID: {}", created.getStatementID());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<RoyaltyStatement>> getAllStatements() {
        logger.info("GET /api/royalty-statements - Fetch all statements");
        List<RoyaltyStatement> statements = service.getAllStatements();
        logger.info("Retrieved {} statements", statements.size());
        return ResponseEntity.ok(statements);
    }

    @GetMapping("/{statementID}")
    public ResponseEntity<RoyaltyStatement> getStatementById(
            @PathVariable int statementID) {
        logger.info("GET /api/royalty-statements/{} - Fetch statement by ID", statementID);
        RoyaltyStatement statement = service.getStatementById(statementID);
        logger.info("Statement with ID: {} retrieved", statementID);
        return ResponseEntity.ok(statement);
    }

    @PutMapping("/{statementID}/finalise")
    public ResponseEntity<Map<String, Object>> finaliseStatement(
            @PathVariable int statementID) {
        logger.info("PUT /api/royalty-statements/{}/finalise - Finalise statement", statementID);
        Map<String, Object> response = service.finaliseStatement(statementID);
        logger.info("Statement with ID: {} finalised", statementID);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{statementID}/mark-paid")
    public ResponseEntity<Map<String, Object>> markAsPaid(
            @PathVariable int statementID) {
        logger.info("PUT /api/royalty-statements/{}/mark-paid - Mark statement as paid", statementID);
        Map<String, Object> response = service.markAsPaid(statementID);
        logger.info("Statement with ID: {} marked as paid", statementID);
        return ResponseEntity.ok(response);
    }
 // ✅ ANALYTICS API
    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getRoyaltyAnalytics() {

        logger.info(
                "GET /api/royalty-statements/analytics");

        return ResponseEntity.ok(
                service.getRoyaltyAnalytics());
    }
}
