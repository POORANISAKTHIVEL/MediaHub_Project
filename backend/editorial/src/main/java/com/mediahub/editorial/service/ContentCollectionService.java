package com.mediahub.editorial.service;

import com.mediahub.editorial.model.ContentCollection;
import com.mediahub.editorial.repository.ContentCollectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ContentCollectionService {

    @Autowired
    private ContentCollectionRepository repository;

    // API 7 — Create collection
    public Map<String, Object> createCollection(
            ContentCollection collection) {
        Map<String, Object> response = new HashMap<>();
        if (collection.getName() == null
                || collection.getName().isEmpty()) {
            response.put("error", "Name is required");
            response.put("statusCode", 400);
            return response;
        }
        String cat = collection.getCategory();
        if (cat == null || (!cat.equals("Featured")
                && !cat.equals("Trending")
                && !cat.equals("Curated")
                && !cat.equals("New"))) {
            response.put("error",
                "Category must be Featured Trending Curated or New");
            response.put("statusCode", 400);
            return response;
        }
        if (collection.getContentIDs() == null
                || collection.getContentIDs().isEmpty()) {
            response.put("error", "ContentIDs are required");
            response.put("statusCode", 400);
            return response;
        }
        if (collection.getPublishDate() == null
                || collection.getExpiryDate() == null) {
            response.put("error", "Dates are required");
            response.put("statusCode", 400);
            return response;
        }
        collection.setStatus("Scheduled");
        ContentCollection saved = repository.save(collection);
        response.put("collectionID", saved.getCollectionID());
        response.put("name",         saved.getName());
        response.put("category",     saved.getCategory());
        response.put("contentIDs",   saved.getContentIDs());
        response.put("publishDate",  saved.getPublishDate());
        response.put("expiryDate",   saved.getExpiryDate());
        response.put("status",       "Scheduled");
        response.put("message",      "Collection created successfully.");
        response.put("statusCode",   201);
        return response;
    }

    // API 8 — Get all collections
    public List<ContentCollection> getAllCollections() {
        return repository.findAll();
    }

    // API 9 — Get collection by ID
    public Map<String, Object> getCollectionById(int collectionID) {
        Map<String, Object> response = new HashMap<>();
        Optional<ContentCollection> opt =
                repository.findById(collectionID);
        if (opt.isPresent()) {
            response.put("collection", opt.get());
            response.put("statusCode", 200);
        } else {
            response.put("error",
                "Collection not found with ID: " + collectionID);
            response.put("statusCode", 404);
        }
        return response;
    }

    // API 10 — Update full collection
    public Map<String, Object> updateCollection(
            int collectionID, ContentCollection collection) {
        Map<String, Object> response = new HashMap<>();
        Optional<ContentCollection> opt =
                repository.findById(collectionID);
        if (opt.isPresent()) {
            ContentCollection existing = opt.get();
            existing.setName(collection.getName());
            existing.setCategory(collection.getCategory());
            existing.setContentIDs(collection.getContentIDs());
            existing.setPublishDate(collection.getPublishDate());
            existing.setExpiryDate(collection.getExpiryDate());
            repository.save(existing);
            response.put("collectionID", collectionID);
            response.put("message",
                "Collection updated successfully.");
            response.put("statusCode", 200);
        } else {
            response.put("error", "Collection not found");
            response.put("statusCode", 404);
        }
        return response;
    }

    // API 11 — Expire collection
    public Map<String, Object> expireCollection(int collectionID) {
        Map<String, Object> response = new HashMap<>();
        Optional<ContentCollection> opt =
                repository.findById(collectionID);
        if (opt.isPresent()) {
            ContentCollection existing = opt.get();
            existing.setStatus("Expired");
            repository.save(existing);
            response.put("collectionID", collectionID);
            response.put("status",       "Expired");
            response.put("message",
                "Collection expired successfully.");
            response.put("statusCode", 200);
        } else {
            response.put("error", "Collection not found");
            response.put("statusCode", 404);
        }
        return response;
    }

    // API 12 — Delete collection
    public Map<String, Object> deleteCollection(int collectionID) {
        Map<String, Object> response = new HashMap<>();
        Optional<ContentCollection> opt =
                repository.findById(collectionID);
        if (opt.isPresent()) {
            if ("Active".equals(opt.get().getStatus())) {
                response.put("error",
                    "Cannot delete Active collection. Expire it first.");
                response.put("statusCode", 400);
                return response;
            }
            repository.deleteById(collectionID);
            response.put("collectionID", collectionID);
            response.put("message",
                "Collection deleted successfully.");
            response.put("statusCode", 200);
        } else {
            response.put("error", "Collection not found");
            response.put("statusCode", 404);
        }
        return response;
    }
}
