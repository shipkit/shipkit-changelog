# Shipkit Changelog Gradle plugin

Minimalistic Gradle plugin that generates changelog based on commit history and GitHub pull requests/issues

## Design
     
Tasks:
    - changelog generation      
        - collects ticket IDs from commit messages between revs (previous version and HEAD)
        - call GitHub API to get PRs matching ticket IDs
    - posting release to GitHub

## Usage

### Realistic example

Realistic example, uses 'shipkit-auto-version' plugin (source: gradle/release.gradle)

```
    plugins {  
        id 'org.shipkit.shipkit-changelog'
        id 'org.shipkit.shipkit-gh-release'
        id 'org.shipkit.shipkit-auto-version'
    }

    tasks.named("generateChangelog") {
        previousRevision = "v" + project.ext.'shipkit-auto-version.previous-version'
        readOnlyToken = "a0a4c0f41c200f7c653323014d6a72a127764e17"
        repository = "shipkit/shipkit-changelog"
    }
    
    tasks.named("githubRelease") {
        dependsOn tasks.named("generateChangelog")
        repository = "shipkit/shipkit-changelog"
        changelog = tasks.named("generateChangelog").get().outputFile
        writeToken = System.getenv("GH_WRITE_TOKEN")
    }
```

### 'org.shipkit.shipkit-changelog' plugin

Basic task configuration (source: ChangelogPluginIntegTest)

```
    plugins {  
        id 'org.shipkit.shipkit-changelog'
    }
    
    tasks.named("generateChangelog") {
        previousRevision = "v3.3.10"
        readOnlyToken = "a0a4c0f41c200f7c653323014d6a72a127764e17"
        repository = "mockito/mockito"
    }
```

Complete task configuration (source: ChangelogPluginIntegTest)

```
    plugins {  
        id 'org.shipkit.shipkit-changelog'
    }
    
    tasks.named("generateChangelog") {
        //file where the release notes are generated, default as below
        outputFile = new File(buildDir, "changelog.md")
        
        //Working directory for running 'git' commands, default as below
        workingDir = project.projectDir                
        
        //GitHub url, configure if you use GitHub Enterprise, default as below
        ghUrl = "https://github.com"
        
        //GitHub API url, configure if you use GitHub Enterprise, default as below
        ghApiUrl = "https://api.github.com"
        
        //The release date, the default is today date 
        date = "2020-06-06"
        
        //Previous revision to generate changelog, *no default*
        previousRevision = "v3.3.10"
        
        //Current revision to generate changelog, default as below 
        revision = "HEAD" 
        
        //The release version, default as below
        version = project.version       
        
        //Token that enables querying GitHub, safe to check-in because it is read-only, *no default*              
        readOnlyToken = "a0a4c0f41c200f7c653323014d6a72a127764e17"
        
        //Repository to look for tickets, *no default*
        repository = "mockito/mockito"
    }              
```

### 'org.shipkit.shipkit-gh-release'

Basic task configuration (source: GitHubReleasePluginIntegTest)

```
    plugins {
        id 'org.shipkit.shipkit-gh-release'
    }
                
    tasks.named("githubRelease") {
        repository = "shipkit/shipkit-changelog"
        changelog = file("changelog.md")
        writeToken = "secret"
    }
```

Complete task configuration (source: GitHubReleasePluginIntegTest)

```
    plugins {
        id 'org.shipkit.shipkit-gh-release'
    }
                
    tasks.named("githubRelease") {
        //GitHub API url, configure if you use GitHub Enterprise, default as below
        ghApiUrl = "https://api.github.com"
        
        //Repository where to create a release, *no default*
        repository = "shipkit/shipkit-changelog"
        
        //The file with changelog (release notes), *no default*
        changelog = file("changelog.md")
        
        //The name of the release name, default as below
        releaseName = "v" + project.version
        
        //The release tag, default as below
        releaseName = "v" + project.version
        
        //GitHub write token, *no default*
        writeToken = "secret"
    }
``` 