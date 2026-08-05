package com.mediahub.royalty.controller;

import com.mediahub.royalty.client.AuditClient;
import com.mediahub.royalty.model.RoyaltyStatement;
import com.mediahub.royalty.service.RoyaltyStatementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/royalty-statements")
public class RoyaltyStatementController {

    private static final Logger logger = LoggerFactory.getLogger(RoyaltyStatementController.class);
    private final RoyaltyStatementService service;

    @Autowired
    private AuditClient auditClient;

    public RoyaltyStatementController(RoyaltyStatementService service) {
        this.service = service;
    }

    private static String actorRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(a -> a.startsWith("ROLE_"))
            .map(a -> a.substring(5))
            .findFirst()
            .orElse("UNKNOWN");
    }

    @PreAuthorize("hasAuthority('royalty:approve')")
    @PostMapping
    public ResponseEntity<RoyaltyStatement> generateStatement(
            @RequestBody RoyaltyStatement statement,
            Authentication authentication) {
        logger.info("POST /api/royalty-statements - Generate statement for CreatorID: {}", statement.getCreatorID());
        RoyaltyStatement created = service.generateStatement(statement, Long.valueOf(authentication.getName()));
        logger.info("Statement generated successfully with ID: {}", created.getStatementID());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize("hasAuthority('royalty:view')")
    @GetMapping
    public ResponseEntity<List<RoyaltyStatement>> getAllStatements() {
        logger.info("GET /api/royalty-statements - Fetch all statements");
        List<RoyaltyStatement> statements = service.getAllStatements();
        logger.info("Retrieved {} statements", statements.size());
        return ResponseEntity.ok(statements);
    }

    @PreAuthorize("hasAuthority('royalty:view')")
    @GetMapping("/{statementID}")
    public ResponseEntity<RoyaltyStatement> getStatementById(
            @PathVariable int statementID) {
        logger.info("GET /api/royalty-statements/{} - Fetch statement by ID", statementID);
        RoyaltyStatement statement = service.getStatementById(statementID);
        logger.info("Statement with ID: {} retrieved", statementID);
        return ResponseEntity.ok(statement);
    }

    @PreAuthorize("hasAuthority('royalty:approve')")
    @PutMapping("/{statementID}/finalise")
    public ResponseEntity<Map<String, Object>> finaliseStatement(
            @PathVariable int statementID,
            Authentication authentication) {
        logger.info("PUT /api/royalty-statements/{}/finalise - Finalise statement", statementID);
        Map<String, Object> response = service.finaliseStatement(statementID, Long.valueOf(authentication.getName()));
        logger.info("Statement with ID: {} finalised", statementID);
        auditClient.log("STATEMENT_FINALISED", "ROYALTY", Long.valueOf(authentication.getName()),
            actorRole(authentication), "RoyaltyStatement", String.valueOf(statementID),
            "Finalised royalty statement", "LOW");
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('royalty:approve')")
    @PutMapping("/{statementID}/mark-paid")
    public ResponseEntity<Map<String, Object>> markAsPaid(
            @PathVariable int statementID,
            Authentication authentication) {
        logger.info("PUT /api/royalty-statements/{}/mark-paid - Mark statement as paid", statementID);
        Map<String, Object> response = service.markAsPaid(statementID, Long.valueOf(authentication.getName()));
        logger.info("Statement with ID: {} marked as paid", statementID);
        auditClient.log("STATEMENT_MARKED_PAID", "ROYALTY", Long.valueOf(authentication.getName()),
            actorRole(authentication), "RoyaltyStatement", String.valueOf(statementID),
            "Marked royalty statement as paid", "MEDIUM");
        return ResponseEntity.ok(response);
    }
 // ✅ ANALYTICS API
    @PreAuthorize("hasAuthority('report:view')")
    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getRoyaltyAnalytics() {

        logger.info(
                "GET /api/royalty-statements/analytics");

        return ResponseEntity.ok(
                service.getRoyaltyAnalytics());
    }
}
