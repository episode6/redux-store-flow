# Ship Release Branch Skill

This directory contains the skill definition for shipping a release branch by publishing a GitHub release pointing to the tip of the release branch.

## Contents
- `skill.json`: Metadata about the skill.
- `SKILL.md`: Detailed instructions and steps for the agent to follow.

## Usage
When tasked with shipping a release branch, the agent should follow the steps outlined in `SKILL.md` to ensure that the version name is correctly parsed from the repository, the release notes are extracted from the changelog, and the GitHub release is published using the `gh` CLI.
