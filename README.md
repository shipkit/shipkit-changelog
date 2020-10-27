[![CI](https://github.com/shipkit/shipkit-changelog/workflows/CI/badge.svg)](https://github.com/shipkit/shipkit-changelog/actions)

# Shipkit Changelog Gradle plugin

Minimalistic Gradle plugin that generates changelog based on commit history and GitHub pull requests/issues. 
Optionally, the changelog content can be posted to GitHub Releases.
This plugin is very small (<1kloc) and has a single dependency "com.eclipsesource.minimal-json:minimal-json:0.9.5".
The dependency is very small (30kb), stable (no changes since 2017), and brings zero transitive dependencies.

Example ([more examples](https://github.com/shipkit/shipkit-changelog/releases)):

----
#### 0.0.7
 - 2020-07-15 - [1 commit(s)](https://github.com/shipkit/shipkit-changelog/compare/v0.0.6...v0.0.7) by Szczepan Faber
 - Fixed broken links [(#12)](https://github.com/shipkit/shipkit-changelog/pull/12)
----

## Basic usage

```
plugins {  
    id 'org.shipkit.shipkit-changelog'
}

tasks.named("generateChangelog") {
    previousRevision = "v0.0.1"
    readOnlyToken = "a0a4c0f41c200f7c653323014d6a72a127764e17"
    repository = "shipkit/shipkit-changelog"
}

```

## Realistic example

Realistic example, also uses a sibling plugin [shipkit-auto-version](https://github.com/shipkit/shipkit-auto-version) plugin
(source: [gradle/release.gradle](https://github.com/shipkit/shipkit-changelog/blob/master/gradle/release.gradle))

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

## Configuration reference

The standard way to enable automated tasks read/write to GitHub are [personal access tokens](https://docs.github.com/en/free-pro-team@latest/github/authenticating-to-github/creating-a-personal-access-token#creating-a-token).
When creating the tokens, please select the following token **scopes** ([more info on scopes](https://docs.github.com/en/free-pro-team@latest/developers/apps/scopes-for-oauth-apps)):

- readOnlyToken - should have **no scope**, this way it only provides read-only access to **public** repositories
(it **does not** provide read-only access to private repositories).
- writeToken - needs 'repo/public_repo' scope to post releases via GH REST API.

## Customers / sample projects

- https://github.com/shipkit/shipkit-demo (great example/reference project)
- https://github.com/shipkit/shipkit-changelog (this project)
- https://github.com/shipkit/shipkit-auto-version

## Other plugins/tools

There are other Gradle plugins or tools that provide similar functionality:

1. [github-changelog-generator](https://github.com/github-changelog-generator/github-changelog-generator)
is a popular Ruby Gem (6K stars on GitHub) that generates changelog based on commits/pull requests
but does not publish GitHub releases ([#56](https://github.com/github-changelog-generator/github-changelog-generator/issues/56)).
Our plugin is a pure Gradle solution and it can publish a GitHub release.

2. GitHub Action [Release Drafter](https://github.com/marketplace/actions/release-drafter)
drafts the next release notes as pull requests are merged.
This is a good option when the team wants to release on demand. 
Our plugin is great for fully automated releases on every merged pull request.

3. Gradle Plugin [git-changelog-gradle-plugin](https://github.com/tomasbjerre/git-changelog-gradle-plugin)
seems like a nice plugin, maintained but not very popular (<50 stars on GitHub) and pulls in a lot of other dependencies
([#21](https://github.com/tomasbjerre/git-changelog-gradle-plugin/issues/21)).
Our plugin is simpler, smaller and brings only one dependency (that is very small, simple and has no transitive dependencies).

4. Semantic Release [semantic-release](https://github.com/semantic-release/semantic-release)
is a npm module for fully automated "semantic" releases, with changelog generation.  
It has impressive 10K stars on GitHub.
Our plugin is less opinionated, smaller, simpler and a pure Gradle solution.

Pick the best tool that work for you and start automating releases and changelog generation!

## Design
     
### Changelog generation

1. Collect commits between 2 revisions
2. Find ticket IDs based on '#' prefix in commit messages (e.g. looking for #1, #50, etc.)
3. Use GitHub REST API to collect ticket information (issue or pull request) from GitHub
4. Create markdown file using the PR/issue titles 

### Posting GitHub releases

Uses GitHub REST API to post releases. 

## Usage

### 'org.shipkit.shipkit-changelog' plugin

Basic task configuration
(source: [ChangelogPluginIntegTest](https://github.com/shipkit/shipkit-changelog/blob/master/src/integTest/groovy/org/shipkit/changelog/ChangelogPluginIntegTest.groovy))

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

Complete task configuration
(source: [ChangelogPluginIntegTest](https://github.com/shipkit/shipkit-changelog/blob/master/src/integTest/groovy/org/shipkit/changelog/ChangelogPluginIntegTest.groovy))

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

Basic task configuration
(source: [GitHubReleasePluginIntegTest](https://github.com/shipkit/shipkit-changelog/blob/master/src/integTest/groovy/org/shipkit/gh/release/GitHubReleasePluginIntegTest.groovy))

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

Complete task configuration
(source: [GitHubReleasePluginIntegTest](https://github.com/shipkit/shipkit-changelog/blob/master/src/integTest/groovy/org/shipkit/gh/release/GitHubReleasePluginIntegTest.groovy))

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
