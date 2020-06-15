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

generateChangelog {
    fromRev = "v1.0.0" //required, no default value                    
    toRev = "HEAD" //optional, default                      
    outputFile = "$buildDir/changelog.md" //optional, default
}

//usage with auto-version plugin
apply plugin: "shipkit-auto-version"
apply plugin: "shipkit-changelog"

generateChangelog {
    fromRev = ext['shipkit-auto-version.previous-tag']
}

//posting with GitHub
postGitHubRelease {
    writeToken = System.getenv("GH_WRITE_TOKEN") //required, not default value
    releaseNotes = generateChangelog.outputFile //optional, default    
}
```