# Fastlane for TempBox Beta

This directory contains the fastlane configuration for TempBox Beta, specifically prepared for F-Droid submission.

## 📋 F-Droid Submission Process

### 1. Metadata Structure
```
fastlane/
├── Appfile                    # App identification
├── Fastfile                   # Build automation
├── README.md                  # This file
└── metadata/
    └── android/
        └── en-US/
            ├── title.txt              # App name
            ├── short_description.txt  # Brief description
            ├── description.txt        # Full description
            ├── summary.txt           # App summary
            ├── changelogs/
            │   └── 1.txt             # Version 1.0 changelog
            └── images/
                ├── icon.png           # App icon
                └── phoneScreenshots/  # Screenshots
                    ├── 1.png
                    ├── 2.png
                    ├── 3.png
                    └── 4.png
```

### 2. F-Droid Metadata File
The main F-Droid metadata file is located at:
```
metadata/com.samyak.tempboxbeta.yml
```

### 3. Available Fastlane Lanes

#### Build Debug APK
```bash
fastlane build_debug
```

#### Build Release APK
```bash
fastlane build_release
```

#### Run Tests
```bash
fastlane test
```

#### Prepare for F-Droid Submission
```bash
fastlane fdroid
```

#### Validate Metadata
```bash
fastlane prepare_metadata
```

## 🚀 F-Droid Submission Steps

1. **Fork F-Droid Data Repository**
   ```bash
   # Go to https://gitlab.com/fdroid/fdroiddata
   # Click "Fork" to create your own copy
   ```

2. **Add Metadata File**
   ```bash
   # Copy metadata/com.samyak.tempboxbeta.yml to your fork
   cp metadata/com.samyak.tempboxbeta.yml /path/to/fdroiddata/metadata/
   ```

3. **Submit Merge Request**
   - Create a merge request on GitLab
   - Include a clear description of your app
   - Reference any related issues

4. **Wait for Review**
   - F-Droid maintainers will review your submission
   - Address any feedback or requested changes
   - Once approved, your app will be built and published

## 📱 App Information

- **Package Name**: com.samyak.tempboxbeta
- **App Name**: TempBox Beta
- **Version**: 1.0 (versionCode 1)
- **License**: MIT
- **Category**: Internet
- **Source Code**: https://github.com/samyak2403/TempBoxBeta

## 🔧 F-Droid Requirements Met

✅ **Open Source License**: MIT License  
✅ **No Proprietary Dependencies**: All dependencies are open source  
✅ **No Tracking**: Zero analytics or user tracking  
✅ **No Ads**: Clean, ad-free experience  
✅ **Builds from Source**: Gradle build system  
✅ **Public Repository**: Available on GitHub  

## 📞 Support

For questions about F-Droid submission:
- **Issues**: https://github.com/samyak2403/TempBoxBeta/issues
- **Email**: samyakkamble2403@gmail.com
- **F-Droid Documentation**: https://f-droid.org/docs/

## 🏗️ Build Requirements

- **Minimum SDK**: 23 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Compile SDK**: 35
- **Java Version**: 11
- **Gradle**: 8.0+
- **Android Studio**: Arctic Fox or newer 