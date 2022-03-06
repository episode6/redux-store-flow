## Redux StoreFlow Release Checklist

(we should be able to automate most of this eventually)

### Cut new Release Branch

1. Ensure main branch is green
2. `git checkout -b release/v<VERSION>`
3. Push/track empty branch

### Version bump PRs

- Create 2 PRs to bump version
    - `[VERSION] Snapshot v<version>` points at `main`
    - `[VERSION] Release v<version>` points at new release branch
    - Update version in files:
        - `build.gradle.kts`

### Harden Release Branch

- Fix any bugs on the `main` branch first then cherry-pick (via PR) into release branch

### Release

1. Wait for green builds on release branch
2. Fetch tags: `git fetch origin --tags`
3. Create new release tag: `git tag -a v<VERSION>`
4. Push tags: `git push --tags`
5. Build tag on jenkins and deploy result on sonatype

### Sync Docs PR

1. **IMPORTANT** POINT GITHUB PAGES TO NEW BRANCH NOW
    - pages will not re-publish until we push to the branch again
2. Run `./gradlew syncDocs`
3. If there are no changes in docs, make a non-visible change to a README
    - we need a commit to trigger github pages generation
4. Point PR to new release branch: `[DOCS] Sync v<version>`

### Hotfixes

- We do not cut new release branches for hotfixes, instead we append to the effected release branch and add a new
  release tag
- All fixes (including hotfixes) should be applied to the `main` branch first whenever possible and cherry-picked onto
  the appropriate release-branch for a hotfix.
