/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */

import groovy.transform.Field
import org.jenkinsci.plugins.credentialsbinding.impl.CredentialNotFoundException

/*
 * See https://github.com/hibernate/hibernate-jenkins-pipeline-helpers
 */
@Library('hibernate-jenkins-pipeline-helpers@1.0')
import org.hibernate.jenkins.pipeline.helpers.job.JobHelper
import org.hibernate.jenkins.pipeline.helpers.alternative.AlternativeMultiMap
import org.hibernate.jenkins.pipeline.helpers.version.Version

/*
 * WARNING: DO NOT IMPORT LOCAL LIBRARIES HERE.
 *
 * By local, I mean libraries whose files are in the same Git repository.
 *
 * The Jenkinsfile is protected and will not be executed if modified in pull requests from external users,
 * but other local library files are not protected.
 * A user could potentially craft a malicious PR by modifying a local library.
 *
 * See https://blog.grdryn.me/blog/jenkins-pipeline-trust.html for a full explanation,
 * and a potential solution if we really need local libraries.
 * Alternatively we might be able to host libraries in a separate GitHub repo and configure
 * them in the GUI: see http://ci.hibernate.org/job/hibernate-search-6-poc/configure, "Pipeline Libraries".
 */

/*
 * See https://github.com/hibernate/hibernate-jenkins-pipeline-helpers for the documentation
 * of the helpers library used in this Jenkinsfile,
 * and for help writing Jenkinsfiles.
 *
 * ### Jenkins configuration
 *
 * #### Jenkins plugins
 *
 * This file requires the following plugins in particular:
 *
 * - everything required by the helpers library (see the org.hibernate.(...) imports for a link to its documentation)
 * - https://plugins.jenkins.io/ec2 for AWS credentials
 * - https://plugins.jenkins.io/lockable-resources for the lock on the AWS Elasticsearch server
 * - https://plugins.jenkins.io/pipeline-github for the trigger on pull request comments
 *
 * #### Script approval
 *
 * If not already done, you will need to allow the following calls in <jenkinsUrl>/scriptApproval/:
 *
 * - everything required by the helpers library (see the org.hibernate.(...) imports for a link to its documentation)
 *
 * ### Integrations
 *
 * #### Nexus deployment
 *
 * This job includes two deployment modes:
 *
 * - A deployment of snapshot artifacts for every non-PR build on "primary" branches (master and maintenance branches).
 * - A full release when starting the job with specific parameters.
 *
 * In the first case, the name of a Maven settings file must be provided in the job configuration file
 * (see below).
 *
 * #### Coveralls (optional)
 *
 * You need to enable your repository in Coveralls first: see https://coveralls.io/repos/new.
 *
 * Then you will also need to add the Coveralls repository token as credentials in Jenkins
 * (see "Credentials" section below).
 *
 * ### Job configuration
 *
 * This Jenkinsfile gets its configuration from four sources:
 * branch name, environment variables, a configuration file, and credentials.
 * All configuration is optional for the default build (and it should stay that way),
 * but some features require some configuration.
 *
 * #### Branch name
 *
 * See the org.hibernate.(...) imports for a link to the helpers library documentation,
 * which explains the basics.
 *
 * #### Environment variables
 *
 * The following environment variables are necessary for some features:
 *
 * - 'ES_AWS_REGION', containing the name of an AWS region such as 'us-east-1', to test Elasticsearch as a service on AWS.
 * - 'ES_AWS_<majorminor without dot>_ENDPOINT' (e.g. 'ES_AWS_52_ENDPOINT'),
 * containing the URL of an AWS Elasticsearch service endpoint using that exact version,
 * to test Elasticsearch as a service on AWS.
 *
 * #### Job configuration file
 *
 * See the org.hibernate.(...) imports for a link to the helpers library documentation,
 * which explains the basic structure of this file and how to set it up.
 *
 * Below is the additional structure specific to this Jenkinsfile:
 *
 *     deployment:
 *       maven:
 *         # String containing the ID of a Maven settings file registered using the config-file-provider Jenkins plugin.
 *         # The settings must provide credentials to the servers with ID
 *         # 'jboss-releases-repository' and 'jboss-snapshots-repository'.
 *         settingsId: ...
 *
 * #### Credentials
 *
 * The following credentials are necessary for some features:
 *
 * - 'aws-elasticsearch' AWS credentials, to test Elasticsearch as a service on AWS
 * - 'coveralls-repository-token' secret text credentials containing the repository token,
 * to send coverage reports to coveralls.io. Note these credentials should be registered at the job level, not system-wide.
 */

@Field final String MAVEN_TOOL = 'Apache Maven 3.5.4'

// Default node pattern, to be used for resource-intensive stages.
// Should not include the master node.
@Field final String NODE_PATTERN_BASE = 'Slave'
// Quick-use node pattern, to be used for very light, quick, and environment-independent stages,
// such as sending a notification. May include the master node in particular.
@Field final String QUICK_USE_NODE_PATTERN = 'Master||Slave'

@Field AlternativeMultiMap<ITEnvironment> environments
@Field JobHelper helper

@Field boolean enableDefaultBuild = false
@Field boolean enableDefaultEnvIT = false
@Field boolean enableNonDefaultSupportedEnvIT = false
@Field boolean enableExperimentalEnvIT = false
@Field boolean performRelease = false
@Field boolean deploySnapshot = false

@Field Version releaseVersion
@Field Version afterReleaseDevelopmentVersion

this.helper = new JobHelper(this)

helper.runWithNotification {

stage('Configure') {
	this.environments = AlternativeMultiMap.create([
			jdk: [
					// This should not include every JDK; in particular let's not care too much about EOL'd JDKs like version 9
					// See http://www.oracle.com/technetwork/java/javase/eol-135779.html
					new JdkITEnvironment(version: '8', tool: 'Oracle JDK 8', status: ITEnvironmentStatus.USED_IN_DEFAULT_BUILD),
					new JdkITEnvironment(version: '10', tool: 'Oracle JDK 10.0.1', status: ITEnvironmentStatus.SUPPORTED),
					// TODO HSEARCH-3238 (and maybe more) to support JDK11 builds
					new JdkITEnvironment(version: '11', tool: 'OpenJDK 11 Latest', status: ITEnvironmentStatus.EXPERIMENTAL)
			],
			database: [
					new DatabaseITEnvironment(dbName: 'h2', mavenProfile: 'h2', status: ITEnvironmentStatus.USED_IN_DEFAULT_BUILD),
					new DatabaseITEnvironment(dbName: 'mariadb', mavenProfile: 'ci-mariadb', status: ITEnvironmentStatus.SUPPORTED),
					new DatabaseITEnvironment(dbName: 'postgresql', mavenProfile: 'ci-postgresql', status: ITEnvironmentStatus.SUPPORTED)
			],
			esLocal: [
					new EsLocalITEnvironment(versionRange: '[2.0,2.2)', mavenProfile: 'elasticsearch-2.0', status: ITEnvironmentStatus.SUPPORTED),
					new EsLocalITEnvironment(versionRange: '[2.2,5.0)', mavenProfile: 'elasticsearch-2.2', status: ITEnvironmentStatus.SUPPORTED),
					// Use Elasticsearch 5.0.2 instead of the default 5.1.2, because a bug crashes ES on startup in our environment
					// See https://github.com/elastic/elasticsearch/issues/23218
					new EsLocalITEnvironment(versionRange: '[5.0,5.2)', version: '5.0.2', mavenProfile: 'elasticsearch-5.0', status: ITEnvironmentStatus.SUPPORTED),
					new EsLocalITEnvironment(versionRange: '[5.2,6.0)', mavenProfile: 'elasticsearch-5.2', status: ITEnvironmentStatus.USED_IN_DEFAULT_BUILD)
			],
			esAws: [
					new EsAwsITEnvironment(version: '2.3', mavenProfile: 'elasticsearch-2.2', status: ITEnvironmentStatus.SUPPORTED),
					new EsAwsITEnvironment(version: '5.1', mavenProfile: 'elasticsearch-5.0', status: ITEnvironmentStatus.SUPPORTED),
					new EsAwsITEnvironment(version: '5.3', mavenProfile: 'elasticsearch-5.2', status: ITEnvironmentStatus.SUPPORTED),
					new EsAwsITEnvironment(version: '5.5', mavenProfile: 'elasticsearch-5.2', status: ITEnvironmentStatus.SUPPORTED),
					new EsAwsITEnvironment(version: '5.6', mavenProfile: 'elasticsearch-5.2', status: ITEnvironmentStatus.SUPPORTED)
			]
	])

	// Load the configuration specific to each job set up in Jenkins
	def jobConfigurationFromNode
	node(QUICK_USE_NODE_PATTERN) {
		jobConfigurationFromNode = helper.loadYamlConfiguration('job-configuration.yaml')
		echo "Job configuration: $jobConfigurationFromNode"
	}

	helper.configure {
		file jobConfigurationFromNode
		jdk {
			defaultTool environments.content.jdk.default.tool
		}
		maven {
			defaultTool MAVEN_TOOL
			producedArtifactPattern "org/hibernate/hibernate-search*"
		}
	}

	properties([
			pipelineTriggers(
					[
							issueCommentTrigger('.*test this please.*'),
							// Normally we don't have snapshot dependencies, so this doesn't matter, but some branches do
							snapshotDependencies()
					]
							+ helper.configuration.tracking.trackedAsString ? [
									// Rebuild when tracked jobs are rebuilt
									upstream(helper.configuration.tracking.trackedAsString)
							]
							: []
			),
			parameters([
					choice(
							name: 'INTEGRATION_TESTS',
							choices: """AUTOMATIC
DEFAULT_ENV_ONLY
SUPPORTED_ENV_ONLY
EXPERIMENTAL_ENV_ONLY
ALL_ENV""",
							defaultValue: 'AUTOMATIC',
							description: """Which integration tests to run.
'AUTOMATIC' chooses based on the branch name and whether a release is being performed.
'DEFAULT_ENV_ONLY' means a single build, while other options will trigger multiple Maven executions in different environments."""
					),
					string(
							name: 'RELEASE_VERSION',
							defaultValue: '',
							description: 'The version to be released, e.g. 5.10.0.Final. Setting this triggers a release.',
							trim: true
					),
					string(
							name: 'RELEASE_DEVELOPMENT_VERSION',
							defaultValue: '',
							description: 'The next version to be used after the release, e.g. 5.10.0-SNAPSHOT.',
							trim: true
					),
					booleanParam(
							name: 'RELEASE_DRY_RUN',
							defaultValue: false,
							description: 'If true, just simulate the release, without pushing any commits or tags, and without uploading any artifacts or documentation.'
					)
			])
	])

	performRelease = (params.RELEASE_VERSION ? true : false)

	if (!performRelease && helper.scmSource.branch.primary && !helper.scmSource.pullRequest) {
		if (helper.configuration.file?.deployment?.maven?.settingsId) {
			deploySnapshot = true
		}
		else {
			echo "Missing deployment configuration in job configuration file - snapshot deployment will be skipped."
		}
	}

	switch (params.INTEGRATION_TESTS) {
		case 'DEFAULT_ENV_ONLY':
			enableDefaultEnvIT = true
			break
		case 'SUPPORTED_ENV_ONLY':
			enableDefaultEnvIT = true
			enableNonDefaultSupportedEnvIT = true
			break
		case 'ALL_ENV':
			enableDefaultEnvIT = true
			enableNonDefaultSupportedEnvIT = true
			enableExperimentalEnvIT = true
			break
		case 'EXPERIMENTAL_ENV_ONLY':
			enableExperimentalEnvIT = true
			break
		case 'AUTOMATIC':
			if (params.RELEASE_VERSION) {
				echo "Skipping default build and integration tests to speed up the release of version $params.RELEASE_VERSION"
			} else if (helper.scmSource.pullRequest) {
				echo "Enabling only the default build and integration tests in the default environment for pull request $helper.scmSource.pullRequest.id"
				enableDefaultEnvIT = true
			} else if (helper.scmSource.branch.primary) {
				echo "Enabling integration tests on all supported environments for primary branch '$helper.scmSource.branch.name'"
				enableDefaultBuild = true
				enableDefaultEnvIT = true
				enableNonDefaultSupportedEnvIT = true
			} else {
				echo "Enabling only the default build and integration tests in the default environment for feature branch $helper.scmSource.branch.name"
				enableDefaultBuild = true
				enableDefaultEnvIT = true
			}
			break
		default:
			throw new IllegalArgumentException(
					"Unknown value for param 'INTEGRATION_TESTS': '$params.INTEGRATION_TESTS'."
			)
	}

	enableDefaultBuild =
			enableDefaultEnvIT || enableNonDefaultSupportedEnvIT || enableExperimentalEnvIT || deploySnapshot

	echo """Branch: ${helper.scmSource.branch.name}, PR: ${helper.scmSource.pullRequest?.id}, integration test setting: $params.INTEGRATION_TESTS, resulting execution plan:
enableDefaultBuild=$enableDefaultBuild
enableDefaultEnvIT=$enableDefaultEnvIT
enableNonDefaultSupportedEnvIT=$enableNonDefaultSupportedEnvIT
enableExperimentalEnvIT=$enableExperimentalEnvIT
performRelease=$performRelease
deploySnapshot=$deploySnapshot"""

	// Filter environments

	environments.content.each { key, envSet ->
		// No need to re-test default environments, they are already tested as part of the default build
		envSet.enabled.remove(envSet.default)

		if (!enableNonDefaultSupportedEnvIT) {
			envSet.enabled.removeAll { itEnv -> itEnv.status == ITEnvironmentStatus.SUPPORTED }
		}
		if (!enableExperimentalEnvIT) {
			envSet.enabled.removeAll { itEnv -> itEnv.status == ITEnvironmentStatus.EXPERIMENTAL }
		}
	}

	environments.content.esAws.enabled.removeAll { itEnv ->
		itEnv.endpointUrl = env.getProperty(itEnv.endpointVariableName)
		if (!itEnv.endpointUrl) {
			echo "Skipping test ${itEnv.tag} because environment variable '${itEnv.endpointVariableName}' is not defined."
			return true
		}
		itEnv.awsRegion = env.ES_AWS_REGION
		if (!itEnv.awsRegion) {
			echo "Skipping test ${itEnv.tag} because environment variable 'ES_AWS_REGION' is not defined."
			return true
		}
		return false // Environment is fully defined, do not remove
	}

	if (environments.isAnyEnabled()) {
		echo "Enabled non-default environment ITs: ${environments.enabledAsString}"
	}
	else {
		echo "Non-default environment ITs are completely disabled."
	}

	if (performRelease) {
		releaseVersion = Version.parseReleaseVersion(params.RELEASE_VERSION)
		echo "Inferred version family for the release to '$releaseVersion.family'"

		// Check that all the necessary parameters are set
		if (!params.RELEASE_DEVELOPMENT_VERSION) {
			throw new IllegalArgumentException(
					"Missing value for parameter RELEASE_DEVELOPMENT_VERSION." +
							" This parameter must be set when RELEASE_VERSION is set."
			)
		}
	}

	if (params.RELEASE_DEVELOPMENT_VERSION) {
		afterReleaseDevelopmentVersion = Version.parseDevelopmentVersion(params.RELEASE_DEVELOPMENT_VERSION)
	}
}

stage('Default build') {
	if (!enableDefaultBuild) {
		echo 'Skipping default build and integration tests in the default environment'
		helper.markStageSkipped()
		return
	}
	node(NODE_PATTERN_BASE) {
		helper.withMavenWorkspace {
			sh """ \
					mvn clean install \
					-Pdist -Pcoverage -Pjqassistant \
					${enableDefaultEnvIT ? '' : '-DskipITs'} \
			"""

			// Don't try to report to Coveralls.io or SonarCloud if coverage data is missing
			if (enableDefaultEnvIT) {
				try {
					withCredentials([string(credentialsId: 'coveralls-repository-token', variable: 'COVERALLS_TOKEN')]) {
						sh """ \
								mvn coveralls:report \
								-DrepoToken=${COVERALLS_TOKEN} \
								${helper.scmSource.pullRequest ? """ \
										-DpullRequest=${helper.scmSource.pullRequest.id} \
								""" : """ \
										-Dbranch=${helper.scmSource.branch.name} \
								"""} \
						"""
					}
				}
				catch (CredentialNotFoundException e) {
					echo "No Coveralls token configured - skipping Coveralls report. Error was: ${e}"
				}
			}

			dir(helper.configuration.maven.localRepositoryPath) {
				stash name:'main-build', includes:"org/hibernate/hibernate-search*/**"
			}
		}
	}
}

stage('Non-default environment ITs') {
	Map<String, Closure> executions = [:]

	// Test with multiple JDKs
	environments.content.jdk.enabled.each { JdkITEnvironment itEnv ->
		executions.put(itEnv.tag, {
			node(NODE_PATTERN_BASE) {
				helper.withMavenWorkspace(jdk: itEnv.tool) {
					mavenNonDefaultIT itEnv, """ \
							clean install --fail-at-end \
					"""
				}
			}
		})
	}

	// Test ORM integration with multiple databases
	environments.content.database.enabled.each { DatabaseITEnvironment itEnv ->
		executions.put(itEnv.tag, {
			node(NODE_PATTERN_BASE) {
				helper.withMavenWorkspace {
					resumeFromDefaultBuild()
					mavenNonDefaultIT itEnv, """ \
							clean install -pl org.hibernate:hibernate-search-orm -P$itEnv.mavenProfile \
					"""
				}
			}
		})
	}

	// Test Elasticsearch integration with multiple versions in a local instance
	environments.content.esLocal.enabled.each { EsLocalITEnvironment itEnv ->
		executions.put(itEnv.tag, {
			node(NODE_PATTERN_BASE) {
				helper.withMavenWorkspace {
					resumeFromDefaultBuild()
					mavenNonDefaultIT itEnv, """ \
							clean install -pl org.hibernate:hibernate-search-integrationtest-elasticsearch \
							${toMavenElasticsearchProfileArg(itEnv.mavenProfile)} \
							${itEnv.version ? "-Dtest.elasticsearch.host.version=$itEnv.version" : ''} \
					"""
				}
			}
		})
	}

	// Test Elasticsearch integration with multiple versions in an AWS instance
	environments.content.esAws.enabled.each { EsAwsITEnvironment itEnv ->
		if (!itEnv.endpointUrl) {
			throw new IllegalStateException("Unexpected empty endpoint URL")
		}
		if (!itEnv.awsRegion) {
			throw new IllegalStateException("Unexpected empty AWS region")
		}
		executions.put(itEnv.tag, {
			lock(label: itEnv.lockedResourcesLabel) {
				node(NODE_PATTERN_BASE + '&&AWS') {
					helper.withMavenWorkspace {
						resumeFromDefaultBuild()
						withCredentials([[$class: 'AmazonWebServicesCredentialsBinding',
										 credentialsId   : 'aws-elasticsearch',
										 usernameVariable: 'AWS_ACCESS_KEY_ID',
										 passwordVariable: 'AWS_SECRET_ACCESS_KEY'
						]]) {
							mavenNonDefaultIT itEnv, """ \
								clean install -pl org.hibernate:hibernate-search-integrationtest-elasticsearch \
								${toMavenElasticsearchProfileArg(itEnv.mavenProfile)} \
								-Dtest.elasticsearch.host.provided=true \
								-Dtest.elasticsearch.host.url=$itEnv.endpointUrl \
								-Dtest.elasticsearch.host.aws.signing.enabled=true \
								-Dtest.elasticsearch.host.aws.access_key=$AWS_ACCESS_KEY_ID \
								-Dtest.elasticsearch.host.aws.secret_key=$AWS_SECRET_ACCESS_KEY \
								-Dtest.elasticsearch.host.aws.region=$itEnv.awsRegion \
							"""
						}
					}
				}
			}
		})
	}

	if (executions.isEmpty()) {
		echo 'Skipping integration tests in non-default environments'
		helper.markStageSkipped()
	}
	else {
		parallel(executions)
	}
}

stage('Deploy') {
	if (deploySnapshot) {
		echo "Deploying snapshots"
		node(NODE_PATTERN_BASE) {
			helper.withMavenWorkspace(mavenSettingsConfig: helper.configuration.file.deployment.maven.settingsId) {
				sh "mvn clean deploy -Pdist -DskipTests"
			}
		}
	}
	else if (performRelease) {
		echo "Performing full release for version ${releaseVersion.toString()}"
		node(NODE_PATTERN_BASE) {
			helper.withMavenWorkspace {
				sh "git clone https://github.com/hibernate/hibernate-noorm-release-scripts.git"
				sh "bash -xe hibernate-noorm-release-scripts/prepare-release.sh search ${releaseVersion.toString()}"

				String deployCommand = "bash -xe hibernate-noorm-release-scripts/deploy.sh search"
				if (!params.RELEASE_DRY_RUN) {
					sh deployCommand
				} else {
					echo "WARNING: Not deploying. Would have executed:"
					echo deployCommand
				}

				String uploadDistributionCommand = "bash -xe hibernate-noorm-release-scripts/upload-distribution.sh search ${releaseVersion.toString()}"
				String uploadDocumentationCommand = "bash -xe hibernate-noorm-release-scripts/upload-documentation.sh search ${releaseVersion.toString()} ${releaseVersion.family}"
				if (!params.RELEASE_DRY_RUN) {
					sh uploadDistributionCommand
					sh uploadDocumentationCommand
				}
				else {
					echo "WARNING: Not uploading anything. Would have executed:"
					echo uploadDistributionCommand
					echo uploadDocumentationCommand
				}

				sh "bash -xe hibernate-noorm-release-scripts/update-version.sh search ${afterReleaseDevelopmentVersion.toString()}"
				sh "bash -xe hibernate-noorm-release-scripts/push-upstream.sh search ${releaseVersion.toString()} ${helper.scmSource.branch.name} ${!params.RELEASE_DRY_RUN}"
			}
		}
	}
	else {
		echo "Skipping deployment"
		helper.markStageSkipped()
		return
	}
}

} // End of helper.runWithNotification

// Job-specific helpers

enum ITEnvironmentStatus {
	// For environments used as part of the integration tests in the default build (tested on all branches)
	USED_IN_DEFAULT_BUILD,
	// For environments that are expected to work correctly (tested on master and maintenance branches)
	SUPPORTED,
	// For environments that may not work correctly (only tested when explicitly requested through job parameters)
	EXPERIMENTAL;

	// Work around JENKINS-33023
	// See https://issues.jenkins-ci.org/browse/JENKINS-33023?focusedCommentId=325738&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-325738
	public ITEnvironmentStatus() {}
}

abstract class ITEnvironment {
	ITEnvironmentStatus status
	String toString() { getTag() }
	abstract String getTag()
	boolean isDefault() { status == ITEnvironmentStatus.USED_IN_DEFAULT_BUILD }
}

class JdkITEnvironment extends ITEnvironment {
	String version
	String tool
	String getTag() { "jdk-$version" }
}

class DatabaseITEnvironment extends ITEnvironment {
	String dbName
	String mavenProfile
	String getTag() { "database-$dbName" }
}

class EsLocalITEnvironment extends ITEnvironment {
	String versionRange
	String version
	String mavenProfile
	String getTag() { "elasticsearch-local-$versionRange${version ? "-as-$version" : ''}" }
}

class EsAwsITEnvironment extends ITEnvironment {
	String version
	String mavenProfile
	String endpointUrl = null
	String awsRegion = null
	String getTag() { "elasticsearch-aws-$version" }
	String getNameEmbeddableVersion() {
		version.replaceAll('\\.', '')
	}
	String getEndpointVariableName() {
		"ES_AWS_${nameEmbeddableVersion}_ENDPOINT"
	}
	String getLockedResourcesLabel() {
		"es-aws-${nameEmbeddableVersion}"
	}
}

void resumeFromDefaultBuild() {
	dir(helper.configuration.maven.localRepositoryPath) {
		unstash name:'main-build'
	}
}

void mavenNonDefaultIT(ITEnvironment itEnv, String args) {
	// Add a suffix to tests to distinguish between different executions
	// of the same test in different environments in reports
	def testSuffix = itEnv.tag.replaceAll('[^a-zA-Z0-9_\\-+]+', '_')
	sh "mvn -Dsurefire.environment=$testSuffix $args"
}

String toMavenElasticsearchProfileArg(String mavenEsProfile) {
	String defaultEsProfile = environments.content.esLocal.default.mavenProfile
	if (mavenEsProfile != defaultEsProfile) {
		// Disable the default profile to avoid conflicting configurations
		"-P!$defaultEsProfile,$mavenEsProfile"
	}
	else {
		// Do not do as above, as we would tell Maven "disable the default profile, but enable it"
		// and Maven would end up disabling it.
		''
	}
}