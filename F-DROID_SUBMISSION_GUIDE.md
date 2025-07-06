# F-Droid Submission Guide 2025 - TempBox Beta

## ✅ Pre-Submission Checklist (COMPLETED)

- [x] **App builds successfully** with `./gradlew build`
- [x] **Fastlane metadata structure** complete in `fastlane/metadata/android/en-US/`
- [x] **F-Droid YML file** ready: `com.samyak.tempboxbeta.yml`
- [x] **Screenshots and icon** properly placed
- [x] **Changelog for version 1.0** created
- [x] **Branch `fdroid-submission`** pushed to GitHub
- [x] **License file** present (MIT)
- [x] **No proprietary dependencies** (all FOSS)

## 🚀 Submission Process

### Step 1: Fork F-Droid Data Repository
1. Go to: https://gitlab.com/fdroid/fdroiddata
2. Click "Fork" to create your own copy
3. Clone your fork locally:
   ```bash
   git clone https://gitlab.com/YOUR_USERNAME/fdroiddata.git
   cd fdroiddata
   ```

### Step 2: Add Your App Metadata
1. Copy your `com.samyak.tempboxbeta.yml` file to the `metadata/` directory:
   ```bash
   cp /path/to/your/com.samyak.tempboxbeta.yml metadata/
   ```

### Step 3: Create Merge Request
1. Create a new branch in fdroiddata:
   ```bash
   git checkout -b add-tempbox-beta
   git add metadata/com.samyak.tempboxbeta.yml
   git commit -m "Add TempBox Beta - Professional temporary email client"
   git push origin add-tempbox-beta
   ```

2. Go to GitLab and create a Merge Request from your branch to the main fdroiddata repository

### Step 4: Merge Request Details
Fill out the merge request with:

**Title:** `Add TempBox Beta - Professional temporary email client`

**Description:**
```
# TempBox Beta - Professional Temporary Email Client

## App Details
- **Package Name:** com.samyak.tempboxbeta
- **Version:** 1.0
- **License:** MIT
- **Category:** Email, Internet
- **Repository:** https://github.com/samyak2403/TempBoxBeta

## Features
- Gmail-inspired modern UI with Material Design 3
- Professional WebView with enterprise-grade security
- Zero JavaScript execution for maximum safety
- Smart HTML sanitization and content filtering
- Privacy-focused with no tracking or analytics
- Uses mail.tm service for temporary email addresses

## Security & Privacy
- JavaScript completely disabled in WebView
- Advanced HTML sanitization
- External link blocking
- SSL/TLS validation with strict certificate verification
- No tracking or analytics
- Encrypted local storage

## Build Information
- Target SDK: 34
- Min SDK: 23
- Build Tools: Gradle with no proprietary dependencies
- All dependencies are FOSS compatible
- Builds successfully on fdroid-submission branch

## Testing
- App has been thoroughly tested
- All features work correctly
- No crashes or security issues
- Professional quality suitable for F-Droid

## Author
- **Name:** Samyak Kamble
- **Email:** samyakkamble6659@gmail.com
- **GitHub:** https://github.com/samyak2403
```

## 📱 Your App Metadata Summary

**Repository:** https://github.com/samyak2403/TempBoxBeta  
**Branch:** fdroid-submission  
**Package:** com.samyak.tempboxbeta  
**Version:** 1.0 (versionCode: 1)  
**License:** MIT  
**Author:** Samyak Kamble (samyakkamble6659@gmail.com)  

## 🔍 F-Droid Review Process

1. **Automated Checks:** F-Droid will automatically check if your app builds
2. **Manual Review:** F-Droid maintainers will review your app for compliance
3. **Feedback:** You may receive feedback or requests for changes
4. **Approval:** Once approved, your app will be available in F-Droid

## 📞 Support

If you need help during the submission process:
- **Email:** samyakkamble6659@gmail.com
- **F-Droid Forum:** https://forum.f-droid.org/
- **F-Droid Matrix:** #fdroid:f-droid.org

## 🎉 Post-Submission

After successful submission:
- Your app will be available in F-Droid within 24-48 hours
- Users can install TempBox Beta directly from F-Droid
- Updates will be automatically built when you push new tagged versions

---

**Good luck with your F-Droid submission! 🚀** 