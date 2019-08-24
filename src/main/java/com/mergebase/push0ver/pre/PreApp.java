package com.mergebase.push0ver.pre;

import com.mergebase.push0ver.App;
import com.mergebase.push0ver.FileUtil;
import com.mergebase.push0ver.MyLogger;
import com.mergebase.push0ver.Tag;
import com.mergebase.push0ver.TagExtractor;

import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class PreApp {
    public static void main(String[] args) throws Exception {
        invoke(args, System.getProperties(), logLine -> {
            System.out.println(logLine);
            return logLine;
        });
    }

    public static void invoke(String[] args, Properties p, MyLogger log) throws Exception {
        String userName = null;
        String userPassword = null;
        String mvnRepoName = null;
        String nodeRepo = null;
        String pathToEntry = args.length > 0 ? args[0] : null;
        pathToEntry = pathToEntry != null ? pathToEntry.trim() : "";
        if ("".equals(pathToEntry)) {
            pathToEntry = ".";
        }

        if (p.getProperty("repo.name") != null) {
            mvnRepoName = p.getProperty("repo.name");
        } else {
            log.addBuildLogEntry("push0ver - You forgot to enter a Repository name! (-Drepo.name=x)");
        }

        if (p.getProperty("noderepo.name") != null) {
            mvnRepoName = p.getProperty("noderepo.name");
        } else {
            log.addBuildLogEntry("push0ver - You forgot to enter an NPM Repository name! (-Dnoderepo.name=x)");
        }

        if (p.getProperty("noderepo.name") != null) {
            nodeRepo = p.getProperty("noderepo.name");
        }
        if (p.getProperty("art.username") != null) {
            userName = p.getProperty("art.username");
        }
        if (p.getProperty("art.password") != null) {
            userPassword = p.getProperty("art.password");
        }

        String gitRepo;
        String gitTarget = pathToEntry;
        if (p.getProperty("git.repo") != null) {
            gitRepo = p.getProperty("git.repo");
            if (gitRepo != null) {
                gitTarget = gitRepo;
            }
        }

        final String basicAuthHeader = App.basicAuthHeader(userName, userPassword);
        String url = p.getProperty("art.url");
        url = (url != null) ? url.trim() : "";
        if (!url.endsWith("/")) {
            url = url + "/";
        }

        String pathToMaven = null;
        String mvnCommand = "mvn";
        if (p.getProperty("mvn.home") != null) {
            pathToMaven = p.getProperty("mvn.home");
        }
        if (pathToMaven != null && !"".equals(pathToMaven.trim())) {
            mvnCommand = pathToMaven.trim() + "/bin/mvn";
        }

        String[] badTag = new String[1];
        App.fetchTags(log, gitTarget);
        Map<String, Tag> tags = TagExtractor.getTag(gitTarget, pathToEntry, false, log, badTag);

        if (tags != null) {
            for (Tag tag : tags.values()) {
                File packageDir = new File(pathToEntry + tag.getDirectory());
                File f = new File(pathToEntry + tag.getDirectory() + "/push0ver.windup.txt");
                FileWriter pref = new FileWriter(f);

                // Replace the SENTINEL with the TAG !
                Set<File> matches = new HashSet<>();

                try {
                    if (packageDir.exists()) {
                        matches = FileUtil.injectTagRecursive(packageDir, tag.getVersion().toString(), log);
                        log.addBuildLogEntry("Looking at: " + tag.toString() + " isMaven=" + tag.isMaven() + " isNode=" + tag.isNode(matches));

                        App.MavenStruct mavenStruct = null;
                        App.NodeStruct nodeStruct = null;
                        if (tag.isMaven()) {
                            // Switch to SNAPSHOT if appropriate:
                            mavenStruct = App.mavenCheckIfAlreadyReleased(
                                    tag, log, mvnCommand, pathToEntry, basicAuthHeader, url, gitTarget, mvnRepoName);
                        }

                        if (tag.isNode(matches)) {
                            nodeStruct = App.nodeCheckIfAlreadyReleased(
                                    tag, log, pathToEntry, basicAuthHeader, url, nodeRepo);
                        }

                        if (mavenStruct != null || nodeStruct != null) {

                            if (mavenStruct != null) {
                                tag = mavenStruct.tag;
                                if (nodeStruct != null) {
                                    log.addBuildLogEntry("push0ver - WINDUP EXTRACTED VALID NODE+MAVEN TAG: " + tag.getVersion());
                                } else {
                                    log.addBuildLogEntry("push0ver - WINDUP EXTRACTED VALID MAVEN TAG: " + tag.getVersion());
                                }
                            } else {
                                tag = nodeStruct.tag;
                                log.addBuildLogEntry("push0ver - WINDUP EXTRACTED VALID NODE TAG: " + tag.getVersion());
                            }
                            pref.write("pre=valid\n");
                        } else {
                            log.addBuildLogEntry("push0ver - WINDUP EXTRACTED ALREADY RELEASED TAG: " + tag.getVersion());
                            pref.write("pre=released\n");
                        }
                    } else if (badTag[0] != null) {
                        String tagString = badTag[0];
                        log.addBuildLogEntry("push0ver - WINDUP EXTRACTED STALE TAG: " + tagString);
                        pref.write("pre=stale\n");
                    } else {
                        log.addBuildLogEntry("what is happening in here");
                    }
                } finally {
                    try {
                        for (File match : matches) {
                            pref.write(match.getAbsolutePath() + "\n");
                        }
                    } finally {
                        pref.close();
                    }
                }
            }
        } else {
            log.addBuildLogEntry("PUSH0VER COULD NOT EXTRACT TAG FROM: " + gitTarget);
        }
    }
}
