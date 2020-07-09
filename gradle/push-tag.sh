#!/bin/sh

#Git push is implemented in the script to make sure we are not leaking GH key to the output
# Expects:
# 1. 'GIT_SECRET' env variable to be in format: user:github_access_token, for example: mockitoguy:qq43234xc23x23d24d
# 2. tag name as a parameter of this script

TAG_NAME=$1

echo "Running git push without output for security. If it fails make sure that GIT_SECRET env variable is set."
git push --quiet https://$GIT_SECRET@github.com/shipkit/org.shipkit.shipkit-changelog.git $TAG_NAME > /dev/null 2>&1
EXIT_CODE=$?
echo "'git push --quiet' exit code: $EXIT_CODE"
exit $EXIT_CODE