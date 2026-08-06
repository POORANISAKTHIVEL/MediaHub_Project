package com.mediahub.contentcatalog.seed;

import com.mediahub.contentcatalog.entity.ContentAsset;
import com.mediahub.contentcatalog.enums.ContentType;
import com.mediahub.contentcatalog.repository.ContentAssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

// Inserts a handful of demo Video/Image/Article rows on startup, backing the Content Catalog's
// local-file preview feature. Idempotent by title, so re-running/restarting never duplicates rows.
@Component
public class DemoDataSeeder implements CommandLineRunner {

    @Autowired
    private ContentAssetRepository contentAssetRepository;

    @Override
    public void run(String... args) {
        seed("Space Documentary", ContentType.Video, "Documentary", "English", 720,
                "A sweeping look at humanity's push into orbit and beyond.", "Videos/space-documentary.mp4");
        seed("Future Cities", ContentType.Video, "Documentary", "English", 540,
                "How tomorrow's urban centers are being designed today.", "Videos/future-cities.mp4");
        seed("AI Landscape", ContentType.Image, "Digital Art", "English", 0,
                "A generative-art landscape exploring AI-assisted imagery.", "Images/ai-landscape.jpg");
        seed("Nature Gallery", ContentType.Image, "Photography", "English", 0,
                "A curated gallery of natural landscapes.", "Images/nature-gallery.jpg");
        seed("Future of Streaming Platforms", ContentType.Article, "Technology", "English", 0,
                "An analysis of where streaming platforms are headed next.", "Articles/future-of-streaming-platforms.html");
        seed("Introduction to Digital Rights Management", ContentType.Article, "Technology", "English", 0,
                "A primer on how DRM protects licensed digital content.", "Articles/introduction-to-digital-rights-management.html");
    }

    private void seed(String title, ContentType type, String genre, String language,
                       int durationSeconds, String synopsis, String filePath) {
        if (contentAssetRepository.existsByTitle(title)) {
            return;
        }
        ContentAsset asset = new ContentAsset();
        asset.setCreatorId(1);
        asset.setTitle(title);
        asset.setType(type);
        asset.setGenre(genre);
        asset.setLanguage(language);
        asset.setDurationSeconds(durationSeconds);
        asset.setSynopsis(synopsis);
        asset.setFilePath(filePath);
        asset.setThumbnailPath("");
        asset.setStatus("Published");
        contentAssetRepository.save(asset);
    }
}
