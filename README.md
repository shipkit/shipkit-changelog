# Shipkit Changelog Gradle plugin

Minimalistic Gradle plugin that generates changelog based on commit history and GitHub pull requests/issues

## Design
     
- plugin adds tasks:
    - generateChangelog      
        - collects ticket IDs from commit messages between revs (previous tag and HEAD)
        - call GitHub API to get PRs matching ticket IDs
    - postGitHubRelease
        - post a new release to GitHub

## Usage

```
//example usage
apply plugin: "shipkit-changelog"

tasks.named("generateChangelog") {
    fromRev = "v1.0.0" //required, no default value                    
    toRev = "HEAD" //optional, default                      
    outputFile = file("$buildDir/changelog.md") //optional, default
}

//usage with auto-version plugin
apply plugin: "shipkit-auto-version"
apply plugin: "shipkit-changelog"

tasks.named("generateChangelog") {
    fromRev = ext['shipkit-auto-version.previous-tag']
}

//posting with GitHub
tasks.named("githubRelease") {
    writeToken = System.getenv("GH_WRITE_TOKEN") //required, no default value
    releaseNotes = tasks.named("generateChangelog").get().outputFile //optional, default    
}
```