# Fastlane Setup for F-Droid

This directory contains the Fastlane configuration for TempBox Beta to support F-Droid deployment.

## Structure

```
fastlane/
├── Appfile                 # App configuration
├── Fastfile                # Build lanes for F-Droid
├── README.md               # This file
└── metadata/
    └── android/
        └── en-US/
            ├── title.txt                    # App title
            ├── summary.txt                  # Short description
            ├── description.txt              # Full description
            ├── changelogs/
            │   └── 1.txt                   # Version 1.0 changelog
            └── images/
                ├── icon.png                # App icon
                └── phoneScreenshots/       # App screenshots
                    ├── 1.png
                    ├── 2.png
                    ├── 3.png
                    ├── 4.png
                    └── logo.png
```

## F-Droid Integration

This Fastlane setup enables F-Droid to:

1. **Automatically extract app metadata** from the `metadata/android/en-US/` directory
2. **Build and deploy** the app using the configuration in `Fastfile`
3. **Update app descriptions and screenshots** without manual intervention
4. **Handle version updates** and changelog management

## Usage

F-Droid will automatically detect and use this Fastlane configuration when building the app.

For manual testing:
```bash
fastlane android fdroid
```

## Metadata Updates

To update app metadata:
1. Edit the files in `metadata/android/en-US/`
2. Commit and push changes
3. F-Droid will automatically pick up the updates 