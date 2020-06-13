# Shipkit Changelog Gradle plugin

Minimalistic Gradle plugin that generates changelog based on commit history and GitHub pull requests/issues

## Design

- identify previous version by looking at release tags and finding latest
    - this code exists in shipkit-auto-version plugin, need to expose a library and share the code       
- collect ticket IDs from commit messages between the previous version and HEAD
- call GitHub API to get PRs matching ticket IDs
- post to new release with release notes to GitHub 