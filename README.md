# madie-translator-commons

#### CQL-ELM Translator Support

| translator-commons | CQL-ELM Translator      |
|--------------------|-------------------------|
| 1.x.y              | \>= 3.3.2 and <= 3.14.0 |
| 2.x.y              | \>= 3.15.0              |


## Publishing SNAPSHOT for madie-fhir-elm-translator
- Create a PR with necessary changes against develop branch
- Make sure you update the package version example: <version>2.0.5-SNAPSHOT</version>
- On Merge to Develop a SNAPSHOT version will be published.

## Publishing a Release for madie-fhir-elm-translator
- Create a PR with necessary changes against main branch
- Make sure you update the package version example: <version>2.0.5</version>
- On Merge to main a release version will be published.

## Publishing a Release for madie-qdm-elm-translator
- Using the latest tag v1.x.y create a new branch with name as of the next release ex: if the Latest tag is 1.0.4 then based on that tag create a new branch "1.0.5"
- Using the latest tag create another feature branch ex:feature/MAT-1234 and push your changes.
- Make sure you update the package version (pom.xml) to: <version>1.0.5</version>
- Also update the release.yml file with the new version number, as per our example it is 1.0.5
- Create a PR for feature/MAT-1234 against the new branch 1.0.5
- On Merge to branch 1.0.5 a release version will be published and a new tag will be automatically generated.
