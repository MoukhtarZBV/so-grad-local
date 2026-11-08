# Orders self-hosted lab: initial repository

This folder is a separate repository root. It uses the same baseline application as
the Maven lab with Gradle, JaCoCo XML and a Jenkins pipeline. Use JDK 21.
Community Build supports this main-branch lab; it does not provide branch/PR analysis.

## 1. Start the local evaluation instance

From this folder with Docker Desktop (Linux containers) running:

```powershell
docker compose up -d
docker compose logs --tail 80 sonarqube
```

Open http://localhost:9000, wait until ready, sign in with admin/admin and change the
password. This compose setup has named volumes, an embedded database and relaxed
bootstrap checks. It is for local learning, not a production deployment.
The `community` image tag moves; record `docker compose images` if comparing runs.

## 2. Configure a project and coverage-only gate

Create a local project with key `orders-gradle-lab`, and generate its analysis token.
In Quality Gates, create a custom gate `Lab coverage`, with one condition:
**overall Coverage must be at least 80%** (fail when less than 80).
Choose overall-code coverage, not new-code coverage, to avoid baseline/version
ambiguity in this main-branch replay. Associate this gate with the project.
Do not add other conditions until you have completed the coverage exercise.

## 3. Run the baseline analysis

```powershell
.\gradlew.bat clean test jacocoTestReport --console=plain
$env:SONAR_TOKEN = 'YOUR_LOCAL_PROJECT_TOKEN'
.\Analyze.ps1
```

On Linux/macOS: `chmod +x gradlew`, use `./gradlew`, and set SONAR_TOKEN using your
shell. The helper is PowerShell-only; equivalent analysis task arguments are:
`clean test jacocoTestReport sonar -Dsonar.qualitygate.wait=true`.
Gradle defaults to the local URL/project key; overrides are available in Analyze.ps1.
Inspect `build/reports/jacoco/test/html/index.html` and
`build/reports/jacoco/test/jacocoTestReport.xml`.

## 4. Replay the feature and repair on the main analysis

Choose a local-only replay or a Jenkins replay before applying either stage.
For Jenkins, first publish THIS untouched baseline folder as another Git repository:
follow the Cloud guide's Git steps, using `gradlew` for the executable bit and a
separate repository URL. Complete steps 5 and 6 below and get a passing baseline
Pipeline run before adding the feature. Configure a single Pipeline from SCM job
tracking `main`, Script Path `Jenkinsfile`. This is main-branch analysis, not a
multibranch/PR Jenkins job.

Then use the kit Apply-Stage helper with `-Project local-gradle`, stage 01 followed
by stage 02. For a local-only replay, run `./Analyze.ps1` after EACH stage. For
Jenkins, commit/push each stage separately to main and trigger a build after each
commit. In both cases inspect the gate and Background Tasks, and wait before
applying the next stage. The first adds an uncovered class; the second adds its
tests. Tests pass in all stages; overall coverage should fall below 80% and then
return to 100%. If you finish a local-only replay and later want to repeat it in
Jenkins, start from a fresh initial repository copy from the ZIP, not the repaired
working folder.

## 5. Connect Jenkins (host-based agent recommended on Windows)

Install Jenkins following its official installation guide. Give its agent Git and
JDK 21. A Windows service does not necessarily inherit your interactive terminal's
JAVA_HOME/PATH; configure the agent environment or JDK tool accordingly.
Install the SonarQube Scanner for Jenkins, Pipeline, Git, Credentials Binding and
JUnit plugins if missing. In Jenkins configuration:

- Create Secret text credential ID `sonar-lab-token` using your local Sonar token.
- Enable the Sonar server environment injection option if displayed.
- Add Sonar installation named exactly `Sonar Lab`, URL `http://localhost:9000`,
  and select `sonar-lab-token` as its server credential.
- Create the SCM Pipeline job above. Disable shallow checkout.

The Jenkinsfile works with Windows `bat` and Unix `sh`. It uses the Gradle wrapper,
publishes JUnit results, submits analysis and waits for the asynchronous gate.
The `sonar` task depends on coverage; Gradle should report tasks up-to-date when
rerunning it after the separate build stage. No scanner polling flag is used here.

## 6. Configure the return webhook

In Sonar project Administration > Webhooks, create a webhook:

`http://host.docker.internal:8080/sonarqube-webhook/`

This assumes Sonar in Docker Desktop and Jenkins on the Windows host at port 8080.
The trailing slash matters. Use Jenkins' actual port/context path if different.
`localhost` inside the Sonar container means that container, not your Windows host.
If Jenkins/its agent also run in containers, configure a shared Docker network and
use service hostnames/reachable ports for both the Sonar URL and webhook URL.
Verify webhook delivery in Sonar. If using a webhook secret, configure the same
value in Jenkins' Sonar integration; it is separate from the analysis token.

Run the initial Pipeline and then each staged commit. The final `Ready to release`
stage runs only after the gate passes. The Pipeline has a five-minute gate timeout.

## 7. Troubleshoot and stop

Try the outer kit's EXPERIMENTS.md. Check scanner logs, Sonar Background Tasks,
webhook deliveries, and `docker compose logs --tail 100 sonarqube`.
Stop without deleting the named-volume data using `docker compose stop`.
Do not use `down -v` unless you intentionally want to erase the lab's Sonar data.
